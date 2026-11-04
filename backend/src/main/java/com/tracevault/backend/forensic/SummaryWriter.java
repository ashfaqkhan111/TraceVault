package com.tracevault.backend.forensic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Membuat skor risiko, ringkasan, dan rekomendasi dari temuan.
 *
 * Catatan: ringkasan ini dibuat dengan ATURAN (bukan model AI sungguhan), jadi selalu
 * sesuai dengan temuan yang benar-benar ada. Kalau nanti tim ingin memakai AI (LLM),
 * cukup ganti isi method summary() dan kirim daftar temuan sebagai bahan.
 */
public final class SummaryWriter {

    private SummaryWriter() {}

    /** Sama dengan rumus di frontend: critical 30, warning 15, info 5, maksimal 100. */
    public static int riskScore(ForensicReport r) {
        long score = r.count("critical") * 30 + r.count("warning") * 15 + r.count("info") * 5;
        return (int) Math.min(100, score);
    }

    public static String riskLevel(int score) {
        return score >= 70 ? "high" : score >= 35 ? "medium" : "low";
    }

    public static String summary(ForensicReport r, String type) {
        long critical = r.count("critical");
        long warning = r.count("warning");
        String subject = switch (type) {
            case "email" -> "This email";
            case "malware" -> "This file";
            default -> "This document";
        };

        StringBuilder sb = new StringBuilder();
        if (critical > 0) {
            sb.append(subject).append(" shows ").append(critical).append(" critical indicator")
                    .append(critical > 1 ? "s" : "").append(" and should be treated as high risk. ");
        } else if (warning > 0) {
            sb.append(subject).append(" has ").append(warning).append(" warning")
                    .append(warning > 1 ? "s" : "").append(" that deserve a closer look, but no critical red flags. ");
        } else {
            sb.append(subject).append(" did not trigger any critical or warning checks. ");
        }

        // Sebut temuan critical dulu, baru warning (maksimal 3)
        List<String> top = new ArrayList<>();
        for (String level : List.of("critical", "warning")) {
            for (Finding f : r.getFindings()) {
                if (top.size() < 3 && f.severity().equals(level)) top.add(f.title().toLowerCase());
            }
        }
        if (!top.isEmpty()) sb.append("The main concerns are: ").append(String.join("; ", top)).append(". ");

        sb.append("SHA-256 ").append(r.getSha256(), 0, 16).append("… identifies this exact file; ")
                .append("compare it with a trusted copy to confirm it has not changed. ");
        sb.append("These are automated indicators, not final proof — a human examiner should review them.");
        return sb.toString();
    }

    public static List<String> recommendations(ForensicReport r, String type) {
        Set<String> categories = new LinkedHashSet<>();
        for (Finding f : r.getFindings()) {
            if (!f.severity().equals("ok")) categories.add(f.category());
        }
        Set<String> recs = new LinkedHashSet<>();

        if (type.equals("email")) {
            if (categories.contains("authentication") || categories.contains("sender"))
                recs.add("Do not reply or send personal data until the sender is confirmed through an official channel.");
            if (categories.contains("links"))
                recs.add("Do not click the links. Type the official website address yourself instead.");
            if (categories.contains("attachments"))
                recs.add("Do not open the attachment. Scan it separately in an isolated environment.");
            if (categories.contains("routing"))
                recs.add("Check the originating IP address on an IP reputation service.");
            if (r.count("critical") + r.count("warning") > 0)
                recs.add("Report the email as phishing to your email provider if it looks fake.");
        }
        if (categories.contains("code") || categories.contains("imports") || categories.contains("persistence"))
            recs.add("Do NOT open or run this file outside an isolated sandbox or virtual machine.");
        if (categories.contains("integrity"))
            recs.add("Confirm the real file type before opening it; do not trust the file name alone.");
        if (categories.contains("strings"))
            recs.add("Block the hardcoded IP address(es) at the firewall and look for them in network logs.");
        if (categories.contains("structure"))
            recs.add("Submit the file to a sandbox for deeper dynamic analysis.");
        if (categories.contains("metadata"))
            recs.add("Compare the metadata dates and authors with other records (system logs, witness statements).");

        recs.add("Record the SHA-256 hash in your chain-of-custody notes.");
        recs.add("Keep the original evidence unchanged and work only on copies.");
        return new ArrayList<>(recs);
    }
}
