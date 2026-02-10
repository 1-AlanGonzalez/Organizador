
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
    body.classList.remove("modo-eliminar-activo");
    body.classList.toggle("modo-editar");
}

function toggleModoEliminar() {
    const body = document.body;
    body.classList.remove("modo-editar");
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