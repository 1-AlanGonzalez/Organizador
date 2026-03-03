let ordenSeleccion = [];

function toggleSeccion(tipo, checkboxElem) {
    const label = document.getElementById('label-' + tipo);
    const seccion = document.getElementById('seccion-' + tipo);

    // 1. Estilos del label izquierdo
    // Ahora solo agregamos/quitamos la clase 'selected'.
    // El CSS se encarga de mostrar u ocultar el ícono del check sin romper el layout.
    if (checkboxElem.checked) {
        label.classList.add('selected');
        seccion.classList.remove('d-none');
        //Agregamos al orden si no estaba
        if (!ordenSeleccion.includes(tipo)) {
            ordenSeleccion.push(tipo);
        }
    } else {
        label.classList.remove('selected');
        seccion.classList.add('d-none');
        //Lo quitamos del orden
        ordenSeleccion = ordenSeleccion.filter(t => t !== tipo);    
    }

    actualizarEstadoPanelDerecho();
}

function actualizarEstadoPanelDerecho() {
    // Verifica cuántas secciones están visibles
    const visibles = document.querySelectorAll('.seccion-datos:not(.d-none)').length;
    const emptyState = document.getElementById('empty-state');
    const columnasConfig = document.getElementById('columnas-config');
    const btnSubmit = document.getElementById('btn-submit');

    if (visibles > 0) {
        emptyState.classList.add('d-none');
        columnasConfig.classList.remove('d-none');
        columnasConfig.classList.add('d-flex');
        btnSubmit.disabled = false;
    } else {
        emptyState.classList.remove('d-none');
        columnasConfig.classList.add('d-none');
        columnasConfig.classList.remove('d-flex');
        btnSubmit.disabled = true;
    }
}

function seleccionarTodos() {
    // Busca los checkboxes de las secciones que ESTÁN VISIBLES actualmente
    const seccionesVisibles = document.querySelectorAll('.seccion-datos:not(.d-none)');
    
    if(seccionesVisibles.length > 0) {
        // Obtenemos todos los checkboxes de las secciones visibles
        let todosLosCheckboxes = [];
        seccionesVisibles.forEach(sec => {
            todosLosCheckboxes.push(...sec.querySelectorAll('input[type="checkbox"]'));
        });

        // Verificamos si todos están marcados
        const allChecked = todosLosCheckboxes.every(c => c.checked);
        
        // Si todos están marcados, los desmarcamos. Si no, los marcamos todos.
        todosLosCheckboxes.forEach(c => c.checked = !allChecked);
    }
}

document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("form-exportar");
    if (!form) return;

    form.addEventListener("submit", handleExportSubmit);

});

async function handleExportSubmit(e) {
    e.preventDefault();

    const fecha = document.querySelector('input[name="fecha"]').value;
    const atributos = buildAtributosSeleccionados();

    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    const requestBody = {
        fecha: fecha || null,
        entidades: [
            {
                nombre: "inscripcion",
                atributos: atributos
            }
        ]
    };

    try {
        const response = await fetch("/exportar/excel", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken   // 👈 ESTO ES CLAVE
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error backend:", errorText);
            alert("Error: " + response.status);
            return;
        }

        descargarArchivo(await response.blob());

    } catch (error) {
        console.error(error);
        alert("Error al conectar con el backend");
    }
}

function buildAtributosSeleccionados() {

    const atributos = [];

    if (typeof ordenSeleccion === "undefined") return atributos;

    ordenSeleccion.forEach(tipo => {

        switch (tipo) {

            case "clientes":
                document.querySelectorAll('input[name="camposClientes"]:checked')
                    .forEach(c => atributos.push("cliente." + c.value));
                break;

            case "ingresos":
                document.querySelectorAll('input[name="camposIngresos"]:checked')
                    .forEach(c => atributos.push("pagos." + c.value));
                break;

            case "actividades":
                document.querySelectorAll('input[name="camposActividades"]:checked')
                    .forEach(c => atributos.push("actividad." + c.value));
                break;

            case "asistencias":
                document.querySelectorAll('input[name="camposAsistencias"]:checked')
                    .forEach(c => atributos.push("asistencias." + c.value));
                break;

            case "instructores":
                document.querySelectorAll('input[name="camposInstructores"]:checked')
                    .forEach(c => atributos.push("actividad.instructor." + c.value));
                break;
        }

    });

    return atributos;
}

function descargarArchivo(blob) {

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = "reporte.xlsx";
    document.body.appendChild(a);
    a.click();
    a.remove();

    window.URL.revokeObjectURL(url);
}