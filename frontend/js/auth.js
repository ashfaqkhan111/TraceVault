/* ============================================================
   TraceVault — Sign in, sign up, forgot and reset password
   ============================================================ */
(() => {
  const { setError, clearErrors, setLoading, isEmail, toast } = UI;
  const val = (form, name) => (form.elements[name] ? form.elements[name].value.trim() : '');

  function showSuccess() {
    const overlay = document.getElementById('successOverlay');
    overlay.classList.add('is-open');
    setTimeout(() => { window.location.href = 'dashboard.html'; }, 1400);
  }

  function strengthOf(pw) {
    let s = 0;
    if (pw.length >= 8) s++;
    if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) s++;
    if (/\d/.test(pw)) s++;
    if (/[^A-Za-z0-9]/.test(pw)) s++;
    return pw ? Math.max(s, 1) : 0;
  }
  const strongEnough = (pw) => pw.length >= 8 && /[A-Za-z]/.test(pw) && /\d/.test(pw);

  // Sign in
  const login = document.getElementById('loginForm');
  if (login) login.addEventListener('submit', async (e) => {
    e.preventDefault(); clearErrors(login);
    const email = val(login, 'email'); const password = login.elements.password.value;
    let ok = true;
    if (!isEmail(email)) { setError('fEmail', 'Enter a valid email address.'); ok = false; }
    if (!password) { setError('fPassword', 'Enter your password.'); ok = false; }
    if (!ok) return;
    const btn = login.querySelector('[type=submit]'); setLoading(btn, true);
    try { await TraceVaultAPI.login(email, password); showSuccess(); }
    catch (err) { setError('fPassword', err.message || 'Sign in failed.'); setLoading(btn, false); }
  });

  // Sign up
  const signup = document.getElementById('signupForm');
  if (signup) {
    const meter = signup.querySelector('[data-strength]');
    signup.elements.password.addEventListener('input', (e) => { meter.dataset.level = strengthOf(e.target.value); });
    signup.addEventListener('submit', async (e) => {
      e.preventDefault(); clearErrors(signup);
      const name = val(signup, 'name'); const email = val(signup, 'email');
      const pw = signup.elements.password.value; const confirm = signup.elements.confirm.value;
      let ok = true;
      if (name.length < 2) { setError('fName', 'Enter your full name.'); ok = false; }
      if (!isEmail(email)) { setError('fEmail', 'Enter a valid email address.'); ok = false; }
      if (!strongEnough(pw)) { setError('fPassword', 'Use at least 8 characters with letters and numbers.'); ok = false; }
      if (pw !== confirm) { setError('fConfirm', 'Passwords do not match.'); ok = false; }
      if (!signup.elements.terms.checked) { setError('fTerms', 'Please accept the Terms of Service and Privacy Policy.'); ok = false; }
      if (!ok) return;
      const btn = signup.querySelector('[type=submit]'); setLoading(btn, true);
      try { await TraceVaultAPI.register({ name, email, password: pw }); showSuccess(); }
      catch (err) { setError('fEmail', err.message || 'Could not create the account.'); setLoading(btn, false); }
    });
  }

  // Forgot password
  const forgot = document.getElementById('forgotForm');
  if (forgot) forgot.addEventListener('submit', async (e) => {
    e.preventDefault(); clearErrors(forgot);
    const email = val(forgot, 'email');
    if (!isEmail(email)) { setError('fEmail', 'Enter the email address you registered with.'); return; }
    const btn = forgot.querySelector('[type=submit]'); setLoading(btn, true);
    try { await TraceVaultAPI.requestPasswordReset(email); toast(`If ${email} is registered, a reset link is on its way.`); }
    catch (err) { toast(err.message || 'Could not send the reset link.', 'error'); }
    setLoading(btn, false);
  });

  // Reset password
  const reset = document.getElementById('resetForm');
  if (reset) reset.addEventListener('submit', async (e) => {
    e.preventDefault(); clearErrors(reset);
    const pw = reset.elements.password.value; const confirm = reset.elements.confirm.value;
    let ok = true;
    if (!strongEnough(pw)) { setError('fPassword', 'Use at least 8 characters with letters and numbers.'); ok = false; }
    if (pw !== confirm) { setError('fConfirm', 'Passwords do not match.'); ok = false; }
    if (!ok) return;
    const btn = reset.querySelector('[type=submit]'); setLoading(btn, true);
    const token = new URLSearchParams(location.search).get('token') || '';
    try { await TraceVaultAPI.resetPassword(token, pw); toast('Password updated. You can sign in now.'); setTimeout(() => { location.href = 'login.html'; }, 1200); }
    catch (err) { toast(err.message || 'Could not reset the password.', 'error'); setLoading(btn, false); }
  });

  document.querySelectorAll('[data-google]').forEach((b) => b.addEventListener('click', () => toast('Google sign-in will be available once the backend is connected.')));
})();
