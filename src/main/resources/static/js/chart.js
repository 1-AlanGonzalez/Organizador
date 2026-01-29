document.addEventListener("DOMContentLoaded", function() {
    const chartDom = document.querySelector("#chart");
    
    // Verificamos que existan datos y el contenedor
    if (chartDom && window.datosGrafico) {
        const options = {
            chart: { 
                type: 'area',
                height: '100%',
                toolbar: { show: false },
                zoom: { enabled: false },
                dropShadow: {
                    enabled: true,
                    top: 3,
                    left: 2,
                    blur: 4,
                    opacity: 0.1,
                }
            },
            series: [{
                name: 'Ingresos Mensuales',
                data: window.datosGrafico
            }],
            stroke: { 
                curve: 'stepline', 
                width: 3 
            },
            fill: {
                type: 'gradient',
                gradient: {
                    shadeIntensity: 1,
                    opacityFrom: 0.45,
                    opacityTo: 0.05,
                    stops: [20, 100]
                }
            },
            colors: ['#198754'], 
            xaxis: {
                categories: window.categoriasGrafico,
                axisBorder: { show: false },
                tooltip: { enabled: false }
            },
            yaxis: {
                labels: {
                    formatter: function (val) {
                        return "$" + val.toLocaleString();
                    }
                }
            },
            markers: {
                size: 4, 
                hover: { size: 6 }
            },
            dataLabels: {
                enabled: false
            },
            tooltip: {
                shared: true,
                intersect: false,
                y: {
                    formatter: function (val) {
                        return "$ " + val.toLocaleString();
                    }
                }
            }
        };

        const chart = new ApexCharts(chartDom, options);
        chart.render();
    }
});