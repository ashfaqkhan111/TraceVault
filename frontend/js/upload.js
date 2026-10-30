/* ============================================================
   TraceVault — Upload Module
   Drag-and-drop, file selection, validation, analysis trigger,
   progress overlay with step-by-step animation.
   ============================================================ */

const TraceVaultUpload = (() => {

  /* ---------- Constants ---------- */
  const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB

  const ALLOWED_EXTENSIONS = [
    'pdf','doc','docx','xls','xlsx','ppt','pptx',
    'eml','msg',
    'jpg','jpeg','png','gif',
    'exe','dll',
    'zip','tar','gz',
    'txt','csv','json','xml',
  ];

  const FILE_ICONS = {
    pdf:'📄', doc:'📝', docx:'📝', xls:'📊', xlsx:'📊',
    ppt:'📋', pptx:'📋', eml:'📧', msg:'📧',
    jpg:'🖼️', jpeg:'🖼️', png:'🖼️', gif:'🖼️',
    exe:'⚙️', dll:'⚙️', zip:'🗜️', tar:'🗜️', gz:'🗜️',
    txt:'📄', csv:'📊', json:'📋', xml:'📋',
  };

  /* ---------- State ---------- */
  let selectedFile = null;

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

  /* ---------- Format bytes ---------- */
  function formatSize(bytes) {
    if (bytes < 1024)    return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  /* ---------- Get selected analysis type ---------- */
  function getAnalysisType() {
    const checked = document.querySelector('input[name="analysisType"]:checked');
    return checked ? checked.value : 'file';
  }

  /* ---------- Validate file ---------- */
  function validateFile(file) {
    const ext = file.name.split('.').pop().toLowerCase();
    if (!ALLOWED_EXTENSIONS.includes(ext)) {
      return `File type .${ext} is not supported. Supported: ${ALLOWED_EXTENSIONS.join(', ')}.`;
    }
    if (file.size > MAX_FILE_SIZE) {
      return `File is too large (${formatSize(file.size)}). Maximum is 50 MB.`;
    }
    return null; // valid
  }

  /* ---------- Show file preview ---------- */
  function showFilePreview(file) {
    const previewEl  = document.getElementById('filePrev');
    const iconEl     = document.getElementById('filePrevIco');
    const nameEl     = document.getElementById('filePrevName');
    const metaEl     = document.getElementById('filePrevMeta');
    const submitBtn  = document.getElementById('startBtn');
    const dropzone   = document.getElementById('dropzone');

    if (!previewEl) return;

    const ext = file.name.split('.').pop().toLowerCase();

    nameEl.textContent  = file.name;
    metaEl.textContent  = `${formatSize(file.size)} · .${ext.toUpperCase()} · Modified: ${new Date(file.lastModified).toLocaleDateString()}`;

    previewEl.style.display = 'flex';
    dropzone.style.display  = 'none';
    if (submitBtn) submitBtn.disabled = false;
  }

  /* ---------- Clear file selection ---------- */
  function clearFile() {
    selectedFile = null;
    const previewEl  = document.getElementById('filePrev');
    const dropzone   = document.getElementById('dropzone');
    const submitBtn  = document.getElementById('startBtn');
    const fileInput  = document.getElementById('fileInput');

    if (previewEl) previewEl.style.display = 'none';
    if (dropzone)  dropzone.style.display  = 'block';
    if (submitBtn) submitBtn.disabled = true;
    if (fileInput) fileInput.value = '';
  }

  /* ---------- Handle file selection ---------- */
  function handleFileSelect(file) {
    const err = validateFile(file);
    if (err) {
      toast(err, 'error');
      return;
    }
    selectedFile = file;
    showFilePreview(file);
  }

  /* ---------- Progress overlay ---------- */
  function showProgressOverlay(filename) {
    const overlay = document.getElementById('analysisOverlay');
    const fileEl  = document.getElementById('analysisProgressFile');
    if (overlay) overlay.classList.add('open');
    if (fileEl)  fileEl.textContent = filename;

    // Reset steps
    for (let i = 1; i <= 4; i++) {
      const s = document.getElementById('step' + i);
      if (s) {
        s.classList.remove('done', 'active');
        s.querySelector('.progress-step__icon').textContent = '⏳';
      }
    }

    // Animate steps sequentially
    const timings = [400, 900, 1600, 2400]; // ms when each step becomes active
    const doneTimes = [800, 1400, 2200, 3000];

    timings.forEach((t, i) => {
      setTimeout(() => {
        const s = document.getElementById('step' + (i + 1));
        if (!s) return;
        s.classList.add('active');
        s.querySelector('.progress-step__icon').textContent = '🔄';
      }, t);
    });

    doneTimes.forEach((t, i) => {
      setTimeout(() => {
        const s = document.getElementById('step' + (i + 1));
        if (!s) return;
        s.classList.remove('active');
        s.classList.add('done');
        s.querySelector('.progress-step__icon').textContent = '✅';
      }, t);
    });
  }

  function hideProgressOverlay() {
    const overlay = document.getElementById('analysisOverlay');
    if (overlay) overlay.classList.remove('open');
  }

  /* ---------- Run analysis ---------- */
  async function runAnalysis() {
    if (!selectedFile) {
      toast('Please select a file first.', 'warning');
      return;
    }

    const type  = getAnalysisType();
    const notes = document.getElementById('caseNotes')?.value?.trim() || '';
    const btn   = document.getElementById('startAnalysisSubmitBtn');

    // Disable button
    if (btn) { btn.disabled = true; btn.textContent = '⚙ Analyzing…'; }

    showProgressOverlay(selectedFile.name);

    try {
      const result = await TraceVaultAPI.submitAnalysis(selectedFile, type, notes);
      hideProgressOverlay();
      toast(`Analysis complete — Risk: ${result.riskScore}/100`, 'success');

      // Short delay then navigate to results
      setTimeout(() => {
        window.location.href = `results.html?id=${result.id}`;
      }, 800);

    } catch (err) {
      hideProgressOverlay();
      toast('Analysis failed: ' + (err.message || 'Unknown error'), 'error');
      if (btn) { btn.disabled = false; btn.textContent = '⚙ Start Analysis'; }
    }
  }

  /* ---------- Init ---------- */
  function init() {
    const dropzone  = document.getElementById('dropzone');
    const fileInput = document.getElementById('fileInput');
    const clearBtn  = document.getElementById('clearFileBtn');
    const submitBtn = document.getElementById('startAnalysisSubmitBtn');

    if (!dropzone) return; // not on dashboard page

    /* Click to browse */
    dropzone.addEventListener('click', () => fileInput?.click());
    fileInput?.addEventListener('change', e => {
      const file = e.target.files?.[0];
      if (file) handleFileSelect(file);
    });

    /* Drag & drop */
    dropzone.addEventListener('dragover', e => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    });
    dropzone.addEventListener('dragleave', e => {
      if (!dropzone.contains(e.relatedTarget)) {
        dropzone.classList.remove('dragover');
      }
    });
    dropzone.addEventListener('drop', e => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
      const file = e.dataTransfer?.files?.[0];
      if (file) handleFileSelect(file);
    });

    /* Also allow drag & drop on the whole page */
    document.addEventListener('dragover', e => e.preventDefault());
    document.addEventListener('drop', e => {
      e.preventDefault();
      const file = e.dataTransfer?.files?.[0];
      if (file && !document.getElementById('filePreview')?.style.display !== 'none') {
        handleFileSelect(file);
      }
    });

    /* Clear button */
    clearBtn?.addEventListener('click', clearFile);

    /* Submit button */
    submitBtn?.addEventListener('click', runAnalysis);
  }

  return { init, clearFile };

})();
