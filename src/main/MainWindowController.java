package main;

import admin.auth.AdminAuthenticationController;
import custom_cell.AlmostExpiredCustomCell;
import custom_cell.AlmostOutOfStockistViewCustomCell;
import data.db.DataExtractor;
import data.models.Builder;
import data.models.Drug;
import data.models.encryption.Cryptography;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import stocks.DrugStockController;
import sales.SalesController;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML public BorderPane mainBorderPane;
    @FXML private VBox navigationBox;
    @FXML protected AnchorPane dashBoardPane;
    @FXML private Label dashBoardSalesLabel;
    @FXML private LineChart<String, Double> lineChart;
    @FXML private ListView<Drug> outOfStockListView;
    @FXML private ListView<Drug> expiredListView;
    @FXML private Label thisWeekSalesLabel;
    @FXML private Label thisMonthSaleTextField;
    @FXML private Label outOfStockHeaderLabel;
    @FXML private Label almostExpiredHeaderLabel;
    @FXML private CategoryAxis xaxis;

    @FXML private VBox listParent;
    @FXML private StackPane graphParent;

    //this variable is to ensure that the admin panel is not reloaded once the user loads the admin panel to the center
    private boolean isCenterEqualsAdmin;

    static final String[]  months = {"January", "February", "March", "April", "May", "June", "July", "August",
                                     "September", "October", "November", "December"};

    private char dataShowing;
    private ObservableList<Drug> almostOutOfStockDrugs;
    private ObservableList<Drug> almostExpiredDrugs;

    private double graphParentTopAnchor;
    private double graphParentRightAnchor;

    private AnchorPane salesPane;
    private AnchorPane stocksPane;
    private VBox adminAuthBox;

    Node currentCenterNode;
    private DrugStockController drugStockController;
    private SalesController salesController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        graphParentTopAnchor = AnchorPane.getTopAnchor(graphParent);
        graphParentRightAnchor = AnchorPane.getRightAnchor(graphParent);

        createMainWindowPanes();
        setAmounts();
        initDailySalesGraph();
        loadDrugsAlertLists();

        expiredListView.setCellFactory(param -> new AlmostExpiredCustomCell());
        outOfStockListView.setCellFactory(param -> new AlmostOutOfStockistViewCustomCell());

        setCurrentCenterNode(dashBoardPane);
        mainBorderPane.centerProperty().addListener((observable, oldValue, newValue) -> {


            if(newValue.equals(dashBoardPane)){
                setCurrentCenterNode(newValue);
                setAmounts();
                initDailySalesGraph();
                loadDrugsAlertLists();
            }

            if(mainBorderPane.getCenter().equals(dashBoardPane) || mainBorderPane.getCenter().equals(salesPane) ||
                    mainBorderPane.getCenter().equals(stocksPane) || mainBorderPane.getCenter().equals(adminAuthBox)){
                setCurrentCenterNode(newValue);

                salesController.loadData();
                drugStockController.manualInitialize();

                try{
                    mainBorderPane.setLeft(navigationBox);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }else{
                mainBorderPane.setLeft(null);
            }


        });

        expiredListView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                expiredListView.getSelectionModel().select(null);
            }
        });

        outOfStockListView.focusedProperty().addListener((observable, oldValue, newValue) ->{
            if(!newValue){
                outOfStockListView.getSelectionModel().select(null);
            }
        });

        lineChart.setOnMouseClicked(mouseClickedEvent ->{

            boolean isChartClickedTwice = mouseClickedEvent.getClickCount() == 2;

            if(isChartClickedTwice){

                switch (dataShowing){
                    case 'd':
                        initWeeklySalesGraph();
                        break;

                    case 'w':
                        initMonthlySalesGraph();
                        break;

                    case 'm':
                        initDailySalesGraph();
                        break;
                }

            }

        });

    }

    private void initDailySalesGraph(){

        lineChart.getData().clear();

        XYChart.Series<String, Double> dailySalesChartSeries = new XYChart.Series<>();
        dailySalesChartSeries.getData().addAll(DataExtractor.getDailySalesData());

        dailySalesChartSeries.getData().sort(Comparator.comparing(XYChart.Data::getXValue));

        xaxis.setLabel("Days");
        lineChart.setTitle("Daily Sales");
        lineChart.getData().setAll(dailySalesChartSeries);
        dataShowing = 'd';
    }

    private void initWeeklySalesGraph(){

        lineChart.getData().clear();
        XYChart.Series<String, Double> weeklySaleChartSeries = new XYChart.Series<>();
        weeklySaleChartSeries.getData().addAll(DataExtractor.getWeeklySalesData());
        weeklySaleChartSeries.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        xaxis.setLabel("Weeks");
        lineChart.setTitle("Weekly Sales");
        lineChart.getData().setAll(weeklySaleChartSeries);
        dataShowing = 'w';

    }

    private void initMonthlySalesGraph(){

        XYChart.Series<String, Double> monthlySalesSeries = new XYChart.Series<>();
        monthlySalesSeries.getData().addAll(DataExtractor.getMonthlySalesData());
        monthlySalesSeries.getData().sort(Comparator.comparing(XYChart.Data::getXValue));

        xaxis.setLabel("Months");
        lineChart.setTitle("Monthy Sales");
        lineChart.getData().setAll(monthlySalesSeries);
        dataShowing = 'm';

    }

    private void setAmounts(){

        Double dailySales = DataExtractor.getDailySalesTotal();
        Double weeklySales = DataExtractor.getWeeklySalesTotal();
        Double pastMonthSales = DataExtractor.getThisMonthSalesTotal();
        this.dashBoardSalesLabel.setText(String.format("%.2f", dailySales));
        thisWeekSalesLabel.setText(String.format("%.2f", weeklySales));
        this.thisMonthSaleTextField.setText(String.format("%.2f", pastMonthSales));

    }

    private void loadDrugsAlertLists(){

        almostExpiredDrugs = DataExtractor.getAlmostExpiredDrugs();

        if(almostExpiredDrugs.size() > 0){
            expiredListView.setItems(almostExpiredDrugs);
            try{
                VBox.setMargin(expiredListView, new Insets(5));
                listParent.getChildren().addAll(almostExpiredHeaderLabel, expiredListView);
            }catch (Exception ex){
                //do nothing
            }
        }else{

            try {
                listParent.getChildren().removeAll(almostExpiredHeaderLabel, expiredListView);
            }catch (Exception ex){
                //do mother fucking nothing
            }

        }


        almostOutOfStockDrugs = DataExtractor.getAlmostOutOfStockDrugs(false);

        if(almostOutOfStockDrugs.size() > 0){
            outOfStockListView.setItems(almostOutOfStockDrugs);

            try{
                VBox.setMargin(outOfStockListView, new Insets(5));
                listParent.getChildren().addAll(outOfStockHeaderLabel, outOfStockListView);
            }catch (Exception ex){
                //do nothing
            }

        }else{
            try{
                listParent.getChildren().removeAll(outOfStockHeaderLabel, outOfStockListView);
            }catch(Exception ex){
                //do motherfucking nothing here
            }

        }


        if(listParent.getChildren().isEmpty()){
            dashBoardPane.getChildren().remove(listParent);
            AnchorPane.setRightAnchor(graphParent, 5.0);
        }else{

            try{
                AnchorPane.clearConstraints(graphParent);

                AnchorPane.setTopAnchor(graphParent, this.graphParentTopAnchor);
                AnchorPane.setRightAnchor(graphParent, this.graphParentRightAnchor);
                AnchorPane.setBottomAnchor(graphParent, 5.0);
                AnchorPane.setLeftAnchor(graphParent, 5.0);

                dashBoardPane.getChildren().add(listParent);
                AnchorPane.setTopAnchor(listParent, this.graphParentTopAnchor);
                AnchorPane.setRightAnchor(listParent, 5.0);
                AnchorPane.setBottomAnchor(listParent, 5.0);


            }catch (Exception ex){
                //do nothing
            }

        }


    }

    @FXML private void handleDashBoard(ActionEvent event){

        if(!mainBorderPane.getCenter().equals(dashBoardPane)){
            isCenterEqualsAdmin = false;
            mainBorderPane.setCenter(dashBoardPane);
            setAmounts();
            setCurrentCenterNode(dashBoardPane);
        }
    }

    @FXML private void handleSales(ActionEvent event) throws IOException {

        if(!mainBorderPane.getCenter().equals(salesPane)){
            isCenterEqualsAdmin = false;
            mainBorderPane.setCenter(salesPane);
            salesController.loadData();
            setCurrentCenterNode(salesPane);
        }

    }

    @FXML private void handleStock(ActionEvent event)throws IOException{

        if(!mainBorderPane.getCenter().equals(stocksPane)){
            isCenterEqualsAdmin = false;
            drugStockController.manualInitialize();
            mainBorderPane.setCenter(stocksPane);
            setCurrentCenterNode(stocksPane);
        }
    }

    @FXML private void handleAdminButtonPressed(ActionEvent event) throws IOException {

        if(!mainBorderPane.getCenter().equals(adminAuthBox) && !isCenterEqualsAdmin){
            isCenterEqualsAdmin = true;
            mainBorderPane.setCenter(adminAuthBox);
            setCurrentCenterNode(dashBoardPane);
        }


    }

    @FXML private void handleAboutSoftware(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/about/aboutSoftware.fxml"));
        VBox parent = loader.load();

        Scene scene = new Scene(parent);
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(mainBorderPane.getScene().getWindow());
        stage.setTitle("Pharmacy 404 - About");
        stage.getIcons().add(new Image(Builder.defaultAppImage));

        stage.setOnShowing(showing-> mainBorderPane.setEffect(new BoxBlur(5, 5, 5)));
        stage.setOnHidden(hidden -> mainBorderPane.setEffect(null));

        stage.show();
    }

    private void createMainWindowPanes(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/stocks/drugStock.fxml"));
        try {
            stocksPane = loader.load();
            drugStockController = loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loader = new FXMLLoader(getClass().getResource("/sales/sales.fxml"));
        try {
            salesPane = loader.load();
            salesController = loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loader = new FXMLLoader(getClass().getResource("/admin/auth/adminAuthentication.fxml"));
        try {
            adminAuthBox = loader.load();

            AdminAuthenticationController adminAuthController = loader.getController();
            adminAuthController.manualIntialize(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static String getMonth(int i){
        return months[i];
    }

    public Node getCurrentCenterNode() {
        return currentCenterNode;
    }

    public void setCurrentCenterNode(Node currentCenterNode) {
        this.currentCenterNode = currentCenterNode;
    }
}
