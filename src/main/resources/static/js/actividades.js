// ── Helpers ───────────────────────────────────────────────────────────────────

function setVal(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = (value !== null && value !== undefined) ? value : "";
}

// ── Panel actividad ───────────────────────────────────────────────────────────

function abrirPanelActividad() {
    const overlay = document.getElementById("panelOverlayActividad");
    if (!overlay) return;

    setVal("act-idActividad",  "");
    setVal("act-nombre",       "");
    setVal("act-precio",       "");
    setVal("act-precioDiario", "0");   // fallback 0 para evitar 400
    setVal("act-cupoMaximo",   "");

    document.getElementById("panelActividadTitulo").innerText = "Nueva Actividad";
    document.getElementById("btnSubmitActividad").innerText   = "Guardar";

    resetFilasInstructor();
    agregarFilaInstructor();

    overlay.classList.remove("d-none");
}

function cerrarPanelActividad() {
    const overlay = document.getElementById("panelOverlayActividad");
    if (overlay) overlay.classList.add("d-none");
}

async function abrirPanelEditarActividad(btn) {
    const id      = btn.dataset.id;
    const overlay = document.getElementById("panelOverlayActividad");
    if (!overlay) return;

    overlay.classList.remove("d-none");
    document.getElementById("panelActividadTitulo").innerText = "Editar Actividad";
    document.getElementById("btnSubmitActividad").innerText   = "Guardar cambios";

    // Limpiar mientras llega el fetch
    setVal("act-idActividad",  "");
    setVal("act-nombre",       "");
    setVal("act-precio",       "");
    setVal("act-precioDiario", "0");
    setVal("act-cupoMaximo",   "");
    resetFilasInstructor();

    try {
        const res = await fetch(`/actividades/editar/${id}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        setVal("act-idActividad",  data.id);
        setVal("act-nombre",       data.nombre);
        setVal("act-precio",       data.precio);
        setVal("act-precioDiario", data.precioDiario ?? "0");
        setVal("act-cupoMaximo",   data.cupoMaximo);

        if (Array.isArray(data.instructores) && data.instructores.length > 0) {
            data.instructores.forEach(i =>
                agregarFilaInstructor(i.instructorId, i.dias, i.horario)
            );
        } else {
            agregarFilaInstructor();
        }

    } catch (e) {
        console.error("Error al cargar actividad:", e);
        alert("No se pudieron cargar los datos. Error: " + e.message);
    }
}

// ── Filas de instructor dinámicas ─────────────────────────────────────────────

function resetFilasInstructor() {
    const c = document.getElementById("instructoresContainer");
    if (c) c.innerHTML = "";
}

function agregarFilaInstructor(instructorId = null, dias = "", horario = "") {
    const container = document.getElementById("instructoresContainer");
    if (!container) return;

    const lista = window.instructoresDisponibles || [];

    const opciones = lista.map(i =>
        `<option value="${i.id}" ${String(i.id) === String(instructorId) ? "selected" : ""}>${i.nombre}</option>`
    ).join("");

    const fila = document.createElement("div");
    fila.className = "row g-2 mb-2 align-items-center fila-instructor";
    fila.innerHTML = `
        <div class="col-md-4">
            <select name="instructorIds" class="form-select form-select-sm" required>
                <option value="" disabled ${!instructorId ? "selected" : ""}>Instructor...</option>
                ${opciones}
            </select>
        </div>
        <div class="col-md-3">
            <input type="text" name="dias" class="form-control form-control-sm"
                   placeholder="Lun - Mie" value="${dias || ""}" required>
        </div>
        <div class="col-md-3">
            <input type="text" name="horarios" class="form-control form-control-sm"
                   placeholder="18:00" value="${horario || ""}" required>
        </div>
        <div class="col-md-2">
            <button type="button" class="btn btn-sm btn-outline-danger w-100 btn-quitar-instructor"
                    onclick="eliminarFilaInstructor(this)">
                <i class="bi bi-trash"></i>
            </button>
        </div>`;

    container.appendChild(fila);
    actualizarBotonesEliminar();
}

function eliminarFilaInstructor(btn) {
    btn.closest(".fila-instructor").remove();
    actualizarBotonesEliminar();
}

function actualizarBotonesEliminar() {
    const filas = document.querySelectorAll(".fila-instructor");
    filas.forEach(f => {
        const btn = f.querySelector(".btn-quitar-instructor");
        if (btn) btn.style.visibility = filas.length === 1 ? "hidden" : "visible";
    });
}

// ── Panel eliminar ────────────────────────────────────────────────────────────

function abrirPanelEliminar(btn) {
    const nombreEl = document.getElementById("nombreEliminar");
    if (nombreEl) nombreEl.innerText = btn.dataset.nombre;

    const form = document.getElementById("formEliminar");
    if (form) form.action = `${btn.dataset.url}/${btn.dataset.id}`;

    const panel = document.getElementById("panelEliminarOverlay");
    if (panel) panel.classList.remove("d-none");
}

function cerrarPanelEliminar() {
    const panel = document.getElementById("panelEliminarOverlay");
    if (panel) panel.classList.add("d-none");
}

function toggleModoEliminar() {
    document.body.classList.toggle("modo-eliminar-activo");
}

// ── Buscador ──────────────────────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("filtroActividades");
    if (input) {
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

            const msg = document.getElementById("mensajeVacioActividades");
            if (msg) msg.style.display = (visibles === 0 && q.length > 0) ? "" : "none";

            if (window._paginators?.tablaActividades) {
                window._paginators.tablaActividades.refresh();
            }
        });
    }

    document.addEventListener("keydown", e => {
        if (e.key === "Escape") {
            cerrarPanelActividad();
            cerrarPanelEliminar();
        }
    });
});

function toggleCupo(checkbox) {
    const input = document.getElementById('act-cupoMaximo');
    if (checkbox.checked) {
        input.value = '';
        input.disabled = true;
    } else {
        input.disabled = false;
    }
}