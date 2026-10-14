import { CONFIG } from './config.js';
import { showToast } from './ui.js';
import { logout } from './auth.js';

export async function fetchApi(endpoint, options = {}) {
  const url = `${CONFIG.API_BASE_URL}${endpoint}`;
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  const token = sessionStorage.getItem('token');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers
  };

  if (options.body && typeof options.body === 'object') {
    config.body = JSON.stringify(options.body);
  }

  try {
    const response = await fetch(url, config);

    // Parse response
    let data = null;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      data = await response.json();
    }

    if (response.status === 401) {
      // Don't auto-logout or redirect if failing login attempt
      if (!endpoint.includes('/auth/login')) {
        showToast('Tu sesión expiró o es inválida', 'error');
        logout();
        return null;
      }
    }

    if (!response.ok) {
      let errorMsg = 'Error en la operación';
      
      if (data) {
        if (data.message) {
          errorMsg = data.message;
        } else if (data.errors && Object.keys(data.errors).length > 0) {
          errorMsg = Object.values(data.errors).join(', ');
        }
      } else if (response.status === 403) {
        errorMsg = 'No tienes permisos para esta acción';
      }

      // For non-auth endpoints, show 403 toast
      if (response.status === 403 && !endpoint.startsWith('/auth/')) {
        showToast(errorMsg, 'error');
      }

      const error = new Error(errorMsg);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return data;
  } catch (error) {
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      showToast('No se pudo conectar con el servidor', 'error');
      throw new Error('Servidor caído');
    }
    throw error;
  }
}

// ---------------------------
// Endpoints API
// ---------------------------

export const API = {
  // Auth
  login: (data) => fetchApi('/auth/login', { method: 'POST', body: data }),
  register: (data) => fetchApi('/auth/register', { method: 'POST', body: data }),
  verifyEmail: (data) => fetchApi('/auth/verify-email', { method: 'POST', body: data }),
  resendCode: (data) => fetchApi('/auth/resend-code', { method: 'POST', body: data }),
  
  // Users
  getTechnicians: () => fetchApi('/users/technicians'),
  getAllUsers: () => fetchApi('/users'),
  
  // Tickets
  getTickets: (filter = null) => {
    let queryParams = [];
    if (typeof filter === 'string' && filter.trim() !== '') {
      queryParams.push(`state=${encodeURIComponent(filter)}`);
    } else if (filter && typeof filter === 'object') {
      if (filter.state) queryParams.push(`state=${encodeURIComponent(filter.state)}`);
      if (filter.priority) queryParams.push(`priority=${encodeURIComponent(filter.priority)}`);
      if (filter.category) queryParams.push(`category=${encodeURIComponent(filter.category)}`);
    }
    const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
    return fetchApi(`/tickets${queryString}`);
  },
  getTicketById: (id) => fetchApi(`/tickets/${id}`),
  createTicket: (data) => fetchApi('/tickets', { method: 'POST', body: data }),
  updateTicket: (id, data) => fetchApi(`/tickets/${id}`, { method: 'PUT', body: data }),
  assignTechnician: (id, technicianId) => fetchApi(`/tickets/${id}/assign?technicianId=${technicianId}`, { method: 'PATCH' }),
  changeStatus: (id, status) => fetchApi(`/tickets/${id}/status?status=${status}`, { method: 'PATCH' }),
  
  // Comments
  getComments: (ticketId) => fetchApi(`/comments/ticket/${ticketId}`),
  addComment: (data) => fetchApi('/comments', { method: 'POST', body: data }),
  
  // History
  getHistory: (ticketId) => fetchApi(`/tickets/${ticketId}/history`),
  
  // Dashboard
  getSummary: () => fetchApi('/reports/summary')
};
