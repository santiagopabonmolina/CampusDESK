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

/**
 * Resets ticket filter options.
 */
function resetFilters() {
    if (filterState) filterState.value = '';
    if (filterPriority) filterPriority.value = '';
    if (filterCategory) filterCategory.value = '';
    loadTickets();
}

const btnResetFilters = document.getElementById('btn-reset-filters');
if (btnResetFilters) btnResetFilters.addEventListener('click', resetFilters);

const btnClearHeader = document.getElementById('btn-clear-filters-header');
if (btnClearHeader) btnClearHeader.addEventListener('click', resetFilters);

/**
 * Renders loading skeleton rows.
 */
function renderSkeletonRows(tbody, count = 5) {
    if (!tbody) return;
    let html = '';
    for (let i = 0; i < count; i++) {
        html += `
            <tr class="skeleton-tr">
                <td><span class="skeleton skeleton-text" style="width: 40px;"></span></td>
                <td>
                    <span class="skeleton skeleton-text" style="width: 160px; margin-bottom: 6px;"></span>
                    <span class="skeleton skeleton-text" style="width: 90px; height: 10px;"></span>
                </td>
                <td><span class="skeleton skeleton-text" style="width: 70px;"></span></td>
                <td><span class="skeleton skeleton-pill"></span></td>
                <td><span class="skeleton skeleton-pill"></span></td>
                <td><span class="skeleton skeleton-text" style="width: 80px;"></span></td>
                <td style="text-align: right;"><span class="skeleton skeleton-pill" style="width: 60px;"></span></td>
            </tr>
        `;
    }
    tbody.innerHTML = html;
}

/**
 * Loads filtered ticket records.
 */
export async function loadTickets() {
    const tbody = document.getElementById('tickets-tbody');
    const loading = document.getElementById('loading-indicator');
    const empty = document.getElementById('empty-state');

    const stateValue = filterState ? filterState.value : '';
    const priorityValue = filterPriority ? filterPriority.value : '';
    const categoryValue = filterCategory ? filterCategory.value : '';

    if (empty) empty.classList.add('hidden');
    if (loading) loading.classList.remove('hidden');
    renderSkeletonRows(tbody, 4);

    try {
        const tickets = await API.getTickets(stateValue || null, priorityValue || null, categoryValue || null);
        
        if (!tickets || tickets.length === 0) {
            if (tbody) tbody.innerHTML = '';
            if (empty) empty.classList.remove('hidden');
        } else {
            // Sort by id desc (newest first)
            tickets.sort((a, b) => b.id - a.id);
            if (tbody) tbody.innerHTML = '';
            
            const catLabels = { 'HARDWARE':'Hardware', 'SOFTWARE':'Software', 'NETWORK':'Redes', 'ACCESS':'Accesos', 'OTHER':'Otros' };

            tickets.forEach(ticket => {
                const tr = document.createElement('tr');
                tr.onclick = () => window.location.href = `ticket-detail.html?id=${ticket.id}`;
                tr.setAttribute('title', 'Clic para ver detalles');
                
                tr.innerHTML = `
                    <td style="font-weight: 700; color: var(--accent-light);">#${ticket.id}</td>
                    <td>
                        <div style="font-weight: 600; color: #f8fafc;">${escapeHTML(ticket.title)}</div>
                        <small class="text-muted" style="display: block; margin-top: 2px;">${escapeHTML(ticket.requesterName || 'N/A')}</small>
                    </td>
                    <td><span style="font-size: 0.86rem; color: #cbd5e1; font-weight: 500;">${catLabels[ticket.category] || ticket.category}</span></td>
                    <td>${getPriorityBadge(ticket.priority)}</td>
                    <td>${getStateBadge(ticket.state)}</td>
                    <td style="color: var(--text-muted); font-size: 0.85rem;">${formatDate(ticket.createDate)}</td>
                    <td style="text-align: right;">
                        <span class="btn-quick-view">
                            Ver &rarr;
                        </span>
                    </td>
                `;
                if (tbody) tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast(error.message || 'Error al cargar tickets', 'error');
        if (tbody) tbody.innerHTML = '';
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
            title: document.getElementById('ticket-title').value.trim(),
            description: document.getElementById('ticket-desc').value.trim(),
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