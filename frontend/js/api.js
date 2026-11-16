/* ============================================================
   TraceVault — API layer
   All backend calls go through here. While the Spring Boot API
   is not ready, MOCK_MODE returns realistic sample data so every
   page works on its own. Set MOCK_MODE to false once the
   endpoints in README "API Plan" are live.
   ============================================================ */
const TraceVaultAPI = (() => {
  const BASE_URL = 'http://localhost:8080/api';
  const MOCK_MODE = true;
  const MOCK_DELAY = 900;

  const delay = (ms) => new Promise((r) => setTimeout(r, ms));
  const store = {
    get(key, fallback) { try { const v = localStorage.getItem(key); return v ? JSON.parse(v) : fallback; } catch { return fallback; } },
    set(key, value) { try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage unavailable */ } },
    remove(key) { try { localStorage.removeItem(key); } catch { /* ignore */ } },
  };

  async function request(method, path, body) {
    const token = store.get('tv_token', null);
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers.Authorization = `Bearer ${token}`;
    const res = await fetch(BASE_URL + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.status === 204 ? null : res.json();
  }

  const DEFAULT_USER = { name: 'Ashfaq Khan', email: 'ashfaq.khan@email.com', id: 'TV-2026-0142', photo: null };

  function randomHex(n) { return Array.from({ length: n }, () => Math.floor(Math.random() * 16).toString(16)).join(''); }
  function formatSize(bytes) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${Math.round(bytes / 1024)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  const MOCK_HISTORY = [
    { id: 'tv_01', name: 'evidence_sample.pdf', hash: '9f3c4a…e71b', size: '2.4 MB', format: 'PDF', date: 'Today, 10:42 AM', integrity: 'verified', status: 'completed' },
    { id: 'tv_02', name: 'suspect_email_thread.eml', hash: 'calculating…', size: '184 KB', format: 'EML', date: 'Today, 09:15 AM', integrity: 'verifying', status: 'processing' },
    { id: 'tv_03', name: 'disk_image_summary.docx', hash: '2b8d4c…a0f9', size: '1.1 MB', format: 'DOCX', date: 'Today, 08:36 AM', integrity: 'verified', status: 'completed' },
    { id: 'tv_04', name: 'suspicious_attachment.zip', hash: 'c7e0f5…3dd4', size: '3.7 MB', format: 'ZIP', date: 'Yesterday, 04:16 PM', integrity: 'verified', status: 'completed' },
    { id: 'tv_05', name: 'corrupted_archive.zip', hash: 'e41b7a…92ac', size: '12.3 KB', format: 'ZIP', date: 'Yesterday, 03:05 PM', integrity: 'mismatch', status: 'failed' },
    { id: 'tv_06', name: 'corporate_wire_receipt.pdf', hash: '5a6f0d…7e2b', size: '860 KB', format: 'PDF', date: 'Oct 21, 11:30 AM', integrity: 'verified', status: 'completed' },
    { id: 'tv_07', name: 'forensic_memory_dump.dmp', hash: 'd09a3e…1c55', size: '240 MB', format: 'DMP', date: 'Oct 18, 02:47 PM', integrity: 'verified', status: 'completed' },
  ];

  return {
    MOCK_MODE, store, formatSize,

    getUser() { return store.get('tv_user', DEFAULT_USER); },
    setUser(user) { store.set('tv_user', user); },
    isLoggedIn() { return !!store.get('tv_token', null); },
    logout() { store.remove('tv_token'); },

    async login(email, password) {
      if (!MOCK_MODE) { const r = await request('POST', '/auth/login', { email, password }); store.set('tv_token', r.token); store.set('tv_user', r.user); return r; }
      await delay(MOCK_DELAY);
      const user = { ...this.getUser(), email };
      store.set('tv_token', 'mock_' + Date.now()); store.set('tv_user', user);
      return { user };
    },
    async register({ name, email, password }) {
      if (!MOCK_MODE) { const r = await request('POST', '/auth/register', { name, email, password }); store.set('tv_token', r.token); store.set('tv_user', r.user); return r; }
      await delay(MOCK_DELAY);
      const user = { ...DEFAULT_USER, name, email };
      store.set('tv_token', 'mock_' + Date.now()); store.set('tv_user', user);
      return { user };
    },
    async requestPasswordReset(email) {
      if (!MOCK_MODE) return request('POST', '/auth/forgot-password', { email });
      await delay(MOCK_DELAY); return { message: 'Reset link sent.' };
    },
    async resetPassword(token, password) {
      if (!MOCK_MODE) return request('POST', '/auth/reset-password', { token, password });
      await delay(MOCK_DELAY); return { message: 'Password updated.' };
    },
    async updateProfile(data) {
      if (!MOCK_MODE) return request('PUT', '/account', data);
      await delay(500); const user = { ...this.getUser(), ...data }; store.set('tv_user', user); return user;
    },
    async changePassword(current, next) {
      if (!MOCK_MODE) return request('PUT', '/account/password', { current, next });
      await delay(600); return { message: 'Password changed.' };
    },
    async deleteAccount() {
      if (!MOCK_MODE) return request('DELETE', '/account');
      await delay(700); store.remove('tv_token'); store.remove('tv_user'); return null;
    },

    async submitAnalysis(file) {
      if (!MOCK_MODE) {
        const form = new FormData(); form.append('file', file);
        const token = store.get('tv_token', null);
        const res = await fetch(BASE_URL + '/analyses', { method: 'POST', headers: token ? { Authorization: `Bearer ${token}` } : {}, body: form });
        if (!res.ok) throw new Error(`Upload failed (HTTP ${res.status})`);
        return res.json();
      }
      await delay(1800);
      const result = { id: 'tv_' + Date.now(), name: file.name, size: formatSize(file.size), hash: randomHex(64), uploadedAt: new Date().toISOString() };
      store.set('tv_last_analysis', result);
      return result;
    },
    async getAnalyses() {
      if (!MOCK_MODE) return request('GET', '/analyses');
      await delay(300); return { items: MOCK_HISTORY, total: 128 };
    },
    async getAnalysis(id) {
      if (!MOCK_MODE) return request('GET', `/analyses/${id}`);
      await delay(200); return store.get('tv_last_analysis', null);
    },
    reportUrl(id) { return `${BASE_URL}/analyses/${id}/report`; },
  };
})();
