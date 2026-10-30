/* ============================================================
   TraceVault — API Layer
   Wraps all backend calls. Falls back to mock data when the
   backend is not running (prototype / offline mode).
   ============================================================ */

const TraceVaultAPI = (() => {

  /* ---------- Config ---------- */
  const BASE_URL   = 'http://localhost:8080/api';
  const MOCK_MODE  = true;   // set false when backend is live
  const MOCK_DELAY = 1200;   // ms — simulates network latency

  /* ---------- Utility ---------- */
  function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  function getAuthHeader() {
    const token = localStorage.getItem('tv_token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }

  async function request(method, path, body = null) {
    const headers = {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
    };
    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(`${BASE_URL}${path}`, opts);
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  }

  async function upload(path, formData) {
    const headers = { ...getAuthHeader() };
    const res = await fetch(`${BASE_URL}${path}`, {
      method: 'POST',
      headers,
      body: formData,
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  }

  /* ============================================================
     MOCK DATA GENERATORS
     ============================================================ */

  function mockAnalysisResult(file, type) {
    const id   = 'tv_' + Date.now() + '_' + Math.random().toString(36).slice(2, 7);
    const name = file ? file.name : 'sample_file.pdf';
    const size = file ? file.size : 204800;
    const now  = new Date().toISOString();

    const hash256 = Array.from({ length: 64 }, () =>
      Math.floor(Math.random() * 16).toString(16)).join('');
    const hashMD5 = Array.from({ length: 32 }, () =>
      Math.floor(Math.random() * 16).toString(16)).join('');

    /* --- Generate findings based on type --- */
    const allFindings = {
      file: [
        { id:'f1', severity:'warning', title:'Metadata author mismatch',
          desc:'The document\'s Author field ("John D.") differs from the Last Modified By field ("Admin"). This inconsistency may indicate the file was modified after its original creation.',
          category:'metadata' },
        { id:'f2', severity:'info', title:'Creation timestamp',
          desc:`File was created on ${new Date(Date.now() - 86400000 * 7).toUTCString()}. Verify this matches the claimed evidence acquisition date.`,
          category:'metadata' },
        { id:'f3', severity:'ok', title:'File signature matches extension',
          desc:'The file\'s magic bytes match the declared extension. No extension spoofing detected.',
          category:'integrity' },
        { id:'f4', severity:'info', title:'Software fingerprint',
          desc:'Created with Microsoft Office 16.0.14931.20132. Confirm this version is consistent with the claimed source system.',
          category:'metadata' },
      ],
      email: [
        { id:'f1', severity:'critical', title:'SPF authentication failed',
          desc:'The sending IP address (185.220.101.47) is not listed in the domain\'s SPF record. The email may be spoofed or relayed through an unauthorized server.',
          category:'authentication' },
        { id:'f2', severity:'critical', title:'Suspicious link detected',
          desc:'The email contains a link to http://paypa1-secure-login.xyz which mimics a legitimate payment service. This is a likely phishing indicator.',
          category:'links' },
        { id:'f3', severity:'warning', title:'DKIM signature missing',
          desc:'No DKIM signature was found in the email headers. Legitimate senders typically sign their emails with DKIM.',
          category:'authentication' },
        { id:'f4', severity:'warning', title:'Received header timestamp anomaly',
          desc:'There is a 14-hour gap between two consecutive Received headers, which is unusual and may indicate header injection.',
          category:'routing' },
        { id:'f5', severity:'info', title:'Originating IP geolocation',
          desc:'The originating mail server IP resolves to a VPS provider in Eastern Europe. Cross-reference with known threat intelligence.',
          category:'routing' },
      ],
      malware: [
        { id:'f1', severity:'critical', title:'Macro code detected',
          desc:'The file contains 3 embedded VBA macros. Two of them call Shell() with obfuscated arguments — a common technique used by malicious documents to execute arbitrary commands.',
          category:'code' },
        { id:'f2', severity:'critical', title:'High entropy section',
          desc:'Section .rsrc has an entropy of 7.82/8.0, strongly suggesting packed or encrypted content. This is a common indicator of malware attempting to evade detection.',
          category:'structure' },
        { id:'f3', severity:'warning', title:'Suspicious imported API calls',
          desc:'The binary imports VirtualAlloc, WriteProcessMemory, and CreateRemoteThread — a classic combination used for process injection.',
          category:'imports' },
        { id:'f4', severity:'warning', title:'Hardcoded IP address',
          desc:'The string "192.168.100.254:4444" was found in the binary. Port 4444 is commonly associated with Metasploit reverse shells.',
          category:'strings' },
        { id:'f5', severity:'info', title:'Compilation timestamp',
          desc:`PE header compilation timestamp: ${new Date(Date.now() - 86400000 * 3).toUTCString()}. Note that this value can be forged.`,
          category:'structure' },
      ],
    };

    const findings = allFindings[type] || allFindings.file;
    const critical = findings.filter(f => f.severity === 'critical').length;
    const warning  = findings.filter(f => f.severity === 'warning').length;
    const info     = findings.filter(f => f.severity === 'info').length;
    const ok       = findings.filter(f => f.severity === 'ok').length;

    const riskScore = Math.min(100, critical * 30 + warning * 15 + info * 5);
    const riskLevel = riskScore >= 70 ? 'high' : riskScore >= 35 ? 'medium' : 'low';

    const metadata = {
      file: {
        'Filename':        name,
        'File size':       formatFileSize(size),
        'MIME type':       guessMime(name),
        'Created':         new Date(Date.now() - 86400000 * 7).toUTCString(),
        'Last modified':   new Date(Date.now() - 86400000 * 2).toUTCString(),
        'Author':          'John D.',
        'Last modified by':'Admin',
        'Software':        'Microsoft Office 16.0',
        'Pages':           '4',
        'Words':           '1,204',
      },
      email: {
        'Message-ID':    `<${hash256.slice(0,12)}@mail.example.com>`,
        'From':          'billing@paypa1-secure.xyz',
        'Reply-To':      'no-reply@mail-secure.info',
        'To':            'victim@company.com',
        'Subject':       'Action Required: Verify your account',
        'Date':          new Date(Date.now() - 86400000).toUTCString(),
        'MIME-Version':  '1.0',
        'X-Mailer':      'PHPMailer 6.1.8',
        'Originating IP':'185.220.101.47',
        'SPF result':    'FAIL',
        'DKIM result':   'NONE',
        'DMARC result':  'FAIL',
      },
      malware: {
        'Filename':       name,
        'File size':      formatFileSize(size),
        'File type':      'PE32 executable (GUI) Intel 80386',
        'Compilation TS': new Date(Date.now() - 86400000 * 3).toUTCString(),
        'Entry point':    '0x00401000',
        'Sections':       '.text, .data, .rsrc, .reloc',
        'Imports':        'kernel32.dll, user32.dll, advapi32.dll',
        'Packers':        'UPX (detected)',
        'Entropy':        '7.82',
        'Architecture':   'x86 (32-bit)',
      },
    };

    const aiSummaries = {
      file: `This file exhibits several metadata inconsistencies that warrant further investigation. The author mismatch between "John D." and "Admin" suggests the document may have been modified after its initial creation — potentially by someone with access to the system under a different user account. While no malicious content was identified within the file itself, the timestamps should be cross-referenced with system logs from the source machine to establish a verifiable chain of custody. The file signature is valid and matches the declared extension, confirming no extension spoofing occurred.`,
      email: `This email exhibits multiple strong indicators of a phishing attempt. The SPF authentication failure confirms the email was sent from an unauthorized server, and the absence of a DKIM signature removes a key layer of sender authenticity verification. The embedded link to "paypa1-secure-login.xyz" is a typosquatting domain designed to impersonate a legitimate payment service. The routing anomaly (14-hour gap in Received headers) further suggests this email passed through infrastructure used to obscure its true origin. Recommend blocking the originating IP range and flagging the domain for takedown.`,
      malware: `This file exhibits multiple high-confidence indicators of malicious intent. The presence of obfuscated VBA macros using Shell() calls, combined with the high entropy .rsrc section (consistent with packed or encrypted payload), strongly suggests this is a malicious document designed to deliver a secondary payload upon opening. The imported API combination (VirtualAlloc + WriteProcessMemory + CreateRemoteThread) is a textbook process injection technique. The hardcoded IP:port string is consistent with a Metasploit-style reverse shell command-and-control configuration. This file should be treated as confirmed malware and handled in an isolated environment.`,
    };

    const recommendations = {
      file: [
        'Cross-reference creation and modification timestamps against system event logs.',
        'Identify both user accounts referenced in the metadata.',
        'Verify the file hash against any known-good baseline if available.',
        'Document the chain of custody for this evidence item.',
      ],
      email: [
        'Block the originating IP address (185.220.101.47) at the perimeter.',
        'Report the phishing domain to the registrar and hosting provider.',
        'Search mail server logs for other emails from this sender.',
        'Notify potentially affected users and reset credentials if clicked.',
        'Submit the URL and sender domain to threat intelligence platforms.',
      ],
      malware: [
        'Do NOT open or execute this file outside an isolated sandbox environment.',
        'Search for this file hash across all endpoints using your EDR platform.',
        'Block the hardcoded C2 IP address (192.168.100.254) at the firewall.',
        'Quarantine any systems where this file was found.',
        'Submit for deeper dynamic analysis in a controlled sandbox.',
        'Review endpoint logs for any Shell() executions in the relevant timeframe.',
      ],
    };

    const timeline = [
      { time: new Date(Date.now() - 120000).toISOString(), title: 'File uploaded', desc: `Evidence file "${name}" was submitted for analysis.` },
      { time: new Date(Date.now() - 90000).toISOString(),  title: 'Hash computed', desc: `SHA-256: ${hash256.slice(0,16)}…` },
      { time: new Date(Date.now() - 60000).toISOString(),  title: 'Metadata extracted', desc: `${Object.keys(metadata[type] || metadata.file).length} metadata fields extracted.` },
      { time: new Date(Date.now() - 30000).toISOString(),  title: 'Forensic checks completed', desc: `${findings.length} findings identified (${critical} critical, ${warning} warnings).` },
      { time: new Date(Date.now() - 5000).toISOString(),   title: 'AI summary generated', desc: 'AI analysis summary produced based on findings.' },
      { time: now, title: 'Analysis complete', desc: 'Full report available for review and export.' },
    ];

    return {
      id, name, size, type,
      status:    'completed',
      createdAt: now,
      completedAt: now,
      duration:  '~2s',
      hash: { sha256: hash256, md5: hashMD5 },
      riskScore, riskLevel,
      findings,
      counts: { critical, warning, info, ok },
      metadata: metadata[type] || metadata.file,
      aiSummary: aiSummaries[type] || aiSummaries.file,
      recommendations: recommendations[type] || recommendations.file,
      timeline,
    };
  }

  /* ---------- Helpers ---------- */
  function formatFileSize(bytes) {
    if (bytes < 1024)       return bytes + ' B';
    if (bytes < 1048576)    return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  function guessMime(filename) {
    const ext = (filename.split('.').pop() || '').toLowerCase();
    const map = {
      pdf:'application/pdf', docx:'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      doc:'application/msword', eml:'message/rfc822', msg:'application/vnd.ms-outlook',
      jpg:'image/jpeg', jpeg:'image/jpeg', png:'image/png', gif:'image/gif',
      exe:'application/x-msdownload', dll:'application/x-msdownload',
      zip:'application/zip', tar:'application/x-tar', gz:'application/gzip',
    };
    return map[ext] || 'application/octet-stream';
  }

  /* ============================================================
     AUTH ENDPOINTS
     ============================================================ */

  async function login(email, password) {
    if (MOCK_MODE) {
      await delay(MOCK_DELAY);
      if (!email || !password) throw new Error('Email and password are required.');
      // Accept any credentials for prototype
      const user = {
        id: 'u_001',
        firstName: email.split('@')[0].charAt(0).toUpperCase() + email.split('@')[0].slice(1),
        lastName: 'User',
        email,
        role: 'forensic_investigator',
      };
      return { token: 'mock_token_' + Date.now(), user };
    }
    return request('POST', '/auth/login', { email, password });
  }

  async function register(data) {
    if (MOCK_MODE) {
      await delay(MOCK_DELAY);
      if (!data.email || !data.password) throw new Error('Email and password are required.');
      if (data.password.length < 8) throw new Error('Password must be at least 8 characters.');
      const nameParts = (data.fullName || '').trim().split(' ');
      const user = {
        id: 'u_' + Date.now(),
        firstName: nameParts[0] || 'User',
        lastName:  nameParts.slice(1).join(' ') || '',
        email: data.email,
        role: data.role || 'forensic_investigator',
      };
      return { token: 'mock_token_' + Date.now(), user };
    }
    return request('POST', '/auth/register', data);
  }

  async function requestPasswordReset(email) {
    if (MOCK_MODE) {
      await delay(MOCK_DELAY);
      if (!email) throw new Error('Email is required.');
      return { message: 'Reset link sent.' };
    }
    return request('POST', '/auth/password-reset', { email });
  }

  /* ============================================================
     ANALYSIS ENDPOINTS
     ============================================================ */

  async function submitAnalysis(file, type, notes) {
    if (MOCK_MODE) {
      await delay(MOCK_DELAY * 2);
      const result = mockAnalysisResult(file, type);
      if (notes) result.notes = notes;
      // Persist to localStorage
      const analyses = JSON.parse(localStorage.getItem('tv_analyses') || '[]');
      analyses.unshift(result);
      localStorage.setItem('tv_analyses', JSON.stringify(analyses));
      return result;
    }
    const fd = new FormData();
    fd.append('file', file);
    fd.append('type', type);
    if (notes) fd.append('notes', notes);
    return upload('/analyses', fd);
  }

  async function getAnalyses() {
    if (MOCK_MODE) {
      await delay(400);
      return JSON.parse(localStorage.getItem('tv_analyses') || '[]');
    }
    return request('GET', '/analyses');
  }

  async function getAnalysis(id) {
    if (MOCK_MODE) {
      await delay(300);
      const analyses = JSON.parse(localStorage.getItem('tv_analyses') || '[]');
      const found = analyses.find(a => a.id === id);
      if (!found) throw new Error('Analysis not found.');
      return found;
    }
    return request('GET', `/analyses/${id}`);
  }

  async function deleteAnalysis(id) {
    if (MOCK_MODE) {
      await delay(400);
      const analyses = JSON.parse(localStorage.getItem('tv_analyses') || '[]');
      const updated  = analyses.filter(a => a.id !== id);
      localStorage.setItem('tv_analyses', JSON.stringify(updated));
      return { message: 'Deleted.' };
    }
    return request('DELETE', `/analyses/${id}`);
  }

  async function exportReport(id, format = 'pdf') {
    if (MOCK_MODE) {
      await delay(800);
      const analysis = await getAnalysis(id);
      // Generate a text-based mock PDF content
      const content = buildMockReportText(analysis);
      return { content, filename: `tracevault-report-${id}.txt`, format: 'text' };
    }
    return request('GET', `/analyses/${id}/report?format=${format}`);
  }

  function buildMockReportText(a) {
    const line = '─'.repeat(60);
    const lines = [
      'TRACEVAULT FORENSIC ANALYSIS REPORT',
      line,
      `Analysis ID   : ${a.id}`,
      `File          : ${a.name}`,
      `Analysis Type : ${a.type}`,
      `Date          : ${new Date(a.createdAt).toLocaleString()}`,
      `Risk Score    : ${a.riskScore}/100 (${a.riskLevel.toUpperCase()})`,
      line,
      '',
      'HASH VERIFICATION',
      `SHA-256 : ${a.hash?.sha256 || '—'}`,
      `MD5     : ${a.hash?.md5    || '—'}`,
      '',
      line,
      '',
      'FINDINGS SUMMARY',
      `Critical : ${a.counts?.critical || 0}`,
      `Warning  : ${a.counts?.warning  || 0}`,
      `Info     : ${a.counts?.info     || 0}`,
      `Clean    : ${a.counts?.ok       || 0}`,
      '',
      line,
      '',
      'DETAILED FINDINGS',
      ...(a.findings || []).map((f, i) =>
        `[${i+1}] [${f.severity.toUpperCase()}] ${f.title}\n    ${f.desc}`
      ),
      '',
      line,
      '',
      'AI-ASSISTED SUMMARY',
      a.aiSummary || '—',
      '',
      line,
      '',
      'RECOMMENDATIONS',
      ...(a.recommendations || []).map((r, i) => `${i+1}. ${r}`),
      '',
      line,
      '',
      'DISCLAIMER',
      'This report was generated by TraceVault as an analysis aid.',
      'Findings should be reviewed by a qualified forensic examiner.',
      'AI-generated summaries are based solely on observable indicators.',
      '',
      `Generated: ${new Date().toUTCString()}`,
    ];
    return lines.join('\n');
  }

  /* ============================================================
     PUBLIC API
     ============================================================ */
  return {
    /* Auth */
    login,
    register,
    requestPasswordReset,
    /* Analyses */
    submitAnalysis,
    getAnalyses,
    getAnalysis,
    deleteAnalysis,
    exportReport,
    /* Helpers (exposed so other modules can reuse) */
    formatFileSize,
    guessMime,
    MOCK_MODE,
  };

})();
