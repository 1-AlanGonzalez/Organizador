function filtrarTabla() {
    const filtro = document.getElementById('filtroActividad').value;
    const filas = document.querySelectorAll('.fila-alumno');

    filas.forEach(fila => {
        const actividadFila = fila.getAttribute('data-actividad');
        
        if (filtro === 'todos' || actividadFila === filtro) {
            fila.style.display = ''; 
        } else {
            fila.style.display = 'none'; 
        }
    });
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