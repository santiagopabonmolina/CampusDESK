import { API } from './api.js';

/**
 * Retrieves filtered tickets list.
 */
export async function listTickets(filters = {}) {
    const status = filters.status || filters.state || null;
    const priority = filters.priority || null;
    const category = filters.category || null;
    return await API.getTickets(status, priority, category);
}

export const TicketService = {
    /**
     * Lists all filtered tickets.
     */
    listTickets,

    /**
     * Retrieves ticket by identifier.
     */
    getById: (id) => API.getTicketById(id),

    /**
     * Creates new ticket record.
     */
    create: (data) => API.createTicket(data),

    /**
     * Updates open ticket details.
     */
    update: (id, data) => API.updateTicket(id, data),

    /**
     * Assigns technician to ticket.
     */
    assign: (id, techId) => API.assignTechnician(id, techId),

    /**
     * Updates ticket lifecycle status.
     */
    changeStatus: (id, status) => API.changeStatus(id, status),

    /**
     * Deletes ticket from system.
     */
    delete: (id) => API.deleteTicket(id)
};