/* ============================================================
   TraceVault — Results Module
   Loads and renders a single analysis result:
   findings, metadata, hash, timeline, AI summary, export.
   ============================================================ */

const TraceVaultResults = (() => {

  /* ---------- Toast ---------- */
  function toast(msg, type = 'info') {
    const icons = { success:'✅', error:'⚠', warning:'⚠', info:'ℹ' };
    const c = document.getElementById('toastContainer');
    if (!c) return;
    const t = document.createElement('div');
    t.className = `toast toast--${type}`;
    t.innerHTML = `<span class="toast__icon">${icons[type]||'ℹ'}</span><span class="toast__msg">${msg}</span>`;
    c.appendChild(t);
    setTimeout(() => t.remove(), 3500);
  }

  /* ---------- Helpers ---------- */
  function escapeHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }
  function capitalize(str) { return str ? str.charAt(0).toUpperCase() + str.slice(1) : ''; }
  function formatDate(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleString(undefined, {
      year:'numeric', month:'short', day:'numeric',
      hour:'2-digit', minute:'2-digit',
    });
  }

  /* ---------- File icon ---------- */
  function fileIcon(filename) {
    const ext = (filename.split('.').pop() || '').toLowerCase();
    const map = {
      pdf:'📄', doc:'📝', docx:'📝', eml:'📧', msg:'📧',
      jpg:'🖼️', jpeg:'🖼️', png:'🖼️', gif:'🖼️',
      exe:'⚙️', dll:'⚙️', zip:'🗜️',
    };
    return map[ext] || '📁';
  }

  /* ---------- Severity icon + label ---------- */
  const SEV = {
    critical: { icon:'🔴', label:'Critical' },
    warning:  { icon:'🟡', label:'Warning'  },
    info:     { icon:'🔵', label:'Info'     },
    ok:       { icon:'🟢', label:'Clean'    },
  };

  /* ---------- Tabs ---------- */
  function initTabs() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        const panel = document.getElementById('tab-' + btn.dataset.tab);
        if (panel) panel.classList.add('active');
      });
    });
  }

  /* ---------- Render findings ---------- */
  function renderFindings(findings) {
    const listEl  = document.getElementById('findingsList');
    const emptyEl = document.getElementById('findingsEmpty');
    if (!listEl) return;

    if (!findings || findings.length === 0) {
      if (emptyEl) emptyEl.style.display = 'flex';
      return;
    }
    if (emptyEl) emptyEl.style.display = 'none';

    const order = ['critical','warning','info','ok'];
    const sorted = [...findings].sort((a, b) =>
      order.indexOf(a.severity) - order.indexOf(b.severity));

    listEl.innerHTML = sorted.map(f => {
      const s = SEV[f.severity] || SEV.info;
      return `
        <div class="finding-item finding-item--${f.severity}">
          <div class="finding-item__icon">${s.icon}</div>
          <div class="finding-item__content">
            <div class="finding-item__title">
              ${escapeHtml(f.title)}
              <span class="badge badge--${f.severity === 'ok' ? 'success' : f.severity === 'critical' ? 'danger' : f.severity === 'warning' ? 'warning' : 'info'}"
                style="margin-left:8px;">${s.label}</span>
            </div>
            <div class="finding-item__desc">${escapeHtml(f.desc)}</div>
          </div>
        </div>`;
    }).join('');
  }

  /* ---------- Render metadata ---------- */
  function renderMetadata(metadata) {
    const el = document.getElementById('metadataList');
    if (!el || !metadata) return;
    el.innerHTML = Object.entries(metadata).map(([k, v]) => `
      <div class="meta-row">
        <div class="meta-row__key">${escapeHtml(k)}</div>
        <div class="meta-row__val">${escapeHtml(String(v))}</div>
      </div>`).join('');
  }

  /* ---------- Render hash ---------- */
  function renderHash(hash) {
    const sha = document.getElementById('sha256Value');
    const md5 = document.getElementById('md5Value');
    if (sha) sha.textContent = hash?.sha256 || '—';
    if (md5) md5.textContent = hash?.md5    || '—';

    // Copy buttons
    document.getElementById('copySHA256')?.addEventListener('click', () => {
      copyToClipboard(hash?.sha256 || '', 'SHA-256 hash copied.');
    });
    document.getElementById('copyMD5')?.addEventListener('click', () => {
      copyToClipboard(hash?.md5 || '', 'MD5 hash copied.');
    });

    // Compare hash
    document.getElementById('compareHashBtn')?.addEventListener('click', () => {
      const input  = document.getElementById('compareHashInput')?.value.trim().toLowerCase();
      const actual = hash?.sha256?.toLowerCase();
      const resultEl = document.getElementById('compareResult');
      if (!resultEl) return;
      resultEl.style.display = 'block';
      if (!input) {
        resultEl.innerHTML = `<div class="alert alert--info"><span class="alert__icon">ℹ</span><div class="alert__content">Enter a hash to compare.</div></div>`;
        return;
      }
      if (input === actual) {
        resultEl.innerHTML = `<div class="alert alert--success"><span class="alert__icon">✅</span><div class="alert__content"><div class="alert__title">Hash match confirmed</div><div class="alert__body">The file matches the expected hash. Integrity is verified.</div></div></div>`;
      } else {
        resultEl.innerHTML = `<div class="alert alert--danger"><span class="alert__icon">⚠</span><div class="alert__content"><div class="alert__title">Hash mismatch</div><div class="alert__body">The computed hash does not match. The file may have been modified or is a different version.</div></div></div>`;
      }
    });
  }

  /* ---------- Render timeline ---------- */
  function renderTimeline(timeline) {
    const el = document.getElementById('investigationTimeline');
    if (!el || !timeline) return;
    el.innerHTML = timeline.map(item => `
      <div class="timeline-item">
        <div class="timeline-item__time">${formatDate(item.time)}</div>
        <div class="timeline-item__title">${escapeHtml(item.title)}</div>
        <div class="timeline-item__desc">${escapeHtml(item.desc)}</div>
      </div>`).join('');
  }

  /* ---------- Render AI summary ---------- */
  function renderAISummary(summary, recommendations) {
    const summaryEl = document.getElementById('aiSummaryText');
    const recsEl    = document.getElementById('aiRecommendations');

    if (summaryEl) {
      summaryEl.innerHTML = '';
      // Typewriter effect
      let i = 0;
      const txt = summary || 'No AI summary available for this analysis.';
      const interval = setInterval(() => {
        if (i >= txt.length) { clearInterval(interval); return; }
        summaryEl.textContent += txt[i++];
      }, 12);
    }

    if (recsEl && recommendations?.length) {
      recsEl.innerHTML = `
        <ul style="display:flex;flex-direction:column;gap:var(--space-3);">
          ${recommendations.map((r, idx) => `
            <li style="display:flex;align-items:flex-start;gap:var(--space-3);font-size:var(--text-base);">
              <span style="background:var(--color-primary-subtle);color:var(--color-primary);width:24px;height:24px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:var(--text-sm);font-weight:700;flex-shrink:0;">${idx+1}</span>
              <span style="color:var(--color-text-muted);line-height:1.6;">${escapeHtml(r)}</span>
            </li>`).join('')}
        </ul>`;
    }
  }

  /* ---------- Render risk score ring ---------- */
  function renderRiskRing(score, level) {
    const ring = document.getElementById('riskRing');
    const num  = document.getElementById('riskNum');
    const lvl  = document.getElementById('riskLevel');

    if (ring) {
      ring.style.setProperty('--pct', score);
      ring.className = `risk-score-card__ring risk-score-card__ring--${level}`;
    }
    if (num) num.textContent = score;
    if (lvl) {
      lvl.textContent  = capitalize(level) + ' Risk';
      lvl.className    = `risk-score-card__level risk-score-card__level--${level}`;
    }
  }

  /* ---------- Render findings summary counts ---------- */
  function renderCounts(counts) {
    ['critical','warning','info','ok'].forEach(k => {
      const el = document.getElementById('count' + capitalize(k));
      if (el) el.textContent = counts?.[k] ?? 0;
    });
  }

  /* ---------- Render analysis details ---------- */
  function renderDetails(a) {
    setText('detailAnalysisId', a.id);
    setText('detailType',       capitalize(a.type));
    setText('detailStatus',     capitalize(a.status || 'completed'));
    setText('detailStarted',    formatDate(a.createdAt));
    setText('detailCompleted',  formatDate(a.completedAt));
    setText('detailDuration',   a.duration || '—');
  }

  /* ---------- Render evidence header ---------- */
  function renderEvidenceHeader(a) {
    const icon = fileIcon(a.name);
    setText('evidenceFilename', a.name);
    setText('evidenceDate',     formatDate(a.createdAt));
    setText('evidenceSize',     TraceVaultAPI.formatFileSize(a.size || 0));
    setText('evidenceType',     capitalize(a.type) + ' Analysis');
    setText('evidenceIcon',     icon);
    setText('breadcrumbFilename', a.name);

    // Risk badge
    const badgeEl = document.getElementById('evidenceRiskBadge');
    if (badgeEl) {
      const map = { high:'badge--danger', medium:'badge--warning', low:'badge--success' };
      badgeEl.className = `badge ${map[a.riskLevel] || 'badge--neutral'}`;
      badgeEl.textContent = capitalize(a.riskLevel || 'unknown') + ' Risk';
    }

    // Case notes
    if (a.notes) {
      const notesEl = document.getElementById('evidenceCaseNotes');
      const textEl  = document.getElementById('evidenceCaseNotesText');
      if (notesEl) notesEl.style.display = 'flex';
      if (textEl)  textEl.textContent = a.notes;
    }

    // Update document title
    document.title = `${a.name} — TraceVault`;
  }

  /* ---------- Export PDF (mock) ---------- */
  async function exportPDF(id) {
    toast('Preparing report…', 'info');
    try {
      const res  = await TraceVaultAPI.exportReport(id, 'pdf');
      const blob = new Blob([res.content], { type: 'text/plain' });
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = res.filename;
      a.click();
      URL.revokeObjectURL(url);
      toast('Report downloaded.', 'success');
    } catch (err) {
      toast('Export failed: ' + err.message, 'error');
    }
  }

  /* ---------- Export JSON ---------- */
  function exportJSON(analysis) {
    const blob = new Blob([JSON.stringify(analysis, null, 2)], { type: 'application/json' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = `tracevault-${analysis.id}.json`;
    a.click();
    URL.revokeObjectURL(url);
    toast('JSON exported.', 'success');
  }

  /* ---------- Copy to clipboard ---------- */
  function copyToClipboard(text, msg = 'Copied.') {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => toast(msg, 'success')).catch(() => {
      // fallback
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.style.position = 'fixed'; ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select(); document.execCommand('copy');
      document.body.removeChild(ta);
      toast(msg, 'success');
    });
  }

  /* ---------- Sidebar toggle ---------- */
  function initSidebarToggle() {
    document.getElementById('sidebarToggle')?.addEventListener('click', () => {
      document.getElementById('sidebar')?.classList.toggle('open');
    });
  }

  /* ---------- Main init ---------- */
  async function init() {
    initSidebarToggle();
    initTabs();

    // Get analysis ID from URL
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');

    if (!id) {
      document.getElementById('loadingSkeleton').style.display = 'none';
      document.getElementById('resultsContent').innerHTML = `
        <div class="empty-state">
          <div class="empty-state__icon">⚠</div>
          <div class="empty-state__title">No analysis ID specified</div>
          <div class="empty-state__desc">Open this page from an analysis result in your history.</div>
          <a href="history.html" class="btn btn-primary" style="margin-top:var(--space-4);">Go to History</a>
        </div>`;
      document.getElementById('resultsContent').style.display = 'block';
      return;
    }

    // Load analysis
    try {
      const a = await TraceVaultAPI.getAnalysis(id);

      // Hide skeleton, show content
      document.getElementById('loadingSkeleton').style.display = 'none';
      document.getElementById('resultsContent').style.display  = 'block';

      // Render all sections
      renderEvidenceHeader(a);
      renderRiskRing(a.riskScore, a.riskLevel);
      renderCounts(a.counts);
      renderDetails(a);
      renderFindings(a.findings);
      renderMetadata(a.metadata);
      renderHash(a.hash);
      renderTimeline(a.timeline);
      renderAISummary(a.aiSummary, a.recommendations);

      // Copy hash button in header
      document.getElementById('btnCopyHash')?.addEventListener('click', () => {
        copyToClipboard(a.hash?.sha256, 'SHA-256 hash copied.');
      });

      // Copy metadata
      document.getElementById('btnCopyMeta')?.addEventListener('click', () => {
        const text = Object.entries(a.metadata || {})
          .map(([k, v]) => `${k}: ${v}`).join('\n');
        copyToClipboard(text, 'Metadata copied.');
      });

      // Export PDF (all three buttons map to same action)
      ['btnExportPDF','btnExportPDFInline','btnExportPDFSidebar'].forEach(btnId => {
        document.getElementById(btnId)?.addEventListener('click', () => exportPDF(a.id));
      });

      // Export JSON
      document.getElementById('btnExportJSON')?.addEventListener('click', () => exportJSON(a));

      // Share link
      document.getElementById('btnShareLink')?.addEventListener('click', () => {
        copyToClipboard(window.location.href, 'Link copied to clipboard.');
      });

      // Delete analysis
      document.getElementById('btnDeleteAnalysis')?.addEventListener('click', () => {
        document.getElementById('deleteModal')?.classList.add('open');
      });
      document.getElementById('confirmDeleteBtn')?.addEventListener('click', async () => {
        try {
          await TraceVaultAPI.deleteAnalysis(a.id);
          toast('Analysis deleted.', 'success');
          setTimeout(() => window.location.href = 'history.html', 800);
        } catch (err) {
          toast('Delete failed: ' + err.message, 'error');
        }
      });

      // Close modal
      document.querySelector('.modal__close')?.addEventListener('click', () => {
        document.getElementById('deleteModal')?.classList.remove('open');
      });

    } catch (err) {
      document.getElementById('loadingSkeleton').style.display = 'none';
      document.getElementById('resultsContent').innerHTML = `
        <div class="empty-state">
          <div class="empty-state__icon">⚠</div>
          <div class="empty-state__title">Analysis not found</div>
          <div class="empty-state__desc">${escapeHtml(err.message)}</div>
          <a href="history.html" class="btn btn-primary" style="margin-top:var(--space-4);">Go to History</a>
        </div>`;
      document.getElementById('resultsContent').style.display = 'block';
    }
  }

  /* ---------- Tiny helpers ---------- */
  function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
  }

  return { init };

})();
