function toggleSeccion(tipo, checkboxElem) {
    const label = document.getElementById('label-' + tipo);
    const seccion = document.getElementById('seccion-' + tipo);

    // 1. Estilos del label izquierdo
    // Ahora solo agregamos/quitamos la clase 'selected'.
    // El CSS se encarga de mostrar u ocultar el ícono del check sin romper el layout.
    if (checkboxElem.checked) {
        label.classList.add('selected');
        seccion.classList.remove('d-none');
    } else {
        label.classList.remove('selected');
        seccion.classList.add('d-none');
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