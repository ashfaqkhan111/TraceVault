/* ============================================================
   TraceVault — Account settings
   ============================================================ */
(() => {
  const user = TraceVaultAPI.getUser();
  const info = document.getElementById('infoForm');
  info.elements.name.value = user.name; info.elements.email.value = user.email;
  info.addEventListener('submit', async (e) => {
    e.preventDefault(); UI.clearErrors(info);
    const name = info.elements.name.value.trim(); const email = info.elements.email.value.trim();
    let ok = true;
    if (name.length < 2) { UI.setError('fInfoName', 'Enter your full name.'); ok = false; }
    if (!UI.isEmail(email)) { UI.setError('fInfoEmail', 'Enter a valid email address.'); ok = false; }
    if (!ok) return;
    const btn = info.querySelector('[type=submit]'); UI.setLoading(btn, true);
    await TraceVaultAPI.updateProfile({ name, email }); UI.setLoading(btn, false);
    document.querySelectorAll('[data-user-name]').forEach((el) => { el.textContent = name; });
    UI.toast('Personal information updated.');
  });

  const pw = document.getElementById('pwForm');
  pw.addEventListener('submit', async (e) => {
    e.preventDefault(); UI.clearErrors(pw);
    const cur = pw.querySelector('#curPw').value; const next = pw.querySelector('#newPw').value; const conf = pw.querySelector('#confPw').value;
    let ok = true;
    if (!cur) { UI.setError('fCur', 'Enter your current password.'); ok = false; }
    if (!(next.length >= 8 && /[A-Za-z]/.test(next) && /\d/.test(next))) { UI.setError('fNew', 'Use at least 8 characters with letters and numbers.'); ok = false; }
    else if (next === cur) { UI.setError('fNew', 'Choose a password different from your current one.'); ok = false; }
    if (next !== conf) { UI.setError('fConf', 'Passwords do not match.'); ok = false; }
    if (!ok) return;
    const btn = pw.querySelector('[type=submit]'); UI.setLoading(btn, true);
    await TraceVaultAPI.changePassword(cur, next); UI.setLoading(btn, false);
    pw.reset(); UI.toast('Password changed.');
  });

  const prefs = TraceVaultAPI.store.get('tv_prefs', { email: true, verification: true });
  document.querySelectorAll('[data-pref]').forEach((t) => {
    t.checked = prefs[t.dataset.pref] !== false;
    t.addEventListener('change', () => { prefs[t.dataset.pref] = t.checked; TraceVaultAPI.store.set('tv_prefs', prefs); UI.toast('Notification preference saved.'); });
  });

  // Delete account: all three boxes ticked and DELETE typed.
  const del = document.getElementById('deleteForm');
  const checks = [...del.querySelectorAll('[data-del-check]')];
  const typed = document.getElementById('delType');
  const btn = document.getElementById('delBtn');
  const sync = () => { btn.disabled = !(checks.every((c) => c.checked) && typed.value.trim() === 'DELETE'); };
  checks.forEach((c) => c.addEventListener('change', sync)); typed.addEventListener('input', sync);
  document.querySelectorAll('[data-open="deleteModal"]').forEach((b) => b.addEventListener('click', () => { checks.forEach((c) => { c.checked = false; }); typed.value = ''; sync(); }));
  del.addEventListener('submit', async (e) => {
    e.preventDefault(); if (btn.disabled) return;
    UI.setLoading(btn, true);
    await TraceVaultAPI.deleteAccount();
    window.location.href = '../../index.html';
  });
})();
