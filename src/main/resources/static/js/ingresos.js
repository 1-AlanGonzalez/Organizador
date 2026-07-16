const MESES = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
];

function formatearMes(valor) {
    if (!valor) return "Todos";
    const [anio, mes] = valor.split("-");
    return `${MESES[Number(mes) - 1]} ${anio}`;
}

async function actualizarCards(mes) {
    if (!mes) return;
    try {
        const respuesta = await fetch(`/ingresos/stats?mes=${encodeURIComponent(mes)}`);
        if (!respuesta.ok) return;
        const datos = await respuesta.json();
        const moneda = valor => "$" + Number(valor).toLocaleString("es-AR", {
            minimumFractionDigits: 2
        });
        document.getElementById("cardTotal").textContent = moneda(datos.total);
        document.getElementById("cardEfectivo").textContent = moneda(datos.efectivo);
        document.getElementById("cardTransferencia").textContent = moneda(datos.transferencia);
        document.getElementById("cardPendiente").textContent = moneda(datos.pendiente);
    } catch (error) {
        console.error("No se pudieron actualizar las estadísticas", error);
    }
}

window.filtrarEstado = function (estado) {
    const form = document.getElementById("filtrosIngresos");
    document.getElementById("estadoIngreso").value = estado;
    form?.submit();
};

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("filtrosIngresos");
    const buscador = document.getElementById("buscadorTabla");
    const filtroMes = document.getElementById("filtroMes");
    const filtroAnio = document.getElementById("filtroAnio");
    const labelMes = document.getElementById("labelMes");
    let temporizador;

    buscador?.addEventListener("input", () => {
        clearTimeout(temporizador);
        temporizador = setTimeout(() => form?.requestSubmit(), 350);
    });

    filtroMes?.addEventListener("change", () => {
        if (filtroMes.value && filtroAnio) filtroAnio.value = "";
        form?.requestSubmit();
    });

    filtroAnio?.addEventListener("change", () => {
        if (filtroAnio.value && filtroMes) filtroMes.value = "";
        form?.requestSubmit();
    });

    if (labelMes) labelMes.textContent = filtroMes?.value
        ? formatearMes(filtroMes.value)
        : (filtroAnio?.value || "Todos");
    if (filtroMes?.value) actualizarCards(filtroMes.value);
});
