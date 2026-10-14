import { API } from './api.js';
import { CONFIG } from './config.js';
import { hasRole, isLoggedIn } from './auth.js';
import { showToast, initAppShell } from './ui.js';

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

export async function loadDashboard() {
    try {
        let stats = { totalTickets: 0, opened: 0, assigned: 0, inProcess: 0, resolved: 0, closed: 0 };

        try {
            // El endpoint /api/dashboard/stats y /api/reports/summary filtra automáticamente según el rol del usuario
            stats = await API.getSummary();
        } catch (e) {
            // Fallback directo a tickets filtrados
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

        // Actualizar UI: 5 indicadores desglosados individualmente (sin agrupar)
        const statTotal = document.getElementById('stat-total');
        const statOpened = document.getElementById('stat-opened');
        const statAssigned = document.getElementById('stat-assigned');
        const statInProcess = document.getElementById('stat-in-process');
        const statResolved = document.getElementById('stat-resolved');
        const statClosed = document.getElementById('stat-closed');

        if (statTotal) statTotal.textContent = stats.totalTickets ?? 0;
        if (statOpened) statOpened.textContent = stats.opened ?? 0;
        if (statAssigned) statAssigned.textContent = stats.assigned ?? 0;
        if (statInProcess) statInProcess.textContent = stats.inProcess ?? 0;
        if (statResolved) statResolved.textContent = stats.resolved ?? 0;
        if (statClosed) statClosed.textContent = stats.closed ?? 0;

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

function renderChart(stats) {
    const chart = document.getElementById('states-chart');
    if (!chart) return;
    const max = Math.max(stats.opened || 0, stats.assigned || 0, stats.inProcess || 0, stats.resolved || 0, stats.closed || 0, 1);
    
    const bars = [
        { label: 'Abierta', value: stats.opened || 0, color: 'var(--color-opened)' },
        { label: 'Asignada', value: stats.assigned || 0, color: 'var(--color-assigned)' },
        { label: 'En Proceso', value: stats.inProcess || 0, color: 'var(--color-in_process)' },
        { label: 'Resuelta', value: stats.resolved || 0, color: 'var(--color-resolved)' },
        { label: 'Cerrada', value: stats.closed || 0, color: 'var(--color-closed)' }
    ];

    chart.innerHTML = bars.map(b => {
        const height = Math.max((b.value / max) * 180, 8);
        return `
            <div class=\"bar-wrap\">
                <span style=\"font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: #f8fafc;\">${b.value}</span>
                <div class=\"bar\" style=\"height: ${height}px; background: ${b.color}; min-width: 36px; border-radius: 6px 6px 0 0;\"></div>
                <span class=\"bar-label\" style=\"margin-top: 6px; font-size: 0.8rem;\">${b.label}</span>
            </div>
        `;
    }).join('');
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadDashboard);
} else {
    loadDashboard();
}
