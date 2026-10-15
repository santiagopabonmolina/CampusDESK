import { CONFIG } from './config.js';

export { CONFIG };

/**
 * Retrieves stored session token.
 */
export function getToken() {
  return sessionStorage.getItem('token');
}

/**
 * Retrieves current session user.
 */
export function getCurrentUser() {
  const user = sessionStorage.getItem('user');
  try {
    return user ? JSON.parse(user) : null;
  } catch (e) {
    return null;
  }
}

/**
 * Checks if user authenticated.
 */
export function isLoggedIn() {
  const token = getToken();
  const user = getCurrentUser();
  if (!token || !user) return false;
  
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

/**
 * Retrieves current user role.
 */
export function getUserRole() {
  const user = getCurrentUser();
  return user ? user.role : null;
}

/**
 * Checks specific user role.
 */
export function hasRole(roleName) {
  return getUserRole() === roleName;
}

/**
 * Stores user session data.
 */
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

/**
 * Clears user session storage.
 */
export function logout(redirect = true) {
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('user');
  if (redirect) {
    window.location.href = 'index.html';
  }
}

/**
 * Verifies user authentication state.
 */
export function checkAuth() {
  if (!isLoggedIn()) {
    logout(true);
  }
}

/**
 * Enforces administrator access permissions.
 */
export function checkAdminAccess() {
  checkAuth();
  if (!hasRole(CONFIG.ROLES.ADMIN)) {
    window.location.href = 'dashboard.html';
  }
}

const path = window.location.pathname.toLowerCase();
const isPublicPage = path.endsWith('index.html') || 
                     path.endsWith('landing.html') || 
                     path === '/' || 
                     path.endsWith('fronted/') || 
                     path.endsWith('fronted');

if (!isPublicPage) {
  checkAuth();
}
