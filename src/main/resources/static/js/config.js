// ── TICKET PREVIEW ────────────────────────────────────────────────────────────
function updatePreview() {
    const pn = document.getElementById('previewNombre');
    const pm = document.getElementById('previewMensaje');
    if (pn) pn.innerText = document.getElementById('inputNombreGym')?.value  || 'MI GIMNASIO';
    if (pm) pm.innerText = document.getElementById('inputMensaje')?.value    || 'Gracias por su visita';
}

// ── SIMULACIÓN RECARGO ─────────────────────────────────────────────────────────
const ars = new Intl.NumberFormat('es-AR', {
    style: 'currency', currency: 'ARS', maximumFractionDigits: 0
});

function actualizarSimulacion(input) {
    const pct   = parseFloat(input.value) || 0;
    const label = input.closest('.metodo-item')?.querySelector('.simulacion-label');
    if (label) label.innerText = ars.format(10000 + 10000 * pct / 100);
}

// ── AGREGAR NUEVO MÉTODO ───────────────────────────────────────────────────────
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

    const recargo   = parseFloat(recargoInput.value) || 0;

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

// ── CSRF ───────────────────────────────────────────────────────────────────────
// Lee el token que Thymeleaf inyectó en el HTML con:
//   <meta name="_csrf"        th:content="${_csrf.token}"/>
//   <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
function getCsrfHeaders() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return (token && header) ? { [header]: token } : {};
}

// ── DESACTIVAR MÉTODO EXISTENTE ────────────────────────────────────────────────
let pendienteBtn = null;

function confirmarEliminar(btn) {
    pendienteBtn = btn;
    new bootstrap.Modal(document.getElementById('modalEliminar')).show();
}

document.addEventListener('DOMContentLoaded', () => {

    document.querySelectorAll('.metodo-item input[type="number"]')
            .forEach(actualizarSimulacion);

    document.getElementById('btnConfirmarEliminar')
        ?.addEventListener('click', () => {
            if (!pendienteBtn) return;

            const idMetodo = pendienteBtn.dataset.id;
            const row      = pendienteBtn.closest('.metodo-item');

            fetch(`/configuracion/metodos/${idMetodo}`, {
                method: 'DELETE',
                headers: getCsrfHeaders()   // ← token CSRF incluido aquí
            })
            .then(res => {
                if (res.ok) {
                    row.remove();
                    bootstrap.Modal.getInstance(
                        document.getElementById('modalEliminar')
                    ).hide();
                } else {
                    alert('Error ' + res.status + ': No se pudo desactivar el método.');
                }
            })
            .catch(() => alert('Error de conexión.'));
        });
});

// ── UTILS ──────────────────────────────────────────────────────────────────────
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