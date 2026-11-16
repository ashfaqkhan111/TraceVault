/* ============================================================
   TraceVault — Shared UI helpers (toasts, modals, form fields)
   ============================================================ */
const UI = (() => {
  const ICON = {
    ok: '<svg class="ic" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/></svg>',
    error: '<svg class="ic" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>',
  };
  const EYE = '<svg class="ic" viewBox="0 0 24 24"><path d="M2.06 12.35a1 1 0 0 1 0-.7 10.75 10.75 0 0 1 19.88 0 1 1 0 0 1 0 .7 10.75 10.75 0 0 1-19.88 0"/><circle cx="12" cy="12" r="3"/></svg>';
  let EYE_OFF = null;

  function toast(message, type = 'ok') {
    let wrap = document.querySelector('.toasts');
    if (!wrap) { wrap = document.createElement('div'); wrap.className = 'toasts'; wrap.setAttribute('role', 'status'); document.body.appendChild(wrap); }
    const el = document.createElement('div');
    el.className = `toast${type === 'error' ? ' toast--error' : ''}`;
    el.innerHTML = (type === 'error' ? ICON.error : ICON.ok) + '<span></span>';
    el.querySelector('span').textContent = message;
    wrap.appendChild(el);
    setTimeout(() => el.remove(), 3200);
  }

  let lastFocus = null;
  function openModal(id) {
    const m = document.getElementById(id); if (!m) return;
    lastFocus = document.activeElement;
    m.classList.add('is-open');
    document.body.style.overflow = 'hidden';
    const first = m.querySelector('input:not([readonly]), button:not([data-close]), [href]');
    if (first) setTimeout(() => first.focus(), 30);
  }
  function closeModal(m) {
    if (typeof m === 'string') m = document.getElementById(m);
    if (!m) return;
    m.classList.remove('is-open');
    if (!document.querySelector('.modal.is-open')) document.body.style.overflow = '';
    if (lastFocus) lastFocus.focus();
  }

  function setError(field, message) {
    const f = typeof field === 'string' ? document.getElementById(field) : field;
    if (!f) return;
    f.classList.toggle('has-error', !!message);
    const e = f.querySelector('.field__error'); if (e) e.textContent = message || '';
  }
  function clearErrors(form) { form.querySelectorAll('.field').forEach((f) => setError(f, '')); }
  function setLoading(btn, loading) { btn.classList.toggle('is-loading', loading); btn.disabled = loading; }
  const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

  document.addEventListener('click', (e) => {
    const opener = e.target.closest('[data-open]');
    if (opener) { e.preventDefault(); openModal(opener.dataset.open); return; }
    const closer = e.target.closest('[data-close]');
    if (closer) { closeModal(closer.closest('.modal')); return; }
    if (e.target.classList.contains('modal') && !e.target.dataset.locked) closeModal(e.target);
    const toggle = e.target.closest('[data-toggle-password]');
    if (toggle) {
      const input = toggle.parentElement.querySelector('input');
      if (!EYE_OFF) EYE_OFF = toggle.innerHTML;
      const show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      toggle.innerHTML = show ? EYE : EYE_OFF;
      toggle.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
    }
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') { const m = document.querySelector('.modal.is-open:not([data-locked])'); if (m) closeModal(m); }
  });

  return { toast, openModal, closeModal, setError, clearErrors, setLoading, isEmail };
})();
