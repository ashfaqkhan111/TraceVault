package com.tracevault.backend.forensic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Alat bantu umum: hash, entropi, dan pengenalan jenis file dari "magic bytes"
 * (beberapa byte pertama yang menjadi ciri khas tiap jenis file).
 */
public final class ForensicUtils {

    private ForensicUtils() {}

    public static String sha256(byte[] data) { return hash("SHA-256", data); }

    public static String md5(byte[] data) { return hash("MD5", data); }

    private static String hash(String algorithm, byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " tidak tersedia", e);
        }
    }

    /**
     * Entropi Shannon (0 sampai 8). Makin dekat ke 8, makin "acak" isinya.
     * Data terenkripsi/terkompresi biasanya di atas 7.2.
     */
    public static double entropy(byte[] data) {
        if (data.length == 0) return 0;
        long[] counts = new long[256];
        for (byte b : data) counts[b & 0xFF]++;
        double result = 0;
        for (long c : counts) {
            if (c == 0) continue;
            double p = (double) c / data.length;
            result -= p * (Math.log(p) / Math.log(2));
        }
        return result;
    }

    /** Menebak jenis file asli berdasarkan isi (bukan nama). */
    public static String detectSignature(byte[] d) {
        if (startsWith(d, 0x25, 0x50, 0x44, 0x46)) return "PDF";
        if (startsWith(d, 0x50, 0x4B, 0x03, 0x04) || startsWith(d, 0x50, 0x4B, 0x05, 0x06)) return "ZIP";
        if (startsWith(d, 0x4D, 0x5A)) return "PE";
        if (startsWith(d, 0x7F, 0x45, 0x4C, 0x46)) return "ELF";
        if (startsWith(d, 0x89, 0x50, 0x4E, 0x47)) return "PNG";
        if (startsWith(d, 0xFF, 0xD8, 0xFF)) return "JPEG";
        if (startsWith(d, 0x47, 0x49, 0x46, 0x38)) return "GIF";
        if (startsWith(d, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)) return "OLE";
        if (startsWith(d, 0x7B, 0x5C, 0x72, 0x74, 0x66)) return "RTF";
        if (startsWith(d, 0x1F, 0x8B)) return "GZIP";
        if (startsWith(d, 0x52, 0x61, 0x72, 0x21)) return "RAR";
        if (startsWith(d, 0x37, 0x7A, 0xBC, 0xAF)) return "7Z";
        if (looksLikeText(d)) return "TEXT";
        return "UNKNOWN";
    }

    /** Jenis isi yang wajar untuk tiap ekstensi. */
    private static final Map<String, Set<String>> EXPECTED = Map.ofEntries(
            Map.entry("pdf", Set.of("PDF")),
            Map.entry("docx", Set.of("ZIP")), Map.entry("xlsx", Set.of("ZIP")), Map.entry("pptx", Set.of("ZIP")),
            Map.entry("docm", Set.of("ZIP")), Map.entry("xlsm", Set.of("ZIP")), Map.entry("zip", Set.of("ZIP")),
            Map.entry("apk", Set.of("ZIP")), Map.entry("jar", Set.of("ZIP")),
            Map.entry("doc", Set.of("OLE", "RTF")), Map.entry("xls", Set.of("OLE")), Map.entry("ppt", Set.of("OLE")),
            Map.entry("msg", Set.of("OLE")), Map.entry("rtf", Set.of("RTF")),
            Map.entry("exe", Set.of("PE")), Map.entry("dll", Set.of("PE")), Map.entry("sys", Set.of("PE")),
            Map.entry("png", Set.of("PNG")), Map.entry("jpg", Set.of("JPEG")), Map.entry("jpeg", Set.of("JPEG")),
            Map.entry("gif", Set.of("GIF")), Map.entry("gz", Set.of("GZIP")), Map.entry("rar", Set.of("RAR")),
            Map.entry("7z", Set.of("7Z")), Map.entry("eml", Set.of("TEXT")), Map.entry("txt", Set.of("TEXT")),
            Map.entry("csv", Set.of("TEXT")), Map.entry("html", Set.of("TEXT")), Map.entry("htm", Set.of("TEXT"))
    );

    /** null = ekstensi tidak dikenal (tidak bisa dibandingkan). */
    public static Set<String> expectedSignatures(String extension) {
        return EXPECTED.get(extension);
    }

    public static String extension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static String mimeFor(String signature) {
        return switch (signature) {
            case "PDF" -> "application/pdf";
            case "ZIP" -> "application/zip (termasuk docx/xlsx/pptx)";
            case "PE" -> "application/vnd.microsoft.portable-executable";
            case "ELF" -> "application/x-elf";
            case "PNG" -> "image/png";
            case "JPEG" -> "image/jpeg";
            case "GIF" -> "image/gif";
            case "OLE" -> "application/x-ole-storage (doc/xls/msg lama)";
            case "RTF" -> "application/rtf";
            case "GZIP" -> "application/gzip";
            case "RAR" -> "application/vnd.rar";
            case "7Z" -> "application/x-7z-compressed";
            case "TEXT" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024));
    }

    /** Mengambil potongan teks ASCII yang bisa dibaca (seperti perintah `strings` di Linux). */
    public static List<String> printableStrings(byte[] data, int minLength, int maxCount) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            int c = b & 0xFF;
            if (c >= 0x20 && c < 0x7F) {
                sb.append((char) c);
            } else {
                if (sb.length() >= minLength) {
                    out.add(sb.toString());
                    if (out.size() >= maxCount) return out;
                }
                sb.setLength(0);
            }
        }
        if (sb.length() >= minLength) out.add(sb.toString());
        return out;
    }

    /** Isi file sebagai teks Latin-1 (setiap byte jadi satu karakter, aman untuk pencarian pola). */
    public static String asLatin1(byte[] data) {
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    private static boolean startsWith(byte[] d, int... sig) {
        if (d.length < sig.length) return false;
        for (int i = 0; i < sig.length; i++) {
            if ((d[i] & 0xFF) != sig[i]) return false;
        }
        return true;
    }

    private static boolean looksLikeText(byte[] d) {
        int n = Math.min(d.length, 4096);
        if (n == 0) return false;
        int printable = 0;
        for (int i = 0; i < n; i++) {
            int c = d[i] & 0xFF;
            if (c == 0) return false;
            if (c == 9 || c == 10 || c == 13 || (c >= 0x20 && c < 0x7F) || c >= 0x80) printable++;
        }
        return printable >= n * 0.95;
    }
}
