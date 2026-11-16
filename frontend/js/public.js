/* ============================================================
   TraceVault — Public pages (menu, scrolled bar, contact form, articles)
   ============================================================ */
(() => {
  const nav = document.getElementById('pubNav');
  const bar = document.getElementById('pubBar');
  const toggle = document.querySelector('[data-nav-toggle]');
  toggle && toggle.addEventListener('click', () => {
    const open = nav.classList.toggle('is-open');
    toggle.setAttribute('aria-expanded', open);
  });

  // Dark compact bar appears once the main header scrolls away.
  if (bar && 'IntersectionObserver' in window) {
    new IntersectionObserver(([entry]) => {
      const show = !entry.isIntersecting;
      bar.classList.toggle('is-visible', show);
      bar.setAttribute('aria-hidden', !show);
      bar.querySelectorAll('a').forEach((a) => { a.tabIndex = show ? 0 : -1; });
    }).observe(nav);
  }

  // Contact form.
  const form = document.getElementById('contactForm');
  if (form) form.addEventListener('submit', (e) => {
    e.preventDefault();
    const set = (id, msg) => { const f = document.getElementById(id); f.classList.toggle('has-error', !!msg); const er = f.querySelector('.field__error'); if (er) er.textContent = msg || ''; };
    const name = form.elements.name.value.trim(); const email = form.elements.email.value.trim(); const msg = form.elements.message.value.trim();
    let ok = true;
    set('fName', name ? '' : 'Enter your name.'); if (!name) ok = false;
    const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email); set('fEmail', emailOk ? '' : 'Enter a valid email address.'); if (!emailOk) ok = false;
    set('fMsg', msg ? '' : 'Tell us how we can help.'); if (!msg) ok = false;
    if (!form.elements.agree.checked) { ok = false; document.getElementById('fAgree').style.color = 'var(--tv-red-600)'; }
    else document.getElementById('fAgree').style.color = '';
    if (!ok) return;
    const btn = form.querySelector('[type=submit]'); btn.disabled = true; btn.textContent = 'Message sent';
    form.reset();
    setTimeout(() => { btn.disabled = false; btn.textContent = 'Send Message'; }, 3000);
  });

  // Learn page: category chips, search and load more.
  const grid = document.querySelector('[data-articles]');
  if (grid) {
    const cards = [...grid.querySelectorAll('.b-article')];
    const chips = [...document.querySelectorAll('.b-chip')];
    const empty = document.querySelector('[data-articles-empty]');
    let cat = 'all'; let q = '';
    const apply = () => {
      cards.forEach((c) => { c.hidden = !((cat === 'all' || c.dataset.cat === cat) && (!q || c.textContent.toLowerCase().includes(q))); });
      empty.hidden = cards.some((c) => !c.hidden);
    };
    const pick = (value) => { cat = value; chips.forEach((c) => { const on = c.dataset.filter === value; c.classList.toggle('is-active', on); c.setAttribute('aria-selected', on); }); apply(); };
    chips.forEach((c) => c.addEventListener('click', () => pick(c.dataset.filter)));
    document.querySelectorAll('[data-filter-link]').forEach((a) => a.addEventListener('click', () => pick(a.dataset.filterLink)));
    const search = document.querySelector('[data-article-search]');
    search.addEventListener('submit', (e) => { e.preventDefault(); q = search.elements.q.value.trim().toLowerCase(); pick('all'); document.getElementById('latest').scrollIntoView({ behavior: 'smooth' }); });
    const more = document.querySelector('[data-load-more]');
    more.addEventListener('click', () => { more.textContent = 'You’re all caught up'; more.disabled = true; });
  }
})();
