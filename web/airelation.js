$(function() {
    var ctx = document.getElementById("chart").getContext('2d');

    $.ajax({
        method: "GET",
        url: "http://localhost:8080",
        dataType: "json",
        success: function (data) {
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
                scales: {
                    xAxes: [{
                        ticks: {
                            beginAtZero:true,
                            max: 1
                        }
                    }],
                    yAxes: [{
                        ticks: {
                            beginAtZero:true,
                            max: 1
                        }
                    }]
                }
            }
        });
    }
});