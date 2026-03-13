/* ==========================================================================
   BUSCADOR / FILTRO EN TIEMPO REAL
   ========================================================================== */

const filtroInput = document.getElementById("filtroInstructores");

if (filtroInput) {
    filtroInput.addEventListener("keyup", function () {
        const filtro = this.value.toLowerCase();
        const filas = document.querySelectorAll("#tablaInstructores tbody tr");

        filas.forEach(fila => {
            const textoFila = fila.innerText.toLowerCase();
            fila.style.display = textoFila.includes(filtro) ? "" : "none";
        });
    });
}

document.addEventListener("keydown", e => {
    if (e.key === "Escape") {
        cerrarPanelActividad();
        cerrarPanelEliminar();
    }
    // ← agregar esto:
    if (e.key === "Enter") {
        const panelEliminar = document.getElementById("panelEliminarOverlay");
        if (panelEliminar && !panelEliminar.classList.contains("d-none")) {
            e.preventDefault();
            document.getElementById("formEliminar")?.submit();
        }
    }
});