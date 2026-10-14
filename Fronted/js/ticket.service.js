import { API } from './api.js';

/**
 * Servicio de tickets para consumo directo del frontend.
 * Soporta filtros por status, priority y category comunicando directamente con el backend.
 */
export async function listTickets(filters = {}) {
    const status = filters.status || filters.state || null;
    const priority = filters.priority || null;
    const category = filters.category || null;
    return await API.getTickets(status, priority, category);
}

export const TicketService = {
    listTickets,
    getById: (id) => API.getTicketById(id),
    create: (data) => API.createTicket(data),
    update: (id, data) => API.updateTicket(id, data),
    assign: (id, techId) => API.assignTechnician(id, techId),
    changeStatus: (id, status) => API.changeStatus(id, status),
    delete: (id) => API.deleteTicket(id)
};
