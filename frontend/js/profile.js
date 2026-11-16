/* ============================================================
   TraceVault — Profile (photo, edit details, log out)
   ============================================================ */
(() => {
  const user = () => TraceVaultAPI.getUser();
  const initials = () => window.TV_USER_INITIALS || 'AK';
  let pendingPhoto = null;

  function paintAvatar(el, photo) {
    el.textContent = '';
    if (photo) { const img = document.createElement('img'); img.src = photo; img.alt = ''; el.appendChild(img); }
    else el.textContent = initials();
  }
  const paintAll = () => document.querySelectorAll('[data-user-initials]').forEach((el) => paintAvatar(el, user().photo));

  // Upload photo
  const input = document.getElementById('photoInput');
  const drop = document.getElementById('photoDrop');
  const preview = document.getElementById('photoPreview');
  function readPhoto(file) {
    if (!file) return;
    if (!/^image\/(png|jpeg)$/.test(file.type)) { UI.toast('Please choose a JPG or PNG image.', 'error'); return; }
    if (file.size > 2 * 1024 * 1024) { UI.toast('That photo is larger than 2 MB.', 'error'); return; }
    const reader = new FileReader();
    reader.onload = () => { pendingPhoto = reader.result; paintAvatar(preview, pendingPhoto); };
    reader.readAsDataURL(file);
  }
  input.addEventListener('change', () => readPhoto(input.files[0]));
  ['dragenter', 'dragover'].forEach((t) => drop.addEventListener(t, (e) => { e.preventDefault(); drop.classList.add('is-over'); }));
  ['dragleave', 'drop'].forEach((t) => drop.addEventListener(t, (e) => { e.preventDefault(); drop.classList.remove('is-over'); }));
  drop.addEventListener('drop', (e) => readPhoto(e.dataTransfer.files[0]));
  document.querySelectorAll('[data-open="photoModal"]').forEach((b) => b.addEventListener('click', () => { pendingPhoto = null; input.value = ''; paintAvatar(preview, user().photo); }));
  document.getElementById('savePhoto').addEventListener('click', async () => {
    if (!pendingPhoto) { UI.toast('Choose a photo first.', 'error'); return; }
    await TraceVaultAPI.updateProfile({ photo: pendingPhoto });
    paintAll(); UI.closeModal('photoModal'); UI.toast('Profile photo updated.');
  });

  // Delete photo
  document.getElementById('confirmDeletePhoto').addEventListener('click', async () => {
    await TraceVaultAPI.updateProfile({ photo: null });
    paintAll(); UI.closeModal('deletePhotoModal'); UI.toast('Profile photo removed.');
  });

  // Edit profile
  const form = document.getElementById('editForm');
  document.querySelectorAll('[data-open="editModal"]').forEach((b) => b.addEventListener('click', () => {
    UI.clearErrors(form); form.elements.name.value = user().name; form.elements.email.value = user().email;
  }));
  form.addEventListener('submit', async (e) => {
    e.preventDefault(); UI.clearErrors(form);
    const name = form.elements.name.value.trim(); const email = form.elements.email.value.trim();
    let ok = true;
    if (name.length < 2) { UI.setError('fEditName', 'Enter your full name.'); ok = false; }
    if (!UI.isEmail(email)) { UI.setError('fEditEmail', 'Enter a valid email address.'); ok = false; }
    if (!ok) return;
    const btn = form.querySelector('[type=submit]'); UI.setLoading(btn, true);
    await TraceVaultAPI.updateProfile({ name, email });
    UI.setLoading(btn, false);
    document.querySelectorAll('[data-user-name]').forEach((el) => { el.textContent = name; });
    document.querySelectorAll('[data-user-email]').forEach((el) => { el.textContent = email; });
    window.TV_USER_INITIALS = name.split(/\s+/).filter(Boolean).slice(0, 2).map((w) => w[0].toUpperCase()).join('');
    paintAll(); UI.closeModal('editModal'); UI.toast('Profile updated.');
  });
})();
