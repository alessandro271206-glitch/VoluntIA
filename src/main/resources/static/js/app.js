const profile = document.getElementById('perfil-registro');
function updateRegistrationProfile() {
 if (!profile) return;
 document.querySelectorAll('[data-profile]').forEach(section => {
  const active = section.dataset.profile === profile.value;
  section.hidden = !active;
  section.querySelectorAll('input').forEach(input => {
   input.disabled = !active;
   input.required = active && ['edad', 'ruc', 'infoContacto'].includes(input.name);
  });
 });
}
profile?.addEventListener('change', updateRegistrationProfile);
updateRegistrationProfile();

document.querySelectorAll('form[data-confirm]').forEach(form => {
 form.addEventListener('submit', event => { if (!window.confirm(form.dataset.confirm)) event.preventDefault(); });
});
const search = document.getElementById('table-search');
search?.addEventListener('input', () => {
 const query = search.value.toLocaleLowerCase('es').trim(); let visible = 0;
 document.querySelectorAll('[data-search-row]').forEach(row => { row.hidden = !row.textContent.toLocaleLowerCase('es').includes(query); if (!row.hidden) visible++; });
 document.getElementById('no-results').hidden = visible > 0;
});
document.querySelectorAll('form').forEach(form => form.addEventListener('submit', event => {
 if (event.defaultPrevented || !form.checkValidity()) return;
 const button = event.submitter;
 if (button) { button.disabled = true; button.setAttribute('aria-busy', 'true'); }
}));
