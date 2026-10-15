import { CONFIG } from './config.js';
import { showToast } from './ui.js';
import { logout } from './auth.js';

/**
 * Performs HTTP API request.
 */
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
    
    let data = null;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      data = await response.json();
    }

    if (response.status === 401) {
      if (endpoint.includes('/auth/login')) {
        const msg = (data && data.message) ? data.message : 'Credenciales incorrectas: correo electrónico o contraseña inválidos';
        const err = new Error(msg);
        err.data = data;
        throw err;
      }
      showToast('Tu sesión expiró o es inválida', 'error');
      logout();
      return null;
    }

    if (response.status === 403) {
      const msg = (data && data.message) ? data.message : 'No tienes permisos para realizar esta acción';
      showToast(msg, 'error');
      const err = new Error(msg);
      err.data = data;
      throw err;
    }

    if (!response.ok) {
      let errorMsg = 'Error en la operación';
      
      if (data) {
        if (data.message) {
          errorMsg = data.message;
        }
        if (data.errors && Object.keys(data.errors).length > 0) {
          errorMsg = Object.values(data.errors).join(', ');
        }
      }
      
      const err = new Error(errorMsg);
      err.data = data;
      throw err;
    }

    return data;
  } catch (error) {
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      showToast('No se pudo conectar con el servidor', 'error');
      const err = new Error('Servidor caído o inalcanzable');
      throw err;
    }
    throw error;
  }
}

export const API = {
  /**
   * Authenticates user with credentials.
   */
  login: (data) => fetchApi('/auth/login', { method: 'POST', body: data }),

  /**
   * Registers new user account.
   */
  register: (data) => fetchApi('/auth/register', { method: 'POST', body: data }),

  /**
   * Retrieves public system statistics.
   */
  getPublicStats: () => fetchApi('/public/stats'),
  
  /**
   * Retrieves available technician users.
   */
  getTechnicians: () => fetchApi('/users/technicians'),

  /**
   * Retrieves all system users.
   */
  getAllUsers: () => fetchApi('/users'),

  /**
   * Updates user role assignments.
   */
  updateUserRoles: (userId, roles) => fetchApi(`/users/${userId}/roles`, { method: 'PUT', body: roles }),
  
  /**
   * Retrieves filtered tickets list.
   */
  getTickets: (state = null, priority = null, category = null) => {
    const params = new URLSearchParams();
    if (state) {
      params.append('state', state);
      params.append('status', state);
    }
    if (priority) params.append('priority', priority);
    if (category) params.append('category', category);
    const qs = params.toString();
    return fetchApi(qs ? `/tickets?${qs}` : '/tickets');
  },

  /**
   * Retrieves ticket by identifier.
   */
  getTicketById: (id) => fetchApi(`/tickets/${id}`),

  /**
   * Creates new ticket record.
   */
  createTicket: (data) => fetchApi('/tickets', { method: 'POST', body: data }),

  /**
   * Updates open ticket details.
   */
  updateTicket: (id, data) => fetchApi(`/tickets/${id}`, { method: 'PUT', body: data }),

  /**
   * Assigns technician to ticket.
   */
  assignTechnician: (id, technicianId) => fetchApi(`/tickets/${id}/assign?technicianId=${technicianId}`, { method: 'PATCH' }),

  /**
   * Updates ticket lifecycle status.
   */
  changeStatus: (id, status) => fetchApi(`/tickets/${id}/status?status=${status}&state=${status}`, { method: 'PATCH' }),

  /**
   * Deletes ticket from system.
   */
  deleteTicket: (id) => fetchApi(`/tickets/${id}`, { method: 'DELETE' }),
  
  /**
   * Retrieves ticket comments list.
   */
  getComments: (ticketId) => fetchApi(`/tickets/${ticketId}/comments`),

  /**
   * Adds comment to ticket.
   */
  addComment: (data) => fetchApi(`/tickets/${data.ticketId}/comments`, { method: 'POST', body: data }),
  
  /**
   * Retrieves ticket audit timeline.
   */
  getHistory: (ticketId) => fetchApi(`/tickets/${ticketId}/history`),
  
  /**
   * Retrieves dashboard summary metrics.
   */
  getSummary: () => fetchApi('/reports/summary'),

  /**
   * Retrieves dashboard statistics data.
   */
  getStats: () => fetchApi('/dashboard/stats')
};
