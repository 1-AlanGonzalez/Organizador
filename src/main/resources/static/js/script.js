// Panel de cliente:
function abrirPanelCliente() {
    document.getElementById("panelOverlay").classList.remove("d-none");
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
function toggleModoEliminar() {
    document.body.classList.toggle('modo-eliminar-activo');
}
// Confirmar eliminación:
// Muestra un cuadro de confirmación al eliminar un cliente
function confirmarEliminacion(nombre, apellido) {
    return confirm(`¿Seguro que querés eliminar a ${nombre} ${apellido}?`);
}


// Errores de inscripción a actividades
function errorActividadYaRegistrada(actividad) {
    
}