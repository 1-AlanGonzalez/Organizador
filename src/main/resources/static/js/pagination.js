/**
 * 
 * Separa "oculto por búsqueda" de "oculto por página" usando data-pag-hidden
 */
class Paginator {
    constructor({ tableId, rowsPerPage = 10, containerId }) {
        this.table       = document.getElementById(tableId);
        this.container   = document.getElementById(containerId);
        this.rowsPerPage = rowsPerPage;
        this.currentPage = 1;

        if (!this.table || !this.container) {
            console.warn(`Paginator: no se encontró #${tableId} o #${containerId}`);
            return;
        }

        this.render();
    }

    // Todas las filas de datos (excluye vacías/mensajes)
    get allRows() {
        return [...this.table.querySelectorAll('tbody tr:not(.empty-row):not(#mensajeVacioJS)')];
    }

    // Filas activas según la búsqueda actual (no marcadas como search-hidden)
    get activeRows() {
        return this.allRows.filter(r => r.dataset.searchHidden !== 'true');
    }

    get totalPages() {
        return Math.max(1, Math.ceil(this.activeRows.length / this.rowsPerPage));
    }

    goTo(page) {
        this.currentPage = Math.min(Math.max(1, page), this.totalPages);
        this.applyPage();
        this.renderControls();
    }

    applyPage() {
        const active = this.activeRows;
        const start  = (this.currentPage - 1) * this.rowsPerPage;
        const end    = start + this.rowsPerPage;

        // Ocultar TODAS las filas primero
        this.allRows.forEach(r => r.style.display = 'none');

        // Mostrar solo las de la página actual (entre las activas)
        active.forEach((r, i) => {
            r.style.display = (i >= start && i < end) ? '' : 'none';
        });
    }

    // Llamar desde el input de búsqueda ANTES de refresh()
    // Marca las filas que no coinciden con la búsqueda
    filter(query) {
        const q = query.trim().toLowerCase();
        this.allRows.forEach(r => {
            const match = !q || r.textContent.toLowerCase().includes(q);
            r.dataset.searchHidden = match ? 'false' : 'true';
        });
        this.refresh();
    }

    // Recalcula desde página 1 (después de filtrar)
    refresh() {
        this.currentPage = 1;
        this.applyPage();
        this.renderControls();
    }

    renderControls() {
        const total  = this.totalPages;
        const cur    = this.currentPage;
        const count  = this.activeRows.length;
        const from   = count === 0 ? 0 : (cur - 1) * this.rowsPerPage + 1;
        const to     = Math.min(cur * this.rowsPerPage, count);

        // Ventana de páginas: máximo 5 botones
        let start = Math.max(1, cur - 2);
        let end   = Math.min(total, start + 4);
        if (end - start < 4) start = Math.max(1, end - 4);

        const pages = [];
        for (let i = start; i <= end; i++) pages.push(i);

        const tid = this.table.id;

        this.container.innerHTML = `
            <div class="d-flex align-items-center justify-content-between px-1 py-2">
                <span class="text-muted small">
                    Mostrando <strong>${from}–${to}</strong> de <strong>${count}</strong>
                </span>
                <nav aria-label="Paginación">
                    <ul class="pagination pagination-sm mb-0 gap-1">
                        <li class="page-item ${cur === 1 ? 'disabled' : ''}">
                            <button class="page-link border-0 rounded-2 px-2"
                                    onclick="window._paginators['${tid}'].goTo(${cur - 1})"
                                    ${cur === 1 ? 'disabled' : ''}>‹</button>
                        </li>
                        ${pages.map(p => `
                            <li class="page-item">
                                <button class="page-link border-0 rounded-2 px-3 ${p === cur ? 'page-active' : ''}"
                                        onclick="window._paginators['${tid}'].goTo(${p})">${p}</button>
                            </li>
                        `).join('')}
                        <li class="page-item ${cur === total ? 'disabled' : ''}">
                            <button class="page-link border-0 rounded-2 px-2"
                                    onclick="window._paginators['${tid}'].goTo(${cur + 1})"
                                    ${cur === total ? 'disabled' : ''}>›</button>
                        </li>
                    </ul>
                </nav>
            </div>
        `;
    }

    render() {
        this.applyPage();
        this.renderControls();
    }
}

window._paginators = window._paginators || {};

function initPaginator(tableId, containerId, rowsPerPage = 10) {
    const p = new Paginator({ tableId, containerId, rowsPerPage });
    window._paginators[tableId] = p;
    return p;
}