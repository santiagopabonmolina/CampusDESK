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

    if (response.status === 401) {
      showToast('Tu sesión expiró o es inválida', 'error');
      logout();
      return null;
    }

    if (response.status === 403) {
      showToast('No tienes permisos para esta acción', 'error');
      throw new Error('Forbidden');
    }
    
    // Parse response
    let data = null;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      data = await response.json();
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
      
      throw new Error(errorMsg);
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
  
  // Users
  getTechnicians: () => fetchApi('/users/technicians'),
  getAllUsers: () => fetchApi('/users'),
  
  // Tickets (Filtros por estado, prioridad y categoría)
  getTickets: (state = null, priority = null, category = null) => {
    const params = new URLSearchParams();
    if (state) params.append('state', state);
    if (priority) params.append('priority', priority);
    if (category) params.append('category', category);
    const qs = params.toString();
    return fetchApi(qs ? `/tickets?${qs}` : '/tickets');
  },
  getTicketById: (id) => fetchApi(`/tickets/${id}`),
  createTicket: (data) => fetchApi('/tickets', { method: 'POST', body: data }),
  updateTicket: (id, data) => fetchApi(`/tickets/${id}`, { method: 'PUT', body: data }),
  assignTechnician: (id, technicianId) => fetchApi(`/tickets/${id}/assign?technicianId=${technicianId}`, { method: 'PATCH' }),
  changeStatus: (id, status) => fetchApi(`/tickets/${id}/status?status=${status}`, { method: 'PATCH' }),
  deleteTicket: (id) => fetchApi(`/tickets/${id}`, { method: 'DELETE' }),
  
  // Comments (Conforme al contrato: /api/tickets/{id}/comments)
  getComments: (ticketId) => fetchApi(`/tickets/${ticketId}/comments`),
  addComment: (data) => fetchApi(`/tickets/${data.ticketId}/comments`, { method: 'POST', body: data }),
  
  // History
  getHistory: (ticketId) => fetchApi(`/tickets/${ticketId}/history`),
  
  // Dashboard
  getSummary: () => fetchApi('/reports/summary')
};
