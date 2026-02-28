// ── TICKET PREVIEW ────────────────────────────────────────────────────────────
function updatePreview() {
    const pn = document.getElementById('previewNombre');
    const pm = document.getElementById('previewMensaje');
    if (pn) pn.innerText = document.getElementById('inputNombreGym')?.value || 'MI GIMNASIO';
    if (pm) pm.innerText = document.getElementById('inputMensaje')?.value   || 'Gracias por su visita';
}

// ── GUARDAR SECCIÓN ACTIVA ────────────────────────────────────────────────────
// El botón "Guardar" del sidebar envía el form del tab visible en ese momento.

function guardarSeccionActiva() {
    const tabActivo = document.querySelector('#configTabs .nav-link.active');
    const target    = tabActivo?.dataset.bsTarget; // ej: "#tab-pagos"

    const mapaForms = {
        '#tab-tickets': 'form-tickets',
        '#tab-pagos':   'form-pagos',
    };

    const formId = mapaForms[target];
    if (formId) {
        document.getElementById(formId)?.submit();
    }
}

// ── RESTAURAR TAB ACTIVO TRAS REDIRECT ────────────────────────────────────────
// El controller manda tabActivo como flash attribute.
// Lo leemos desde el hidden input que Thymeleaf renderiza.

document.addEventListener('DOMContentLoaded', () => {

    // Restaurar tab si el server indicó cuál era el activo
    const tabInicial = document.getElementById('tabActivoInicial')?.value;
    if (tabInicial && tabInicial !== 'tickets') {
        const btn = document.querySelector(`[data-bs-target="#tab-${tabInicial}"]`);
        if (btn) btn.click();
    }

    // Inicializar simulaciones de recargo en métodos ya cargados
    document.querySelectorAll('.metodo-item input[type="number"]')
            .forEach(actualizarSimulacion);

    // ── Confirmar desactivar ──────────────────────────────────────────────────
    document.getElementById('btnConfirmarDesactivar')
        ?.addEventListener('click', () => {
            if (!pendienteBtn) return;
            const idMetodo = pendienteBtn.dataset.id;
            const row      = pendienteBtn.closest('.metodo-item');
            const nombre   = row.querySelector('span:not(.badge)')?.textContent?.trim() || 'Método';

            fetch(`/configuracion/metodos/${idMetodo}`, {
                method: 'DELETE',
                headers: getCsrfHeaders()
            })
            .then(res => {
                if (res.ok) {
                    bootstrap.Modal.getInstance(document.getElementById('modalDesactivar')).hide();
                    row.remove();
                    agregarFilaInactiva(idMetodo, nombre);
                } else {
                    alert('Error ' + res.status + ': No se pudo desactivar el método.');
                }
            })
            .catch(() => alert('Error de conexión.'));
        });

    // ── Confirmar eliminar permanente ─────────────────────────────────────────
    document.getElementById('btnConfirmarEliminarPermanente')
        ?.addEventListener('click', () => {
            if (!idMetodoAEliminar) return;

            fetch(`/configuracion/metodos/${idMetodoAEliminar}/permanente`, {
                method: 'DELETE',
                headers: getCsrfHeaders()
            })
            .then(res => {
                if (res.ok) {
                    bootstrap.Modal.getInstance(document.getElementById('modalEliminarPermanente')).hide();
                    document.getElementById(`inactivo-${idMetodoAEliminar}`)?.remove();
                    ocultarBloqueInactivosSiVacio();
                } else {
                    alert('No se pudo eliminar el método.');
                }
            })
            .catch(() => alert('Error de conexión.'));
        });
});

// ── SIMULACIÓN RECARGO ────────────────────────────────────────────────────────
const ars = new Intl.NumberFormat('es-AR', {
    style: 'currency', currency: 'ARS', maximumFractionDigits: 0
});

function actualizarSimulacion(input) {
    const pct   = parseFloat(input.value) || 0;
    const label = input.closest('.metodo-item')?.querySelector('.simulacion-label');
    if (label) label.innerText = ars.format(10000 + 10000 * pct / 100);
}

