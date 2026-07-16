// ── ingresos.js ───────────────────────────────────────────────────────────────

const MESES = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];

let estadoActivo = 'todos';
let pagPagos = null;
let modoAccionPago = null;


// ── Helpers ───────────────────────────────────────────────────────────────────

function formatearMes(valor) {
    if (!valor) return '';

    const [anio, mes] = valor.split('-');

    return MESES[parseInt(mes) - 1] + ' ' + anio;
}


// ── Cards via fetch ───────────────────────────────────────────────────────────

async function actualizarCards(mes) {

    try {

        const res = await fetch(`/ingresos/stats?mes=${mes}`);

        if (!res.ok) return;

        const data = await res.json();

        const fmt = n =>
            '$' + Number(n).toLocaleString('es-AR', {
                minimumFractionDigits: 2
            });

        document.getElementById('cardTotal').textContent =
            fmt(data.total);

        document.getElementById('cardEfectivo').textContent =
            fmt(data.efectivo);

        document.getElementById('cardTransferencia').textContent =
            fmt(data.transferencia);

        document.getElementById('cardPendiente').textContent =
            fmt(data.pendiente);

    } catch (e) {

        console.error('Error al cargar stats:', e);

    }

}


// ── Filtros ───────────────────────────────────────────────────────────────────

function aplicarFiltros() {

    const texto =
        (document.getElementById('buscadorTabla')?.value || '')
            .toLowerCase();

    const mes =
        document.getElementById('filtroMes')?.value || '';

    const anio =
        document.getElementById('filtroAnio')?.value || '';

    const filas =
        document.querySelectorAll('#tablaPagos tbody tr.fila-pago');

    let visibles = 0;


    filas.forEach(row => {

        const fechaMes =
            row.dataset.fecha || '';

        const fechaAnio =
            fechaMes.split('-')[0];

        const pagado =
            row.dataset.estado === 'pagado';


        const cumpleEstado =
            estadoActivo === 'todos'
                ? true
                : estadoActivo === 'pagado'
                    ? pagado
                    : estadoActivo === 'pendiente'
                        ? !pagado
                        : true;


        const cumpleMes =
            !mes || fechaMes === mes;


        const cumpleAnio =
            mes
                ? true
                : (!anio || fechaAnio === anio);


        const cumpleTexto =
            !texto ||
            row.textContent.toLowerCase().includes(texto);


        const visible =
            cumpleEstado &&
            cumpleMes &&
            cumpleAnio &&
            cumpleTexto;


        row.style.display =
            visible ? '' : 'none';


        row.dataset.searchHidden =
            visible ? 'false' : 'true';


        if (visible) {
            visibles++;
        }

    });


    const msgVacio =
        document.getElementById('mensajeVacioPagos');


    if (msgVacio) {

        msgVacio.style.display =
            visibles === 0 ? '' : 'none';

    }


    if (pagPagos) {

        pagPagos.refresh();

    }

}


// ── Tabs Todos / Pagados / Pendientes ─────────────────────────────────────────

window.filtrarEstado = function (estado) {

    estadoActivo = estado;


    document
        .querySelectorAll('#paymentTabs .nav-link')
        .forEach(btn => {

            btn.classList.toggle(
                'active',
                btn.dataset.estado === estado
            );

        });


    const filtroMes =
        document.getElementById('filtroMes');


    const mes =
        filtroMes?.value ||
        new Date().toISOString().slice(0, 7);


    actualizarCards(mes);

    aplicarFiltros();

};


// ── Modo Editar / Eliminar ────────────────────────────────────────────────────

window.toggleModoAccionPago = function (modo) {

    const thAccion =
        document.getElementById('thAccionPago');

    const columnasAccion =
        document.querySelectorAll('.td-accion-pago');

    const botonesEditar =
        document.querySelectorAll('.btn-editar-pago');

    const botonesEliminar =
        document.querySelectorAll('.btn-eliminar-pago');

    const btnEditar =
        document.getElementById('btnModoEditarPago');

    const btnEliminar =
        document.getElementById('btnModoEliminarPago');


    // Si toca otra vez el mismo botón, cerrar el modo
    if (modoAccionPago === modo) {

        modoAccionPago = null;


        if (thAccion) {
            thAccion.style.display = 'none';
        }


        columnasAccion.forEach(td => {
            td.style.display = 'none';
        });


        botonesEditar.forEach(btn => {
            btn.style.display = 'none';
        });


        botonesEliminar.forEach(btn => {
            btn.style.display = 'none';
        });


        btnEditar?.classList.remove('active');
        btnEliminar?.classList.remove('active');


        return;
    }


    // Activar el nuevo modo
    modoAccionPago = modo;


    if (thAccion) {
        thAccion.style.display = '';
    }


    columnasAccion.forEach(td => {
        td.style.display = '';
    });


    // Ocultar primero ambos tipos de botones
    botonesEditar.forEach(btn => {
        btn.style.display = 'none';
    });


    botonesEliminar.forEach(btn => {
        btn.style.display = 'none';
    });


    btnEditar?.classList.remove('active');
    btnEliminar?.classList.remove('active');


    // Modo editar
    if (modo === 'editar') {

        botonesEditar.forEach(btn => {
            btn.style.display = 'inline-flex';
        });


        btnEditar?.classList.add('active');

    }


    // Modo eliminar
    if (modo === 'eliminar') {

        botonesEliminar.forEach(btn => {
            btn.style.display = 'inline-flex';
        });


        btnEliminar?.classList.add('active');

    }


    // Recargar iconos Lucide
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

};



