var ctx = document.getElementById("chart").getContext('2d');
var myChart = new Chart(ctx, {
    type: 'bubble',
    data: {
        datasets: {{datasets}}
    },
    options:  {
        aspectRatio: 1,
        legend:false,
        elements: {
            point: {
                radius: 12
            }
        },
        tooltips: {
            callbacks: {
                label: function(tooltipItem, data) {
                    return data.datasets[tooltipItem.datasetIndex].label.split(":")[0]
                }
            }
        },
        scales: {
            yAxes: [{
                labels: ['Abstractness'],
                ticks: {
                    beginAtZero:true,
                    max: 1.0
                }
            }],
            xAxes: [{
                labels: ['Instability'],
                ticks: {
                    beginAtZero:true,
                    max: 1.0
                }
            }]
        },
        title: {
            display: true,
                text: '{{chartTitle}}'
        }
    }
});