// ── AGREGAR NUEVO MÉTODO ──────────────────────────────────────────────────────
function agregarMetodo() {
    const nombreInput  = document.getElementById('nuevoMetodoNombre');
    const recargoInput = document.getElementById('nuevoMetodoRecargo');
    const errorDiv     = document.getElementById('nuevoMetodoError');
    const nombre       = nombreInput.value.trim();

    if (!nombre) {
        errorDiv.classList.remove('d-none');
        nombreInput.focus();
        return;
    }
    errorDiv.classList.add('d-none');

    const recargo  = parseFloat(recargoInput.value) || 0;
    const hNombre  = crearHidden('nuevoNombre',  nombre);
    const hRecargo = crearHidden('nuevoRecargo', recargo);
    document.getElementById('nuevosMetodosHidden').append(hNombre, hRecargo);

    document.getElementById('emptyState')?.classList.add('d-none');

    const div = document.createElement('div');
    div.className = 'metodo-item d-flex align-items-center gap-3 p-3 border rounded mb-2 bg-light';
    div.innerHTML = `
        <div class="flex-grow-1 fw-bold text-dark">
            <i class="bi bi-wallet2 me-2 text-success"></i>
            ${escapeHtml(nombre)}
            <span class="badge text-bg-success ms-2" style="font-size:.7rem;">Nuevo</span>
        </div>
        <div style="min-width:180px;">
            <label class="form-label small text-muted mb-1">Recargo (%)</label>
            <div class="input-group input-group-sm">
                <input type="number" step="0.01" min="0" max="100" value="${recargo}"
                       class="form-control text-center" oninput="actualizarSimulacion(this)">
                <span class="input-group-text">%</span>
            </div>
        </div>
        <div class="text-muted small text-center" style="min-width:120px;">
            Sobre $10.000:<br>
            <strong class="text-dark simulacion-label">${ars.format(10000 + 10000 * recargo / 100)}</strong>
        </div>
        <button type="button" class="btn btn-sm btn-outline-danger"
                onclick="eliminarNuevo(this)" title="Quitar">
            <i class="bi bi-trash"></i>
        </button>`;

    div.querySelector('input[type="number"]')
       .addEventListener('input', e => hRecargo.value = parseFloat(e.target.value) || 0);

    div._hNombre  = hNombre;
    div._hRecargo = hRecargo;

    document.getElementById('metodosContainer').appendChild(div);
    nombreInput.value  = '';
    recargoInput.value = '';
    nombreInput.focus();
}

function eliminarNuevo(btn) {
    const row = btn.closest('.metodo-item');
    row._hNombre?.remove();
    row._hRecargo?.remove();
    row.remove();
}

// ── CSRF ──────────────────────────────────────────────────────────────────────
function getCsrfHeaders() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return (token && header) ? { [header]: token } : {};
}

// ── DESACTIVAR MÉTODO ─────────────────────────────────────────────────────────
let pendienteBtn = null;

function confirmarDesactivar(btn) {
    pendienteBtn = btn;
    new bootstrap.Modal(document.getElementById('modalDesactivar')).show();
}

function confirmarEliminar(btn) { confirmarDesactivar(btn); }

// ── REACTIVAR MÉTODO ──────────────────────────────────────────────────────────
function reactivarMetodo(btn) {
    const idMetodo = btn.dataset.id;
    const fila     = document.getElementById(`inactivo-${idMetodo}`);
    const nombre   = fila.querySelector('.fw-semibold')?.textContent?.trim() || 'Método';

    btn.disabled  = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

    fetch(`/configuracion/metodos/${idMetodo}/reactivar`, {
        method: 'PATCH',
        headers: getCsrfHeaders()
    })
    .then(res => {
        if (res.ok) {
            fila.remove();
            ocultarBloqueInactivosSiVacio();
            agregarFilaActiva(idMetodo, nombre);
        } else {
            btn.disabled  = false;
            btn.innerHTML = '<i class="bi bi-arrow-counterclockwise"></i><span>Reactivar</span>';
            alert('No se pudo reactivar el método.');
        }
    })
    .catch(() => {
        btn.disabled  = false;
        btn.innerHTML = '<i class="bi bi-arrow-counterclockwise"></i><span>Reactivar</span>';
        alert('Error de conexión.');
    });
}

