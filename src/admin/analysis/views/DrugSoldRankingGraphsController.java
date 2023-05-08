package admin.analysis.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class DrugSoldRankingGraphsController implements Initializable {

    @FXML private BarChart<String, Number> barChart;
    @FXML private StackPane chartContainer;
    @FXML private ComboBox<String> chartTypeComboBox;

    private PieChart pieChart;
    ObservableList<String> chartypes = FXCollections.observableArrayList("Bar Chart", "Pie Chart");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chartTypeComboBox.setItems(chartypes);
        chartTypeComboBox.getSelectionModel().select(0);
    }

    public void manualInitialize(ObservableList<XYChart.Data<String, Number>> drugsRankingsData){

        XYChart.Series<String, Number> drugRankingsSeries= new XYChart.Series<>(drugsRankingsData);
        drugRankingsSeries.getData().sort(Comparator.comparing(data ->(Integer) data.getYValue()));

        barChart.getData().setAll(drugRankingsSeries);

        buildPieChart(drugsRankingsData);
        comboBoxListener();
    }

    public void buildPieChart(ObservableList<XYChart.Data<String, Number>> drugsRankingsData){

        pieChart = new PieChart();

        for(XYChart.Data<String, Number> rawData: drugsRankingsData){
            pieChart.getData().add(new PieChart.Data(rawData.getXValue(), rawData.getYValue().doubleValue()));
        }

    }

    private void comboBoxListener(){
        chartTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) ->{

            chartContainer.getChildren().clear();

            switch (newValue.toLowerCase()){
                case "bar chart":
                    chartContainer.getChildren().add(barChart);
                    break;

                case "pie chart":
                    chartContainer.getChildren().add(pieChart);
                    break;
            }

        });
    }


}
