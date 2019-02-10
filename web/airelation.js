$(function() {
    var ctx = document.getElementById("chart").getContext('2d');

    $.ajax({
        method: "GET",
        url: "http://localhost:8080",
        dataType: "json",
        success: function (data) {
            $("#graph-title").text(data.title);
            createChart(data);
        }
    });

    function createChart(jsonData){
        var chart = new Chart(ctx, {
            type: 'bubble',
            data: {
                datasets: jsonData.datasets
            },
            options: {
                aspectRatio: 1,
                legend:{
                    position: 'bottom'
                },
                scales: {
                    xAxes: [{
                        scaleLabel:{
                            display: true,
                            labelString: "Instability"
                        },
                        ticks: {
                            beginAtZero:true,
                            max: 1
                        }
                    }],
                    yAxes: [{
                        scaleLabel:{
                            display: true,
                            labelString: "Abstractness"
                        },
                        ticks: {
                            beginAtZero:true,
                            max: 1
                        }
                    }]
                },
                tooltips: {
                    callbacks: {
                        label: function(tooltipItem, data) {
                            var label = data.datasets[tooltipItem.datasetIndex].label;

                            if (tooltipItem.datasetIndex > data.datasets.length - 4){ // Label exceptions (zones and main sequence)
                                return label
                            }


                            label += ': (I = ';
                            label += Math.round(tooltipItem.xLabel * 100) / 100;
                            label += ', A = ';
                            label += Math.round(tooltipItem.yLabel * 100) / 100;
                            label += ') ';
                            return label;
                        }
                    }
                }
            }
        });

        chart.generateLegend();
    }
});