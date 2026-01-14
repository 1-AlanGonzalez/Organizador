var options = {
  chart: {
    type: 'line',
    height: 300
  },
  stroke: {
    curve: 'smooth'
  },
  series: [{
    name: 'Ingresos',
    data: [450, 470, 520, 610, 700, 750, 800, 850, 500]
  }],
  xaxis: {
    categories: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep']
  }
}

var chart = new ApexCharts(document.querySelector("#chart"), options);
chart.render();