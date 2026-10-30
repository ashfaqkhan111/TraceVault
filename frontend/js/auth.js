/* ============================================================
   TraceVault — Auth Module
   Handles login, signup, logout, session & user display.
   ============================================================ */

const TraceVaultAuth = (() => {

  /* ---------- Toast helper (self-contained so auth works standalone) ---------- */
  function toast(msg, type = 'info') {
    const icons = { success:'✅', error:'⚠', warning:'⚠', info:'ℹ' };
    const c = document.getElementById('toastContainer');
    if (!c) { console.warn('[TraceVault]', msg); return; }
    const t = document.createElement('div');
    t.className = `toast toast--${type}`;
    t.innerHTML = `<span class="toast__icon">${icons[type]||'ℹ'}</span><span class="toast__msg">${msg}</span>`;
    c.appendChild(t);
    setTimeout(() => t.remove(), 3500);
  }

  /* ---------- Session helpers ---------- */
  function setSession(token, user) {
    localStorage.setItem('tv_token', token);
    localStorage.setItem('tv_user',  JSON.stringify(user));
  }

  function clearSession() {
    localStorage.removeItem('tv_token');
    localStorage.removeItem('tv_user');
  }

  function isLoggedIn() {
    return !!localStorage.getItem('tv_token');
  }

  function getUser() {
    try {
      return JSON.parse(localStorage.getItem('tv_user') || 'null');
    } catch {
      return null;
    }
  }

  function getToken() {
    return localStorage.getItem('tv_token');
  }

  /* Redirect to login if not authenticated */
  function requireAuth() {
    if (!isLoggedIn()) {
      window.location.href = 'login.html';
    } else {
      updateUserDisplay();
    }
  }

  /* Update all sidebar/topbar user display elements */
  function updateUserDisplay() {
    const user = getUser();
    if (!user) return;
    const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || 'Investigator';
    const initials = ((user.firstName || '?')[0] + (user.lastName || '?')[0]).toUpperCase();

    const nameEls = document.querySelectorAll('#userName, #welcomeName');
    nameEls.forEach(el => { el.textContent = fullName; });

    const roleEls = document.querySelectorAll('#userRole');
    roleEls.forEach(el => { el.textContent = labelRole(user.role); });

    const avatarEls = document.querySelectorAll('#userAvatar, .sidebar__avatar');
    avatarEls.forEach(el => { el.textContent = initials; });
  }

  function labelRole(role) {
    const map = {
      forensic_investigator: 'Forensic Investigator',
      security_analyst:      'Security Analyst',
      incident_responder:    'Incident Responder',
      law_enforcement:       'Law Enforcement',
      researcher:            'Security Researcher',
      student:               'Student',
      other:                 'Investigator',
    };
    return map[role] || 'Investigator';
  }

  function logout() {
    clearSession();
    window.location.href = '../../index.html';
  }

  function socialLogin(provider) {
    toast(`${provider.charAt(0).toUpperCase() + provider.slice(1)} login is not available in prototype mode.`, 'info');
  }

  /* ============================================================
     LOGIN PAGE
     ============================================================ */
  function initLoginPage() {
    if (isLoggedIn()) {
      window.location.href = 'dashboard.html';
      return;
    }

    const form      = document.getElementById('loginForm');
    const btn       = document.getElementById('loginBtn');
    const errorBox  = document.getElementById('loginErr');
    const errorMsg  = document.getElementById('loginErrMsg');
    const toggleBtn = document.getElementById('togglePwd');
    const pwdInput  = document.getElementById('password');

    /* Password visibility toggle */
    toggleBtn?.addEventListener('click', () => {
      const showing = pwdInput.type === 'text';
      pwdInput.type = showing ? 'password' : 'text';
    });

    form?.addEventListener('submit', async e => {
      e.preventDefault();
      if (errorBox) errorBox.style.display = 'none';
      clearFieldErrors(['grpEmail', 'grpPwd']);

      const email    = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;
      let valid = true;

      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        setFieldError('grpEmail', 'Please enter a valid email address.'); valid = false;
      }
      if (!password) {
        setFieldError('grpPwd', 'Please enter your password.'); valid = false;
      }
      if (!valid) return;

      setLoading(btn, true);
      try {
        const res = await TraceVaultAPI.login(email, password);
        setSession(res.token, res.user);
        toast('Logged in successfully.', 'success');
        await sleep(600);
        window.location.href = 'dashboard.html';
      } catch (err) {
        if (errorMsg) errorMsg.textContent = err.message || 'Invalid email or password.';
        if (errorBox) errorBox.style.display = 'flex';
      } finally {
        setLoading(btn, false);
      }
    });
  }

  /* ============================================================
     SIGNUP PAGE
     ============================================================ */
  function initSignupPage() {
    if (isLoggedIn()) {
      window.location.href = 'dashboard.html';
      return;
    }

    const form     = document.getElementById('signupForm');
    const btn      = document.getElementById('signupBtn');
    const errorBox = document.getElementById('signupErr');
    const errorMsg = document.getElementById('signupErrMsg');

    /* Password toggle */
    document.getElementById('togglePwd')?.addEventListener('click', () => {
      const el = document.getElementById('password');
      el.type = el.type === 'text' ? 'password' : 'text';
    });

    /* Password strength meter */
    document.getElementById('password')?.addEventListener('input', e => {
      updatePasswordStrength(e.target.value);
    });

    form?.addEventListener('submit', async e => {
      e.preventDefault();
      if (errorBox) errorBox.style.display = 'none';
      clearFieldErrors(['grpName','grpEmail','grpPwd','grpConfirm','grpTerms']);

      const fullName = document.getElementById('fullName').value.trim();
      const email    = document.getElementById('email').value.trim();
      const role     = document.getElementById('role').value;
      const password = document.getElementById('password').value;
      const confirm  = document.getElementById('confirmPassword').value;
      const terms    = document.getElementById('terms').checked;
      let valid = true;

      if (!fullName) {
        setFieldError('grpName', 'Please enter your full name.'); valid = false;
      }
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        setFieldError('grpEmail', 'Please enter a valid email address.'); valid = false;
      }
      if (password.length < 8) {
        setFieldError('grpPwd', 'Password must be at least 8 characters.'); valid = false;
      }
      if (password !== confirm) {
        setFieldError('grpConfirm', 'Passwords do not match.'); valid = false;
      }
      if (!terms) {
        setFieldError('grpTerms', 'You must accept the terms to continue.'); valid = false;
      }
      if (!valid) return;

      setLoading(btn, true);
      try {
        const res = await TraceVaultAPI.register({ fullName, email, password, role });
        setSession(res.token, res.user);
        toast('Account created! Welcome to TraceVault.', 'success');
        await sleep(700);
        window.location.href = 'dashboard.html';
      } catch (err) {
        if (errorMsg) errorMsg.textContent = err.message || 'Registration failed. Please try again.';
        if (errorBox) errorBox.style.display = 'flex';
      } finally {
        setLoading(btn, false);
      }
    });
  }

  /* ============================================================
     FORGOT PASSWORD PAGE
     ============================================================ */
  let _resetEmail = '';

  function initForgotPage() {
    const form     = document.getElementById('resetForm');
    const btn      = document.getElementById('resetBtn');
    const errorBox = document.getElementById('resetErr');
    const errorMsg = document.getElementById('resetErrMsg');

    form?.addEventListener('submit', async e => {
      e.preventDefault();
      if (errorBox) errorBox.style.display = 'none';
      clearFieldErrors(['grpEmail']);

      const email = document.getElementById('email').value.trim();
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        setFieldError('grpEmail', 'Please enter a valid email address.'); return;
      }

      setLoading(btn, true);
      try {
        await TraceVaultAPI.requestPasswordReset(email);
        _resetEmail = email;
        document.getElementById('sentEmail').textContent = email;
        document.getElementById('stateRequest').style.display  = 'none';
        document.getElementById('stateSuccess').style.display  = 'block';
        document.getElementById('footSignup').style.display    = 'none';
      } catch (err) {
        if (errorMsg) errorMsg.textContent = err.message || 'No account found with that email.';
        if (errorBox) errorBox.style.display = 'flex';
      } finally {
        setLoading(btn, false);
      }
    });
  }

  function resendReset() {
    if (!_resetEmail) return;
    toast(`Reset link resent to ${_resetEmail}.`, 'success');
  }

  /* ============================================================
     PASSWORD STRENGTH
     ============================================================ */
  function updatePasswordStrength(password) {
    const container = document.getElementById('pwStrength');
    if (!container) return;
    if (!password) { container.style.display = 'none'; return; }
    container.style.display = 'block';

    let score = 0;
    if (password.length >= 8)  score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
    if (/\d/.test(password))   score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    const level = score <= 1 ? 'weak' : score <= 3 ? 'fair' : 'strong';
    const labels = { weak: 'Weak', fair: 'Fair', strong: 'Strong' };
    const barFill = score <= 1 ? 1 : score <= 3 ? 2 : 4;

    for (let i = 1; i <= 4; i++) {
      const bar = document.getElementById('b' + i);
      if (!bar) continue;
      bar.className = 'pw-bar';
      if (i <= barFill) bar.classList.add(level);
    }

    const labelEl = document.getElementById('pwLbl');
    if (labelEl) {
      labelEl.textContent = labels[level];
      labelEl.className = 'pw-lbl ' + level;
    }
  }

  /* ============================================================
     FIELD ERROR HELPERS
     ============================================================ */
  function setFieldError(groupId, message) {
    const group = document.getElementById(groupId);
    if (!group) return;
    group.classList.add('has-error');
    const errEl = group.querySelector('.form-error');
    if (errEl && message) errEl.textContent = message;
  }

  function clearFieldErrors(groupIds) {
    groupIds.forEach(id => {
      const g = document.getElementById(id);
      if (g) g.classList.remove('has-error');
    });
  }

  function setLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
      btn.classList.add('loading');
      btn.disabled = true;
      const spinner = document.createElement('span');
      spinner.className = 'spinner spinner--sm';
      spinner.id = 'btnSpinner';
      btn.prepend(spinner);
    } else {
      btn.classList.remove('loading');
      btn.disabled = false;
      document.getElementById('btnSpinner')?.remove();
    }
  }

  function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

  /* ============================================================
     PUBLIC
     ============================================================ */
  return {
    initLoginPage,
    initSignupPage,
    initForgotPage,
    resendReset,
    requireAuth,
    isLoggedIn,
    getUser,
    getToken,
    logout,
    socialLogin,
    updateUserDisplay,
    setSession,
    clearSession,
  };

})();
