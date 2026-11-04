package com.tracevault.backend.forensic;

/**
 * Satu temuan hasil pemeriksaan.
 * severity: "critical", "warning", "info", atau "ok" (sama seperti di frontend).
 */
public record Finding(String severity, String title, String desc, String category) {

    public static Finding critical(String title, String desc, String category) {
        return new Finding("critical", title, desc, category);
    }

    public static Finding warning(String title, String desc, String category) {
        return new Finding("warning", title, desc, category);
    }

    public static Finding info(String title, String desc, String category) {
        return new Finding("info", title, desc, category);
    }

    public static Finding ok(String title, String desc, String category) {
        return new Finding("ok", title, desc, category);
    }
}
