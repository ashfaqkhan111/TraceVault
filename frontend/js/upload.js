/* ============================================================
   TraceVault — New verification (file upload)
   ============================================================ */
(() => {
  const MAX = 100 * 1024 * 1024;
  const ALLOWED = ['pdf', 'jpg', 'jpeg', 'png', 'eml', 'msg', 'zip', 'docx', 'exe'];
  const drop = document.getElementById('dropzone');
  const input = document.getElementById('fileInput');
  const list = document.getElementById('fileList');
  const start = document.getElementById('startBtn');
  let file = null;

  const FILE_ICON = '<svg class="ic" viewBox="0 0 24 24"><path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="m9 15 2 2 4-4"/></svg>';
  const TRASH = '<svg class="ic" viewBox="0 0 24 24"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>';

  function render() {
    start.disabled = !file;
    if (!file) { list.innerHTML = '<p class="uploaded__empty">No file selected yet.</p>'; return; }
    list.innerHTML = `<div class="file-row">${FILE_ICON}<div><strong></strong><small></small></div><button type="button" aria-label="Remove file">${TRASH}</button></div>`;
    list.querySelector('strong').textContent = file.name;
    list.querySelector('small').textContent = `${TraceVaultAPI.formatSize(file.size)} Uploaded Successfully`;
    list.querySelector('button').addEventListener('click', () => { file = null; input.value = ''; render(); });
  }

  function accept(f) {
    if (!f) return;
    const ext = f.name.split('.').pop().toLowerCase();
    if (!ALLOWED.includes(ext)) { UI.toast(`.${ext} files are not supported yet.`, 'error'); return; }
    if (f.size > MAX) { UI.toast('That file is larger than 100 MB.', 'error'); return; }
    file = f; render();
  }

  input.addEventListener('change', () => accept(input.files[0]));
  ['dragenter', 'dragover'].forEach((t) => drop.addEventListener(t, (e) => { e.preventDefault(); drop.classList.add('is-over'); }));
  ['dragleave', 'drop'].forEach((t) => drop.addEventListener(t, (e) => { e.preventDefault(); drop.classList.remove('is-over'); }));
  drop.addEventListener('drop', (e) => accept(e.dataTransfer.files[0]));

  start.addEventListener('click', async () => {
    if (!file) return;
    const modal = document.getElementById('startedModal');
    modal.dataset.locked = '1';
    UI.openModal('startedModal');
    try {
      const result = await TraceVaultAPI.submitAnalysis(file);
      window.location.href = `report.html?id=${encodeURIComponent(result.id)}`;
    } catch (err) {
      UI.closeModal(modal);
      UI.toast(err.message || 'Upload failed. Please try again.', 'error');
    }
  });
  render();
})();
