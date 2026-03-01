// ── Panel actividad (crear/editar) ───────────────────────────────────────────

function abrirPanelActividad() {
    const overlay = document.getElementById("panelOverlayActividad");
    const form    = overlay.querySelector("form");

    // Limpiar para nueva actividad
    form.reset();
    document.getElementById("idActividad").value  = "";
    document.getElementById("panelActividadTitulo").innerText = "Nueva Actividad";
    document.getElementById("btnSubmitActividad").innerText   = "Guardar";

    overlay.classList.remove("d-none");
}

function cerrarPanelActividad() {
    document.getElementById("panelOverlayActividad").classList.add("d-none");
}

// Abre el panel y precarga los datos via fetch al endpoint JSON
async function abrirPanelEditarActividad(btn) {
    const id      = btn.dataset.id;
    const overlay = document.getElementById("panelOverlayActividad");

    overlay.classList.remove("d-none");
    document.getElementById("panelActividadTitulo").innerText = "Editar Actividad";
    document.getElementById("btnSubmitActividad").innerText   = "Guardar cambios";

    try {
        const res  = await fetch(`/actividades/editar/${id}`);
        const data = await res.json();

        // Campos con th:field (Thymeleaf genera el id igual que el nombre del campo)
        document.getElementById("idActividad").value  = data.id  ?? "";
        document.getElementById("nombre").value       = data.nombre     ?? "";
        document.getElementById("precio").value       = data.precio     ?? "";
        document.getElementById("precioDiario").value = data.precioDiario ?? "";
        document.getElementById("cupoMaximo").value   = data.cupoMaximo ?? "";

        // Campos sin th:field — usamos los id manuales del panel
        document.getElementById("editDias").value     = data.dias    ?? "";
        document.getElementById("editHorario").value  = data.horario ?? "";

        // Instructor select
        const selectInstr = document.getElementById("editInstructor");
        if (selectInstr && data.instructorId) {
            selectInstr.value = data.instructorId;
        }

    } catch (e) {
        console.error("Error cargando actividad:", e);
        alert("No se pudieron cargar los datos de la actividad.");
    }
}

// ── Panel eliminar ────────────────────────────────────────────────────────────

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

// ── Modo eliminar ─────────────────────────────────────────────────────────────

function toggleModoEliminar() {
    document.body.classList.toggle("modo-eliminar-activo");
}

// ── Búsqueda con mensaje vacío ────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("filtroActividades");
    if (!input) return;

    input.addEventListener("input", function () {
        const q      = this.value.toLowerCase();
        const filas  = document.querySelectorAll("#tablaActividades tbody tr:not(.empty-row)");
        let visibles = 0;

        filas.forEach(fila => {
            const match = fila.textContent.toLowerCase().includes(q);
            fila.style.display        = match ? "" : "none";
            fila.dataset.searchHidden = match ? "false" : "true";
            if (match) visibles++;
        });

        // Mensaje vacío JS
        const msgVacio = document.getElementById("mensajeVacioActividades");
        if (msgVacio) msgVacio.style.display = (visibles === 0 && q.length > 0) ? "" : "none";

        // Repaginar si hay paginador
        if (window._paginators && window._paginators["tablaActividades"]) {
            window._paginators["tablaActividades"].refresh();
        }
    });

    // Escape cierra paneles
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            cerrarPanelActividad();
            cerrarPanelEliminar();
        }
    });
});