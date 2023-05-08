package admin;

import alert.AlertViewController;
import custom_cell.CustomAmountTableCell;
import custom_cell.CustomDateTableCell;
import data.db.DataExtractor;
import data.db.DatabaseConnector;
import data.models.Builder;
import data.models.Drug;
import data.models.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sales.EditSaleController;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class AdminSalesController implements Initializable {

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> idCol;
    @FXML private TableColumn<Sale, LocalDate> dateCol;
    @FXML private TableColumn<Sale, Integer> quantityCol;
    @FXML private TableColumn<Sale, String> drugNameCol;
    @FXML private TableColumn<Sale, Double> unitPriceCol;
    @FXML private TableColumn<Sale, Double> amountCol;
    @FXML private ContextMenu contextMenu;
    @FXML private TextField totalTextField;
    @FXML private RadioButton allTimeRadioButton;
    @FXML private RadioButton todaySalesRadioButton;
    @FXML private RadioButton selectDateRadioButton;
    @FXML private DatePicker saleDatePicker;
    @FXML private Button clearSelectedSalesButton;

    ObservableList<Sale> allTimeSales = FXCollections.observableArrayList();
    ObservableList<Sale> selectedDateSales = FXCollections.observableArrayList();
    ObservableList<Sale> todaySales = FXCollections.observableArrayList();

    private Set<String> drugNameSet = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initializeTableColumns();
        createAlert();

        saleDatePicker.setValue(LocalDate.now());
        allTimeRadioButton.setSelected(true);
        saleDatePicker.disableProperty().bind(selectDateRadioButton.selectedProperty().not());

        saleDatePicker.valueProperty().addListener((observable, oldValue, newValue) ->{
            loadSelectedDateSalesData(Date.valueOf(newValue));
            calculateTotal();
        });

        salesTable.setOnContextMenuRequested(menuReqeusted ->{

            if(salesTable.getItems().isEmpty()){
                contextMenu.hide();
                return;
            }

            contextMenu.show(salesTable, menuReqeusted.getScreenX(), menuReqeusted.getScreenY());

        });

        clearSelectedSalesButton.disableProperty().bind(selectDateRadioButton.selectedProperty().not());

        loadAllTimeSalesData();
        loadDrugNames();

    }

    private void initializeTableColumns(){
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        drugNameCol.setCellValueFactory(new PropertyValueFactory<>("drugName"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        dateCol.setCellFactory(param -> new CustomDateTableCell());
        unitPriceCol.setCellFactory(param -> new CustomAmountTableCell());
        amountCol.setCellFactory(param -> new CustomAmountTableCell());
    }

    private void loadAllTimeSalesData(){
        allTimeSales.clear();

        String query = "select * from sales";

        try {
           ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();

           while (rs.next()){

               int saleId = rs.getInt("id");
               Date saleDate = rs.getDate("sale_date");
               int quantity = rs.getInt("quantity");
               int drugId = rs.getInt("drug_id");
               String drugName = rs.getString("drug_name");
               double price = rs.getDouble("price");
               double amount = rs.getDouble("amount");

               allTimeSales.add(
                       new Sale(saleId,
                               LocalDate.parse(saleDate.toString()),
                               quantity, drugId, drugName, price, amount));

           }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        salesTable.setItems(allTimeSales);
        calculateTotal();

    }

    private void calculateTotal(){

        double total = 0;
        for(Sale sale: salesTable.getItems()){
            total = total + sale.getAmount();
        }

        totalTextField.setText(String.format("%.2f", total));

    }

    private void loadSelectedDateSalesData(Date selectedDate){
        selectedDateSales.clear();
        selectedDateSales.addAll(DataExtractor.getSales(selectedDate));
        salesTable.setItems(selectedDateSales);
    }

    @FXML
    void handleAllTimeSalesView(ActionEvent event) {
        selectDateRadioButton.setSelected(false);
        todaySalesRadioButton.setSelected(false);
        try{
            salesTable.setItems(allTimeSales);
            calculateTotal();
        }catch (Exception ex){
            //do nothing
        }

    }

    @FXML
    void handleSelectDateSalesView(ActionEvent event) {
        allTimeRadioButton.setSelected(false);
        todaySalesRadioButton.setSelected(false);
    }

    @FXML private void handleSelectTodaySalesView(ActionEvent event){
        allTimeRadioButton.setSelected(false);
        selectDateRadioButton.setSelected(false);

        todaySales.clear();
        todaySales.addAll(DataExtractor.getSales(Date.valueOf(LocalDate.now())));
        salesTable.setItems(todaySales);
        calculateTotal();
    }

    @FXML private void clearSalesData(ActionEvent event){

        //show the warninng alert
        alertController.alertViewSetup(AlertViewController.AlertStyle.WARNING, "Clear Sales Record",
                "Clearing sales records will delete all sales from the database. This action is irreversible." +
                        "\n\nNOTE THAT: However, the sales records for today will not be cleared or deleted from the " +
                        "esales table in the database." +
                        "\n Do you wish to continue?");

        alertController.confirmButton.setText("Yes, clear Data");
        alertController.cancelButton.setText("No, Cancel");

        alertController.confirmButton.setOnAction(alertButtonClicked ->{
            if(DataExtractor.clearAllSalesExceptToday()){
               showInformativeAlert(AlertViewController.AlertStyle.INFORMATION, "Success",
                       "Sales has been cleared from the database");

               alertStage.setOnHiding(stageHiding ->{

                   if(allTimeRadioButton.isSelected()){
                       loadAllTimeSalesData();
                   }else if(selectDateRadioButton.isSelected()){
                       try{
                           loadSelectedDateSalesData(Date.valueOf(this.saleDatePicker.getValue()));
                       }catch (Exception ex){

                       }
                   }

               });
            }else{
                showInformativeAlert(AlertViewController.AlertStyle.ERROR, "Error",
                        "An unexpected Error is keeping you from executing this operation");
            }
        });

        alertStage.show();

    }

    @FXML private void clearSelectedSalesData(ActionEvent event){


        if(saleDatePicker.getValue().equals(LocalDate.now())){

            showInformativeAlert(AlertViewController.AlertStyle.ERROR, "Error",
                    "Cannot delete today's sales record from the database");

            alertStage.show();

            return;
        }

        alertController.alertViewSetup(AlertViewController.AlertStyle.WARNING, "Clear Sales Record",
                String.format("Do you want to clear sales on that occurred on %s from the database?" +
                        "\n\n Note that this action cannot be reversed", Builder.buildDateString(saleDatePicker.getValue())));

        alertController.confirmButton.setText("Yes, clear Data");
        alertController.cancelButton.setText("No, Cancel");

        alertController.confirmButton.setOnAction(alertButtonClicked ->{
            if(DataExtractor.clearSalesExceptToday(saleDatePicker.getValue())){
                showInformativeAlert(AlertViewController.AlertStyle.INFORMATION, "Success",
                        String.format("Sales that occurred on %s has been cleared from the database",
                                Builder.buildDateString(saleDatePicker.getValue())));

                alertStage.setOnHiding(stageHiding ->{

                    if(allTimeRadioButton.isSelected()){
                        loadAllTimeSalesData();
                    }else if(selectDateRadioButton.isSelected()){
                        try{
                            loadSelectedDateSalesData(Date.valueOf(this.saleDatePicker.getValue()));
                        }catch (Exception ex){

                        }
                    }

                });
            }else{
                showInformativeAlert(AlertViewController.AlertStyle.ERROR, "Error",
                        "An unexpected Error is keeping you from executing this operation");
            }
        });

        alertStage.show();


    }

    private void showInformativeAlert(AlertViewController.AlertStyle alertStyle, String title, String message){
        alertController.alertViewSetup(alertStyle, title, message, true);
    }

    @FXML
    void handleEdit(ActionEvent event) throws IOException {

        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();

        if(selectedSale == null){
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sales/editSales.fxml"));
        Parent editSaleParent = loader.load();

        EditSaleController editSaleController = loader.getController();
        editSaleController.inflateUI(selectedSale, drugNameSet);

        Scene scene = new Scene(editSaleParent);
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(scene);
        stage.setTitle("Pharmacy404 -admin/Edit Sale");
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setOnHiding(stageHiding -> loadData());

        stage.show();

    }

    @FXML
    void removeItem(ActionEvent event) {

        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();

        if(!selectedSale.getSaleDate().equals(LocalDate.now())){

            if(DataExtractor.removeSale(selectedSale.getId())){
                loadData();
            }

            return;
        }

        int saleId = selectedSale.getId();
        String drugName = selectedSale.getDrugName();
        String saleDate = selectedSale.getSaleDate().toString();

        alertController.alertViewSetup(AlertViewController.AlertStyle.WARNING, "Delete Sale Record",
                String.format("Sale ID: %d\n" +
                        "Item Purchased: %s\n" +
                        "Sale Date: %s(Today)\n\n" +
                        "Would you like to remove the selected sale record from the database " +
                        "or treat it as a return ?", saleId, drugName, saleDate));

        Button treatAsReturnButton = new Button("Treat as return");
        treatAsReturnButton.setPrefHeight(-1);
        alertController.buttonContainers.getChildren().add(1, treatAsReturnButton);
        HBox.setMargin(treatAsReturnButton, new Insets(0, 5, 0, 5));
        HBox.setHgrow(treatAsReturnButton, Priority.ALWAYS);

        treatAsReturnButton.setOnAction(buttonOnAction ->{
            treatSaleAsReturn(selectedSale);
        });

        alertController.confirmButton.setText("Delete Sale Record");
        alertController.confirmButton.setOnAction(confirmButtonAction ->{

            if(DataExtractor.removeSale(salesTable.getSelectionModel().getSelectedItem().getId())){
                loadData();
                alertStage.close();
            }

        });

        alertStage.show();

    }

    private void treatSaleAsReturn(Sale sale){

        int drugId = sale.getDrugId();
        Drug drug = DataExtractor.getDrug(drugId);

        int newQuantity = drug.getQuantity() + sale.getQuantity();

        if(DataExtractor.removeSale(sale.getId()) && DataExtractor.updateDrugQuantity(drugId, newQuantity)){
            loadData();

           try{
               alertController.buttonContainers.getChildren().remove(1);
           }catch (Exception ex){
               //do nothing
           }

            alertController.confirmButton.setText("Okay");
            alertController.confirmButton.getStyleClass().remove("cancel-button");
            alertController.alertViewSetup(AlertViewController.AlertStyle.INFORMATION, "Sale Removal Sucees",
                    String.format("Sales remove has been treated as return hence the quantity of drug, '%s' " +
                            "has been updated to stock", sale.getDrugName()), true);
        }

    }

    private void loadDrugNames(){

        String query = "select drug_name from drugs";

        try {
            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()){
                String drugName = rs.getString("drug_name");
                drugNameSet.add(drugName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadData(){

        if(allTimeRadioButton.isSelected()){
            loadAllTimeSalesData();
        }else if(selectDateRadioButton.isSelected()){
            loadSelectedDateSalesData(Date.valueOf(saleDatePicker.getValue()));
        }else if(todaySalesRadioButton.isSelected()){
            loadSelectedDateSalesData(Date.valueOf(LocalDate.now()));
        }

    }

    AlertViewController alertController;
    Stage alertStage;

    private void createAlert(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/alert/alertView.fxml"));
        try {
            AnchorPane parent = loader.load();
            alertController = loader.getController();

            alertStage = new Stage();
            alertStage.setScene(new Scene(parent));
            alertStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
