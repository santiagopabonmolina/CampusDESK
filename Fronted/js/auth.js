import { CONFIG } from './config.js';

export { CONFIG };

export function getToken() {
  return sessionStorage.getItem('token');
}

export function getCurrentUser() {
  const user = sessionStorage.getItem('user');
  try {
    return user ? JSON.parse(user) : null;
  } catch (e) {
    return null;
  }
}

export function isLoggedIn() {
  const token = getToken();
  const user = getCurrentUser();
  if (!token || !user) return false;
  
  // Basic expiration check (assuming JWT payload)
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      logout(false);
      return false;
    }
    return true;
  } catch (e) {
    return false;
  }
}

export function getUserRole() {
  const user = getCurrentUser();
  return user ? user.role : null;
}

export function hasRole(roleName) {
  return getUserRole() === roleName;
}

export function setSession(data) {
  sessionStorage.setItem('token', data.token);
  sessionStorage.setItem('user', JSON.stringify({
    id: data.userId,
    email: data.email,
    firstName: data.firstName,
    lastName: data.lastName,
    role: data.role
  }));
}

export function logout(redirect = true) {
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('user');
  if (redirect) {
    window.location.href = 'index.html';
  }
}

export function checkAuth() {
  if (!isLoggedIn()) {
    logout(true);
  }
}

export function checkAdminAccess() {
  checkAuth();
  if (!hasRole(CONFIG.ROLES.ADMIN)) {
    window.location.href = 'dashboard.html';
  }
}

// Initial check for all protected pages (if we're not on index.html)
if (!window.location.pathname.endsWith('index.html') && 
    window.location.pathname !== '/' && 
    !window.location.pathname.endsWith('Fronted/') &&
    !window.location.pathname.endsWith('Fronted')) {
  checkAuth();
}
