package com.tracevault.backend.forensic;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks raw email files (.eml) for: sender headers, SPF/DKIM/DMARC results,
 * server path (Received), suspicious links, and malicious attachments.
 */
public class EmailInspector {

    private static final Pattern URL = Pattern.compile("https?://[^\\s\"'<>()\\]]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern IPV4 = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,3}){3})\\b");
    private static final Pattern EMAIL_DOMAIN = Pattern.compile("@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})");

    private static final Set<String> RISKY_TLDS = Set.of(
            "xyz", "top", "click", "zip", "mov", "tk", "ml", "ga", "cf", "gq", "work", "rest", "cam", "icu", "buzz", "lol");
    private static final Set<String> BRANDS = Set.of(
            "paypal", "google", "microsoft", "apple", "amazon", "facebook", "instagram", "whatsapp", "netflix",
            "bca", "klikbca", "bri", "bni", "mandiri", "dana", "gopay", "ovo", "shopee", "tokopedia", "telkomsel");
    private static final Set<String> DANGEROUS_ATTACH = Set.of(
            "exe", "scr", "bat", "cmd", "js", "jse", "vbs", "vbe", "ps1", "hta", "lnk", "iso", "img", "msi", "apk", "jar", "wsf", "html", "htm");
    private static final List<String> PRESSURE_WORDS = List.of(
            "urgent", "verify your account", "suspended", "password", "confirm your", "click here", "limited time",
            "segera", "akun anda", "diblokir", "verifikasi", "hadiah", "selamat anda", "menang", "transfer", "kode otp");

    private static final int MAX_LINK_FINDINGS = 6;

    public void inspect(byte[] data, ForensicReport r) {
        String raw = new String(data, StandardCharsets.UTF_8);
        int split = headerEnd(raw);
        String headerPart = split < 0 ? raw : raw.substring(0, split);
        String body = split < 0 ? "" : raw.substring(split);

        List<String[]> headers = parseHeaders(headerPart);
        if (headers.isEmpty() || first(headers, "from") == null) {
            r.add(Finding.warning("Not a valid email file",
                    "No email headers (From, To, Subject) were found. Upload the raw .eml file "
                            + "(in Gmail: ⋮ → \"Download message\").", "structure"));
            return;
        }

        String from = first(headers, "from");
        String replyTo = first(headers, "reply-to");
        String returnPath = first(headers, "return-path");
        r.meta("Message-ID", first(headers, "message-id"));
        r.meta("From", from);
        r.meta("Reply-To", replyTo);
        r.meta("Return-Path", returnPath);
        r.meta("To", first(headers, "to"));
        r.meta("Subject", first(headers, "subject"));
        r.meta("Date", first(headers, "date"));
        r.meta("X-Mailer", first(headers, "x-mailer"));

        checkAuthentication(headers, r);
        checkSenderMismatch(from, replyTo, returnPath, r);
        checkRouting(headers, r);

        String decodedBody = decodeQuotedPrintable(body);
        checkLinks(decodedBody, r);
        checkAttachments(body, r);
        checkPressureLanguage((or(first(headers, "subject")) + " " + decodedBody).toLowerCase(Locale.ROOT), r);
    }

