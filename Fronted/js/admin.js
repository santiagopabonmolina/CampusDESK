import { API } from './api.js';
import { checkAdminAccess } from './auth.js';
import { showToast, initAppShell, escapeHTML, setButtonLoading } from './ui.js';

// Protected route
checkAdminAccess();
initAppShell();

let usersList = [];
let selectedUser = null;

// Modal elements
const modalRoles = document.getElementById('modal-roles');
const btnCloseRoles = document.getElementById('btn-close-roles');
const btnCancelRoles = document.getElementById('btn-cancel-roles');
const btnSaveRoles = document.getElementById('btn-save-roles');

const modalUserName = document.getElementById('modal-user-name');
const modalUserEmail = document.getElementById('modal-user-email');
const chkUser = document.getElementById('role-chk-user');
const chkTech = document.getElementById('role-chk-tech');
const chkAdmin = document.getElementById('role-chk-admin');
const rolesErrorMsg = document.getElementById('roles-error-msg');

/**
 * Closes role management modal.
 */
function closeModal() {
    if (modalRoles) modalRoles.classList.remove('active');
    selectedUser = null;
    if (rolesErrorMsg) rolesErrorMsg.classList.add('hidden');
}

if (btnCloseRoles) btnCloseRoles.onclick = closeModal;
if (btnCancelRoles) btnCancelRoles.onclick = closeModal;

/**
 * Opens user role modal.
 */
function openRoleModal(user) {
    selectedUser = user;
    if (modalUserName) modalUserName.textContent = `${user.firstName} ${user.lastName} (#${user.id})`;
    if (modalUserEmail) modalUserEmail.textContent = user.email;

    const currentRoles = (user.roles || []).map(r => r.name);
    if (chkUser) chkUser.checked = currentRoles.includes('USER');
    if (chkTech) chkTech.checked = currentRoles.includes('TECHNICIAN');
    if (chkAdmin) chkAdmin.checked = currentRoles.includes('ADMIN');

    if (rolesErrorMsg) rolesErrorMsg.classList.add('hidden');
    if (modalRoles) modalRoles.classList.add('active');
}

if (btnSaveRoles) {
    btnSaveRoles.onclick = async () => {
        if (!selectedUser) return;

        const rolesToAssign = [];
        if (chkUser && chkUser.checked) rolesToAssign.push('USER');
        if (chkTech && chkTech.checked) rolesToAssign.push('TECHNICIAN');
        if (chkAdmin && chkAdmin.checked) rolesToAssign.push('ADMIN');

        if (rolesToAssign.length === 0) {
            if (rolesErrorMsg) {
                rolesErrorMsg.textContent = 'Debe seleccionar al menos un rol para el usuario';
                rolesErrorMsg.classList.remove('hidden');
            }
            return;
        }

        try {
            setButtonLoading(btnSaveRoles, true, 'Guardar Cambios');
            await API.updateUserRoles(selectedUser.id, rolesToAssign);
            showToast(`Roles de ${selectedUser.firstName} actualizados correctamente`, 'success');
            closeModal();
            await loadUsers();
        } catch (error) {
            showToast(error.message || 'Error al actualizar roles', 'error');
            if (rolesErrorMsg) {
                rolesErrorMsg.textContent = error.message;
                rolesErrorMsg.classList.remove('hidden');
            }
        } finally {
            setButtonLoading(btnSaveRoles, false, 'Guardar Cambios');
        }
    };
}

/**
 * Generates role badge markup.
 */
function getRoleBadge(roleName) {
    if (roleName === 'ADMIN') {
        return `<span class="badge" style="background: rgba(244, 63, 94, 0.15); color: #f43f5e; border: 1px solid rgba(244, 63, 94, 0.3); margin-right: 4px;">ADMIN</span>`;
    }
    if (roleName === 'TECHNICIAN') {
        return `<span class="badge" style="background: rgba(139, 92, 246, 0.15); color: #a78bfa; border: 1px solid rgba(139, 92, 246, 0.3); margin-right: 4px;">TECHNICIAN</span>`;
    }
    return `<span class="badge" style="background: rgba(59, 130, 246, 0.15); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3); margin-right: 4px;">USER</span>`;
}

/**
 * Loads all system users.
 */
async function loadUsers() {
    const tbody = document.getElementById('users-tbody');
    const loading = document.getElementById('loading-indicator');
    const empty = document.getElementById('empty-state');

    if (tbody) tbody.innerHTML = '';
    if (empty) empty.classList.add('hidden');
    if (loading) loading.classList.remove('hidden');

    try {
        usersList = await API.getAllUsers();
        
        if (!usersList || usersList.length === 0) {
            if (empty) empty.classList.remove('hidden');
        } else {
            usersList.sort((a, b) => a.id - b.id);
            usersList.forEach(u => {
                const tr = document.createElement('tr');
                const roleBadges = (u.roles && u.roles.length > 0)
                    ? u.roles.map(r => getRoleBadge(r.name)).join('')
                    : getRoleBadge('USER');

                tr.innerHTML = `
                    <td style="font-weight: 600; color: var(--text-secondary);">#${u.id}</td>
                    <td>
                        <div style="font-weight: 500; font-size: 0.95rem; margin-bottom: 4px;">${escapeHTML(u.firstName)} ${escapeHTML(u.lastName)}</div>
                        <div style="display: flex; gap: 4px; flex-wrap: wrap;">${roleBadges}</div>
                    </td>
                    <td style="color: var(--text-secondary);">${escapeHTML(u.email)}</td>
                    <td style="text-align: right; padding-right: 1.5rem;">
                        <button class="btn btn-secondary btn-manage-roles" data-user-id="${u.id}" style="padding: 6px 14px; font-size: 0.85rem; display: inline-flex; align-items: center; gap: 6px;">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
                            Gestionar Roles
                        </button>
                    </td>
                `;

                const btnManage = tr.querySelector('.btn-manage-roles');
                if (btnManage) {
                    btnManage.onclick = (e) => {
                        e.stopPropagation();
                        openRoleModal(u);
                    };
                }

                if (tbody) tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast(error.message || 'Error al cargar usuarios', 'error');
        if (empty) {
            empty.classList.remove('hidden');
            empty.innerHTML = `<p class="text-muted" style="color:#ef4444;">${error.message}</p>`;
        }
    } finally {
        if (loading) loading.classList.add('hidden');
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadUsers);
} else {
    loadUsers();
}