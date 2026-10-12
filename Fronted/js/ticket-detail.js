import { API } from './api.js';
import { hasRole, CONFIG, getCurrentUser } from './auth.js';
import { showToast, setupModal, formatDate, getStateBadge, getPriorityBadge, escapeHTML } from './ui.js';

setupModal();

if (hasRole(CONFIG.ROLES.ADMIN)) {
    document.getElementById('admin-menu').classList.remove('hidden');
}

const urlParams = new URLSearchParams(window.location.search);
const ticketId = urlParams.get('id');
const user = getCurrentUser();
let currentTicket = null;

if (!ticketId) {
    window.location.href = 'tickets.html';
}

async function loadTicketData() {
    try {
        currentTicket = await API.getTicketById(ticketId);
        renderTicket(currentTicket);
        loadComments();
        loadHistory();
        
        document.getElementById('loading-indicator').classList.add('hidden');
        document.getElementById('ticket-content').classList.remove('hidden');
    } catch (error) {
        showToast(error.message, 'error');
        setTimeout(() => window.location.href = 'tickets.html', 2000);
    }
}

function renderTicket(t) {
    document.getElementById('header-ticket-id').textContent = `Ticket #${t.id}`;
    document.getElementById('ticket-title').textContent = t.title;
    document.getElementById('ticket-desc').textContent = t.description;
    
    document.getElementById('ticket-priority-badge').innerHTML = getPriorityBadge(t.priority);
    const catLabels = { 'HARDWARE':'Hardware', 'SOFTWARE':'Software', 'NETWORK':'Redes', 'ACCESS':'Accesos', 'OTHER':'Otros' };
    document.getElementById('ticket-category-badge').textContent = catLabels[t.category] || t.category;

    document.getElementById('ticket-requester').textContent = t.requesterName || 'N/A';
    document.getElementById('ticket-requester-email').textContent = t.requesterEmail || '';
    document.getElementById('ticket-technician').textContent = t.technicianName || 'Sin asignar';
    document.getElementById('ticket-created').textContent = formatDate(t.createDate);

    if (t.resolutionDate) {
        document.getElementById('group-resolved').classList.remove('hidden');
        document.getElementById('ticket-resolved').textContent = formatDate(t.resolutionDate);
    }
    if (t.closeDate) {
        document.getElementById('group-closed').classList.remove('hidden');
        document.getElementById('ticket-closed').textContent = formatDate(t.closeDate);
    }

    updateStepper(t.state);
    renderActions(t);
}

function updateStepper(currentState) {
    const states = ['OPENED', 'ASSIGNED', 'IN_PROCESS', 'RESOLVED', 'CLOSED'];
    const currentIndex = states.indexOf(currentState);
    
    states.forEach((s, idx) => {
        const el = document.getElementById(`step-${s}`);
        el.className = 'step';
        if (idx < currentIndex) el.classList.add('completed');
        if (idx === currentIndex) el.classList.add('active');
    });
}

function renderActions(t) {
    const container = document.getElementById('actions-container');
    const commentForm = document.getElementById('form-comment');
    container.innerHTML = '';
    
    const isAdmin = hasRole(CONFIG.ROLES.ADMIN);
    const isTech = hasRole(CONFIG.ROLES.TECHNICIAN);
    const isUser = hasRole(CONFIG.ROLES.USER);
    
    const isMyTicket = t.requesterId === user.id;
    const isMyAssigned = t.technicianId === user.id;

    // Bloquear comentarios si está cerrado
    if (t.state === 'CLOSED') {
        commentForm.classList.add('hidden');
    } else {
        commentForm.classList.remove('hidden');
    }

    // ADMIN: Asignar / Reasignar
    if (isAdmin && t.state !== 'CLOSED' && t.state !== 'RESOLVED') {
        const btnAsignar = document.createElement('button');
        btnAsignar.className = 'btn btn-secondary';
        btnAsignar.innerHTML = t.technicianId ? 'Reasignar Técnico' : 'Asignar Técnico';
        btnAsignar.onclick = openAssignModal;
        container.appendChild(btnAsignar);
    }

    // TECH: Cambiar estados (ASIGNADA -> EN_PROCESO -> RESUELTA)
    if (isTech && isMyAssigned) {
        if (t.state === 'ASSIGNED') {
            const btn = document.createElement('button');
            btn.className = 'btn btn-primary';
            btn.textContent = 'Iniciar Trabajo (En Proceso)';
            btn.onclick = () => changeStatus('IN_PROCESS');
            container.appendChild(btn);
        }
        if (t.state === 'IN_PROCESS') {
            const btn = document.createElement('button');
            btn.className = 'btn btn-primary';
            btn.style.background = 'var(--color-resolved)';
            btn.textContent = 'Marcar como Resuelta';
            btn.onclick = () => changeStatus('RESOLVED');
            container.appendChild(btn);
        }
    }

    // USER: Confirmar cierre
    if (isUser && isMyTicket && t.state === 'RESOLVED') {
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary';
        btn.style.background = 'var(--color-closed)';
        btn.textContent = 'Confirmar Cierre';
        btn.onclick = () => changeStatus('CLOSED');
        container.appendChild(btn);
    }
}

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
document.getElementById('btn-close-assign').onclick = () => modalAssign.classList.remove('active');
document.getElementById('btn-cancel-assign').onclick = () => modalAssign.classList.remove('active');

