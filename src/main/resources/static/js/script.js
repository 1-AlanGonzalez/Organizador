// Panel de cliente:
function abrirPanelCliente() {
    const overlay = document.getElementById("panelOverlay");
    const form = overlay.querySelector("form");
    if (!form) return;

    overlay.classList.remove("d-none");

    // Limpiar campos
    form.reset();

    // Cambiar título
    const titulo = document.getElementById("panelClienteTitulo");
    if (titulo) titulo.textContent = "Nuevo Cliente";

    // Cambiar texto del botón submit
    const submitBtn = form.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.textContent = "Guardar Cliente";
}

function cerrarPanelCliente() {
    document.getElementById("panelOverlay").classList.add("d-none");
}

// Cerrar panel de cliente con el Escape 
document.addEventListener("keydown", function(e) {
    if (e.key === "Escape") cerrarPanelCliente();
});
// Panel de asistencia:
function abrirPanelAsistencia() {
    document.getElementById("panelOverlayAsistencia").classList.remove("d-none");
}

function cerrarPanelAsistencia() {
    document.getElementById("panelOverlayAsistencia").classList.add("d-none");
}

// Cerrar panel de asistencia con el Escape 
document.addEventListener("keydown", function(e) {
    if (e.key === "Escape") cerrarPanelAsistencia();
});
    
// Panel de actividad:
function abrirPanelActividad() {
    document.getElementById("panelOverlayActividad").classList.remove("d-none");
}

function cerrarPanelActividad() {
    document.getElementById("panelOverlayActividad").classList.add("d-none");
}
// Panel de instructor
function abrirPanelInstructor() {
    document.getElementById("panelOverlayInstructor").classList.remove("d-none");
}
function cerrarPanelInstructor() {
    document.getElementById("panelOverlayInstructor").classList.add("d-none");
}

// Filtro de clientes

// Dentro de "filtroClientes", al escribir algo en el Input escucho
// el evento "keyup" (cuando se suelta la tecla)
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
function abrirPanelEliminar(btn) {
    const id = btn.dataset.id;
    const nombre = btn.dataset.nombre;
    const url = btn.dataset.url;
    const nombreEliminar = document.getElementById("nombreEliminar");
    if (nombreEliminar) {
        nombreEliminar.innerText = nombre;
    }

    const form = document.getElementById("formEliminar");
    if (form) {
        form.action = `${url}/${id}`;
    }

    const panel = document.getElementById("panelEliminarOverlay");
    if (panel) {
        panel.classList.remove("d-none");
    }
}

// Función para cerrar el panel de eliminar Cliente, actividad e instructor.
function cerrarPanelEliminar() {
    // Con bootstrap añado la clase d-none para ocultar el panel
    document
        .getElementById("panelEliminarOverlay")
        .classList.add("d-none");
}



// Editar cliente

function abrirPanelEditarCliente(btn) {
    const overlay = document.getElementById("panelOverlay");
    const form = overlay.querySelector("form");
    if (!form) return;

    // Abrimos el panel
    overlay.classList.remove("d-none");

    // Cambiamos el título del panel
    const titulo = document.getElementById("panelClienteTitulo");
    if (titulo) titulo.textContent = "Editar Cliente";

    // Cambiamos el texto del botón submit
    const submitBtn = form.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.textContent = "Guardar Cambios";

    // Llenamos los campos con los datos del cliente
    form.querySelector("input[name='idCliente']").value = btn.dataset.id || "";
    form.querySelector("input[name='nombre']").value = btn.dataset.nombre || "";
    form.querySelector("input[name='apellido']").value = btn.dataset.apellido || "";
    form.querySelector("input[name='dni']").value = btn.dataset.dni || "";
    form.querySelector("input[name='telefono']").value = btn.dataset.telefono || "";
}
// Editar instructor

