/**
 * (Lógica de Clientes)
 todo lo relacionado con el formulario de alta/edición y los cálculos matemáticos.

    Abrir/Editar Cliente.

    Lógica de precios: calcularTotal(), actualizarModoPlan(), procesarSeleccion().

    Validaciones del formulario de cliente.

    Filtro de búsqueda de clientes.
 */

/* ==========================================================================
   GESTIÓN DE PANELES (ALTA Y EDICIÓN)
   ========================================================================== */

function abrirPanelCliente() {
    const overlay = document.getElementById("panelOverlay");
    const form = overlay.querySelector("form");
    if (!form) return;

    overlay.classList.remove("d-none");

    // Limpiar campos para un alta nueva
    form.reset();

    // Resetear visualmente los precios y fechas
    actualizarModoPlan(); 
    document.querySelectorAll('.activity-checkbox').forEach(cb => {
        procesarSeleccion(cb); // Esto deshabilitará las fechas
    });

    // Configurar textos para "Nuevo Cliente"
    const titulo = document.getElementById("panelClienteTitulo");
    if (titulo) titulo.textContent = "Nuevo Cliente";

    const submitBtn = form.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.textContent = "Guardar Cliente";
}

function abrirPanelEditarCliente(btn) {
    const overlay = document.getElementById("panelOverlay");
    const form = overlay.querySelector("form");
    if (!form) return;

    overlay.classList.remove("d-none");

    // Configurar textos para "Editar Cliente"
    const titulo = document.getElementById("panelClienteTitulo");
    if (titulo) titulo.textContent = "Editar Cliente";

    const submitBtn = form.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.textContent = "Guardar Cambios";

    // Llenar los campos básicos (Los checkbox se llenan desde Thymeleaf o al cargar, 
    // pero aquí cargamos los datos simples del dataset del botón)
    form.querySelector("input[name='idCliente']").value = btn.dataset.id || "";
    form.querySelector("input[name='nombre']").value = btn.dataset.nombre || "";
    form.querySelector("input[name='apellido']").value = btn.dataset.apellido || "";
    form.querySelector("input[name='dni']").value = btn.dataset.dni || "";
    form.querySelector("input[name='telefono']").value = btn.dataset.telefono || "";
    
    // NOTA: Si necesitas recargar los checkbox marcados dinámicamente, 
    // idealmente se hace una petición AJAX aquí o se renderizan en el HTML oculto.
    // Por ahora, asumimos que el form ya tiene los datos o se maneja aparte.
}

function cerrarPanelCliente() {
    const overlay = document.getElementById("panelOverlay");
    if(overlay) overlay.classList.add("d-none");
}

/* ==========================================================================
   LÓGICA DE PRECIOS Y PLANES (FORMULARIO)
   ========================================================================== */

function actualizarModoPlan() {
    const radioSeleccionado = document.querySelector('input[name="tipoDeCobro"]:checked');
    if (!radioSeleccionado) return; 

    const modo = radioSeleccionado.value;
    const lblPlan = document.getElementById('lblPlanSeleccionado');
    
    // Actualizar badges visuales de precio
    const inputs = document.querySelectorAll('.activity-checkbox');
    inputs.forEach((input) => {
        const idActividad = input.value;
        const badge = document.getElementById('badge_price_' + idActividad);
        
        const precioMensual = input.getAttribute('data-precio-mensual');
        const precioDiario = input.getAttribute('data-precio-diario');

        if (modo === 'MENSUAL') {
            if(lblPlan) lblPlan.textContent = "Plan Mensual Estándar";
            if(badge) {
                badge.innerHTML = `<i class="bi bi-calendar-month"></i> $${precioMensual}`;
                badge.className = "badge bg-light text-secondary border price-badge";
            }
        } else if (modo === 'DIARIO') {
            if(lblPlan) lblPlan.textContent = "Pase Diario (Un día)";
            if(badge) {
                badge.innerHTML = `<i class="bi bi-calendar-day"></i> $${precioDiario}`;
                badge.className = "badge bg-info bg-opacity-10 text-info border border-info price-badge";
            }
        } else if (modo === 'LIBRE') {
            if(lblPlan) lblPlan.textContent = "Promoción Libre (Sin cargo)";
            if(badge) {
                badge.innerHTML = `<i class="bi bi-gift"></i> $0`;
                badge.className = "badge bg-success bg-opacity-10 text-success border border-success price-badge";
            }
        }
    });

    calcularTotal();
}

function procesarSeleccion(checkbox) {
    const idActividad = checkbox.value;
    const dateInput = document.getElementById('date_' + idActividad);
    
    // Habilitar/Deshabilitar fecha de inicio según selección
    if (dateInput) {
        dateInput.disabled = !checkbox.checked;
        
        // Poner fecha de hoy si se marca y está vacía
        if (checkbox.checked && !dateInput.value) {
            dateInput.value = new Date().toISOString().split('T')[0];
        } else if (!checkbox.checked) {
            dateInput.value = ''; // Limpiar si se desmarca
        }
    }

    calcularTotal();
}

