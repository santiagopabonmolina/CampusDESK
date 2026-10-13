import { API } from './api.js';
import { CONFIG } from './config.js';
import { hasRole, getCurrentUser, isLoggedIn } from './auth.js';
import { showToast, setupModal, formatDate, getStateBadge, getPriorityBadge, setButtonLoading, escapeHTML, initAppShell } from './ui.js';

// Initialize user and UI
initAppShell();

if (!isLoggedIn()) {
    window.location.href = 'index.html';
}

const { open: openCreate, close: closeCreate } = setupModal(
    'modal-create-ticket', 
    ['btn-open-create-modal'], 
    ['btn-close-create-modal', 'btn-cancel-create']
);

// Check URL params for auto-open modal
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('action') === 'new') {
    openCreate();
}

if (hasRole(CONFIG.ROLES.ADMIN)) {
    const adminMenu = document.getElementById('admin-menu');
    if (adminMenu) adminMenu.classList.remove('hidden');
}

// Filters (Estado, Prioridad, Categoría)
const filterState = document.getElementById('filter-state');
const filterPriority = document.getElementById('filter-priority');
const filterCategory = document.getElementById('filter-category');

if (filterState) filterState.addEventListener('change', loadTickets);
if (filterPriority) filterPriority.addEventListener('change', loadTickets);
if (filterCategory) filterCategory.addEventListener('change', loadTickets);

// Load Tickets
export async function loadTickets() {
    const tbody = document.getElementById('tickets-tbody');
    const loading = document.getElementById('loading-indicator');
    const empty = document.getElementById('empty-state');

    const stateValue = filterState ? filterState.value : '';
    const priorityValue = filterPriority ? filterPriority.value : '';
    const categoryValue = filterCategory ? filterCategory.value : '';

    if (tbody) tbody.innerHTML = '';
    if (empty) empty.classList.add('hidden');
    if (loading) loading.classList.remove('hidden');

    try {
        const tickets = await API.getTickets(stateValue || null, priorityValue || null, categoryValue || null);
        
        if (!tickets || tickets.length === 0) {
            if (empty) empty.classList.remove('hidden');
        } else {
            // Sort by id desc (newest first)
            tickets.sort((a, b) => b.id - a.id);
            
            tickets.forEach(ticket => {
                const tr = document.createElement('tr');
                tr.onclick = () => window.location.href = `ticket-detail.html?id=${ticket.id}`;
                
                // Categories translation mapping
                const catLabels = { 'HARDWARE':'Hardware', 'SOFTWARE':'Software', 'NETWORK':'Redes', 'ACCESS':'Accesos', 'OTHER':'Otros' };
                
                tr.innerHTML = `
                    <td style="font-weight: 500;">#${ticket.id}</td>
                    <td>
                        <div>${escapeHTML(ticket.title)}</div>
                        <small class="text-muted">${escapeHTML(ticket.requesterName || 'N/A')}</small>
                    </td>
                    <td>${catLabels[ticket.category] || ticket.category}</td>
                    <td>${getPriorityBadge(ticket.priority)}</td>
                    <td>${getStateBadge(ticket.state)}</td>
                    <td>${formatDate(ticket.createDate)}</td>
                `;
                if (tbody) tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast(error.message || 'Error al cargar tickets', 'error');
        if (empty) empty.classList.remove('hidden');
    } finally {
        if (loading) loading.classList.add('hidden');
    }
}
window.loadTickets = loadTickets;

// Create Ticket
const formCreate = document.getElementById('form-create-ticket');
if (formCreate) {
    formCreate.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('btn-submit-ticket');
        const user = getCurrentUser();

        if (!user) {
            showToast('Debe iniciar sesión para crear un ticket', 'error');
            return;
        }

        const data = {
            title: document.getElementById('ticket-title').value,
            description: document.getElementById('ticket-desc').value,
            category: document.getElementById('ticket-category').value,
            priority: document.getElementById('ticket-priority').value,
            requesterId: user.id
        };

        try {
            setButtonLoading(btn, true, 'Crear Ticket');
            await API.createTicket(data);
            showToast('Ticket creado exitosamente', 'success');
            closeCreate();
            formCreate.reset();
            loadTickets();
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setButtonLoading(btn, false, 'Crear Ticket');
        }
    });
}

// Init
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadTickets);
} else {
    loadTickets();
}
