import { API } from './api.js';
import { CONFIG } from './config.js';
import { getCurrentUser, hasRole, isLoggedIn } from './auth.js';
import { showToast, formatDate, getStateBadge, getPriorityBadge, escapeHTML, initAppShell } from './ui.js';

// Init
initAppShell();

if (!isLoggedIn()) {
    window.location.href = 'index.html';
}

const urlParams = new URLSearchParams(window.location.search);
const ticketId = urlParams.get('id');

if (!ticketId) {
    showToast('ID de ticket inválido', 'error');
    setTimeout(() => window.location.href = 'tickets.html', 1500);
}

const user = getCurrentUser();

if (hasRole(CONFIG.ROLES.ADMIN)) {
    const adminMenu = document.getElementById('admin-menu');
    if (adminMenu) adminMenu.classList.remove('hidden');
}

/**
 * Loads ticket detail information.
 */
export async function loadTicketData() {
    try {
        const t = await API.getTicketById(ticketId);
        
        // Render Title & Headers
        const headerId = document.getElementById('header-ticket-id');
        const titleEl = document.getElementById('ticket-title');
        const descEl = document.getElementById('ticket-desc');
        
        if (headerId) headerId.textContent = `Ticket #${t.id}`;
        if (titleEl) titleEl.textContent = t.title;
        if (descEl) descEl.textContent = t.description;

        // Meta Badges
        const prioBadge = document.getElementById('ticket-priority-badge');
        const catBadge = document.getElementById('ticket-category-badge');
        if (prioBadge) prioBadge.innerHTML = getPriorityBadge(t.priority);
        if (catBadge) {
            const catLabels = { 'HARDWARE':'Hardware', 'SOFTWARE':'Software', 'NETWORK':'Redes', 'ACCESS':'Accesos', 'OTHER':'Otros' };
            catBadge.textContent = catLabels[t.category] || t.category;
        }

        // Stepper animado
        updateStepper(t.state);

        // Sidebar Info
        const reqName = document.getElementById('ticket-requester');
        const reqEmail = document.getElementById('ticket-requester-email');
        const techName = document.getElementById('ticket-technician');
        const createDate = document.getElementById('ticket-created');
        const resDateGroup = document.getElementById('group-resolved');
        const resDate = document.getElementById('ticket-resolved');
        const closedDateGroup = document.getElementById('group-closed');
        const closedDate = document.getElementById('ticket-closed');

        if (reqName) reqName.textContent = t.requesterName || 'N/A';
        if (reqEmail) reqEmail.textContent = t.requesterEmail || '';
        if (techName) techName.textContent = t.technicianName || 'Sin asignar';
        if (createDate) createDate.textContent = formatDate(t.createDate);

        if (resDateGroup && resDate) {
            if (t.resolutionDate) {
                resDateGroup.classList.remove('hidden');
                resDate.textContent = formatDate(t.resolutionDate);
            } else {
                resDateGroup.classList.add('hidden');
            }
        }

        if (closedDateGroup && closedDate) {
            if (t.state === 'CLOSED') {
                closedDateGroup.classList.remove('hidden');
                closedDate.textContent = t.resolutionDate ? formatDate(t.resolutionDate) : 'Cerrado';
            } else {
                closedDateGroup.classList.add('hidden');
            }
        }

        renderActions(t);
        loadComments();
        loadHistory();

        const loading = document.getElementById('loading-indicator');
        const content = document.getElementById('ticket-content');
        if (loading) loading.classList.add('hidden');
        if (content) content.classList.remove('hidden');

    } catch (error) {
        showToast(error.message || 'Error al cargar detalles del ticket', 'error');
    }
}

/**
 * Updates state stepper indicators.
 */
function updateStepper(currentState) {
    const states = ['OPENED', 'ASSIGNED', 'IN_PROCESS', 'RESOLVED', 'CLOSED'];
    const currentIndex = states.indexOf(currentState);
    
    states.forEach((s, idx) => {
        const el = document.getElementById(`step-${s}`);
        if (!el) return;
        el.className = 'step';
        const marker = el.querySelector('.step-marker');
        
        if (idx < currentIndex) {
            el.classList.add('completed');
            if (marker) marker.innerHTML = '✓';
        } else if (idx === currentIndex) {
            el.classList.add('active');
            if (marker) marker.textContent = idx + 1;
        } else {
            if (marker) marker.textContent = idx + 1;
        }
    });
}

/**
 * Renders ticket action buttons.
 */