    // ---------------------------------------------------------------- SPF / DKIM / DMARC
    private void checkAuthentication(List<String[]> headers, ForensicReport r) {
        String auth = String.join(" ; ", all(headers, "authentication-results")).toLowerCase(Locale.ROOT);
        String receivedSpf = or(first(headers, "received-spf")).toLowerCase(Locale.ROOT);
        boolean hasDkimSig = first(headers, "dkim-signature") != null;

        String spf = result(auth, "spf");
        if (spf == null && !receivedSpf.isBlank()) spf = receivedSpf.split("\\s+")[0];
        String dkim = result(auth, "dkim");
        String dmarc = result(auth, "dmarc");

        r.meta("SPF result", spf == null ? "NOT FOUND" : spf.toUpperCase(Locale.ROOT));
        r.meta("DKIM result", dkim == null ? (hasDkimSig ? "SIGNED (not verified)" : "NONE") : dkim.toUpperCase(Locale.ROOT));
        r.meta("DMARC result", dmarc == null ? "NOT FOUND" : dmarc.toUpperCase(Locale.ROOT));

        if (spf == null) {
            r.add(Finding.info("No SPF result in headers",
                    "The receiving server did not record an SPF check, so the sender server cannot be confirmed.", "authentication"));
        } else if (spf.equals("fail")) {
            r.add(Finding.critical("SPF authentication failed",
                    "The server that sent this email is NOT allowed to send for the sender's domain. The sender may be faked.", "authentication"));
        } else if (spf.equals("softfail") || spf.equals("neutral")) {
            r.add(Finding.warning("SPF check was weak (" + spf + ")",
                    "The sending server is not clearly approved by the sender's domain.", "authentication"));
        } else if (spf.equals("pass")) {
            r.add(Finding.ok("SPF passed", "The sending server is approved by the sender's domain.", "authentication"));
        }

        if ("fail".equals(dkim)) {
            r.add(Finding.critical("DKIM signature failed",
                    "The digital signature does not match. The email may have been changed or faked.", "authentication"));
        } else if (dkim == null && !hasDkimSig || "none".equals(dkim)) {
            r.add(Finding.warning("DKIM signature missing",
                    "The email is not digitally signed. Most real companies sign their emails with DKIM.", "authentication"));
        } else if ("pass".equals(dkim)) {
            r.add(Finding.ok("DKIM passed", "The email's digital signature is valid.", "authentication"));
        }

        if ("fail".equals(dmarc)) {
            r.add(Finding.critical("DMARC failed",
                    "The sender's domain policy says this email should not be trusted.", "authentication"));
        } else if ("pass".equals(dmarc)) {
            r.add(Finding.ok("DMARC passed", "The email follows the sender domain's policy.", "authentication"));
        }
    }

    private static String result(String auth, String mechanism) {
        Matcher m = Pattern.compile("\\b" + mechanism + "=([a-z]+)").matcher(auth);
        return m.find() ? m.group(1) : null;
    }

    // ---------------------------------------------------------------- pengirim
    private void checkSenderMismatch(String from, String replyTo, String returnPath, ForensicReport r) {
        String fromDomain = domainOf(from);
        String replyDomain = domainOf(replyTo);
        String returnDomain = domainOf(returnPath);

        if (fromDomain != null && replyDomain != null && !sameOrg(fromDomain, replyDomain)) {
            r.add(Finding.warning("Reply-To goes to a different domain",
                    "The email says it is from \"" + fromDomain + "\" but replies go to \"" + replyDomain + "\". "
                            + "Scammers use this to receive your reply.", "sender"));
        }
        if (fromDomain != null && returnDomain != null && !sameOrg(fromDomain, returnDomain)) {
            r.add(Finding.info("Return-Path domain is different",
                    "Bounces go to \"" + returnDomain + "\" instead of \"" + fromDomain + "\". "
                            + "This is normal for newsletter services, but unusual for personal or bank emails.", "sender"));
        }
        if (fromDomain != null) checkDomain(fromDomain, "sender address", r);
    }

    // ---------------------------------------------------------------- server path
    private void checkRouting(List<String[]> headers, ForensicReport r) {
        List<String> received = all(headers, "received");
        r.meta("Received hops", String.valueOf(received.size()));

        // The bottom-most “Received” header = the first (origin) server
        if (!received.isEmpty()) {
            String origin = received.get(received.size() - 1);
            Matcher ip = IPV4.matcher(origin);
            while (ip.find()) {
                String candidate = ip.group(1);
                if (!isPrivateIp(candidate)) {
                    r.meta("Originating IP", candidate);
                    r.add(Finding.info("Originating IP address",
                            "The first public server in the chain is " + candidate + ". "
                                    + "You can look it up on an IP reputation site.", "routing"));
                    break;
                }
            }
        }

        // Look for unusual time differences between servers
        List<ZonedDateTime> times = new ArrayList<>();
        for (String h : received) {
            int semi = h.lastIndexOf(';');
            if (semi < 0) continue;
            ZonedDateTime t = parseDate(h.substring(semi + 1));
            if (t != null) times.add(t);
        }
        for (int i = 0; i + 1 < times.size(); i++) {
            long hours = Math.abs(Duration.between(times.get(i + 1), times.get(i)).toHours());
            if (hours >= 6) {
                r.add(Finding.warning("Long delay between mail servers",
                        "There is a " + hours + "-hour gap between two Received headers. "
                                + "This is unusual and can mean the headers were edited.", "routing"));
                break;
            }
        }
    }

