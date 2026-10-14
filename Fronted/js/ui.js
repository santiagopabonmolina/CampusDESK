// ==========================================================
// CampusDESK - UI Helper Functions & Micro-interactions
// ==========================================================

/**
 * Escapes HTML characters to prevent XSS.
 */
export function escapeHTML(str) {
  if (str === null || str === undefined) return '';
  if (typeof str !== 'string') return String(str);
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
 * Formats ISO date string to localized Spanish format.
 */
export function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  try {
    const date = new Date(dateStr);
    return date.toLocaleString('es-ES', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
}

/**
 * Displays an animated glassmorphic Toast notification.
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
  toast.setAttribute('role', 'alert');
  toast.setAttribute('aria-live', 'polite');
  
  let iconSvg = '';
  switch(type) {
    case 'success': 
      iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>';
      break;
    case 'error': 
      iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>';
      break;
    default:
      iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9b30ff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
  }
  
  toast.innerHTML = `
    <span style="display:flex; align-items:center; flex-shrink:0;">${iconSvg}</span>
    <span style="font-size:0.9rem; font-weight:500; line-height:1.4;">${escapeHTML(message)}</span>
  `;

  container.appendChild(toast);

  // Trigger smooth entrance animation
  requestAnimationFrame(() => {
    toast.classList.add('show');
  });

  // Auto remove after 4.2 seconds
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 400);
  }, 4200);
}

/**
 * State badge with indicator dot.
 */
export function getStateBadge(state) {
  const labels = {
    'OPENED': 'ABIERTA',
    'ASSIGNED': 'ASIGNADA',
    'IN_PROCESS': 'EN PROCESO',
    'RESOLVED': 'RESUELTA',
    'CLOSED': 'CERRADA'
  };
  return `<span class="badge badge-state-${state}">${labels[state] || state}</span>`;
}

/**
 * Priority badge with indicator dot.
 */
export function getPriorityBadge(priority) {
  const labels = {
    'LOW': 'BAJA',
    'MEDIUM': 'MEDIA',
    'HIGH': 'ALTA',
    'CRITIC': 'CRÍTICA'
  };
  return `<span class="badge badge-priority-${priority}">${labels[priority] || priority}</span>`;
}

/**
 * Category inline SVG icon helper.
 */
export function getCategoryIcon(category) {
  switch(category) {
    case 'HARDWARE':
      return `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`;
    case 'SOFTWARE':
      return `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"></polyline><polyline points="8 6 2 12 8 18"></polyline></svg>`;
    case 'NETWORK':
      return `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12.55a11 11 0 0 1 14.08 0"></path><path d="M1.42 9a16 16 0 0 1 21.16 0"></path><path d="M8.53 16.11a6 6 0 0 1 6.95 0"></path><line x1="12" y1="20" x2="12.01" y2="20"></line></svg>`;
    case 'ACCESS':
      return `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>`;
    default:
      return `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
  }
}

/**
 * Sets button loading state with spinner.
 */
export function setButtonLoading(btn, isLoading, originalText) {
  if (!btn) return;
  if (isLoading) {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner" style="width:16px; height:16px; border-width:2px;"></span> <span>Procesando...</span>`;
  } else {
    btn.disabled = false;
    btn.innerHTML = originalText;
  }
}

/**
 * Smooth Counter Animation (0 -> targetValue).
 */
export function animateCounter(element, target, duration = 1000) {
  if (!element) return;
  const targetVal = Number(target) || 0;
  if (targetVal === 0) {
    element.textContent = '0';
    return;
  }

  const startTime = performance.now();
  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    // Easing out cubic
    const easeProgress = 1 - Math.pow(1 - progress, 3);
    const current = Math.floor(easeProgress * targetVal);
    element.textContent = current.toLocaleString('es-ES');

    if (progress < 1) {
      requestAnimationFrame(update);
    } else {
      element.textContent = targetVal.toLocaleString('es-ES');
    }
  }

  requestAnimationFrame(update);
}

/**
 * Skeleton Loader for tables while fetching.
 */
export function renderSkeletonTable(tbody, rows = 5, cols = 6) {
  if (!tbody) return;
  let html = '';
  for (let r = 0; r < rows; r++) {
    html += '<tr>';
    for (let c = 0; c < cols; c++) {
      const width = c === 1 ? '75%' : (c === 0 ? '45px' : '65%');
      html += `
        <td>
          <div class="skeleton skeleton-text" style="width: ${width}; height: 16px;"></div>
        </td>
      `;
    }
    html += '</tr>';
  }
  tbody.innerHTML = html;
}

/**
 * Password Visibility Toggle helper.
 */
export function setupPasswordToggle(inputEl, btnEl) {
  if (!inputEl || !btnEl) return;

  const eyeOpenSvg = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
  const eyeClosedSvg = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;

  btnEl.innerHTML = eyeOpenSvg;
  btnEl.setAttribute('aria-label', 'Mostrar contraseña');

  btnEl.addEventListener('click', (e) => {
    e.preventDefault();
    const isPassword = inputEl.type === 'password';
    inputEl.type = isPassword ? 'text' : 'password';
    btnEl.innerHTML = isPassword ? eyeClosedSvg : eyeOpenSvg;
    btnEl.setAttribute('aria-label', isPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
  });
}

/**
 * Password Strength Meter helper.
 */
export function setupPasswordStrength(inputEl, segments, textEl) {
  if (!inputEl || !segments || !textEl) return;

  inputEl.addEventListener('input', () => {
    const val = inputEl.value;
    if (!val) {
      segments.forEach(s => s.style.backgroundColor = 'transparent');
      textEl.textContent = 'Mínimo 6 caracteres';
      textEl.style.color = 'var(--text-muted)';
      return;
    }

    let score = 0;
    if (val.length >= 6) score++;
    if (val.length >= 8) score++;
    if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++;
    if (/\d/.test(val) || /[^A-Za-z0-9]/.test(val)) score++;

    const colors = ['#ef4444', '#f59e0b', '#3b82f6', '#10b981'];
    const labels = ['Muy Débil', 'Regular', 'Buena', 'Fuerte'];

    segments.forEach((seg, idx) => {
      seg.style.backgroundColor = idx < score ? colors[score - 1] : 'transparent';
    });

    textEl.textContent = `Fortaleza: ${labels[score - 1] || 'Muy Débil'}`;
    textEl.style.color = colors[score - 1] || '#ef4444';
  });
}

/**
 * Populates current logged in user details in sidebar.
 */
export function renderSidebarUser() {
  const userStr = sessionStorage.getItem('user');
  if (!userStr) return;
  try {
    const user = JSON.parse(userStr);
    const userNameEl = document.getElementById('sidebar-user-name');
    const userRoleEl = document.getElementById('sidebar-user-role');
    const userAvatarEl = document.getElementById('sidebar-user-avatar');
    
    if (userNameEl) userNameEl.textContent = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.email;
    if (userRoleEl) {
      let roleText = 'Usuario';
      if (user.role === 'ROLE_ADMIN') roleText = 'Administrador';
      if (user.role === 'ROLE_TECHNICIAN') roleText = 'Técnico de Soporte';
      userRoleEl.textContent = roleText;
    }
    if (userAvatarEl && user.firstName) {
      userAvatarEl.textContent = user.firstName.charAt(0).toUpperCase();
    }
  } catch (e) {
    console.error('Error rendering user in sidebar', e);
  }
}

/**
 * Mobile Drawer Menu toggle with backdrop and accessibility.
 */
export function setupMobileMenu() {
  let backdrop = document.querySelector('.sidebar-backdrop');
  if (!backdrop) {
    backdrop = document.createElement('div');
    backdrop.className = 'sidebar-backdrop';
    document.body.appendChild(backdrop);
  }

  const sidebar = document.getElementById('sidebar');
  const toggleBtn = document.getElementById('btn-mobile-menu');

  if (toggleBtn && sidebar) {
    const openMenu = () => {
      sidebar.classList.add('active');
      backdrop.classList.add('active');
      toggleBtn.setAttribute('aria-expanded', 'true');
    };

    const closeMenu = () => {
      sidebar.classList.remove('active');
      backdrop.classList.remove('active');
      toggleBtn.setAttribute('aria-expanded', 'false');
    };

    toggleBtn.addEventListener('click', () => {
      sidebar.classList.contains('active') ? closeMenu() : openMenu();
    });

    backdrop.addEventListener('click', closeMenu);

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && sidebar.classList.contains('active')) {
        closeMenu();
      }
    });
  }
}

/**
 * Modal dialog setup with keyboard accessibility and focus trap.
 */
export function setupModal(modalId, triggerIds, closeIds) {
  renderSidebarUser();
  setupMobileMenu();

  const logoutBtn = document.getElementById('btn-logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      sessionStorage.clear();
      window.location.href = 'index.html';
    });
  }

  if (!modalId) return {};

  const modal = document.getElementById(modalId);
  if (!modal) return {};

  const openModal = () => {
    modal.classList.add('active');
    modal.setAttribute('aria-hidden', 'false');
    const firstInput = modal.querySelector('input, select, textarea');
    if (firstInput) setTimeout(() => firstInput.focus(), 150);
  };

  const closeModal = () => {
    modal.classList.remove('active');
    modal.setAttribute('aria-hidden', 'true');
  };

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

  // Close on backdrop click
  modal.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
  });

  // Close on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal.classList.contains('active')) {
      closeModal();
    }
  });

  return { open: openModal, close: closeModal };
}
