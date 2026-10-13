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

async function loadDashboard() {
    try {
        let stats = { totalTickets: 0, opened: 0, assigned: 0, inProcess: 0, resolved: 0, closed: 0 };

        if (hasRole(CONFIG.ROLES.ADMIN)) {
            stats = await API.getSummary();
        } else {
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

        // Update UI
        const statTotal = document.getElementById('stat-total');
        const statOpened = document.getElementById('stat-opened');
        const statInProcess = document.getElementById('stat-inprocess');
        const statResolved = document.getElementById('stat-resolved');

        if (statTotal) statTotal.textContent = stats.totalTickets ?? 0;
        if (statOpened) statOpened.textContent = stats.opened ?? 0;
        if (statInProcess) statInProcess.textContent = (stats.inProcess ?? 0) + (stats.assigned ?? 0);
        if (statResolved) statResolved.textContent = stats.resolved ?? 0;

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
        { label: 'Proceso', value: stats.inProcess || 0, color: 'var(--color-in_process)' },
        { label: 'Resuelta', value: stats.resolved || 0, color: 'var(--color-resolved)' },
        { label: 'Cerrada', value: stats.closed || 0, color: 'var(--color-closed)' }
    ];

    chart.innerHTML = bars.map(b => {
        const height = (b.value / max) * 200; // max 200px height
        return `
            <div class="bar-wrap">
                <span style="font-size: 0.8rem; font-weight: 600; margin-bottom: 4px;">${b.value}</span>
                <div class="bar" style="height: ${height}px; background: ${b.color};"></div>
                <span class="bar-label">${b.label}</span>
            </div>
        `;
    }).join('');
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadDashboard);
} else {
    loadDashboard();
}
