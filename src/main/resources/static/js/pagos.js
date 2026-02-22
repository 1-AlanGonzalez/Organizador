/**
 * Script pensado para:
 * Filtrado de estado (pagado/pendiente).
 * Buscador de la tabla de pagos.
 * Tom Select en el selector de clientes (ingresos-nuevo).
 */

function cargarDeudas() {
    const clienteId = document.getElementById("clienteSelect").value;

    fetch(`/ingresos/deudas?clienteId=${clienteId}`)
        .then(response => response.json())
        .then(data => {
            let html = "";

            if (data.length === 0) {
                html = `<div class="alert alert-success">
                            Este cliente no tiene deudas pendientes.
                        </div>`;
            } else {
                html += `
                <div class="card p-3 shadow-sm">
                    <h6 class="fw-bold mb-3">Deudas pendientes</h6>
                `;

                data.forEach(d => {
                    html += `
                        <div class="form-check mb-2">
                            <input class="form-check-input"
                                   type="radio"
                                   name="idActividadCliente"
                                   value="${d.idActividadCliente}"
                                   onchange="setMonto('${d.montoAdeudado}')">
                            <label class="form-check-label">
                                ${d.actividad} - $${d.montoAdeudado.toFixed(2)}
                            </label>
                        </div>
                    `;
                });

                html += `</div>`;
            }

            document.getElementById("deudasContainer").innerHTML = html;
        });
}

function setMonto(monto) {
    document.getElementById("monto").value = parseFloat(monto);
}

// ── INGRESOS: filtro por estado ──────────────────────────────────────────────

function filtrarEstado(estado) {
    const filas = document.querySelectorAll('#tablaPagos tbody .fila-pago');

    filas.forEach(fila => {
        const esPagado = fila.dataset.estado === 'true';

        if (estado === 'todos') {
            fila.style.display = '';
        } else if (estado === 'pagado') {
            fila.style.display = esPagado ? '' : 'none';
        } else if (estado === 'pendiente') {
            fila.style.display = !esPagado ? '' : 'none';
        }
    });
}

// ── INGRESOS: buscador de tabla ──────────────────────────────────────────────
// Guard: solo corre si la tabla existe en esta página

const buscadorTabla = document.getElementById('buscadorTabla');
if (buscadorTabla) {
    buscadorTabla.addEventListener('input', function () {
        const textoBuscado = this.value.toLowerCase().trim();
        const filas = document.querySelectorAll('#tablaPagos tbody .fila-pago');

        filas.forEach(fila => {
            const columnaCliente = fila.querySelector('td.ps-4');
            const nombreCompleto = columnaCliente.innerText.toLowerCase();
            fila.style.display = nombreCompleto.includes(textoBuscado) ? '' : 'none';
        });
    });
}

// ── TOM SELECT: buscador de clientes ────────────────────────────────────────
// Guard: solo corre en ingresos-nuevo, donde existe #clienteSelect

const clienteSelect = document.getElementById('clienteSelect');
if (clienteSelect) {
    new TomSelect('#clienteSelect', {
        placeholder: 'Buscar cliente...',
        allowEmptyOption: true,
        onChange: function () {
            cargarDeudas();
        }
    });
}