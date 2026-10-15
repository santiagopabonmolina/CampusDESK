import { API } from './api.js';
import { CONFIG } from './config.js';
import { hasRole, isLoggedIn } from './auth.js';
import { showToast, initAppShell, animateCounter } from './ui.js';

// Init sidebar & user info
initAppShell();

// Guard route
if (!isLoggedIn()) {
    window.location.href = 'index.html';
}

// Show admin menu if admin
if (hasRole(CONFIG.ROLES.ADMIN)) {
    const adminMenu = document.getElementById('admin-menu');
    if (adminMenu) adminMenu.classList.remove('hidden');
}

/**
 * Loads technician dashboard data.
 */
export async function loadDashboard() {
    try {
        let stats = { totalTickets: 0, opened: 0, assigned: 0, inProcess: 0, resolved: 0, closed: 0 };

        try {
            stats = await API.getSummary();
        } catch (e) {
            const tickets = await API.getTickets();
            if (Array.isArray(tickets)) {
                stats.totalTickets = tickets.length;
                tickets.forEach(t => {
                    if (t.state === 'OPENED') stats.opened++;
                    if (t.state === 'ASSIGNED') stats.assigned++;
                    if (t.state === 'IN_PROCESS') stats.inProcess++;
                    if (t.state === 'RESOLVED') stats.resolved++;
                    if (t.state === 'CLOSED') stats.closed++;
                });
            }
        }

        const statTotal = document.getElementById('stat-total');
        const statOpened = document.getElementById('stat-opened');
        const statAssigned = document.getElementById('stat-assigned');
        const statInProcess = document.getElementById('stat-in-process');
        const statResolved = document.getElementById('stat-resolved');
        const statClosed = document.getElementById('stat-closed');

        if (statTotal) animateCounter(statTotal, stats.totalTickets ?? 0);
        if (statOpened) animateCounter(statOpened, stats.opened ?? 0);
        if (statAssigned) animateCounter(statAssigned, stats.assigned ?? 0);
        if (statInProcess) animateCounter(statInProcess, stats.inProcess ?? 0);
        if (statResolved) animateCounter(statResolved, stats.resolved ?? 0);
        if (statClosed) animateCounter(statClosed, stats.closed ?? 0);

        renderChart(stats);

        const loading = document.getElementById('loading-indicator');
        const content = document.getElementById('dashboard-content');
        if (loading) loading.classList.add('hidden');
        if (content) content.classList.remove('hidden');
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showToast(error.message || 'Error al cargar métricas', 'error');
        const loading = document.getElementById('loading-indicator');
        if (loading) loading.classList.add('hidden');
    }
}

/**
 * Renders technician chart bars.
 */
function renderChart(stats) {
    const chart = document.getElementById('states-chart');
    if (!chart) return;
    const max = Math.max(stats.opened || 0, stats.assigned || 0, stats.inProcess || 0, stats.resolved || 0, stats.closed || 0, 1);
    
    const bars = [
        { label: 'Abierta', value: stats.opened || 0, color: 'linear-gradient(180deg, #38bdf8 0%, rgba(56, 189, 248, 0.4) 100%)', glow: 'rgba(56, 189, 248, 0.5)' },
        { label: 'Asignada', value: stats.assigned || 0, color: 'linear-gradient(180deg, #a78bfa 0%, rgba(167, 139, 250, 0.4) 100%)', glow: 'rgba(167, 139, 250, 0.5)' },
        { label: 'En Proceso', value: stats.inProcess || 0, color: 'linear-gradient(180deg, #fbbf24 0%, rgba(251, 191, 36, 0.4) 100%)', glow: 'rgba(251, 191, 36, 0.5)' },
        { label: 'Resuelta', value: stats.resolved || 0, color: 'linear-gradient(180deg, #34d399 0%, rgba(52, 211, 153, 0.4) 100%)', glow: 'rgba(52, 211, 153, 0.5)' },
        { label: 'Cerrada', value: stats.closed || 0, color: 'linear-gradient(180deg, #94a3b8 0%, rgba(148, 163, 184, 0.4) 100%)', glow: 'rgba(148, 163, 184, 0.5)' }
    ];

    chart.innerHTML = bars.map(b => {
        const targetHeight = Math.max((b.value / max) * 190, 8);
        return `
            <div class="bar-wrap">
                <span class="bar-value">${b.value}</span>
                <div class="bar" style="height: 0px; background: ${b.color}; box-shadow: 0 0 14px ${b.glow};" data-height="${targetHeight}"></div>
                <span class="bar-label">${b.label}</span>
            </div>
        `;
    }).join('');

    requestAnimationFrame(() => {
        chart.querySelectorAll('.bar').forEach(barEl => {
            const h = barEl.getAttribute('data-height');
            setTimeout(() => {
                barEl.style.height = `${h}px`;
            }, 80);
        });
    });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadDashboard);
} else {
    loadDashboard();
}