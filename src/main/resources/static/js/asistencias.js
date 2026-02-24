function filtrarTabla() {
    const selectActividad = document.getElementById('filtroActividad');
    const filtroSelect = selectActividad ? selectActividad.value : 'todos';
    
    const inputBusqueda = document.getElementById('filtroAsistencias');
    const filtroTexto = inputBusqueda ? inputBusqueda.value.toLowerCase() : '';
    
    const filas = document.querySelectorAll('.fila-alumno');
    const mensajeVacioJS = document.getElementById('mensajeVacioJS');
    
    let contadorVisibles = 0;
    filas.forEach(fila => {
        const actividadFila = fila.getAttribute('data-actividad');
        const textoFila = fila.innerText.toLowerCase();
        const cumpleActividad = (filtroSelect === 'todos' || actividadFila === filtroSelect);
        const cumpleTexto = textoFila.includes(filtroTexto);
        if (cumpleActividad && cumpleTexto) {
            fila.style.display = ''; 
            contadorVisibles++;
        } else {
            fila.style.display = 'none'; 
        }
    });

    if (mensajeVacioJS) {
        if (filas.length > 0 && contadorVisibles === 0) {
            mensajeVacioJS.style.display = '';
        } else {
            mensajeVacioJS.style.display = 'none';
        }
    }
}
document.addEventListener('DOMContentLoaded', () => {
    const filtroInput = document.getElementById("filtroAsistencias");
    if (filtroInput) {
        filtroInput.addEventListener("keyup", filtrarTabla);
    }
    actualizarContador();
});
if (filtroInput) {
    filtroInput.addEventListener("keyup", filtrarTabla);
}

function actualizarContador() {
    const checkboxes = document.querySelectorAll('.input-asistencia:checked');
    document.getElementById('contadorPresentes').innerText = checkboxes.length;
}


const filtroInput = document.getElementById("filtroAsistencias");

if (filtroInput) {
    filtroInput.addEventListener("keyup", function () {
        const filtro = this.value.toLowerCase();
        const filas = document.querySelectorAll("#tablaAsistencias tbody tr");

        filas.forEach(fila => {
            const textoFila = fila.innerText.toLowerCase();
            fila.style.display = textoFila.includes(filtro) ? "" : "none";
        });
    });
}