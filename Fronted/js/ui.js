/**
 * Displays temporary toast notification.
 */
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
  
  let iconSvg = '';
  switch(type) {
    case 'success':
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>';
      break;
    case 'error':
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#f87171" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>';
      break;
    default:
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#38bdf8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
      break;
  }
  
  toast.innerHTML = `
    <span style="display:flex; align-items:center; flex-shrink:0;">${iconSvg}</span>
    <span style="font-size:0.92rem; font-weight:500;">${escapeHTML(message)}</span>
  `;

  container.appendChild(toast);

  requestAnimationFrame(() => {
    toast.classList.add('show');
  });

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 400);
  }, 4000);
}

/**
 * Escapes special HTML characters.
 */
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

/**
 * Formats raw date string.
 */
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

/**
 * Generates state badge HTML.
 */
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

/**
 * Generates priority badge HTML.
 */
export function getPriorityBadge(priority) {
  const labels = {
    'LOW': 'Baja',
    'MEDIUM': 'Media',
    'HIGH': 'Alta',
    'CRITIC': 'Crítica'
  };
  return `<span class="badge badge-priority-${priority}">${labels[priority] || priority}</span>`;
}

/**
 * Animates numeric counter element.
 */
export function animateCounter(element, target, duration = 800) {
  if (!element) return;
  const targetNum = parseInt(target, 10);
  if (isNaN(targetNum)) {
    element.textContent = target;
    return;
  }

  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    element.textContent = targetNum;
    return;
  }

  const startNum = 0;
  const startTime = performance.now();

  function updateCount(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easeOut = 1 - Math.pow(1 - progress, 4);
    const current = Math.floor(startNum + (targetNum - startNum) * easeOut);
    element.textContent = current;

    if (progress < 1) {
      requestAnimationFrame(updateCount);
    } else {
      element.textContent = targetNum;
    }
  }

  requestAnimationFrame(updateCount);
}

/**
 * Renders user in sidebar.
 */
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

/**
 * Initializes button ripple effect.
 */
function initRipples() {
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.btn, .btn-new-ticket, .auth-tab');
    if (!btn) return;

    const circle = document.createElement('span');
    const diameter = Math.max(btn.clientWidth, btn.clientHeight);
    const radius = diameter / 2;

    const rect = btn.getBoundingClientRect();
    circle.style.width = circle.style.height = `${diameter}px`;
    circle.style.left = `${e.clientX - rect.left - radius}px`;
    circle.style.top = `${e.clientY - rect.top - radius}px`;
    circle.classList.add('ripple');

    const rippleEl = btn.querySelector('.ripple');
    if (rippleEl) rippleEl.remove();

    btn.appendChild(circle);
    setTimeout(() => circle.remove(), 600);
  });
}

/**
 * Initializes responsive mobile navigation.
 */
function initMobileNavigation() {
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;

  let backdrop = document.querySelector('.sidebar-backdrop');
  if (!backdrop) {
    backdrop = document.createElement('div');
    backdrop.className = 'sidebar-backdrop';
    document.body.appendChild(backdrop);
  }

  const mobileBtns = document.querySelectorAll('.btn-mobile-nav');
  mobileBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
      backdrop.classList.toggle('active');
    });
  });

  backdrop.addEventListener('click', () => {
    sidebar.classList.remove('open');
    backdrop.classList.remove('active');
  });

  sidebar.querySelectorAll('.nav-item, .btn-new-ticket').forEach(link => {
    link.addEventListener('click', () => {
      if (window.innerWidth <= 860) {
        sidebar.classList.remove('open');
        backdrop.classList.remove('active');
      }
    });
  });
}

/**
 * Initializes application layout shell.
 */
export function initAppShell() {
  renderSidebarUser();
  initMobileNavigation();
  initRipples();

  const logoutBtn = document.getElementById('btn-logout');
  if (logoutBtn && !logoutBtn.dataset.bound) {
    logoutBtn.dataset.bound = 'true';
    logoutBtn.addEventListener('click', () => {
      sessionStorage.clear();
      window.location.href = 'index.html';
    });
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initAppShell);
} else {
  initAppShell();
}

/**
 * Sets button loading state.
 */
export function setButtonLoading(btn, isLoading, originalText) {
  if (isLoading) {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner"></span> Procesando...`;
  } else {
    btn.disabled = false;
    btn.innerHTML = originalText;
  }
}

/**
 * Initializes interactive modal dialog.
 */
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

  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      closeModal();
    }
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal.classList.contains('active')) {
      closeModal();
    }
  });
  
  return { open: openModal, close: closeModal };
}
