/**
 * Script pensado para:
 * Filtrado de estado (pagado/pendiente).

    Buscador de la tabla de pagos.
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
                                    onchange="setMonto('${d.montoAdeudado}')"
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

// INGRESOS 

function filtrarEstado(estado) {
    const filas = document.querySelectorAll('#tablaPagos tbody .fila-pago');

    filas.forEach(fila => {
        const esPagado = fila.dataset.estado === 'true';

        if (estado === 'todos') {
            fila.style.display = '';
        } 
        else if (estado === 'pagado') {
            fila.style.display = esPagado ? '' : 'none';
        } 
        else if (estado === 'pendiente') {
            fila.style.display = !esPagado ? '' : 'none';
        }
    });
}

// buscador de pago
document.getElementById('buscadorTabla').addEventListener('input', function () {
    const textoBuscado = this.value.toLowerCase().trim();
    const filas = document.querySelectorAll('#tablaPagos tbody .fila-pago');

    filas.forEach(fila => {
        // Tomamos SOLO la columna del cliente
        const columnaCliente = fila.querySelector('td.ps-4');
        const nombreCompleto = columnaCliente.innerText.toLowerCase();

        fila.style.display = nombreCompleto.includes(textoBuscado) ? '' : 'none';
    });
});