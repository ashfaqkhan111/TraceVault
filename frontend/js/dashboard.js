/* ============================================================
   TraceVault — Dashboard Module
   Stats, recent analyses list, quick actions, risk overview,
   activity timeline, sidebar toggle.
   ============================================================ */

const TraceVaultDashboard = (() => {

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

  /* ---------- File icon by extension ---------- */
  function fileIcon(filename) {
    const ext = (filename.split('.').pop() || '').toLowerCase();
    const map = {
      pdf:'📄', doc:'📝', docx:'📝', xls:'📊', xlsx:'📊',
      ppt:'📋', pptx:'📋', eml:'📧', msg:'📧',
      jpg:'🖼️', jpeg:'🖼️', png:'🖼️', gif:'🖼️',
      exe:'⚙️', dll:'⚙️', zip:'🗜️', tar:'🗜️', gz:'🗜️',
      txt:'📄', csv:'📊', json:'📋', xml:'📋',
    };
    return map[ext] || '📁';
  }

  /* ---------- Risk badge ---------- */
  function riskBadge(level) {
    const map = {
      high:   { cls: 'badge--danger',  label: 'High Risk' },
      medium: { cls: 'badge--warning', label: 'Medium Risk' },
      low:    { cls: 'badge--success', label: 'Low Risk' },
    };
    const b = map[level] || map.low;
    return `<span class="badge ${b.cls}">${b.label}</span>`;
  }

  /* ---------- Relative time ---------- */
  function relativeTime(isoStr) {
    const diff = Date.now() - new Date(isoStr).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1)  return 'just now';
    if (m < 60) return `${m}m ago`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}h ago`;
    const d = Math.floor(h / 24);
    return `${d}d ago`;
  }

  /* ---------- Render recent analysis card ---------- */
  function renderAnalysisCard(a) {
    const icon  = fileIcon(a.name);
    const level = a.riskLevel || 'low';
    const badgeCls = { high:'badge-red', medium:'badge-amber', low:'badge-green' };
    const badgeLbl = { high:'High Risk', medium:'Medium Risk', low:'Low Risk' };
    return `
      <div class="ana-card" onclick="window.location.href='results.html?id=${a.id}'">
        <div class="ana-card__ico">${svgForExt(a.name)}</div>
        <div class="ana-card__info">
          <div class="ana-card__name">${escapeHtml(a.name)}</div>
          <div class="ana-card__meta">
            <span>${capitalize(a.type || 'file')}</span>
            <span>${relativeTime(a.createdAt)}</span>
            ${a.counts ? `<span>⚠ ${a.counts.critical + a.counts.warning} findings</span>` : ''}
          </div>
        </div>
        <div class="ana-card__actions">
          <span class="badge ${badgeCls[level]||'badge-green'}">${badgeLbl[level]||'Low Risk'}</span>
        </div>
      </div>`;
  }

  function svgForExt(name) {
    const ext = (name.split('.').pop()||'').toLowerCase();
    const emailExts = ['eml','msg'];
    const imgExts   = ['jpg','jpeg','png','gif','webp'];
    if (emailExts.includes(ext)) return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>`;
    if (imgExts.includes(ext))   return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>`;
    return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>`;
  }

  /* ---------- Render activity timeline item ---------- */
  function renderTimelineItem(a) {
    return `
      <div class="tl-item">
        <div class="tl-item__time">${relativeTime(a.createdAt)}</div>
        <div class="tl-item__title">Analysis: ${escapeHtml(a.name)}</div>
        <div class="tl-item__desc">
          ${capitalize(a.type)} · Risk ${a.riskScore}/100 · ${a.counts?.critical || 0} critical finding${a.counts?.critical !== 1 ? 's' : ''}
        </div>
      </div>`;
  }

  /* ---------- Compute stats from analyses array ---------- */
  function computeStats(analyses) {
    const total    = analyses.length;
    const threats  = analyses.filter(a => a.riskLevel === 'high').length;
    const clean    = analyses.filter(a => a.riskLevel === 'low').length;
    const reports  = total; // every analysis can produce a report

    const critical = analyses.reduce((s, a) => s + (a.counts?.critical || 0), 0);
    const warning  = analyses.reduce((s, a) => s + (a.counts?.warning  || 0), 0);
    const info     = analyses.reduce((s, a) => s + (a.counts?.info     || 0), 0);

    return { total, threats, clean, reports, critical, warning, info };
  }

  /* ---------- Update risk overview bars ---------- */
  function updateRiskBars(stats) {
    const max = Math.max(stats.critical, stats.warning, stats.info, stats.clean, 1);
    const pct = v => Math.round((v / max) * 100);

    ['critical', 'warning', 'info', 'clean'].forEach(key => {
      const bar   = document.getElementById('risk' + capitalize(key));
      const count = document.getElementById('risk' + capitalize(key) + 'Count');
      if (bar)   bar.style.width   = pct(stats[key]) + '%';
      if (count) count.textContent = stats[key];
    });
  }

  /* ---------- Sidebar toggle ---------- */
  function initSidebarToggle() {
    const toggle  = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    toggle?.addEventListener('click', () => {
      sidebar?.classList.toggle('open');
    });
    // close on outside click (mobile)
    document.addEventListener('click', e => {
      if (sidebar?.classList.contains('open') &&
          !sidebar.contains(e.target) &&
          e.target !== toggle) {
        sidebar.classList.remove('open');
      }
    });
  }

  /* ---------- Smooth scroll to upload ---------- */
  function scrollToUpload() {
    const el = document.getElementById('upload');
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /* ---------- Open last report ---------- */
  function openLastReport() {
    const analyses = JSON.parse(localStorage.getItem('tv_analyses') || '[]');
    if (!analyses.length) { toast('No analyses yet. Run your first one!', 'info'); return; }
    window.location.href = `results.html?id=${analyses[0].id}`;
  }

  /* ---------- Analysis type option selector ---------- */
  function initTypeSelector() {
    document.querySelectorAll('.analysis-type-option').forEach(opt => {
      opt.addEventListener('click', () => {
        document.querySelectorAll('.analysis-type-option').forEach(o => o.classList.remove('selected'));
        opt.classList.add('selected');
      });
    });
  }

  /* ---------- Main init ---------- */
  async function init() {
    initSidebarToggle();
    initTypeSelector();

    // Greet user
    const user = TraceVaultAuth.getUser();
    if (user) {
      const hour    = new Date().getHours();
      const greet   = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
      const nameEl  = document.getElementById('welcomeName');
      if (nameEl) nameEl.textContent = user.firstName || 'Investigator';
      // update greeting text directly
      const greetEl = document.querySelector('.welcome-banner__greeting');
      if (greetEl) {
        greetEl.innerHTML = `${greet}, <span id="welcomeName">${user.firstName || 'Investigator'}</span> 👋`;
      }
    }

    // scroll to #upload if hash present
    if (window.location.hash === '#upload') scrollToUpload();
    document.getElementById('startAnalysisBtn')?.addEventListener('click', scrollToUpload);
    document.getElementById('navUpload')?.addEventListener('click', e => {
      e.preventDefault(); scrollToUpload();
    });

    // Load analyses
    try {
      const analyses = await TraceVaultAPI.getAnalyses();
      const stats    = computeStats(analyses);

      // Update stat cards
      animateCount('statTotal',   stats.total);
      animateCount('statThreats', stats.threats);
      animateCount('statClean',   stats.clean);
      animateCount('statReports', stats.reports);

      // pending count
      const pendingEl = document.getElementById('pendingCount');
      if (pendingEl) pendingEl.textContent = analyses.filter(a => a.riskLevel === 'high' || a.riskLevel === 'medium').length;

      // badge in sidebar
      const badge = document.getElementById('histBadge');
      if (badge) badge.textContent = stats.total;

      const pct = v => Math.round((v / Math.max(stats.critical, stats.warning, stats.info, stats.clean, 1)) * 100);

      // Update risk bars
      ['Critical','Warning','Info','Clean'].forEach((k,i) => {
        const keyLow = k.toLowerCase();
        const bar   = document.getElementById('r' + k);
        const count = document.getElementById('r' + k + 'N');
        if (bar)   bar.style.width   = pct(stats[keyLow]) + '%';
        if (count) count.textContent = stats[keyLow];
      });

      // Recent list
      const listEl  = document.getElementById('recentList');
      const emptyEl = document.getElementById('recentEmpty');
      if (listEl) {
        if (analyses.length === 0) {
          if (emptyEl) emptyEl.style.display = 'flex';
        } else {
          if (emptyEl) emptyEl.style.display = 'none';
          const recent = analyses.slice(0, 5);
          // prepend cards before the empty element
          recent.forEach(a => {
            const div = document.createElement('div');
            div.innerHTML = renderAnalysisCard(a);
            listEl.insertBefore(div.firstElementChild, emptyEl);
          });
        }
      }

      // Activity timeline
      const timelineEl   = document.getElementById('actTimeline');
      const actEmptyEl   = document.getElementById('actEmpty');
      if (timelineEl && analyses.length > 0) {
        if (actEmptyEl) actEmptyEl.style.display = 'none';
        timelineEl.innerHTML = analyses.slice(0, 4).map(renderTimelineItem).join('');
      }

    } catch (err) {
      console.error('[Dashboard] Failed to load analyses:', err);
    }
  }

  /* ---------- Animated counter ---------- */
  function animateCount(elId, target) {
    const el = document.getElementById(elId);
    if (!el) return;
    let start = 0;
    const step = Math.ceil(target / 20);
    const timer = setInterval(() => {
      start = Math.min(start + step, target);
      el.textContent = start;
      if (start >= target) clearInterval(timer);
    }, 40);
  }

  /* ---------- Utils ---------- */
  function escapeHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }
  function capitalize(str) {
    return str ? str.charAt(0).toUpperCase() + str.slice(1) : '';
  }

  return { init, openLastReport, scrollToUpload };

})();
