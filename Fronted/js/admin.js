import { API } from './api.js';
import { checkAdminAccess } from './auth.js';
import { showToast, setupModal, escapeHTML } from './ui.js';

// Protected route
checkAdminAccess();
setupModal();

async function loadUsers() {
    const tbody = document.getElementById('users-tbody');
    const loading = document.getElementById('loading-indicator');
    const empty = document.getElementById('empty-state');

    try {
        const users = await API.getAllUsers();
        
        if (!users || users.length === 0) {
            empty.classList.remove('hidden');
        } else {
            users.forEach(u => {
                const tr = document.createElement('tr');
                const roleNames = u.roles && u.roles.length > 0 
                    ? u.roles.map(r => r.name).join(', ') 
                    : 'USER';
                tr.innerHTML = `
                    <td style="font-weight: 500;">#${u.id}</td>
                    <td>${escapeHTML(u.firstName)} ${escapeHTML(u.lastName)} <span class="badge" style="background:rgba(139, 92, 246, 0.2); color:#a78bfa; margin-left:8px;">${roleNames}</span></td>
                    <td>${escapeHTML(u.email)}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast(error.message, 'error');
        empty.classList.remove('hidden');
        empty.innerHTML = `<p class="text-muted" style="color:#ef4444;">${error.message}</p>`;
    } finally {
        loading.classList.add('hidden');
    }
}

document.addEventListener('DOMContentLoaded', loadUsers);