async function openAssignModal() {
    modalAssign.classList.add('active');
    const select = document.getElementById('select-technician');
    select.innerHTML = '<option>Cargando...</option>';
    try {
        const techs = await API.getTechnicians();
        if (techs.length === 0) {
            select.innerHTML = '<option value="">No hay técnicos disponibles</option>';
            return;
        }
        select.innerHTML = techs.map(t => 
            `<option value="${t.id}">${t.firstName} ${t.lastName} (${t.email})</option>`
        ).join('');
    } catch (error) {
        select.innerHTML = '<option value="">Error al cargar técnicos</option>';
    }
}

document.getElementById('btn-confirm-assign').onclick = async () => {
    const techId = document.getElementById('select-technician').value;
    if (!techId) return showToast('Seleccione un técnico', 'error');
    try {
        await API.assignTechnician(ticketId, techId);
        showToast('Técnico asignado', 'success');
        modalAssign.classList.remove('active');
        loadTicketData();
    } catch (error) {
        showToast(error.message, 'error');
    }
};

// --- Comentarios ---
async function loadComments() {
    try {
        const comments = await API.getComments(ticketId);
        const container = document.getElementById('comments-container');
        if (comments.length === 0) {
            container.innerHTML = '<p class="text-muted" style="text-align:center;">No hay comentarios aún.</p>';
            return;
        }
        
        container.innerHTML = comments.map(c => {
            const initial = "U";
            return `
            <div class="comment">
                <div class="comment-avatar">${initial}</div>
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-author">Usuario ID: ${c.authorId}</span>
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

document.getElementById('form-comment').addEventListener('submit', async (e) => {
    e.preventDefault();
    const input = document.getElementById('comment-text');
    const btn = document.getElementById('btn-submit-comment');
    try {
        btn.disabled = true;
        await API.addComment({
            content: input.value,
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
    }
});

// --- Historial ---
async function loadHistory() {
    try {
        const history = await API.getHistory(ticketId);
        const container = document.getElementById('timeline-container');
        
        if (!history || history.length === 0) {
            container.innerHTML = '<p class="text-muted">Sin historial</p>';
            return;
        }

        container.innerHTML = history.map(h => {
            let actionText = '';
            if (!h.previousStatus) {
                actionText = `Ticket Creado en estado ${getStateBadge(h.newStatus)}`;
            } else if (h.newStatus === 'ASSIGNED') {
                actionText = `Técnico Asignado. Estado pasó a ${getStateBadge(h.newStatus)}`;
            } else {
                actionText = `Estado cambiado a ${getStateBadge(h.newStatus)}`;
            }

            return `
            <div class="timeline-item">
                <div class="timeline-date">${formatDate(h.changedAt)} - Por: ${escapeHTML(h.changedByEmail)}</div>
                <div class="timeline-content">${actionText}</div>
            </div>`;
        }).join('');
    } catch (error) {
        console.error("No se pudo cargar el historial", error);
        document.getElementById('timeline-container').innerHTML = '<p class="text-muted">No tienes permiso para ver el historial o hubo un error.</p>';
    }
}

// Start
document.addEventListener('DOMContentLoaded', loadTicketData);
