function abrirPanelCliente() {
    document.getElementById("panelOverlay").classList.remove("d-none");
}

function cerrarPanelCliente() {
    document.getElementById("panelOverlay").classList.add("d-none");
}

// Cerrar panel con el Escape 
document.addEventListener("keydown", function(e) {
    if (e.key === "Escape") cerrarPanelCliente();
});

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