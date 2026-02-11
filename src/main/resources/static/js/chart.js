
document.addEventListener("DOMContentLoaded", function() {
    
    const datos = window.datosGrafico || [350000, 420000, 380000, 550000, 620000, 780000]; 
    const categorias = window.categoriasGrafico || ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio'];

    const chartDom = document.querySelector("#chart");
    
    if (chartDom) {
        const options = {
            series: [{
                name: 'Ingresos',
                data: datos
            }],
            chart: { 
                type: 'area',
                height: '100%', 
                parentHeightOffset: 0,
                fontFamily: 'Inter, sans-serif',
                toolbar: { show: false },
                zoom: { enabled: false },
                dropShadow: {
                    enabled: true,
                    top: 10,
                    left: 0,
                    blur: 15,
                    color: '#198754',
                    opacity: 0.25
                }
            },
            stroke: { 
                curve: 'smooth', 
                width: 3 
            },
            fill: {
                type: 'gradient',
                gradient: {
                    shadeIntensity: 1,
                    opacityFrom: 0.5,
                    opacityTo: 0.05,
                    stops: [0, 90, 100]
                }
            },
            colors: ['#198754'],
            
            // EJE X
            xaxis: {
                categories: categorias,
                axisBorder: { show: false },
                axisTicks: { show: false },
                labels: {
                    style: { colors: '#9ca3af', fontSize: '10px' } 
                },
                tooltip: { enabled: false }
            },
            
            // EJE Y
            yaxis: {
                show: true,
                min: 0,
                tickAmount: 5,
                labels: {
                    style: { colors: '#9ca3af', fontSize: '11px', fontWeight: 500 },
                    formatter: function (val) {
                        return "$" + val.toLocaleString('es-AR', {minimumFractionDigits: 0, maximumFractionDigits: 0});
                    }
                }
            },
            
            grid: {
                borderColor: '#f3f4f6',
                strokeDashArray: 4,
                padding: { top: 0, right: 10, bottom: 0, left: 10 } 
            },
            
            tooltip: {
                theme: 'light',
                y: {
                    formatter: function (val) {
                        return "$ " + val.toLocaleString('es-AR', {minimumFractionDigits: 2});
                    }
                }
            },
            dataLabels: { enabled: false }
        };

        const chart = new ApexCharts(chartDom, options);
        chart.render();
    }
});