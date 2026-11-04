package com.tracevault.backend.forensic;

/**
 * A single audit finding.
 * severity: “critical”, “warning”, ‘info’, or “ok” (same as in the frontend).
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
