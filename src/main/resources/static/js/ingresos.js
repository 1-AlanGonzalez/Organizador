// ── ingresos.js ───────────────────────────────────────────────────────────────

const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
               'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

let estadoActivo = 'todos';
let pagPagos     = null;

function formatearMes(valor) {
    if (!valor) return '';
    const [anio, mes] = valor.split('-');
    return MESES[parseInt(mes) - 1] + ' ' + anio;
}

// ── Cards via fetch ───────────────────────────────────────────────────────────

async function actualizarCards(mes) {
    try {
        const res  = await fetch(`/ingresos/stats?mes=${mes}`);
        if (!res.ok) return;
        const data = await res.json();
        const fmt  = n => '$' + Number(n).toLocaleString('es-AR', { minimumFractionDigits: 2 });
        document.getElementById('cardTotal').textContent         = fmt(data.total);
        document.getElementById('cardEfectivo').textContent      = fmt(data.efectivo);
        document.getElementById('cardTransferencia').textContent = fmt(data.transferencia);
        document.getElementById('cardPendiente').textContent     = fmt(data.pendiente);
    } catch(e) {
        console.error('Error al cargar stats:', e);
    }
}

// ── Filtro combinado (texto + mes + año + estado tab) ─────────────────────────

function aplicarFiltros() {
    const texto = (document.getElementById('buscadorTabla')?.value || '').toLowerCase();
    const mes   = document.getElementById('filtroMes')?.value  || '';   // "2025-03"
    const anio  = document.getElementById('filtroAnio')?.value || '';   // "2025"

    const filas   = document.querySelectorAll('#tablaPagos tbody tr.fila-pago');
    let visibles  = 0;

    filas.forEach(row => {
        const fechaMes  = row.dataset.fecha  || '';  // "2025-03"
        const fechaAnio = fechaMes.split('-')[0];    // "2025"
        const pagado    = row.dataset.estado === 'pagado';

        // Filtro tab
        const cumpleEstado =
            estadoActivo === 'todos'     ? true :
            estadoActivo === 'pagado'    ? pagado :
            estadoActivo === 'pendiente' ? !pagado : true;

        // Filtro mes (si está seleccionado)
        const cumpleMes  = !mes  || fechaMes  === mes;

        // Filtro año (si está seleccionado y no hay mes)
        const cumpleAnio = mes   ? true : (!anio || fechaAnio === anio);

        // Filtro texto
        const cumpleTexto = !texto || row.textContent.toLowerCase().includes(texto);

        const visible = cumpleEstado && cumpleMes && cumpleAnio && cumpleTexto;
        row.style.display        = visible ? '' : 'none';
        row.dataset.searchHidden = visible ? 'false' : 'true';
        if (visible) visibles++;
    });

    const msgVacio = document.getElementById('mensajeVacioPagos');
    if (msgVacio) msgVacio.style.display = visibles === 0 ? '' : 'none';

    if (pagPagos) pagPagos.refresh();
}

// Tab activo
window.filtrarEstado = function(estado) {
    estadoActivo = estado;

    document.querySelectorAll('#paymentTabs .nav-link').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.estado === estado);
    });

    // Actualizar cards con el mes seleccionado, o el mes actual si no hay ninguno
    const filtroMes = document.getElementById('filtroMes');
    const mes = filtroMes?.value || new Date().toISOString().slice(0, 7); // "yyyy-MM"
    actualizarCards(mes);

    aplicarFiltros();
};

// ── Listeners ─────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', function () {
    pagPagos = initPaginator('tablaPagos', 'paginadorPagos', 10);

    const filtroMes   = document.getElementById('filtroMes');
    const filtroAnio  = document.getElementById('filtroAnio');
    const buscador    = document.getElementById('buscadorTabla');

    // Al cambiar mes: actualiza label, cards y tabla
    filtroMes?.addEventListener('change', function () {
        const mes = this.value;
        document.getElementById('labelMes').textContent = mes ? formatearMes(mes) : '';
        if (mes) actualizarCards(mes);
        aplicarFiltros();
    });

    // Al cambiar año: si no hay mes seleccionado, solo filtra tabla
    filtroAnio?.addEventListener('change', function () {
        // Si cambia el año, limpiar mes para no tener conflicto
        if (filtroMes) filtroMes.value = '';
        document.getElementById('labelMes').textContent = this.value || 'Todos';
        aplicarFiltros();
    });

    buscador?.addEventListener('input', aplicarFiltros);

    // Inicializar con mes actual
    const mesInicial = filtroMes?.value || new Date().toISOString().slice(0, 7);
    if (mesInicial) {
        document.getElementById('labelMes').textContent = formatearMes(mesInicial);
        actualizarCards(mesInicial); // ← agregar esta línea
        aplicarFiltros();
    }

    
});
