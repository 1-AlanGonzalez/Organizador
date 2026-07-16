// ── Helpers ───────────────────────────────────────────────────────────────────

function setVal(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = (value !== null && value !== undefined) ? value : "";
}

function escaparAtributo(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll('"', "&quot;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
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
                   placeholder="Lun - Mie" value="${escaparAtributo(dias)}" required>
        </div>
        <div class="col-md-3">
            <input type="text" name="horarios" class="form-control form-control-sm"
                   placeholder="18:00" value="${escaparAtributo(horario)}" required>
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

function validarAsignaciones(form) {
    const combinaciones = new Set();
    let duplicado = false;

    form.querySelectorAll(".fila-instructor").forEach(fila => {
        const instructor = fila.querySelector('[name="instructorIds"]');
        const dias = fila.querySelector('[name="dias"]');
        const horario = fila.querySelector('[name="horarios"]');
        [instructor, dias, horario].forEach(campo => campo?.setCustomValidity(""));

        const normalizar = valor => valor.trim().replace(/\s+/g, " ").toLocaleLowerCase("es");
        const clave = `${instructor?.value}|${normalizar(dias?.value || "")}|${normalizar(horario?.value || "")}`;
        if (combinaciones.has(clave)) {
            const mensaje = "Este instructor ya tiene una asignación con los mismos días y horario.";
            instructor?.setCustomValidity(mensaje);
            dias?.setCustomValidity(mensaje);
            horario?.setCustomValidity(mensaje);
            duplicado = true;
        }
        combinaciones.add(clave);
    });

    return !duplicado;
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

    
});
document.addEventListener("keydown", e => {
    if (e.key === "Escape") {
        cerrarPanelEliminar();
    }
    if (e.key === "Enter") {
        const panelEliminar = document.getElementById("panelEliminarOverlay");
        if (panelEliminar && !panelEliminar.classList.contains("d-none")) {
            e.preventDefault();
            document.getElementById("formEliminar")?.submit();
        }
    }
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

document.addEventListener("DOMContentLoaded", function () {

    // ... tu código existente del buscador y keydown ...

    // ── Pre-cargar instructores en modo editar ────────────────────────────
    const container = document.getElementById("instructoresContainer");
    if (container) {
        const dictados = window.dictadosIniciales || [];
        if (dictados.length > 0) {
            dictados.forEach(d =>
                agregarFilaInstructor(d.instructorId, d.dias, d.horario)
            );
        } else {
            agregarFilaInstructor(); // fila vacía para nueva actividad
        }
    }

    const formActividad = container?.closest("form");
    container?.addEventListener("input", event => event.target.setCustomValidity?.(""));
    container?.addEventListener("change", event => event.target.setCustomValidity?.(""));
    formActividad?.addEventListener("submit", event => {
        if (!validarAsignaciones(formActividad)) {
            event.preventDefault();
            formActividad.reportValidity();
        }
    });

});
