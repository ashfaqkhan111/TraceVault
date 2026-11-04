package com.tracevault.backend.forensic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Memeriksa dokumen: PDF, Office modern (docx/xlsx/pptx = ZIP), Office lama (OLE), dan gambar.
 * File TIDAK pernah dibuka/dijalankan — hanya dibaca sebagai deretan byte.
 */
public class DocumentInspector {

    private static final int MAX_ZIP_ENTRIES = 2000;
    private static final int MAX_ENTRY_READ = 2 * 1024 * 1024; // baca maksimal 2 MB per isi ZIP (cegah zip bomb)
    private static final Set<String> DANGEROUS_EXT = Set.of(
            "exe", "dll", "scr", "bat", "cmd", "com", "js", "jse", "vbs", "vbe", "ps1", "hta", "lnk", "msi", "jar", "wsf");

    public void inspect(byte[] data, String fileName, String signature, ForensicReport r) {
        switch (signature) {
            case "PDF" -> inspectPdf(data, r);
            case "ZIP" -> inspectZip(data, fileName, r);
            case "OLE" -> inspectOle(data, r);
            case "JPEG", "PNG", "GIF" -> inspectImage(data, signature, r);
            default -> { /* jenis lain: cukup pemeriksaan umum */ }
        }
    }

    // ---------------------------------------------------------------- PDF
    private void inspectPdf(byte[] data, ForensicReport r) {
        String s = ForensicUtils.asLatin1(data);

        Matcher v = Pattern.compile("^%PDF-(\\d\\.\\d)").matcher(s);
        if (v.find()) r.meta("PDF version", v.group(1));
        r.meta("Author", pdfField(s, "Author"));
        r.meta("Creator", pdfField(s, "Creator"));
        r.meta("Producer", pdfField(s, "Producer"));
        r.meta("Created", pdfDate(pdfField(s, "CreationDate")));
        r.meta("Modified", pdfDate(pdfField(s, "ModDate")));

        int pages = count(s, "/Type\\s*/Page(?![a-zA-Z])");
        if (pages > 0) r.meta("Pages", String.valueOf(pages));

        boolean hasJs = s.contains("/JavaScript") || Pattern.compile("/JS\\s*[(<\\[0-9]").matcher(s).find();
        boolean autoRun = s.contains("/OpenAction") || s.contains("/AA");
        if (hasJs && autoRun) {
            r.add(Finding.critical("JavaScript runs when the PDF opens",
                    "The PDF contains JavaScript together with an automatic action (/OpenAction or /AA). "
                            + "This combination is often used by malicious PDFs.", "code"));
        } else if (hasJs) {
            r.add(Finding.warning("Embedded JavaScript",
                    "The PDF contains JavaScript. Normal documents rarely need it.", "code"));
        }
        if (s.contains("/Launch")) {
            r.add(Finding.critical("Launch action found",
                    "The PDF has a /Launch action that can try to open another program or file.", "code"));
        }
        if (s.contains("/EmbeddedFile")) {
            r.add(Finding.warning("Embedded file inside PDF",
                    "The PDF carries another file inside it. Extract and check it separately.", "structure"));
        }
        if (s.contains("/Encrypt")) {
            r.add(Finding.info("PDF is encrypted",
                    "Part of the content is encrypted, so some checks may be incomplete.", "structure"));
        }
        int eofCount = count(s, "%%EOF");
        if (eofCount > 1) {
            r.add(Finding.info("PDF was saved more than once",
                    "Found " + eofCount + " end-of-file markers (incremental updates). "
                            + "The document was edited after it was first created.", "metadata"));
        }
        String author = pdfField(s, "Author");
        if (author == null) {
            r.add(Finding.info("No author information",
                    "The PDF has no Author field. This is common but means less provenance information.", "metadata"));
        }
    }

    private static String pdfField(String s, String name) {
        Matcher m = Pattern.compile("/" + name + "\\s*\\(([^)]{0,300})\\)").matcher(s);
        if (!m.find()) return null;
        String value = m.group(1).replace("\\(", "(").replace("\\)", ")").trim();
        // Teks UTF-16 (diawali BOM FE FF) dibuat lebih rapi dengan membuang byte nol
        if (value.startsWith("þÿ")) value = value.substring(2).replace("\u0000", "");
        return value.isBlank() ? null : value;
    }

    /** Format tanggal PDF: D:20240115103000+07'00' → 2024-01-15 10:30:00 +07:00 */
    private static String pdfDate(String raw) {
        if (raw == null) return null;
        Matcher m = Pattern.compile("D:(\\d{4})(\\d{2})?(\\d{2})?(\\d{2})?(\\d{2})?(\\d{2})?([Zz+\\-])?(\\d{2})?'?(\\d{2})?").matcher(raw);
        if (!m.find()) return raw;
        String date = m.group(1) + "-" + or(m.group(2), "01") + "-" + or(m.group(3), "01")
                + " " + or(m.group(4), "00") + ":" + or(m.group(5), "00") + ":" + or(m.group(6), "00");
        String tz = m.group(7);
        if (tz == null) return date;
        if (tz.equalsIgnoreCase("Z")) return date + " UTC";
        return date + " " + tz + or(m.group(8), "00") + ":" + or(m.group(9), "00");
    }