    // ---------------------------------------------------------------- link
    private void checkLinks(String body, ForensicReport r) {
        Set<String> urls = new LinkedHashSet<>();
        Matcher m = URL.matcher(body);
        while (m.find() && urls.size() < 200) urls.add(m.group().replaceAll("[.,;:!?]+$", ""));
        r.meta("Links found", String.valueOf(urls.size()));

        int reported = 0;
        for (String url : urls) {
            if (reported >= MAX_LINK_FINDINGS) break;
            String host;
            try {
                host = URI.create(url).getHost();
            } catch (IllegalArgumentException ex) {
                continue;
            }
            if (host == null) continue;
            host = host.toLowerCase(Locale.ROOT);

            if (IPV4.matcher(host).matches()) {
                r.add(Finding.critical("Link points to a raw IP address",
                        "The link " + url + " uses an IP number instead of a website name. Real companies almost never do this.", "links"));
                reported++;
            } else if (checkDomain(host, "link " + url, r)) {
                reported++;
            }
        }
    }

    /** @return true if there are any findings for this domain */
    private boolean checkDomain(String host, String where, ForensicReport r) {
        String registered = registeredDomain(host);
        String label = registered.contains(".") ? registered.substring(0, registered.indexOf('.')) : registered;
        String tld = host.substring(host.lastIndexOf('.') + 1);

        if (host.contains("xn--")) {
            r.add(Finding.warning("Look-alike (punycode) domain",
                    "The " + where + " uses \"" + host + "\", which can show letters that look like normal ones.", "links"));
            return true;
        }
        String normalized = host.replace('0', 'o').replace('1', 'l').replace('3', 'e')
                .replace('5', 's').replace("rn", "m").replace("vv", "w");
        for (String brand : BRANDS) {
            boolean looksLike = hasBrand(normalized, brand) && !hasBrand(host, brand);
            if (looksLike) {
                r.add(Finding.critical("Fake brand name in domain",
                        "The " + where + " uses \"" + host + "\", which imitates \"" + brand + "\" by swapping letters.", "links"));
                return true;
            }
            boolean brandButNotOwner = hasBrand(host, brand) && !label.equals(brand);
            if (brandButNotOwner) {
                r.add(Finding.warning("Brand name used in an unrelated domain",
                        "The " + where + " contains \"" + brand + "\" but the real domain is \"" + registered + "\". "
                                + "Check that this really belongs to " + brand + ".", "links"));
                return true;
            }
        }
        if (RISKY_TLDS.contains(tld)) {
            r.add(Finding.warning("Domain ending often used in scams",
                    "The " + where + " ends with \"." + tld + "\". This ending is cheap and often used for phishing.", "links"));
            return true;
        }
        return false;
    }

    /**
     * Short brand names (bri, bca, ovo, dana) must stand alone, for example, “bri-login.com”,
     * so that words like “cambridge” or “canadian” are not also considered part of the brand.
     */
    private static boolean hasBrand(String host, String brand) {
        if (brand.length() >= 5) return host.contains(brand);
        for (String part : host.split("[.\\-]")) if (part.equals(brand)) return true;
        return false;
    }

