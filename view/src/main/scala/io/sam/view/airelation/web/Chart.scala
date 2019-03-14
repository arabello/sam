package io.sam.view.airelation.web

case class Chart(title: String,
                xAxisLabel: String,
                yAxisLabel: String,
                datasets: Seq[Dataset])