function renderActions(t) {
    const container = document.getElementById('actions-container');
    const commentForm = document.getElementById('form-comment');
    if (!container) return;
    container.innerHTML = '';
    
    const isAdmin = hasRole(CONFIG.ROLES.ADMIN);
    const isTech = hasRole(CONFIG.ROLES.TECHNICIAN);
    const isUser = hasRole(CONFIG.ROLES.USER);
    
    const isMyTicket = user && t.requesterId === user.id;
    const isMyAssigned = user && t.technicianId === user.id;

    // Bloquear comentarios si está cerrado
    if (commentForm) {
        if (t.state === 'CLOSED') {
            commentForm.classList.add('hidden');
        } else {
            commentForm.classList.remove('hidden');
        }
    }

    // ADMIN: Asignar / Reasignar técnico
    if (isAdmin && t.state !== 'CLOSED') {
        const btnAsignar = document.createElement('button');
        btnAsignar.className = 'btn btn-secondary';
        btnAsignar.innerHTML = t.technicianId ? 'Reasignar Técnico' : 'Asignar Técnico';
        btnAsignar.onclick = openAssignModal;
        container.appendChild(btnAsignar);
    }

    // TECH / ADMIN: Iniciar Trabajo (ASIGNADA -> EN_PROCESO)
    if (((isTech && isMyAssigned) || isAdmin) && t.state === 'ASSIGNED') {
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary';
        btn.textContent = 'Iniciar Trabajo (En Proceso)';
        btn.onclick = () => changeStatus('IN_PROCESS');
        container.appendChild(btn);
    }

    // TECH / ADMIN: Resolver (EN_PROCESO -> RESUELTA)
    if (((isTech && isMyAssigned) || isAdmin) && t.state === 'IN_PROCESS') {
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary';
        btn.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
        btn.textContent = 'Marcar como Resuelta';
        btn.onclick = () => changeStatus('RESOLVED');
        container.appendChild(btn);
    }

    // USER / ADMIN: Confirmar Cierre (RESUELTA -> CERRADA)
    if (((isUser && isMyTicket) || isAdmin) && t.state === 'RESOLVED') {
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary';
        btn.style.background = 'linear-gradient(135deg, #64748b 0%, #475569 100%)';
        btn.textContent = 'Confirmar Cierre';
        btn.onclick = () => changeStatus('CLOSED');
        container.appendChild(btn);
    }

    // ADMIN: Eliminar incidencia
    if (isAdmin) {
        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-danger';
        btnDelete.textContent = 'Eliminar Ticket';
        btnDelete.onclick = async () => {
            if (confirm(`¿Confirma que desea eliminar el Ticket #${t.id}? Esta operación es irreversible.`)) {
                try {
                    await API.deleteTicket(ticketId);
                    showToast('Ticket eliminado correctamente', 'success');
                    setTimeout(() => window.location.href = 'tickets.html', 800);
                } catch (err) {
                    showToast(err.message, 'error');
                }
            }
        };
        container.appendChild(btnDelete);
    }
}

/**
 * Updates ticket lifecycle status.
 */
