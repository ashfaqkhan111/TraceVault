/* ============================================================
   TraceVault — Verification history
   ============================================================ */
(async () => {
  const listEl = document.getElementById('histList');
  const search = document.getElementById('histSearch');
  const filterBtn = document.getElementById('filterBtn');
  const menu = document.getElementById('filterMenu');
  const countEl = document.getElementById('histCount');
  const pagerInfo = document.getElementById('pagerInfo');
  const pager = document.getElementById('pager');
  let items = []; let total = 0; let status = 'all'; let page = 1;

  const svg = (p) => `<svg class="ic" viewBox="0 0 24 24">${p}</svg>`;
  const I = {
    file: svg('<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/>'),
    shield: svg('<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/>'),
    check: svg('<circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/>'),
    loader: svg('<path d="M21 12a9 9 0 1 1-6.219-8.56"/>'),
    alert: svg('<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/>'),
    x: svg('<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>'),
    eye: svg('<path d="M2.06 12.35a1 1 0 0 1 0-.7 10.75 10.75 0 0 1 19.88 0 1 1 0 0 1 0 .7 10.75 10.75 0 0 1-19.88 0"/><circle cx="12" cy="12" r="3"/>'),
    clock: svg('<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>'),
    rotate: svg('<path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/>'),
    left: svg('<path d="m15 18-6-6 6-6"/>'), right: svg('<path d="m9 18 6-6-6-6"/>'),
  };
  const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

  const INTEGRITY = {
    verified: `<span class="badge badge--green">${I.shield}Verified Intact</span>`,
    verifying: `<span class="badge badge--yellow">${I.loader}Verifying…</span>`,
    mismatch: `<span class="badge badge--red">${I.alert}Mismatch Detected</span>`,
  };
  const STATUS = {
    completed: `<span class="badge badge--green">${I.check}Completed</span>`,
    processing: `<span class="badge badge--yellow">${I.loader}Processing</span>`,
    failed: `<span class="badge badge--red">${I.x}Failed</span>`,
  };
  const ACTION = {
    completed: (it) => `<a class="btn" href="report.html?id=${encodeURIComponent(it.id)}">${I.eye}View Report</a>`,
    processing: () => `<a class="btn btn--prog" href="notifications.html">${I.clock}View Progress</a>`,
    failed: (it) => `<button class="btn btn--re" type="button" data-reanalyze="${esc(it.name)}">${I.rotate}Re-analyze</button>`,
  };
  const TONE = { completed: '', processing: ' hist-row--yellow', failed: ' hist-row--red' };

  function visible() {
    const q = search.value.trim().toLowerCase();
    return items.filter((it) => (status === 'all' || it.status === status) && (!q || it.name.toLowerCase().includes(q) || it.hash.toLowerCase().includes(q)));
  }

  function render() {
    const rows = visible();
    listEl.innerHTML = rows.length ? rows.map((it) => `
      <div class="hist-row${TONE[it.status]}">
        ${I.file}
        <div><strong>${esc(it.name)}</strong><small>SHA-256 ${esc(it.hash)} &middot; ${esc(it.size)}</small></div>
        <div class="hist-col"><small>Format</small><span class="fmt">${esc(it.format)}</span></div>
        <div class="hist-col"><small>Date Analyzed</small><span>${esc(it.date)}</span></div>
        <div class="hist-col"><small>Integrity</small>${INTEGRITY[it.integrity]}</div>
        <div class="hist-col"><small>Status</small>${STATUS[it.status]}</div>
        ${ACTION[it.status](it)}
      </div>`).join('') : '<p class="empty-state">No verifications match your search.</p>';
    const filtered = rows.length !== items.length;
    countEl.textContent = filtered ? `Showing ${rows.length} matching items on this page` : `Showing ${items.length} of ${total} registered evidence items`;
    const from = (page - 1) * 7 + 1;
    pagerInfo.textContent = `Showing ${from}-${from + items.length - 1} of ${total} items`;
  }

  function renderPager() {
    const pages = Math.ceil(total / 7);
    const nums = [1, 2, 3, '…', pages];
    pager.innerHTML = `<button type="button" data-page="${Math.max(1, page - 1)}" aria-label="Previous page">${I.left}</button>` +
      nums.map((n) => (n === '…' ? '<span>…</span>' : `<button type="button" data-page="${n}" class="${n === page ? 'is-active' : ''}">${n}</button>`)).join('') +
      `<button type="button" data-page="${Math.min(pages, page + 1)}" aria-label="Next page">${I.right}</button>`;
  }

  async function load() {
    listEl.innerHTML = '<p class="empty-state">Loading…</p>';
    const res = await TraceVaultAPI.getAnalyses({ page });
    items = res.items; total = res.total;
    render(); renderPager();
  }

  search.addEventListener('input', render);
  filterBtn.addEventListener('click', (e) => { e.stopPropagation(); const open = menu.classList.toggle('is-open'); filterBtn.setAttribute('aria-expanded', open); });
  document.addEventListener('click', () => { menu.classList.remove('is-open'); filterBtn.setAttribute('aria-expanded', 'false'); });
  menu.addEventListener('click', (e) => {
    const b = e.target.closest('button'); if (!b) return;
    status = b.dataset.status;
    menu.querySelectorAll('button').forEach((x) => x.classList.toggle('is-active', x === b));
    filterBtn.querySelector('span').textContent = status === 'all' ? 'Filter' : b.textContent;
    render();
  });
  pager.addEventListener('click', (e) => { const b = e.target.closest('button'); if (!b) return; page = +b.dataset.page; load(); });
  listEl.addEventListener('click', (e) => { const b = e.target.closest('[data-reanalyze]'); if (b) UI.toast(`Re-analysis of ${b.dataset.reanalyze} queued.`); });
  document.getElementById('refreshBtn').addEventListener('click', load);
  document.getElementById('exportCsv').addEventListener('click', () => {
    const head = ['File name', 'SHA-256', 'Size', 'Format', 'Date analyzed', 'Integrity', 'Status'];
    const csv = [head, ...visible().map((i) => [i.name, i.hash, i.size, i.format, i.date, i.integrity, i.status])]
      .map((r) => r.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
    const a = document.createElement('a');
    a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    a.download = 'tracevault-history.csv'; a.click(); URL.revokeObjectURL(a.href);
  });
  load();
})();
