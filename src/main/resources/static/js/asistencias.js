// ── Filtro combinado (texto + actividad) ─────────────────────────────────────

function filtrarTabla() {
    const selectActividad = document.getElementById('filtroActividad');
    const filtroSelect    = selectActividad ? selectActividad.value : 'todos';

    const inputBusqueda = document.getElementById('filtroAsistencias');
    const filtroTexto   = inputBusqueda ? inputBusqueda.value.toLowerCase().trim() : '';

    const filas       = document.querySelectorAll('.fila-alumno');
    const mensajeVacio = document.getElementById('mensajeVacioJS');
    let visibles = 0;

    filas.forEach(fila => {
        const actividadFila  = fila.getAttribute('data-actividad') || '';
        const textoFila      = fila.textContent.toLowerCase();

        const cumpleActividad = (filtroSelect === 'todos' || actividadFila === filtroSelect);
        const cumpleTexto     = !filtroTexto || textoFila.includes(filtroTexto);

        const visible = cumpleActividad && cumpleTexto;
        fila.style.display        = visible ? '' : 'none';
        fila.dataset.searchHidden = visible ? 'false' : 'true';
        if (visible) visibles++;
    });

    if (mensajeVacio) {
        mensajeVacio.style.display = (filas.length > 0 && visibles === 0) ? '' : 'none';
    }

    // Repaginar si hay paginador activo
    if (window._paginators && window._paginators['tablaAsistencias']) {
        window._paginators['tablaAsistencias'].refresh();
    }
}

// ── Contador de presentes ─────────────────────────────────────────────────────

function actualizarContador() {
    const checkboxes = document.querySelectorAll('.input-asistencia:checked');
    const contador   = document.getElementById('contadorPresentes');
    if (contador) contador.innerText = checkboxes.length;
}

// ── DOM Ready ─────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    // Input de texto
    const filtroInput = document.getElementById('filtroAsistencias');
    if (filtroInput) {
        filtroInput.addEventListener('input', filtrarTabla);
    }

    // Select de actividad
    const filtroActividad = document.getElementById('filtroActividad');
    if (filtroActividad) {
        filtroActividad.addEventListener('change', filtrarTabla);
    }

    actualizarContador();
});