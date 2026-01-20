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

document.getElementById("filtroClientes").addEventListener("keyup", function () {
// Obtengo el valor del Input y lo paso a minúsculas
    const filtro = this.value.toLowerCase();
// Obtengo todas las filas de la tabla de clientes
    const filas = document.querySelectorAll("#tablaClientes tbody tr");
// Recorro todas las filas 
    filas.forEach(fila => {
        const textoFila = fila.innerText.toLowerCase();
// Si el texto de la fila incluye el filtro, la muestro;
//  si no, la oculto
        fila.style.display = textoFila.includes(filtro)
            ? ""
            : "none";
    });
});


// Eliminar clientes

// Alternar modo eliminar:
// Muestra u oculta la columna de acciones de eliminar
function abrirPanelEliminarCliente(btn) {   
    // Guardo en una constante la ID que guardé en el botón (data-id... y data-nombre..)
    const id = btn.dataset.id;
    const nombre = btn.dataset.nombre;

    
    document.getElementById('nombreClienteEliminar').innerText = nombre;

    const form = document.getElementById('formEliminarCliente');
    form.action = `/clientes/eliminar/${id}`;

    document
        .getElementById('panelEliminarClienteOverlay')
        .classList.remove('d-none');
}

function cerrarPanelEliminarCliente() {
    document
        .getElementById('panelEliminarClienteOverlay')
        .classList.add('d-none');
}


// Errores de inscripción a actividades
// function errorActividadYaRegistrada(actividad) {
    
// }


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


