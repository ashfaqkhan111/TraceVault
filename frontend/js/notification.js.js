/* ============================================================
   TraceVault — Notifications
   ============================================================ */
(() => {
  const list = document.getElementById('notifList');
  const items = [...list.querySelectorAll('.notif')];
  const tabs = document.querySelector('.tabs');
  const countEl = document.querySelector('[data-unread-count]');
  const empty = document.getElementById('notifEmpty');
  let tab = 'all';

  function apply() {
    items.forEach((n) => {
      const unread = n.classList.contains('is-unread');
      const show = tab === 'all' || (tab === 'unread' && unread) || (tab === 'read' && !unread) || n.dataset.type === tab;
      n.hidden = !show;
    });
    // Hide a day heading when none of its items are showing.
    list.querySelectorAll('[data-group]').forEach((g) => {
      let el = g.nextElementSibling; let any = false;
      while (el && !el.hasAttribute('data-group') && el.classList.contains('notif')) { if (!el.hidden) any = true; el = el.nextElementSibling; }
      g.hidden = !any;
    });
    empty.hidden = items.some((n) => !n.hidden);
    const unread = items.filter((n) => n.classList.contains('is-unread')).length;
    countEl.textContent = unread; countEl.hidden = !unread;
    TraceVaultAPI.store.set('tv_unread', unread);
    document.querySelectorAll('[data-unread-dot]').forEach((d) => { d.hidden = !unread; });
  }

  tabs.addEventListener('click', (e) => {
    const b = e.target.closest('button'); if (!b) return;
    tab = b.dataset.tab;
    tabs.querySelectorAll('button').forEach((x) => { x.classList.toggle('is-active', x === b); x.setAttribute('aria-selected', x === b); });
    apply();
  });
  items.forEach((n) => n.addEventListener('click', () => { n.classList.remove('is-unread'); apply(); }));
  document.getElementById('markAll').addEventListener('click', () => { items.forEach((n) => n.classList.remove('is-unread')); apply(); UI.toast('All notifications marked as read.'); });
  apply();
})();
