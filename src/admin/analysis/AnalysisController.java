package admin.analysis;

import admin.AdminViewDrugDetailController;
import custom_cell.AlmostExpiredCustomCell;
import custom_cell.AlmostOutOfStockistViewCustomCell;
import data.db.DataExtractor;
import data.models.Builder;
import data.models.Drug;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import stocks.AddStockController;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class AnalysisController implements Initializable {

    @FXML private StackPane dailySalesGraphContainer;
    @FXML private StackPane monthSalesGraphContainer;
    @FXML private StackPane weeklySalGraphContainer;
    @FXML private StackPane yearlySaleGraphContainer;

    @FXML private BarChart<String, Double> yearlySalesBarChart;
    @FXML private BarChart<String, Double> monthlySalesBarChart;
    @FXML private BarChart<String, Double> weeklySalesBarChart;
    @FXML private BarChart<String, Double> dailySalesBarChart;

    //radio button for the yearly sales
    @FXML private RadioButton yearlySalesBarChartRadio;
    @FXML private RadioButton yearlySalesLineChartRadio;
    @FXML private RadioButton yearlySalesPieChartRadio;


    //radio buttons for the monthly sales
    @FXML private RadioButton monthlySalesBarChartRadio;
    @FXML private RadioButton monthlySalesPieChartRadio;
    @FXML private RadioButton monthlySalesLineChartRadio;

    //radio buttons for weekly sales
    @FXML private RadioButton weeklySalesBarChartRadio;
    @FXML private RadioButton weeklySalesLineChartRadio;
    @FXML private RadioButton weeklySalesPieChartRadio;

    //radio buttons for  daily sales;
    @FXML private RadioButton dailySalesBarChartRadio;
    @FXML private RadioButton dailySalesLineChartRadio;
    @FXML private RadioButton dailySalesPieChartRadio;

    ObservableList<XYChart.Data<String, Double>> yearlySalesData;
    ObservableList<XYChart.Data<String, Double>> monthlySalesData;
    ObservableList<XYChart.Data<String, Double>> weeklySalesData;
    ObservableList<XYChart.Data<String, Double>> dailySalesData;


    //labels for the text based analysis
    @FXML private Label mostSoldDrugLabel;
    @FXML private Label dailySalesAverageLabel;
    @FXML private Label weeklySalesAverageLabel;
    @FXML private Label monthlySalesAverageLabel;
    @FXML private Label yearlySalesAverageLabel;

    @FXML private ContextMenu expiredListViewContextMenu;
    @FXML private ListView<Drug> expiredListView;
    @FXML private ListView<Drug> outOfStockListView;
    @FXML private ContextMenu outOfStockListViewContextMenu;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        AnchorPane.setLeftAnchor(outOfStockListView.getParent(), ((VBox)expiredListView.getParent()).getPrefWidth() + 20);

        expiredListView.setCellFactory(param -> new AlmostExpiredCustomCell());
        outOfStockListView.setCellFactory(param -> new AlmostOutOfStockistViewCustomCell());

        //retrieve the various data's
        yearlySalesData = DataExtractor.getYearlySalesData();
        monthlySalesData = DataExtractor.getMonthlySalesData();
        weeklySalesData = DataExtractor.getWeeklySalesData();
        dailySalesData = DataExtractor.getDailySalesData();

        initGraphs();

        yearlySalesRadioButtonListeners();
        monthlySalesRadioButtonListeners();
        weeklySalesRadioButtonListeners();
        dailySaleRadioButtonListeners();

        loadExpiredData();
        loadOutOfStockData();

        dailySalesAverageLabel.setText(String.format("%.2f", DataExtractor.getDailySalesAverage()));
        weeklySalesAverageLabel.setText(String.format("%.2f", DataExtractor.getWeeklySalesAverage()));
        monthlySalesAverageLabel.setText(String.format("%.2f", DataExtractor.getMonthlySalesAverage()));
        yearlySalesAverageLabel.setText(String.format("%.2f", DataExtractor.getYearlySalesAverage()));
        mostSoldDrugLabel.setText(DataExtractor.getMostSoldDrug());

        //set the most Sold DrugLabel Mouse clicked
        mostSoldDrugLabel.getParent().setOnMousePressed(mousePressed ->{

           if(mousePressed.getClickCount() == 2){

               try {

                   Parent rankingsParent = FXMLLoader.load(getClass().getResource("/admin/analysis/views/drugsSoldRanking.fxml"));
                   Stage rankingsStage = new Stage(StageStyle.UTILITY);
                   rankingsStage.initModality(Modality.APPLICATION_MODAL);
                   rankingsStage.initOwner(mostSoldDrugLabel.getScene().getWindow());
                   rankingsStage.setScene(new Scene(rankingsParent));

                   rankingsStage.show();
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }

           }

        });

        dailySalesAverageLabel.getParent().setOnMousePressed(mousePressed->{
            if(mousePressed.getClickCount() == 2){
                openWindow("dailySalesSummary.fxml");
            }
        });

        weeklySalesAverageLabel.getParent().setOnMousePressed(mousePressed ->{

           if(mousePressed.getClickCount() == 2){
             openWindow("weeklySalesSummary.fxml");
           }

        });

        monthlySalesAverageLabel.getParent().setOnMousePressed(mousePressed ->{

            if(mousePressed.getClickCount() == 2){
              openWindow("monthlySalesSummary.fxml");
            }


        });

        yearlySalesAverageLabel.getParent().setOnMousePressed(mousePressed ->{

            if(mousePressed.getClickCount() == 2){
               openWindow("yearlySalesSummary.fxml");
            }

        });

    }

    private void initGraphs(){
        if(yearlySalesData.size() <= 1){
            yearlySalesPieChartRadio.setSelected(true);
            loadPieChartDataToGraph(yearlySaleGraphContainer, new PieChart(), yearlySalesData);
        }else{
            yearlySalesBarChartRadio.setSelected(true);
            loadBarChartDataToGraph(yearlySaleGraphContainer, yearlySalesBarChart, yearlySalesData);
        }

        if(monthlySalesData.size() <= 1){

            monthlySalesPieChartRadio.setSelected(true);
            loadPieChartDataToGraph(monthSalesGraphContainer, new PieChart(), monthlySalesData);

        }else{

            monthlySalesBarChartRadio.setSelected(true);
            loadBarChartDataToGraph(monthSalesGraphContainer, monthlySalesBarChart, monthlySalesData);
        }


        if(weeklySalesData.size() <= 1){
            weeklySalesPieChartRadio.setSelected(true);
            loadPieChartDataToGraph(weeklySalGraphContainer, new PieChart(), weeklySalesData);
        }else{
            weeklySalesBarChartRadio.setSelected(true);
            loadBarChartDataToGraph(weeklySalGraphContainer, weeklySalesBarChart, weeklySalesData);
        }

        dailySalesBarChartRadio.setSelected(true);
        loadBarChartDataToGraph(dailySalesGraphContainer, dailySalesBarChart, dailySalesData);

    }

    private void yearlySalesRadioButtonListeners(){

        yearlySalesBarChartRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {

                yearlySalesLineChartRadio.setSelected(false);
                yearlySalesPieChartRadio.setSelected(false);

                loadBarChartDataToGraph(yearlySaleGraphContainer, yearlySalesBarChart, yearlySalesData);
            }
        });

        yearlySalesLineChartRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {

                yearlySalesBarChartRadio.setSelected(false);
                yearlySalesPieChartRadio.setSelected(false);

                loadLineChartDataToGraph(yearlySaleGraphContainer,  (LineChart<String, Double>)
                        buildLineChart("Yearly Sales", "Years"), yearlySalesData);
            }
        });

        yearlySalesPieChartRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){

                yearlySalesLineChartRadio.setSelected(false);
                yearlySalesBarChartRadio.setSelected(false);
                loadPieChartDataToGraph(yearlySaleGraphContainer, new PieChart(), yearlySalesData);
            }
        });

    }

    private void monthlySalesRadioButtonListeners(){

        monthlySalesBarChartRadio.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){

                monthlySalesLineChartRadio.setSelected(false);
                monthlySalesPieChartRadio.setSelected(false);

                loadBarChartDataToGraph(monthSalesGraphContainer, monthlySalesBarChart, monthlySalesData);
            }

        });

        monthlySalesLineChartRadio.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){

                monthlySalesBarChartRadio.setSelected(false);
                monthlySalesPieChartRadio.setSelected(false);


                loadLineChartDataToGraph(monthSalesGraphContainer,
                        (LineChart<String, Double>) buildLineChart("Monthly Sales", "Months"), monthlySalesData);
            }

        });

        monthlySalesPieChartRadio.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){

                monthlySalesLineChartRadio.setSelected(false);
                monthlySalesBarChartRadio.setSelected(false);

                loadPieChartDataToGraph(monthSalesGraphContainer, new PieChart(), monthlySalesData);
            }

        });

    }

    private void weeklySalesRadioButtonListeners(){

        weeklySalesBarChartRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue){

                weeklySalesLineChartRadio.setSelected(false);
                weeklySalesPieChartRadio.setSelected(false);

                loadBarChartDataToGraph(weeklySalGraphContainer, weeklySalesBarChart, weeklySalesData);
            }

        });

        weeklySalesLineChartRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue){

                weeklySalesBarChartRadio.setSelected(false);
                weeklySalesPieChartRadio.setSelected(false);


                loadLineChartDataToGraph(weeklySalGraphContainer, (LineChart<String, Double>)
                        buildLineChart("Weekly Sales", "Weeks"), weeklySalesData);
            }

        });

        weeklySalesPieChartRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue){

                weeklySalesLineChartRadio.setSelected(false);
                weeklySalesBarChartRadio.setSelected(false);

                loadPieChartDataToGraph(weeklySalGraphContainer, new PieChart(), weeklySalesData);

            }

        });

    }

    private void dailySaleRadioButtonListeners(){

        dailySalesBarChartRadio.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){

                dailySalesLineChartRadio.setSelected(false);
                dailySalesPieChartRadio.setSelected(false);

                loadBarChartDataToGraph(dailySalesGraphContainer, dailySalesBarChart, dailySalesData);
            }

        });

        dailySalesLineChartRadio.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){

                dailySalesBarChartRadio.setSelected(false);
                dailySalesPieChartRadio.setSelected(false);

                loadLineChartDataToGraph(dailySalesGraphContainer,  (LineChart<String, Double>)
                        buildLineChart("Daily Sales for this year", "Days"), dailySalesData);
            }

        });

        dailySalesPieChartRadio.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){

                dailySalesLineChartRadio.setSelected(false);
                dailySalesBarChartRadio.setSelected(false);

                PieChart pieChart = new PieChart();
                loadPieChartDataToGraph(dailySalesGraphContainer, pieChart, dailySalesData);
            }

        });

    }


    private void loadBarChartDataToGraph(StackPane graphParent, BarChart<String, Double> barChart,
                                         ObservableList<XYChart.Data<String, Double>> dataList){
        barChart.getData().clear();
        graphParent.getChildren().clear();

        Series<String, Double> series = new Series<>();
        series.getData().addAll(dataList);
        series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));

        barChart.getData().setAll(series);
        graphParent.getChildren().add(barChart);


    }

    private void loadLineChartDataToGraph(StackPane graphParent, LineChart<String, Double> lineChart,
                                          ObservableList<XYChart.Data<String, Double>> dataList){


       graphParent.getChildren().clear();

        Series<String, Double> series = new Series<>();
        series.getData().addAll(dataList);
        series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));

        lineChart.getData().setAll(series);
        graphParent.getChildren().add(lineChart);

    }

    private void loadPieChartDataToGraph(StackPane graphParent, PieChart pieChart,
                                         ObservableList<XYChart.Data<String, Double>> dataList){

        //remove the current graph node from the graph

        graphParent.getChildren().clear();

        for(XYChart.Data<String, Double> rawData: dataList){
            pieChart.getData().add(new PieChart.Data(rawData.getXValue(), rawData.getYValue()));
        }

        pieChart.getData().sort(Comparator.comparing(PieChart.Data::getName));

        graphParent.getChildren().add(pieChart);
    }
    private LineChart<String, ?> buildLineChart(String title, String categoryAxisLabel){

        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabel("Total");

        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setLabel(categoryAxisLabel);

        LineChart<String, ?> lineChart = new LineChart<>(categoryAxis, numberAxis);
        lineChart.setTitle(title);

        return lineChart;
    }

    private void loadExpiredData(){
        ObservableList<Drug> expiredDrugsList = DataExtractor.getAlmostExpiredDrugs();
        expiredListView.setItems(expiredDrugsList);
    }

    private void loadOutOfStockData(){
        ObservableList<Drug> outOfStockDrugsList = DataExtractor.getAlmostOutOfStockDrugs(true);
        outOfStockListView.setItems(outOfStockDrugsList);
    }

    //functions for the context Menus of the list View
    @FXML private void updateDrugQuantity(ActionEvent event) throws IOException {

        Drug selectedDrug = outOfStockListView.getSelectionModel().getSelectedItem();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/adminViewDrugDetail.fxml"));
        Parent parent = loader.load();

        AdminViewDrugDetailController adminViewDrugDetailController = loader.getController();
        adminViewDrugDetailController.outOfStockViewManualInitialize(selectedDrug, "Update Drug Quantity");

        Stage stage = new Stage();
        stage.initOwner(outOfStockListView.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(parent));

        stage.setOnHidden(stageHidden ->{
            loadOutOfStockData();
        });

        stage.show();

    }

    @FXML private void expiredListViewContextMenuRequested(ContextMenuEvent event){

        if(expiredListView.getItems().isEmpty()){
            expiredListViewContextMenu.hide();
            return;
        }
        expiredListViewContextMenu.show(expiredListView, event.getScreenX(), event.getScreenY());

    }

    @FXML private void outOfListViewContextMenuRequested(ContextMenuEvent event){

        if(outOfStockListView.getItems().isEmpty()){
            outOfStockListViewContextMenu.hide();
            return;
        }

        outOfStockListViewContextMenu.show(expiredListView, event.getScreenX(), event.getScreenY());

    }
    @FXML private void addNewDrugBatch(ActionEvent event)throws IOException{

        Drug drug = expiredListView.getSelectionModel().getSelectedItem();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/stocks/addStock.fxml"));
        Parent parent = loader.load();

        AddStockController addController = loader.getController();
        addController.manualInitialize(drug);

        Stage addBatchStage = new Stage(StageStyle.UTILITY);
        addBatchStage.initModality(Modality.APPLICATION_MODAL);
        addBatchStage.setScene(new Scene(parent));

        addBatchStage.setOnHiding(stageHiding ->{
            loadExpiredData();
        });

        addBatchStage.show();
    }

    //if the administrator don't wanna add new stock to the drugs list, choose of dispose of the drug
    //what this method does is that disposes of the quantity of the expired drug
    @FXML private void disposeDrug(ActionEvent event){

        Drug selectedDrug = expiredListView.getSelectionModel().getSelectedItem();

        if(DataExtractor.updateDrugQuantity(selectedDrug.getId(), 0)){
            loadExpiredData();
            loadOutOfStockData();
        }

    }


    private void openWindow(String location){

        try {
            String summaryDirectoryLocation = "/admin/analysis/views/summary/";

            Parent parent = FXMLLoader.load(getClass().getResource(summaryDirectoryLocation + location));
            Scene scene = new Scene(parent);
            Stage stage = new Stage(StageStyle.UTILITY);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Builder.defaultAppImage));

            stage.setTitle(getTitle(location));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private String getTitle(String location){

        String title = "Pharmacy 404 - Admin Panel/";

        switch (location) {
            case "dailySalesSummary.fxml":
                title = title + "Daily Sales Summary";
                break;
            case "weeklySalesSummary.fxml":
                title = title + "Weekly Sale Summary";
                break;
            case "monthlySalesSummary.fxml":
                title = title + "Monthly Sale Summary";
                break;
            case "yearlySalesSummary.fxml":
                title = title + "Yearly Sale Summary";
                break;
        }

        return title;
    }
}
