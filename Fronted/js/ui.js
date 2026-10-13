export function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  
  let icon = '';
  switch(type) {
    case 'success': icon = '✓'; break;
    case 'error': icon = '✕'; break;
    case 'info': icon = 'ℹ'; break;
  }
  
  toast.innerHTML = `
    <span style="font-weight: bold; font-size: 1.1rem;">${icon}</span>
    <span>${escapeHTML(message)}</span>
  `;

  container.appendChild(toast);

  // Trigger animation
  setTimeout(() => toast.classList.add('show'), 10);

  // Remove
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

export function escapeHTML(str) {
  if (typeof str !== 'string') return str;
  return str.replace(/[&<>'"]/g, 
    tag => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&#39;',
      '"': '&quot;'
    }[tag])
  );
}

export function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const date = new Date(dateStr);
  return date.toLocaleString('es-ES', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function getStateBadge(state) {
  const labels = {
    'OPENED': 'Abierta',
    'ASSIGNED': 'Asignada',
    'IN_PROCESS': 'En Proceso',
    'RESOLVED': 'Resuelta',
    'CLOSED': 'Cerrada'
  };
  return `<span class="badge badge-state-${state}">${labels[state] || state}</span>`;
}

export function getPriorityBadge(priority) {
  const labels = {
    'LOW': 'Baja',
    'MEDIUM': 'Media',
    'HIGH': 'Alta',
    'CRITIC': 'Crítica'
  };
  return `<span class="badge badge-priority-${priority}">${labels[priority] || priority}</span>`;
}

export function renderSidebarUser() {
  const userStr = sessionStorage.getItem('user');
  if (!userStr) return;
  try {
    const user = JSON.parse(userStr);
    const userNameEl = document.getElementById('sidebar-user-name');
    const userRoleEl = document.getElementById('sidebar-user-role');
    const userAvatarEl = document.getElementById('sidebar-user-avatar');
    
    if (userNameEl && user.firstName) {
      userNameEl.textContent = `${user.firstName} ${user.lastName || ''}`.trim();
    }
    if (userRoleEl && user.role) {
      let roleText = 'Usuario';
      if (user.role === 'ROLE_ADMIN') roleText = 'Administrador';
      if (user.role === 'ROLE_TECHNICIAN') roleText = 'Técnico';
      userRoleEl.textContent = roleText;
    }
    if (userAvatarEl && user.firstName) {
      userAvatarEl.textContent = user.firstName.charAt(0).toUpperCase();
    }
  } catch (e) {
    console.error('Error rendering sidebar user:', e);
  }
}

export function initAppShell() {
  renderSidebarUser();
  const logoutBtn = document.getElementById('btn-logout');
  if (logoutBtn && !logoutBtn.dataset.bound) {
    logoutBtn.dataset.bound = 'true';
    logoutBtn.addEventListener('click', () => {
      sessionStorage.clear();
      window.location.href = 'index.html';
    });
  }
}

// Auto-run when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initAppShell);
} else {
  initAppShell();
}

export function setButtonLoading(btn, isLoading, originalText) {
  if (isLoading) {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner"></span> Procesando...`;
  } else {
    btn.disabled = false;
    btn.innerHTML = originalText;
  }
}

export function setupModal(modalId, triggerIds, closeIds) {
  initAppShell();
  if (!modalId) return { open: () => {}, close: () => {} };

  const modal = document.getElementById(modalId);
  if (!modal) return { open: () => {}, close: () => {} };
  
  const openModal = () => modal.classList.add('active');
  const closeModal = () => modal.classList.remove('active');
  
  if (triggerIds && Array.isArray(triggerIds)) {
    triggerIds.forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', openModal);
    });
  }
  
  if (closeIds && Array.isArray(closeIds)) {
    closeIds.forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', closeModal);
    });
  }
  
  return { open: openModal, close: closeModal };
}
