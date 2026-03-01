/* ==========================================================================
   GESTIÓN DE PANELES (ALTA Y EDICIÓN)
   ========================================================================== */

function abrirPanelCliente() {
    const overlay = document.getElementById("panelOverlay");
    const form    = overlay.querySelector("form");
    if (!form) return;

    overlay.classList.remove("d-none");
    form.reset();
    actualizarModoPlan();
    document.querySelectorAll('.activity-checkbox').forEach(cb => procesarSeleccion(cb));

    const titulo = document.getElementById("panelClienteTitulo");
    if (titulo) titulo.textContent = "Nuevo Cliente";

    const submitBtn = form.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.textContent = "Guardar Cliente";
}

function abrirPanelEditarCliente(btn) {
    const overlay = document.getElementById("panelOverlay");
    const form    = overlay.querySelector("form");
    if (!form) return;

    overlay.classList.remove("d-none");

    const titulo = document.getElementById("panelClienteTitulo");
    if (titulo) titulo.textContent = "Editar Cliente";

    const submitBtn = form.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.textContent = "Guardar Cambios";

    form.querySelector("input[name='idCliente']").value  = btn.dataset.id       || "";
    form.querySelector("input[name='nombre']").value     = btn.dataset.nombre   || "";
    form.querySelector("input[name='apellido']").value   = btn.dataset.apellido || "";
    form.querySelector("input[name='dni']").value        = btn.dataset.dni      || "";
    form.querySelector("input[name='telefono']").value   = btn.dataset.telefono || "";
}

function cerrarPanelCliente() {
    const overlay = document.getElementById("panelOverlay");
    if (overlay) overlay.classList.add("d-none");
}

/* ==========================================================================
   LÓGICA DE PRECIOS Y PLANES
   ========================================================================== */

function actualizarModoPlan() {
    const radioSeleccionado = document.querySelector('input[name="tipoDeCobro"]:checked');
    if (!radioSeleccionado) return;

    const modo   = radioSeleccionado.value;
    const lblPlan = document.getElementById('lblPlanSeleccionado');

    document.querySelectorAll('.activity-checkbox').forEach(input => {
        const idActividad   = input.value;
        const badge         = document.getElementById('badge_price_' + idActividad);
        const precioMensual = input.getAttribute('data-precio-mensual');
        const precioDiario  = input.getAttribute('data-precio-diario');

        if (modo === 'MENSUAL') {
            if (lblPlan) lblPlan.textContent = "Plan Mensual Estándar";
            if (badge) { badge.innerHTML = `<i class="bi bi-calendar-month"></i> $${precioMensual}`; badge.className = "badge bg-light text-secondary border price-badge"; }
        } else if (modo === 'DIARIO') {
            if (lblPlan) lblPlan.textContent = "Pase Diario (Un día)";
            if (badge) { badge.innerHTML = `<i class="bi bi-calendar-day"></i> $${precioDiario}`; badge.className = "badge bg-info bg-opacity-10 text-info border border-info price-badge"; }
        } else if (modo === 'LIBRE') {
            if (lblPlan) lblPlan.textContent = "Promoción Libre (Sin cargo)";
            if (badge) { badge.innerHTML = `<i class="bi bi-gift"></i> $0`; badge.className = "badge bg-success bg-opacity-10 text-success border border-success price-badge"; }
        }
    });

    calcularTotal();
}

function procesarSeleccion(checkbox) {
    const dateInput = document.getElementById('date_' + checkbox.value);
    if (dateInput) {
        dateInput.disabled = !checkbox.checked;
        if (checkbox.checked && !dateInput.value) {
            dateInput.value = new Date().toISOString().split('T')[0];
        } else if (!checkbox.checked) {
            dateInput.value = '';
        }
    }
    calcularTotal();
}

function calcularTotal() {
    let total = 0;
    const radio = document.querySelector('input[name="tipoDeCobro"]:checked');
    const modo  = radio ? radio.value : 'MENSUAL';

    document.querySelectorAll('.activity-checkbox:checked').forEach(cb => {
        let precio = 0;
        if (modo === 'MENSUAL') precio = parseFloat(cb.getAttribute('data-precio-mensual')) || 0;
        else if (modo === 'DIARIO') precio = parseFloat(cb.getAttribute('data-precio-diario')) || 0;
        total += precio;
    });

    const inputTotal   = document.getElementById('totalEstimado');
    const displayTotal = document.getElementById('totalEstimadoDisplay');
    if (inputTotal)   inputTotal.value = total;
    if (displayTotal) displayTotal.innerText = total.toLocaleString('es-AR');
}