// ── ELIMINAR PERMANENTE ───────────────────────────────────────────────────────
let idMetodoAEliminar = null;

function confirmarEliminarPermanente(btn) {
    idMetodoAEliminar = btn.dataset.id;
    document.getElementById('modalNombreMetodo').textContent = `"${btn.dataset.nombre}"`;
    new bootstrap.Modal(document.getElementById('modalEliminarPermanente')).show();
}

// ── HELPERS DOM ───────────────────────────────────────────────────────────────

function agregarFilaInactiva(idMetodo, nombre) {
    const bloque = document.getElementById('bloqueInactivos');
    bloque.classList.remove('d-none');

    const fila = document.createElement('div');
    fila.className = 'd-flex align-items-center gap-3 p-3 border rounded mb-2';
    fila.style.background = '#fafafa';
    fila.id = `inactivo-${idMetodo}`;
    fila.innerHTML = `
        <div class="flex-grow-1 text-muted">
            <i class="bi bi-wallet2 me-2"></i>
            <span class="fw-semibold text-dark">${escapeHtml(nombre)}</span>
        </div>
        <span class="badge bg-light text-secondary border" style="font-size:0.65rem;">Inactivo</span>
        <button type="button"
                class="btn btn-sm btn-outline-success d-flex align-items-center gap-1"
                data-id="${idMetodo}" onclick="reactivarMetodo(this)">
            <i class="bi bi-arrow-counterclockwise"></i><span>Reactivar</span>
        </button>
        <button type="button"
                class="btn btn-sm btn-danger d-flex align-items-center gap-1"
                data-id="${idMetodo}" data-nombre="${escapeHtml(nombre)}"
                onclick="confirmarEliminarPermanente(this)">
            <i class="bi bi-trash-fill"></i><span>Eliminar</span>
        </button>`;

    document.getElementById('listaInactivos').appendChild(fila);
}

function agregarFilaActiva(idMetodo, nombre) {
    const fila = document.createElement('div');
    fila.className = 'metodo-item d-flex align-items-center gap-3 p-3 border rounded mb-2 bg-light';
    fila.innerHTML = `
        <div class="flex-grow-1 fw-bold text-dark">
            <i class="bi bi-wallet2 me-2 text-success"></i>
            <span>${escapeHtml(nombre)}</span>
        </div>
        <div style="min-width:180px;">
            <label class="form-label small text-muted mb-1">Recargo (%)</label>
            <div class="input-group input-group-sm">
                <input type="number" step="0.01" min="0" max="100"
                       name="configRecargo" class="form-control text-center"
                       placeholder="0.00" oninput="actualizarSimulacion(this)">
                <span class="input-group-text">%</span>
            </div>
        </div>
        <div class="text-muted small text-center" style="min-width:120px;">
            Sobre $10.000:<br>
            <strong class="text-dark simulacion-label">${ars.format(10000)}</strong>
        </div>
        <button type="button" class="btn btn-sm btn-outline-danger"
                data-id="${idMetodo}" onclick="confirmarDesactivar(this)">
            <i class="bi bi-trash"></i>
        </button>`;

    document.getElementById('metodosContainer').appendChild(fila);
}

function ocultarBloqueInactivosSiVacio() {
    const lista  = document.getElementById('listaInactivos');
    const bloque = document.getElementById('bloqueInactivos');
    if (lista?.children.length === 0) bloque.classList.add('d-none');
}

// ── UTILS ─────────────────────────────────────────────────────────────────────
function crearHidden(name, value) {
    const i = document.createElement('input');
    i.type = 'hidden'; i.name = name; i.value = value;
    return i;
}

function escapeHtml(text) {
    const d = document.createElement('div');
    d.appendChild(document.createTextNode(text));
    return d.innerHTML;
}

document.addEventListener('DOMContentLoaded', function () {
    updatePreview(); 
});