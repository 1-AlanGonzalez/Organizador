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

// Función para abrir el panel de eliminar Cliente, activida e instructor.
function abrirPanelEliminar(btn) {
    // Guardo en una constante la ID de la entidad
    // y el nombre que vienen en los data-attributes
    // la url que guardo es la base para el action del form
    const id = btn.dataset.id;
    const nombre = btn.dataset.nombre;
    const url = btn.dataset.url;

    // Imprimo en la pantalla el nombre de la entidad a eliminar
    document.getElementById("nombreEliminar").innerText = nombre;

    // Configuro el action del form de eliminación
    // El action es la url base + la id de la entidad
    const form = document.getElementById("formEliminar");
    form.action = `${url}/${id}`;

    // Abro el panel de eliminación eliminando con bootstrap la clase d-none
    document
        .getElementById("panelEliminarOverlay")
        .classList.remove("d-none");
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