function abrirPanelEditarInstructor(btn) {
    // Abrir el overlay del panel de instructor
    // Rellenar el formulario con los datos del instructor
    const overlay = document.getElementById("panelOverlayInstructor");
    const form = overlay.querySelector("form");

    // Abrimos el panel eliminando la clase d-none
    overlay.classList.remove("d-none");

    // Cambiar el título y el texto del botón submit
    document.getElementById("panelInstructorTitulo").innerText = "Editar Instructor";
    form.querySelector("button[type='submit']").innerText = "Guardar cambios";
    // Rellenar los campos del formulario
    form.querySelector("input[name='idInstructor']").value = btn.dataset.id;
    form.querySelector("input[name='nombre']").value = btn.dataset.nombre;
    form.querySelector("input[name='apellido']").value = btn.dataset.apellido;
    form.querySelector("input[name='dni']").value = btn.dataset.dni;
    form.querySelector("input[name='telefono']").value = btn.dataset.telefono;
}
function abrirPanelEditarActividad(btn) {
    const overlay = document.getElementById("panelOverlayActividad");
    const form = overlay.querySelector("form");
    
    overlay.classList.remove("d-none");
    document.getElementById("panelActividadTitulo").innerText = "Editar Actividad";
    form.querySelector("button[type='submit']").innerText = "Guardar cambios";

    // Rellenar campos
    form.querySelector("input[name='idActividad']").value = btn.dataset.id;
    form.querySelector("input[name='nombre']").value = btn.dataset.nombre;
    form.querySelector("input[name='cupoMaximo']").value = btn.dataset.cupoMaximo;
    form.querySelector("input[name='precio']").value = btn.dataset.precio;

}

// Funciones de modo editar y eliminar
// Sidebar toggle
function toggleSidebar() {
    const sidebar = document.querySelector(".sidebar");
    const overlay = document.getElementById("sidebarOverlay");

    sidebar.classList.toggle("show");
    overlay.classList.toggle("show");
}

function closeSidebar() {
    const sidebar = document.querySelector(".sidebar");
    const overlay = document.getElementById("sidebarOverlay");

    sidebar.classList.remove("show");
    overlay.classList.remove("show");
}

function toggleModoEditar() {
    const body = document.body;

    // Si estaba activo eliminar, lo apagamos
    body.classList.remove("modo-eliminar-activo");

    // Alternamos editar
    body.classList.toggle("modo-editar");
}

function toggleModoEliminar() {
    const body = document.body;

    // Si estaba activo editar, lo apagamos
    body.classList.remove("modo-editar");

    // Alternamos eliminar
    body.classList.toggle("modo-eliminar-activo");
}

// Habilitar o deshabilitar campo fechaPago según estado de pagoAbonado

  function toggleFechaPago(pagoAbonado) {
        const fechaPago = document.getElementById('fechaPago');

        if (pagoAbonado) {
            fechaPago.disabled = false;
            fechaPago.required = true;
        } else {
            fechaPago.value = '';
            fechaPago.disabled = true;
            fechaPago.required = false;
        }
    }

 
    // PANEL CLIENTE
function calcularTotal() {
            let total = 0;
            const esMensual = document.getElementById('cobroMensual').checked;
            const checkboxes = document.querySelectorAll('.activity-checkbox:checked');

            checkboxes.forEach(chk => {
                // Selecciona el atributo data dependiendo del radio button
                let precio = esMensual ? 
                             parseFloat(chk.getAttribute('data-precio')) : 
                             parseFloat(chk.getAttribute('data-precio-diario'));
                
                if (!isNaN(precio)) {
                    total += precio;
                }
            });

            document.getElementById('totalEstimado').value = total;
            document.getElementById('totalEstimadoDisplay').innerText = total;
            
            // Opcional: calcular deuda en tiempo real si ya escribió un monto
            // verificarDeuda(); 
        }

        function pagarTotal() {
            let total = document.getElementById('totalEstimado').value;
            document.getElementById('montoAbonado').value = total;
        }

        // Ejecutar al cargar por si es edición y ya tiene checkboxes marcados
        document.addEventListener("DOMContentLoaded", function() {
            calcularTotal();
        });
