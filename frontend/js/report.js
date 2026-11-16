/* ============================================================
   TraceVault — Analysis report
   ============================================================ */
(async () => {
  // Show the file that was just uploaded, when there is one.
  const id = new URLSearchParams(location.search).get('id');
  if (id) {
    const a = await TraceVaultAPI.getAnalysis(id);
    if (a && a.id === id) {
      document.querySelectorAll('[data-r="name"]').forEach((el) => { el.textContent = a.name; });
      const hash = document.querySelector('[data-hash]'); if (hash) hash.textContent = a.hash;
    }
  }

  // Findings filter.
  const seg = document.querySelector('[data-seg]');
  seg && seg.addEventListener('click', (e) => {
    const b = e.target.closest('button'); if (!b) return;
    seg.querySelectorAll('button').forEach((x) => x.classList.toggle('is-active', x === b));
    document.querySelectorAll('.findings li').forEach((li) => { li.hidden = b.dataset.sev !== 'all' && li.dataset.sev !== b.dataset.sev; });
  });

  // Copy hash.
  const copy = document.querySelector('[data-copy-hash]');
  copy && copy.addEventListener('click', async () => {
    const text = document.querySelector('[data-hash]').textContent.trim();
    try { await navigator.clipboard.writeText(text); UI.toast('SHA-256 hash copied.'); }
    catch { UI.toast('Copy is not available in this browser.', 'error'); }
  });

  // Download and re-analyze.
  document.querySelector('[data-download]').addEventListener('click', () => {
    if (!TraceVaultAPI.MOCK_MODE) { window.location.href = TraceVaultAPI.reportUrl(id || 'latest'); return; }
    window.print();
  });
  document.querySelector('[data-reanalyze]').addEventListener('click', () => UI.toast('Re-analysis queued. You will get a notification when it finishes.'));
})();
