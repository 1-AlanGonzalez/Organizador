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