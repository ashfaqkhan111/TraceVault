package com.tracevault.backend.forensic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wadah hasil pemeriksaan satu file: hash, metadata, dan daftar temuan.
 * Dipakai oleh semua inspector (file, email, malware).
 */
public class ForensicReport {

    private final String sha256;
    private final String md5;
    private final Map<String, String> metadata = new LinkedHashMap<>();
    private final List<Finding> findings = new ArrayList<>();

    public ForensicReport(String sha256, String md5) {
        this.sha256 = sha256;
        this.md5 = md5;
    }

    public void meta(String key, String value) {
        if (value != null && !value.isBlank()) {
            metadata.put(key, value.length() > 500 ? value.substring(0, 500) + "…" : value);
        }
    }

    public void add(Finding finding) {
        findings.add(finding);
    }

    public String getSha256() { return sha256; }
    public String getMd5() { return md5; }
    public Map<String, String> getMetadata() { return metadata; }
    public List<Finding> getFindings() { return findings; }

    public long count(String severity) {
        return findings.stream().filter(f -> f.severity().equals(severity)).count();
    }
}