async function changeStatus(newStatus) {
    try {
        await API.changeStatus(ticketId, newStatus);
        showToast('Estado actualizado', 'success');
        loadTicketData();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// --- Asignación ---
const modalAssign = document.getElementById('modal-assign');
const btnCloseAssign = document.getElementById('btn-close-assign');
const btnCancelAssign = document.getElementById('btn-cancel-assign');
if (btnCloseAssign && modalAssign) btnCloseAssign.onclick = () => modalAssign.classList.remove('active');
if (btnCancelAssign && modalAssign) btnCancelAssign.onclick = () => modalAssign.classList.remove('active');

/**
 * Opens technician assignment modal.
 */
async function openAssignModal() {
    if (!modalAssign) return;
    modalAssign.classList.add('active');
    const select = document.getElementById('select-technician');
    if (!select) return;
    select.innerHTML = '<option>Cargando...</option>';
    try {
        const techs = await API.getTechnicians();
        if (techs.length === 0) {
            select.innerHTML = '<option value="">No hay técnicos disponibles</option>';
            return;
        }
        select.innerHTML = techs.map(t => 
            `<option value="${t.id}">${escapeHTML(t.firstName)} ${escapeHTML(t.lastName)} (${escapeHTML(t.email)})</option>`
        ).join('');
    } catch (error) {
        select.innerHTML = '<option value="">Error al cargar técnicos</option>';
    }
}

const btnConfirmAssign = document.getElementById('btn-confirm-assign');
if (btnConfirmAssign) {
    btnConfirmAssign.onclick = async () => {
        const select = document.getElementById('select-technician');
        const techId = select ? select.value : null;
        if (!techId) return showToast('Seleccione un técnico', 'error');
        try {
            await API.assignTechnician(ticketId, techId);
            showToast('Técnico asignado', 'success');
            if (modalAssign) modalAssign.classList.remove('active');
            loadTicketData();
        } catch (error) {
            showToast(error.message, 'error');
        }
    };
}

// --- Comentarios ---
/**
 * Loads ticket comments conversation.
 */
async function loadComments() {
    try {
        const comments = await API.getComments(ticketId);
        const container = document.getElementById('comments-container');
        if (!container) return;
        if (!comments || comments.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="padding: 2rem 1rem;">
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                    </svg>
                    <p class="text-muted" style="margin: 0;">No hay comentarios aún. Sé el primero en escribir.</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = comments.map(c => {
            const displayName = c.authorName || `Usuario ID: ${c.authorId}`;
            const initial = displayName.charAt(0).toUpperCase();

            let roleClass = 'role-user';
            let roleTag = 'Usuario';
            let tagClass = 'user';

            const userRoles = c.authorRoles || [];
            const roleStr = c.authorRole || (userRoles.length > 0 ? userRoles[0] : '');
            
            if (roleStr.includes('ADMIN') || (user && user.role === 'ROLE_ADMIN' && c.authorId === user.id)) {
                roleClass = 'role-admin';
                roleTag = 'Admin';
                tagClass = 'admin';
            } else if (roleStr.includes('TECH') || (user && user.role === 'ROLE_TECHNICIAN' && c.authorId === user.id)) {
                roleClass = 'role-technician';
                roleTag = 'Técnico';
                tagClass = 'technician';
            }

            return `
            <div class="comment">
                <div class="comment-avatar ${roleClass}" title="${roleTag}">${initial}</div>
                <div class="comment-bubble">
                    <div class="comment-header">
                        <div class="comment-author-wrap">
                            <span class="comment-author">${escapeHTML(displayName)}</span>
                            <span class="comment-role-tag ${tagClass}">${roleTag}</span>
                        </div>
                        <span class="comment-date">${formatDate(c.createdAt)}</span>
                    </div>
                    <div class="comment-text">${escapeHTML(c.content)}</div>
                </div>
            </div>`;
        }).join('');
    } catch (error) {
        console.error("No se pudieron cargar comentarios", error);
    }
}

const formComment = document.getElementById('form-comment');
if (formComment) {
    formComment.addEventListener('submit', async (e) => {
        e.preventDefault();
        const input = document.getElementById('comment-text');
        const btn = document.getElementById('btn-submit-comment');
        if (!input || !btn || !input.value.trim()) return;
        try {
            btn.disabled = true;
            btn.textContent = 'Enviando...';
            await API.addComment({
                content: input.value.trim(),
                ticketId: parseInt(ticketId),
                authorId: user.id
            });
            input.value = '';
            loadComments();
            showToast('Comentario añadido', 'success');
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Enviar';
        }
    });
}

// --- Historial ---
/**
 * Loads ticket audit timeline.
 */
async function loadHistory() {
    try {
        const history = await API.getHistory(ticketId);
        const container = document.getElementById('timeline-container');
        if (!container) return;
        
        if (!history || history.length === 0) {
            container.innerHTML = '<p class="text-muted" style="padding: 1rem 0;">Sin registros de cambios aún.</p>';
            return;
        }

        container.innerHTML = history.map(h => {
            let actionText = '';
            if (!h.previousStatus) {
                actionText = `Ticket creado en estado ${getStateBadge(h.newStatus)}`;
            } else if (h.newStatus === 'ASSIGNED') {
                actionText = `Técnico asignado. Estado cambió a ${getStateBadge(h.newStatus)}`;
            } else {
                actionText = `Estado actualizado a ${getStateBadge(h.newStatus)}`;
            }

            return `
            <div class="timeline-item">
                <div class="timeline-date">${formatDate(h.changedAt)} &bull; ${escapeHTML(h.changedByEmail || 'Sistema')}</div>
                <div class="timeline-content">${actionText}</div>
            </div>`;
        }).join('');
    } catch (error) {
        console.error("No se pudo cargar el historial", error);
        const container = document.getElementById('timeline-container');
        if (container) {
            container.innerHTML = '<p class="text-muted">No tienes permiso para ver el historial o hubo un error.</p>';
        }
    }
}

// Start
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadTicketData);
} else {
    loadTicketData();
}