package io.sam.view.airelation

case class Chart(title: String,
                xAxisLabel: String,
                yAxisLabel: String,
                datasets: Seq[Dataset])