    // ---------------------------------------------------------------- ZIP / Office modern
    private void inspectZip(byte[] data, String fileName, ForensicReport r) {
        String ext = ForensicUtils.extension(fileName);
        List<String> names = new ArrayList<>();
        String coreXml = null, appXml = null;
        List<String> externalTargets = new ArrayList<>();
        boolean hasMacro = false;

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null && names.size() < MAX_ZIP_ENTRIES) {
                String name = e.getName();
                names.add(name);
                String lower = name.toLowerCase(Locale.ROOT);

                if (lower.endsWith("vbaproject.bin")) hasMacro = true;
                if (lower.equals("docprops/core.xml")) coreXml = readText(zip);
                else if (lower.equals("docprops/app.xml")) appXml = readText(zip);
                else if (lower.endsWith(".rels")) {
                    Matcher m = Pattern.compile("Target=\"([^\"]+)\"[^>]*TargetMode=\"External\"")
                            .matcher(readText(zip));
                    while (m.find() && externalTargets.size() < 10) externalTargets.add(m.group(1));
                }

                String entryExt = ForensicUtils.extension(name);
                if (DANGEROUS_EXT.contains(entryExt)) {
                    r.add(Finding.critical("Executable file inside archive",
                            "The archive contains \"" + name + "\". Files like this can run code when opened.",
                            "structure"));
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            r.add(Finding.warning("Archive could not be read completely",
                    "The ZIP structure looks damaged or unusual: " + ex.getMessage(), "structure"));
        }

        r.meta("Archive entries", String.valueOf(names.size()));

        if (coreXml != null) {
            String creator = xmlTag(coreXml, "dc:creator");
            String lastBy = xmlTag(coreXml, "cp:lastModifiedBy");
            r.meta("Author", creator);
            r.meta("Last modified by", lastBy);
            r.meta("Created", xmlTag(coreXml, "dcterms:created"));
            r.meta("Modified", xmlTag(coreXml, "dcterms:modified"));
            r.meta("Revision", xmlTag(coreXml, "cp:revision"));
            if (creator != null && lastBy != null && !creator.equalsIgnoreCase(lastBy)) {
                r.add(Finding.warning("Author and last editor are different",
                        "Author is \"" + creator + "\" but last modified by \"" + lastBy + "\". "
                                + "The file was changed by another account after it was created.", "metadata"));
            }
        }
        if (appXml != null) {
            String app = xmlTag(appXml, "Application");
            String ver = xmlTag(appXml, "AppVersion");
            r.meta("Software", app == null ? null : app + (ver == null ? "" : " " + ver));
            r.meta("Pages", xmlTag(appXml, "Pages"));
            r.meta("Words", xmlTag(appXml, "Words"));
        }

        if (hasMacro) {
            boolean disguised = Set.of("docx", "xlsx", "pptx").contains(ext);
            r.add(disguised
                    ? Finding.critical("Macro hidden in a macro-free format",
                        "The file is named ." + ext + " (a format that should not contain macros) "
                                + "but it contains VBA macro code (vbaProject.bin).", "code")
                    : Finding.warning("VBA macro present",
                        "The document contains VBA macro code. Macros can run commands when enabled.", "code"));
        }
        for (String target : externalTargets) {
            r.add(Finding.warning("Document loads an external resource",
                    "The document links to \"" + target + "\" outside the file. "
                            + "This can be used to download a remote template or track who opens it.", "links"));
        }
    }

    // ---------------------------------------------------------------- OLE (doc/xls lama)
    private void inspectOle(byte[] data, ForensicReport r) {
        String s = ForensicUtils.asLatin1(data);
        if (s.contains("_VBA_PROJECT") || s.contains("VBA") && s.contains("Attribute VB_")) {
            r.add(Finding.warning("VBA macro present",
                    "This older Office file contains VBA macro code. Macros can run commands when enabled.", "code"));
        }
        if (s.contains("AutoOpen") || s.contains("Document_Open") || s.contains("Workbook_Open")) {
            r.add(Finding.critical("Macro runs automatically",
                    "Found an auto-start macro name (AutoOpen / Document_Open / Workbook_Open).", "code"));
        }
    }

    // ---------------------------------------------------------------- Gambar
    private void inspectImage(byte[] data, String signature, ForensicReport r) {
        String s = ForensicUtils.asLatin1(data);
        if ("JPEG".equals(signature) && s.contains("Exif")) {
            r.add(Finding.info("EXIF metadata present",
                    "The photo carries EXIF data (may include camera model, date, or GPS location).", "metadata"));
        }
        for (String tool : List.of("Photoshop", "GIMP", "Snapseed", "Lightroom", "Canva")) {
            if (s.contains(tool)) {
                r.add(Finding.info("Edited with " + tool,
                        "The image metadata mentions " + tool + ", so it was likely edited.", "metadata"));
                r.meta("Software", tool);
                break;
            }
        }
    }

    // ---------------------------------------------------------------- helper
    private static String readText(InputStream in) throws IOException {
        return new String(in.readNBytes(MAX_ENTRY_READ), StandardCharsets.UTF_8);
    }

    private static String xmlTag(String xml, String tag) {
        Matcher m = Pattern.compile("<" + Pattern.quote(tag) + "(?:\\s[^>]*)?>([^<]*)</" + Pattern.quote(tag) + ">")
                .matcher(xml);
        return m.find() && !m.group(1).isBlank() ? m.group(1).trim() : null;
    }

    private static int count(String s, String regex) {
        Matcher m = Pattern.compile(regex).matcher(s);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private static String or(String v, String def) {
        return v == null ? def : v;
    }
}