    // ---------------------------------------------------------------- attachment
    private void checkAttachments(String body, ForensicReport r) {
        Matcher m = Pattern.compile("(?i)(?:file)?name\\*?=\"?([^\"\\r\\n;]+)").matcher(body);
        Set<String> names = new LinkedHashSet<>();
        while (m.find()) names.add(m.group(1).trim());
        if (!names.isEmpty()) r.meta("Attachments", String.join(", ", names));
        for (String name : names) {
            String ext = ForensicUtils.extension(name);
            if (DANGEROUS_ATTACH.contains(ext)) {
                r.add(Finding.critical("Dangerous attachment",
                        "The attachment \"" + name + "\" is a ." + ext + " file, which can run code or open a fake login page.", "attachments"));
            } else if (name.toLowerCase(Locale.ROOT).matches(".*\\.(pdf|docx?|xlsx?|jpg|png)\\.[a-z0-9]{2,4}$")) {
                r.add(Finding.critical("Double file extension",
                        "The attachment \"" + name + "\" hides its real type behind a fake extension.", "attachments"));
            }
        }
    }

    private void checkPressureLanguage(String text, ForensicReport r) {
        List<String> hits = new ArrayList<>();
        for (String w : PRESSURE_WORDS) if (text.contains(w)) hits.add(w);
        if (hits.size() >= 2) {
            r.add(Finding.warning("Pressure or bait language",
                    "The email uses words often found in scams: " + String.join(", ", hits.subList(0, Math.min(5, hits.size()))) + ".", "content"));
        }
    }

    // ---------------------------------------------------------------- helper parsing
    private static int headerEnd(String raw) {
        int a = raw.indexOf("\r\n\r\n");
        int b = raw.indexOf("\n\n");
        if (a < 0) return b;
        if (b < 0) return a;
        return Math.min(a, b);
    }

    /** Headers can continue onto the next line if that line begins with a space or tab. */
    private static List<String[]> parseHeaders(String part) {
        List<String[]> out = new ArrayList<>();
        String name = null;
        StringBuilder value = new StringBuilder();
        for (String line : part.split("\\r?\\n")) {
            if ((line.startsWith(" ") || line.startsWith("\t")) && name != null) {
                value.append(' ').append(line.trim());
                continue;
            }
            if (name != null) out.add(new String[]{name, value.toString().trim()});
            int colon = line.indexOf(':');
            if (colon <= 0) { name = null; value.setLength(0); continue; }
            name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            value.setLength(0);
            value.append(line.substring(colon + 1));
        }
        if (name != null) out.add(new String[]{name, value.toString().trim()});
        return out;
    }

    private static String first(List<String[]> headers, String name) {
        for (String[] h : headers) if (h[0].equals(name)) return h[1];
        return null;
    }

    private static List<String> all(List<String[]> headers, String name) {
        List<String> out = new ArrayList<>();
        for (String[] h : headers) if (h[0].equals(name)) out.add(h[1]);
        return out;
    }

    private static String decodeQuotedPrintable(String s) {
        return s.replaceAll("=\\r?\\n", "").replace("=3D", "=").replace("=2E", ".");
    }

    private static String domainOf(String address) {
        if (address == null) return null;
        Matcher m = EMAIL_DOMAIN.matcher(address);
        return m.find() ? m.group(1).toLowerCase(Locale.ROOT) : null;
    }

    /** Take the last two parts of the domain (for example: mail.google.com → google.com, x.co.id → three parts). */
    private static String registeredDomain(String host) {
        String[] p = host.split("\\.");
        if (p.length <= 2) return host;
        String last2 = p[p.length - 2] + "." + p[p.length - 1];
        boolean secondLevel = Set.of("co.id", "ac.id", "go.id", "or.id", "web.id", "my.id", "co.uk", "com.au", "com.sg")
                .contains(last2);
        return secondLevel && p.length >= 3 ? p[p.length - 3] + "." + last2 : last2;
    }

    private static boolean sameOrg(String a, String b) {
        return registeredDomain(a).equals(registeredDomain(b));
    }

    private static boolean isPrivateIp(String ip) {
        return ip.startsWith("10.") || ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("0.")
                || ip.matches("172\\.(1[6-9]|2\\d|3[01])\\..*");
    }

    private static ZonedDateTime parseDate(String s) {
        String cleaned = s.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim().replaceAll("\\s+", " ");
        try {
            return ZonedDateTime.parse(cleaned, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    private static String or(String s) {
        return s == null ? "" : s;
    }
}