// ── Panel eliminar adaptado dinámicamente sin depender de clases ──────────────

window.abrirPanelEliminar = function (btn) {

    // 1. Seteamos el nombre del cliente en el span de siempre
    const nombreEl =
        document.getElementById('nombreEliminar');

    if (nombreEl) {
        nombreEl.innerText = btn.dataset.nombre;
    }

    // 2. Seteamos la ruta de acción del formulario original
    const form =
        document.getElementById('formEliminar');

    if (form) {
        form.action =
            `${btn.dataset.url}/${btn.dataset.id}`;
    }

    // 3. CAMBIOS EN CALIENTE USANDO SELECTORES GENÉRICOS (A prueba de balas)
    try {
        const overlay = document.getElementById('panelEliminarOverlay');
        if (overlay) {

            // Buscar el título principal (que es el primer h4 que encuentre en el modal)
            const modalHeader = overlay.querySelector('h4, h3');
            if (modalHeader) {
                modalHeader.innerText = 'Anular Pago';
                modalHeader.style.color = '#dc3545'; // Color rojo
            }

            // Buscar el botón de confirmación rojo (que tiene la clase btn-danger)
            const btnConfirmar = overlay.querySelector('.btn-danger, button[type="submit"]');
            if (btnConfirmar) {
                btnConfirmar.innerText = 'Anular';
            }

            // Buscar todos los párrafos <p> para modificar la pregunta y la advertencia
            const parrafos = overlay.querySelectorAll('p');

            if (parrafos.length >= 2) {
                // El primer párrafo suele ser la pregunta de confirmación
                parrafos[0].innerHTML = `¿Seguro que querés anular el pago de <strong id="nombreEliminar">${btn.dataset.nombre}</strong>?`;

                // El segundo párrafo suele ser la advertencia larga
                parrafos[1].innerText = 'Esta acción no se puede deshacer y el estado del pago volverá a figurar como pendiente.';
            } else if (parrafos.length === 1) {
                // Si por alguna razón hay un solo párrafo, cambiamos su texto de forma segura
                parrafos[0].innerHTML = `¿Seguro que querés anular el pago de <strong id="nombreEliminar">${btn.dataset.nombre}</strong>?`;
            }
        }
    } catch (error) {
        console.warn('Error al intentar modificar los textos del modal:', error);
    }

    // 4. Mostramos el panel original
    const panel =
        document.getElementById('panelEliminarOverlay');

    if (panel) {
        panel.classList.remove('d-none');
    }

    // Recargar iconos Lucide
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

};


window.cerrarPanelEliminar = function () {

    const panel =
        document.getElementById('panelEliminarOverlay');


    if (panel) {

        panel.classList.add('d-none');

    }

};


// ── Teclado del panel eliminar ────────────────────────────────────────────────

document.addEventListener('keydown', function (e) {

    if (e.key === 'Escape') {

        cerrarPanelEliminar();

    }


    if (e.key === 'Enter') {

        const panelEliminar =
            document.getElementById('panelEliminarOverlay');


        if (
            panelEliminar &&
            !panelEliminar.classList.contains('d-none')
        ) {

            e.preventDefault();

            document
                .getElementById('formEliminar')
                ?.submit();

        }

    }

});


// ── Inicialización ────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', function () {


    // Paginador
    pagPagos =
        initPaginator(
            'tablaPagos',
            'paginadorPagos',
            10
        );


    const filtroMes =
        document.getElementById('filtroMes');

    const filtroAnio =
        document.getElementById('filtroAnio');

    const buscador =
        document.getElementById('buscadorTabla');


    // Cambio de mes
    filtroMes?.addEventListener('change', function () {

        const mes =
            this.value;


        document
            .getElementById('labelMes')
            .textContent =
            mes
                ? formatearMes(mes)
                : '';


        if (mes) {

            actualizarCards(mes);

        }


        aplicarFiltros();

    });


    // Cambio de año
    filtroAnio?.addEventListener('change', function () {


        // Limpiar mes para evitar conflicto
        if (filtroMes) {

            filtroMes.value = '';

        }


        document
            .getElementById('labelMes')
            .textContent =
            this.value || '';


        aplicarFiltros();

    });


    // Buscador
    buscador?.addEventListener(
        'input',
        aplicarFiltros
    );


    // Inicializar con mes actual
    const mesInicial =
        filtroMes?.value ||
        new Date().toISOString().slice(0, 7);


    if (mesInicial) {

        document
            .getElementById('labelMes')
            .textContent =
            formatearMes(mesInicial);


        actualizarCards(mesInicial);

        aplicarFiltros();

    }

});