/* ==========================================================================
   PAGO EN EL ALTA
   ========================================================================== */

function togglePago() {
    const checkPago = document.getElementById("registrarPagoCheck");
    if (!checkPago) return;

    const activo     = checkPago.checked;
    const bloquePago = document.querySelector("#bloquePago");
    if (bloquePago) bloquePago.classList.toggle("d-none", !activo);

    const selectPago  = document.querySelector("select[name='metodoPago']");
    const txtObserv   = document.querySelector("textarea[name='observacionPago']");
    const inputMonto  = document.getElementById('montoAbonado');

    if (selectPago) selectPago.disabled = !activo;
    if (txtObserv)  txtObserv.disabled  = !activo;
    if (activo && inputMonto) pagarTotal();
}

function pagarTotal() {
    const total       = document.getElementById('totalEstimado').value;
    const inputAbonado = document.getElementById('montoAbonado');
    if (inputAbonado) inputAbonado.value = total;
}

/* ==========================================================================
   ELIMINAR
   ========================================================================== */

function toggleModoEliminar() {
    document.body.classList.remove("modo-editar");
    document.body.classList.toggle("modo-eliminar-activo");
}

function abrirPanelEliminar(btn) {
    const id     = btn.dataset.id;
    const nombre = btn.dataset.nombre;
    const url    = btn.dataset.url;

    const nombreEliminar = document.getElementById("nombreEliminar");
    if (nombreEliminar) nombreEliminar.innerText = nombre;

    const form = document.getElementById("formEliminar");
    if (form) form.action = `${url}/${id}`;

    const panel = document.getElementById("panelEliminarOverlay");
    if (panel) panel.classList.remove("d-none");
}

function cerrarPanelEliminar() {
    const panel = document.getElementById("panelEliminarOverlay");
    if (panel) panel.classList.add("d-none");
}

/* ==========================================================================
   FILTRO DE BÚSQUEDA + FILTRO POR ACTIVIDAD
   ========================================================================== */

function filtrarClientes() {
    const textoFiltro     = (document.getElementById("filtroClientes")?.value     || "").toLowerCase();
    const actividadFiltro = (document.getElementById("filtroActividadClientes")?.value || "todos");

    const filas    = document.querySelectorAll("#tablaClientes tbody tr:not(.empty-row)");
    let visibles   = 0;

    filas.forEach(fila => {
        const textoFila      = fila.textContent.toLowerCase();
        const actividadFila  = fila.dataset.actividades || "";   // ver nota en clientes.html

        const cumpleTexto      = !textoFiltro     || textoFila.includes(textoFiltro);
        const cumpleActividad  = actividadFiltro === "todos" || actividadFila.includes(actividadFiltro);

        const visible = cumpleTexto && cumpleActividad;
        fila.style.display        = visible ? "" : "none";
        fila.dataset.searchHidden = visible ? "false" : "true";
        if (visible) visibles++;
    });

    // Mensaje vacío JS
    const msgVacio = document.getElementById("mensajeVacioClientes");
    if (msgVacio) {
        msgVacio.style.display = (visibles === 0) ? "" : "none";
    }

    // Repaginar
    if (window._paginators?.tablaClientes) {
        window._paginators.tablaClientes.refresh();
    }
}

/* ==========================================================================
   DOM READY
   ========================================================================== */

document.addEventListener("DOMContentLoaded", function () {
    actualizarModoPlan();

    document.querySelectorAll('.activity-checkbox').forEach(cb => {
        const dateInput = document.getElementById('date_' + cb.value);
        if (dateInput) dateInput.disabled = !cb.checked;
    });

    calcularTotal();

    // Conectar filtros
    const filtroTexto     = document.getElementById("filtroClientes");
    const filtroActividad = document.getElementById("filtroActividadClientes");

    if (filtroTexto)     filtroTexto.addEventListener("input",  filtrarClientes);
    if (filtroActividad) filtroActividad.addEventListener("change", filtrarClientes);

    // Teclas
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            cerrarPanelCliente();
            cerrarPanelEliminar();
        } else if (e.key === "Enter") {
            const panelEliminar = document.getElementById("panelEliminarOverlay");
            if (panelEliminar && !panelEliminar.classList.contains("d-none")) {
                e.preventDefault();
                document.getElementById("formEliminar")?.submit();
            }
        }
    });
});