function calcularTotal() {
    let total = 0;
    const radioSeleccionado = document.querySelector('input[name="tipoDeCobro"]:checked');
    const modo = radioSeleccionado ? radioSeleccionado.value : 'MENSUAL';
    
    // Solo sumar checkboxes marcados
    const checkboxes = document.querySelectorAll('.activity-checkbox:checked');

    checkboxes.forEach(cb => {
        let precio = 0;
        if (modo === 'MENSUAL') {
            precio = parseFloat(cb.getAttribute('data-precio-mensual')) || 0;
        } else if (modo === 'DIARIO') {
            precio = parseFloat(cb.getAttribute('data-precio-diario')) || 0;
        }
        total += precio;
    });

    // Actualizar UI
    const inputTotal = document.getElementById('totalEstimado');
    const displayTotal = document.getElementById('totalEstimadoDisplay');

    if (inputTotal) inputTotal.value = total;
    if (displayTotal) displayTotal.innerText = total.toLocaleString('es-AR'); 
}

/* ==========================================================================
   LÓGICA DE PAGO EN EL ALTA (CHECKBOX "REGISTRAR PAGO")
   ========================================================================== */

function togglePago() {
    const checkPago = document.getElementById("registrarPagoCheck");
    if(!checkPago) return;

    const activo = checkPago.checked;
    const bloquePago = document.querySelector("#bloquePago");
    
    if(bloquePago) bloquePago.classList.toggle("d-none", !activo);

    const selectPago = document.querySelector("select[name='metodoPago']");
    const txtObservacion = document.querySelector("textarea[name='observacionPago']");
    const inputMonto = document.getElementById('montoAbonado');

    if(selectPago) selectPago.disabled = !activo;
    if(txtObservacion) txtObservacion.disabled = !activo;
    
    // Si se activa el pago, copiamos automáticamente el total actual
    if(activo && inputMonto) {
        pagarTotal();
    }
}

function pagarTotal() {
    const total = document.getElementById('totalEstimado').value;
    const inputAbonado = document.getElementById('montoAbonado');
    if (inputAbonado) inputAbonado.value = total;
}

/* ==========================================================================
   ELIMINACIÓN DE CLIENTES
   ========================================================================== */

function toggleModoEliminar() {
    const body = document.body;
    body.classList.remove("modo-editar"); // Apagar editar si estaba
    body.classList.toggle("modo-eliminar-activo");
}

function abrirPanelEliminar(btn) {
    const id = btn.dataset.id;
    const nombre = btn.dataset.nombre;
    const url = btn.dataset.url;
    
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
   BUSCADOR / FILTRO EN TIEMPO REAL
   ========================================================================== */

const filtroInput = document.getElementById("filtroClientes");

if (filtroInput) {
    filtroInput.addEventListener("keyup", function () {
        const filtro = this.value.toLowerCase();
        const filas = document.querySelectorAll("#tablaClientes tbody tr");

        filas.forEach(fila => {
            const textoFila = fila.innerText.toLowerCase();
            fila.style.display = textoFila.includes(filtro) ? "" : "none";
        });
    });
}

/* ==========================================================================
   INICIALIZACIÓN (DOM READY)
   ========================================================================== */

document.addEventListener("DOMContentLoaded", function() {
    
    // 1. Configuración inicial
    actualizarModoPlan(); 

    // 2. Verificar fechas
    const checkboxes = document.querySelectorAll('.activity-checkbox');
    checkboxes.forEach(cb => {
        const dateInput = document.getElementById('date_' + cb.value);
        if (dateInput) dateInput.disabled = !cb.checked;
    });

    // 3. Calcular total
    calcularTotal();

    // 4. EVENTOS DE TECLADO (ESCAPE Y ENTER)
    document.addEventListener("keydown", function(e) {
        
        // CERRAR PANELES CON ESCAPE
        if (e.key === "Escape") {
            cerrarPanelCliente();
            cerrarPanelEliminar();
        }

        // CONFIRMAR ELIMINACIÓN CON ENTER
        // Solo si el panel de eliminar está visible (no tiene la clase d-none)
        else if (e.key === "Enter") {
            const panelEliminar = document.getElementById("panelEliminarOverlay");
            
            if (panelEliminar && !panelEliminar.classList.contains("d-none")) {
                e.preventDefault(); // Evita que el Enter haga otra cosa (como salto de línea)
                const form = document.getElementById("formEliminar");
                if (form) {
                    form.submit(); // Envía el formulario de eliminación
                }
            }
        }
    });
});