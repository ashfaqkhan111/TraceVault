/* ============================================================
   TraceVault — Shared behaviour for signed-in pages
   ============================================================ */
(() => {
  // When the real backend is live, send signed-out visitors to the login page.
  if (!TraceVaultAPI.MOCK_MODE && !TraceVaultAPI.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  // Show the signed-in user everywhere it appears.
  const user = TraceVaultAPI.getUser();
  const initials = (user.name || '?').split(/\s+/).filter(Boolean).slice(0, 2).map((w) => w[0].toUpperCase()).join('');
  document.querySelectorAll('[data-user-name]').forEach((el) => { el.textContent = user.name; });
  document.querySelectorAll('[data-user-email]').forEach((el) => { el.textContent = user.email; });
  document.querySelectorAll('[data-user-initials]').forEach((el) => {
    el.textContent = initials;
    if (user.photo) { const img = document.createElement('img'); img.src = user.photo; img.alt = ''; el.textContent = ''; el.appendChild(img); }
  });

  // Unread dot on the bell.
  const unread = TraceVaultAPI.store.get('tv_unread', 3);
  document.querySelectorAll('[data-unread-dot]').forEach((d) => { d.hidden = !unread; });

  // Mobile sidebar.
  const sidebar = document.getElementById('sidebar');
  const scrim = document.querySelector('.sidebar__scrim');
  const setOpen = (open) => { sidebar.classList.toggle('is-open', open); scrim.classList.toggle('is-open', open); };
  document.querySelectorAll('[data-sidebar-open]').forEach((b) => b.addEventListener('click', () => setOpen(true)));
  scrim && scrim.addEventListener('click', () => setOpen(false));

  // Log out buttons.
  document.querySelectorAll('[data-logout]').forEach((b) => b.addEventListener('click', () => {
    TraceVaultAPI.logout();
    window.location.href = 'login.html';
  }));

  window.TV_USER_INITIALS = initials;
})();
