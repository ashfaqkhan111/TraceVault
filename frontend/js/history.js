/* ============================================================
   TraceVault — History Module
   Lists all analyses with search, filter chips, sort, and
   pagination. Also handles delete/clear and CSV export.
   ============================================================ */

const TraceVaultHistory = (() => {

  /* ---------- State ---------- */
  let allAnalyses   = [];
  let filteredList  = [];
  let currentPage   = 1;
  const PAGE_SIZE   = 10;
  let activeFilter  = 'all';
  let activeSortKey = 'date_desc';
  let searchQuery   = '';

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
      year:'numeric', month:'short', day:'numeric', hour:'2-digit', minute:'2-digit',
    });
  }
  function formatSize(bytes) {
    if (!bytes) return '—';
    if (bytes < 1024)    return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }
  function fileIcon(filename) {
    const ext = (filename.split('.').pop() || '').toLowerCase();
    const map = {
      pdf:'📄', doc:'📝', docx:'📝', eml:'📧', msg:'📧',
      jpg:'🖼️', jpeg:'🖼️', png:'🖼️', gif:'🖼️',
      exe:'⚙️', dll:'⚙️', zip:'🗜️',
    };
    return map[ext] || '📁';
  }

  /* ---------- Filter ---------- */
  function applyFilters() {
    let list = [...allAnalyses];

    // Search
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      list = list.filter(a =>
        a.name.toLowerCase().includes(q) ||
        (a.type || '').toLowerCase().includes(q) ||
        (a.hash?.sha256 || '').toLowerCase().includes(q)
      );
    }

    // Type/risk filter
    switch (activeFilter) {
      case 'file':     list = list.filter(a => a.type === 'file');            break;
      case 'email':    list = list.filter(a => a.type === 'email');           break;
      case 'malware':  list = list.filter(a => a.type === 'malware');         break;
      case 'critical': list = list.filter(a => a.riskLevel === 'high');       break;
      case 'warning':  list = list.filter(a => a.riskLevel === 'medium');     break;
      case 'clean':    list = list.filter(a => a.riskLevel === 'low');        break;
      default: break;
    }

    // Sort
    switch (activeSortKey) {
      case 'date_asc':  list.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt)); break;
      case 'date_desc': list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)); break;
      case 'risk_desc': list.sort((a, b) => (b.riskScore || 0) - (a.riskScore || 0));       break;
      case 'risk_asc':  list.sort((a, b) => (a.riskScore || 0) - (b.riskScore || 0));       break;
      case 'name_asc':  list.sort((a, b) => a.name.localeCompare(b.name));                  break;
    }

    filteredList = list;
    currentPage  = 1;
    renderList();
    renderPagination();
    renderSummaryStats();
  }

  /* ---------- Render list ---------- */
  function renderList() {
    const listEl  = document.getElementById('historyList');
    const emptyEl = document.getElementById('historyEmpty');
    if (!listEl) return;

    const start = (currentPage - 1) * PAGE_SIZE;
    const page  = filteredList.slice(start, start + PAGE_SIZE);

    if (filteredList.length === 0) {
      listEl.innerHTML    = '';
      if (emptyEl) emptyEl.style.display = 'flex';
      return;
    }
    if (emptyEl) emptyEl.style.display = 'none';

    listEl.innerHTML = page.map(a => renderAnalysisCard(a)).join('');
  }

  function renderAnalysisCard(a) {
    const icon  = fileIcon(a.name);
    const riskMap = {
      high:   { cls:'badge--danger',  label:'High Risk' },
      medium: { cls:'badge--warning', label:'Medium Risk' },
      low:    { cls:'badge--success', label:'Low Risk' },
    };
    const rb = riskMap[a.riskLevel] || riskMap.low;

    return `
      <div class="analysis-card" onclick="window.location.href='results.html?id=${a.id}'" data-id="${a.id}">
        <div class="analysis-card__file-icon">${icon}</div>
        <div class="analysis-card__info">
          <div class="analysis-card__name">${escapeHtml(a.name)}</div>
          <div class="analysis-card__meta">
            <span class="analysis-card__meta-item">🏷️ ${capitalize(a.type || 'file')}</span>
            <span class="analysis-card__meta-item">💾 ${formatSize(a.size)}</span>
            <span class="analysis-card__meta-item">📅 ${formatDate(a.createdAt)}</span>
            ${a.counts ? `<span class="analysis-card__meta-item">⚠ ${a.counts.critical + a.counts.warning} findings</span>` : ''}
          </div>
        </div>
        <div class="analysis-card__actions" onclick="event.stopPropagation()">
          <span class="badge ${rb.cls}">${rb.label}</span>
          <button class="btn btn-secondary btn-sm" onclick="window.location.href='results.html?id=${a.id}'" title="View results">👁</button>
          <button class="btn btn-danger-ghost btn-sm" onclick="TraceVaultHistory.deleteOne('${a.id}')" title="Delete">🗑</button>
        </div>
      </div>`;
  }

  /* ---------- Summary stats ---------- */
  function renderSummaryStats() {
    const total    = filteredList.length;
    const critical = filteredList.filter(a => a.riskLevel === 'high').length;
    const warning  = filteredList.filter(a => a.riskLevel === 'medium').length;
    const clean    = filteredList.filter(a => a.riskLevel === 'low').length;

    setText('histTotal',    total);
    setText('histCritical', critical);
    setText('histWarning',  warning);
    setText('histClean',    clean);
  }

  /* ---------- Pagination ---------- */
  function renderPagination() {
    const totalPages = Math.ceil(filteredList.length / PAGE_SIZE);
    const prevBtn    = document.getElementById('btnPrevPage');
    const nextBtn    = document.getElementById('btnNextPage');
    const infoEl     = document.getElementById('pageInfo');
    const paginEl    = document.getElementById('pagination');

    if (paginEl) paginEl.style.display = filteredList.length > PAGE_SIZE ? 'flex' : 'none';
    if (infoEl) infoEl.textContent = `Page ${currentPage} of ${totalPages || 1}`;
    if (prevBtn) prevBtn.disabled = currentPage <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages;
  }

  /* ---------- Delete one ---------- */
  async function deleteOne(id) {
    if (!confirm('Delete this analysis? This cannot be undone.')) return;
    try {
      await TraceVaultAPI.deleteAnalysis(id);
      allAnalyses = allAnalyses.filter(a => a.id !== id);
      applyFilters();
      toast('Analysis deleted.', 'success');
    } catch (err) {
      toast('Delete failed: ' + err.message, 'error');
    }
  }

  /* ---------- Clear all ---------- */
  async function clearAll() {
    try {
      for (const a of allAnalyses) {
        await TraceVaultAPI.deleteAnalysis(a.id);
      }
      allAnalyses = [];
      applyFilters();
      document.getElementById('clearModal')?.classList.remove('open');
      toast('All history cleared.', 'success');
    } catch (err) {
      toast('Clear failed: ' + err.message, 'error');
    }
  }

  /* ---------- Export CSV ---------- */
  function exportCSV() {
    if (!filteredList.length) { toast('No analyses to export.', 'warning'); return; }
    const headers = ['ID','Filename','Type','Risk Level','Risk Score','Critical','Warning','Info','Clean','Date','SHA-256'];
    const rows = filteredList.map(a => [
      a.id, a.name, a.type, a.riskLevel, a.riskScore,
      a.counts?.critical || 0, a.counts?.warning || 0,
      a.counts?.info || 0, a.counts?.ok || 0,
      new Date(a.createdAt).toISOString(),
      a.hash?.sha256 || '',
    ]);
    const csv  = [headers, ...rows].map(r => r.map(v => `"${String(v).replace(/"/g,'""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href = url; a.download = 'tracevault-history.csv'; a.click();
    URL.revokeObjectURL(url);
    toast('CSV exported.', 'success');
  }

  /* ---------- Sidebar toggle ---------- */
  function initSidebarToggle() {
    document.getElementById('sidebarToggle')?.addEventListener('click', () => {
      document.getElementById('sidebar')?.classList.toggle('open');
    });
  }

  /* ---------- Debounce ---------- */
  function debounce(fn, delay) {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn(...args), delay);
    };
  }

  /* ---------- Main init ---------- */
  async function init() {
    initSidebarToggle();

    // Search inputs (topbar + toolbar)
    const onSearch = debounce(val => {
      searchQuery = val.trim();
      applyFilters();
    }, 280);

    document.getElementById('historySearch')?.addEventListener('input', e => onSearch(e.target.value));
    document.getElementById('topbarSearch')?.addEventListener('input', e => {
      document.getElementById('historySearch').value = e.target.value;
      onSearch(e.target.value);
    });

    // Filter chips
    document.querySelectorAll('.filter-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        activeFilter = chip.dataset.filter;
        applyFilters();
      });
    });

    // Sort
    document.getElementById('historySortSelect')?.addEventListener('change', e => {
      activeSortKey = e.target.value;
      applyFilters();
    });

    // Pagination
    document.getElementById('btnPrevPage')?.addEventListener('click', () => {
      if (currentPage > 1) { currentPage--; renderList(); renderPagination(); }
    });
    document.getElementById('btnNextPage')?.addEventListener('click', () => {
      const totalPages = Math.ceil(filteredList.length / PAGE_SIZE);
      if (currentPage < totalPages) { currentPage++; renderList(); renderPagination(); }
    });

    // Export CSV
    document.getElementById('btnExportCSV')?.addEventListener('click', exportCSV);

    // Clear all
    document.getElementById('btnClearHistory')?.addEventListener('click', () => {
      if (!allAnalyses.length) { toast('No history to clear.', 'info'); return; }
      setText('clearCount', allAnalyses.length);
      document.getElementById('clearModal')?.classList.add('open');
    });
    document.getElementById('confirmClearBtn')?.addEventListener('click', clearAll);

    // Modal close
    document.querySelectorAll('.modal__close').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('open'));
      });
    });

    // Load data
    try {
      document.getElementById('historyLoading').style.display = 'block';
      allAnalyses = await TraceVaultAPI.getAnalyses();
      document.getElementById('historyLoading').style.display = 'none';
      applyFilters();
    } catch (err) {
      document.getElementById('historyLoading').style.display = 'none';
      toast('Failed to load history: ' + err.message, 'error');
      document.getElementById('historyEmpty').style.display = 'flex';
    }
  }

  /* ---------- Tiny helpers ---------- */
  function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
  }

  return { init, deleteOne };

})();
