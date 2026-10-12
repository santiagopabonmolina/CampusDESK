import { API } from './api.js';
import { hasRole, CONFIG } from './auth.js';
import { showToast, setupModal } from './ui.js';

setupModal();

// Show admin menu if admin
if (hasRole(CONFIG.ROLES.ADMIN)) {
    document.getElementById('admin-menu').classList.remove('hidden');
}

async function loadDashboard() {
    try {
        let stats = { totalTickets: 0, opened: 0, assigned: 0, inProcess: 0, resolved: 0, closed: 0 };

        if (hasRole(CONFIG.ROLES.ADMIN)) {
            stats = await API.getSummary();
        } else {
            const tickets = await API.getTickets();
            stats.totalTickets = tickets.length;
            tickets.forEach(t => {
                if (t.state === 'OPENED') stats.opened++;
                if (t.state === 'ASSIGNED') stats.assigned++;
                if (t.state === 'IN_PROCESS') stats.inProcess++;
                if (t.state === 'RESOLVED') stats.resolved++;
                if (t.state === 'CLOSED') stats.closed++;
            });
        }

        // Update UI
        document.getElementById('stat-total').textContent = stats.totalTickets;
        document.getElementById('stat-opened').textContent = stats.opened;
        document.getElementById('stat-inprocess').textContent = stats.inProcess + stats.assigned; // Group for simplicity
        document.getElementById('stat-resolved').textContent = stats.resolved;

        renderChart(stats);

        document.getElementById('loading-indicator').classList.add('hidden');
        document.getElementById('dashboard-content').classList.remove('hidden');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function renderChart(stats) {
    const chart = document.getElementById('states-chart');
    const max = Math.max(stats.opened, stats.assigned, stats.inProcess, stats.resolved, stats.closed, 1);
    
    const bars = [
        { label: 'Abierta', value: stats.opened, color: 'var(--color-opened)' },
        { label: 'Asignada', value: stats.assigned, color: 'var(--color-assigned)' },
        { label: 'Proceso', value: stats.inProcess, color: 'var(--color-in_process)' },
        { label: 'Resuelta', value: stats.resolved, color: 'var(--color-resolved)' },
        { label: 'Cerrada', value: stats.closed, color: 'var(--color-closed)' }
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

document.addEventListener('DOMContentLoaded', loadDashboard);
