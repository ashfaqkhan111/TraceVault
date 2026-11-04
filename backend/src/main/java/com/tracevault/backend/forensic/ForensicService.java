package com.tracevault.backend.forensic;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

/**
 * "Pintu masuk" pemeriksaan forensik.
 * 1) Pemeriksaan umum untuk SEMUA file (hash, ukuran, jenis asli, entropi)
 * 2) Pemeriksaan khusus sesuai tipe yang dipilih user: file / email / malware
 */
@Service
public class ForensicService {

    private final DocumentInspector documentInspector = new DocumentInspector();
    private final EmailInspector emailInspector = new EmailInspector();
    private final MalwareInspector malwareInspector = new MalwareInspector();

    public ForensicReport analyze(byte[] data, String fileName, String type) {
        ForensicReport r = new ForensicReport(ForensicUtils.sha256(data), ForensicUtils.md5(data));
        String signature = ForensicUtils.detectSignature(data);

        r.meta("Filename", fileName);
        r.meta("File size", ForensicUtils.formatSize(data.length));
        r.meta("MIME type", ForensicUtils.mimeFor(signature));

        checkExtension(fileName, signature, r);
        checkEntropy(data, signature, r);

        switch (type) {
            case "email" -> emailInspector.inspect(data, r);
            case "malware" -> {
                documentInspector.inspect(data, fileName, signature, r);
                malwareInspector.inspect(data, signature, r);
            }
            default -> documentInspector.inspect(data, fileName, signature, r);
        }
        return r;
    }

    private void checkExtension(String fileName, String signature, ForensicReport r) {
        String ext = ForensicUtils.extension(fileName);
        Set<String> expected = ForensicUtils.expectedSignatures(ext);

        if (expected == null) {
            r.add(Finding.info("Extension not checked",
                    "TraceVault does not have a rule for \"." + ext + "\" files yet, so the type match was not checked.", "integrity"));
        } else if (expected.contains(signature)) {
            r.add(Finding.ok("File signature matches extension",
                    "The first bytes of the file match a real ." + ext + " file. No extension spoofing detected.", "integrity"));
        } else {
            boolean executableInDisguise = Set.of("PE", "ELF").contains(signature);
            r.add(executableInDisguise
                    ? Finding.critical("Program disguised as ." + ext,
                        "The file is named ." + ext + " but it is actually a program (" + signature + "). "
                                + "This is a common trick to make people run malware.", "integrity")
                    : Finding.warning("Extension does not match content",
                        "The file is named ." + ext + " but its content looks like " + signature + ". "
                                + "The name may have been changed to hide the real type.", "integrity"));
        }
    }

    private void checkEntropy(byte[] data, String signature, ForensicReport r) {
        double e = ForensicUtils.entropy(data);
        r.meta("Entropy", String.format(Locale.ROOT, "%.2f / 8.00", e));
        // Format yang memang terkompresi wajar punya entropi tinggi
        boolean naturallyCompressed = Set.of("ZIP", "GZIP", "RAR", "7Z", "JPEG", "PNG", "PDF").contains(signature);
        if (!naturallyCompressed && data.length > 4096 && e > 7.5) {
            r.add(Finding.warning("Content looks encrypted or packed",
                    String.format(Locale.ROOT, "Overall entropy is %.2f/8.0, which is very high for a %s file.", e, signature),
                    "structure"));
        }
    }
}