// 


// NUEVO PANEL CLIENTE
function actualizarModoPlan() {
    const radioSeleccionado = document.querySelector('input[name="tipoDeCobro"]:checked');
    if (!radioSeleccionado) return; // Protección por si no hay selección

    const modo = radioSeleccionado.value;
    const lblPlan = document.getElementById('lblPlanSeleccionado');
    
    // Obtenemos todos los checkboxes de actividades
    const inputs = document.querySelectorAll('.activity-checkbox');

    inputs.forEach((input) => {
        const idActividad = input.value; // El value es el ID
        const badge = document.getElementById('badge_price_' + idActividad);
        
        // Obtenemos precios de los atributos data-
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

    // Recalcular el total monetario con los nuevos precios
    calcularTotal();
}
function procesarSeleccion(checkbox) {
    const idActividad = checkbox.value;
    const dateInput = document.getElementById('date_' + idActividad);
    
    // Habilitar input de fecha si está marcado, deshabilitar si no
    if (dateInput) {
        dateInput.disabled = !checkbox.checked;
        
        // Opcional: Si se marca, poner fecha de hoy por defecto si está vacía
        if (checkbox.checked && !dateInput.value) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }
    }

    calcularTotal();
}

// 3. Calcula la suma total basándose en los checkboxes marcados y el modo actual
function calcularTotal() {
    let total = 0;
    const radioSeleccionado = document.querySelector('input[name="tipoDeCobro"]:checked');
    const modo = radioSeleccionado ? radioSeleccionado.value : 'MENSUAL';
    
    // Solo sumamos los checkboxes que estén CHECKED
    const checkboxes = document.querySelectorAll('.activity-checkbox:checked');

    checkboxes.forEach(cb => {
        let precio = 0;
        if (modo === 'MENSUAL') {
            precio = parseFloat(cb.getAttribute('data-precio-mensual')) || 0;
        } else if (modo === 'DIARIO') {
            precio = parseFloat(cb.getAttribute('data-precio-diario')) || 0;
        } else {
            precio = 0; // Libre
        }
        total += precio;
    });

    // Actualizar vista (Input oculto y Texto visible)
    const inputTotal = document.getElementById('totalEstimado');
    const displayTotal = document.getElementById('totalEstimadoDisplay');

    if (inputTotal) inputTotal.value = total;
    if (displayTotal) displayTotal.innerText = total.toLocaleString('es-AR'); 
    // Usamos toLocaleString para que ponga puntos de mil si es necesario
}

// 4. Botón "Copiar Total" al input de pago
function pagarTotal() {
    const total = document.getElementById('totalEstimado').value;
    const inputAbonado = document.getElementById('montoAbonado');
    if (inputAbonado) inputAbonado.value = total;
}

// 5. Inicialización al cargar la página (Importante para Edición)
document.addEventListener("DOMContentLoaded", function() {
    
    // 1. Configurar precios visuales iniciales
    actualizarModoPlan(); 

    // 2. Verificar qué checkboxes vienen marcados (Edición) y habilitar sus fechas
    const checkboxes = document.querySelectorAll('.activity-checkbox');
    checkboxes.forEach(cb => {
        const dateInput = document.getElementById('date_' + cb.value);
        if (dateInput) {
            // Si está marcado, habilita fecha. Si no, deshabilita.
            dateInput.disabled = !cb.checked;
        }
    });

    // 3. Calcular total inicial
    calcularTotal();
});

// REGISTRAR PAGO EN PANEL CLIENTE
function togglePago() {
    const activo = document.getElementById("registrarPagoCheck").checked;
    document.querySelector("#bloquePago").classList.toggle("d-none", !activo);

    document.querySelector("select[name='metodoPago']").disabled = !activo;
    document.querySelector("textarea[name='observacionPago']").disabled = !activo;
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