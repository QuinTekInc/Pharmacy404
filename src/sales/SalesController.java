package sales;

import alert.AlertViewController;
import custom_cell.CustomAmountTableCell;
import data.db.DataExtractor;
import data.db.DatabaseConnector;
import data.models.Builder;
import data.models.Drug;
import data.models.Sale;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

public class SalesController implements Initializable {

    @FXML private DatePicker localDatePicker;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> quantityCol;
    @FXML private TableColumn<Sale, String> drugNameCol;
    @FXML private ComboBox<String> drugTypeComboBox;
    @FXML private ComboBox<String> manufacturerComboBox;
    @FXML private ComboBox<String> manufactureDateComboBox;
    @FXML private TableColumn<Sale, Double> unitPriceCol;
    @FXML private TableColumn<Sale, Double> amountCol;
    @FXML private ContextMenu salesTableContextMenu;
    @FXML private TextField drugNameTextField;
    @FXML private TextField quantityTextField;
    @FXML private TextField priceTextField;
    @FXML private TextField amountTextField;
    @FXML private TextField totalTextField;
    @FXML private Button addButton;

    //adding set for drug names
    Set<String> drugNameSet = new HashSet<>();
    Set<String> manufacturerSet = new HashSet<>();
    Set<String> drugTypeSet = new HashSet<>();
    Set<String> batchDateSet = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        localDatePicker.setValue(LocalDate.now());
        localDatePicker.setDisable(true);
        addButton.setDisable(true);

        initTooltip();

        initTableColumns();
        loadData();
        textFieldListeners();
        loadDrugNames();

        createAlert();

        TextFields.bindAutoCompletion(drugNameTextField, drugNameSet);

        amountTextField.setDisable(true);
        quantityTextField.textProperty().addListener(observable -> handleCalucations());
        priceTextField.textProperty().addListener(observable -> handleCalucations());
    }

    private void initTableColumns(){

        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        drugNameCol.setCellValueFactory(new PropertyValueFactory<>("drugName"));

        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        unitPriceCol.setCellFactory(param -> new CustomAmountTableCell());

        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(param -> new CustomAmountTableCell());
    }

    private void initTooltip(){

        drugNameTextField.setTooltip(new Tooltip("Drug Name"));
        drugTypeComboBox.setTooltip(new Tooltip("Drug Types"));
        manufacturerComboBox.setTooltip(new Tooltip("Manufacturers"));
        manufactureDateComboBox.setTooltip(new Tooltip("Batch or Manufacture Date"));
        quantityTextField.setTooltip(new Tooltip("Quantity"));
        priceTextField.setTooltip(new Tooltip("Retail Price of Drug"));
        amountTextField.setTooltip(new Tooltip("Amount"));

    }

    private void textFieldListeners(){

        drugNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(areFieldsEmpty());

            loadManufacturers(newValue);
            getDrugBatches(newValue, drugTypeComboBox.getValue(), manufacturerComboBox.getValue());
            try{
                loadAvailableDrugType(newValue, manufacturerComboBox.getValue());
                getDrugPrice(newValue, manufacturerComboBox.getValue(), drugTypeComboBox.getValue());
            }catch (Exception ex){
                //do nothing
            }

        });

        manufacturerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            loadAvailableDrugType(drugNameTextField.getText(), newValue);
            getDrugBatches(drugNameTextField.getText(), drugTypeComboBox.getValue(), newValue);
            try {
                getDrugPrice(drugNameTextField.getText(), newValue, drugTypeComboBox.getValue());
            }catch (Exception ex){
                //do fucking nothing as usual
            }

        });

        drugTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            getDrugPrice(drugNameTextField.getText(), manufacturerComboBox.getValue(), newValue);
            getDrugBatches(drugNameTextField.getText(), newValue, manufacturerComboBox.getValue());
        });


        priceTextField.textProperty().addListener(observable -> {
           addButton.setDisable(areFieldsEmpty());
        });

        quantityTextField.textProperty().addListener(observable -> {
            addButton.setDisable(areFieldsEmpty());
        });

    }

    boolean areFieldsEmpty(){
        boolean flag = false;

        if(drugNameTextField.getText().isEmpty() ||
                priceTextField.getText().isEmpty() ||
                quantityTextField.getText().isEmpty()){
           flag = true;
        }

        return flag;
    }

    public void loadData(){
        List<Sale> sales = DataExtractor.getSales(Date.valueOf(LocalDate.now()));
        salesTable.getItems().setAll(sales);
        this.calculateTotalSales();

        loadDrugNames();
        TextFields.bindAutoCompletion(drugNameTextField, drugNameSet);

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

    private void loadManufacturers(String drugName){

        manufacturerSet.clear();
        manufacturerComboBox.getItems().clear();

        String query = "select manufacturer from drugs where drug_name = ?";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drugName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                String manufacturer = rs.getString("manufacturer");
                manufacturerSet.add(manufacturer);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(String manufacturer: manufacturerSet){
            manufacturerComboBox.getItems().add(manufacturer);
        }

        //after it has added the values to the comboBox, select the first one
        manufacturerComboBox.getSelectionModel().select(0);

    }

    private void loadAvailableDrugType(String drugName, String manufacturer){

        drugTypeSet.clear();
        drugTypeComboBox.getItems().clear();

        String query = "select drug_type from drugs where drug_name = ? and manufacturer = ?";

        try{
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drugName);
            ps.setString(2, manufacturer);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                String drugType = rs.getString("drug_type");
                drugTypeSet.add(drugType);
            }
        }catch (SQLException ex){
            throw new RuntimeException(ex);
        }

        for(String dtype: drugTypeSet){
            drugTypeComboBox.getItems().add(dtype);
        }

        //selecting the first item
        drugTypeComboBox.getSelectionModel().select(0);
    }

    private void getDrugPrice(String drugName, String manufacturer, String drugType){

        String query = "select retail_price from drugs where drug_name = ? and manufacturer = ? and drug_type = ?";

        try{
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drugName);
            ps.setString(2, manufacturer);
            ps.setString(3, drugType);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                double price = rs.getDouble("retail_price");
                priceTextField.setText(String.format("%.2f", price));
            }
        }catch (SQLException ex){
            throw new RuntimeException(ex);
        }

    }

    private void getDrugBatches(String drugName, String drugType, String manufacturer){

        batchDateSet.clear();
        manufactureDateComboBox.getItems().clear();

        String query = "select manufacture_date from drugs where drug_name = ? and drug_type = ? and manufacturer = ?";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drugName);
            ps.setString(2, drugType);
            ps.setString(3, manufacturer);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                LocalDate batchDate = LocalDate.parse(rs.getDate("manufacture_date").toString());
                if(!batchDateSet.contains(batchDate))
                    batchDateSet.add(batchDate.toString());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(String batchDateString: batchDateSet){
            manufactureDateComboBox.getItems().add(batchDateString);
        }

        manufactureDateComboBox.getSelectionModel().select(0);
    }

    private void handleCalucations(){

        try{
            int quantity = Integer.parseInt(quantityTextField.getText());
            double price = Double.parseDouble(priceTextField.getText());

            double amount = (double) quantity * price;
            amountTextField.setText(String.format("%.2f", amount));
        }catch (Exception exception){
            //do nothing
        }

    }

    @FXML private void addSale(ActionEvent event){

        String stockDrugName = drugNameTextField.getText();
        String stockDrugType = drugTypeComboBox.getValue();
        String stockManufacturer = manufacturerComboBox.getValue();
        LocalDate batchDate = LocalDate.parse(manufactureDateComboBox.getValue());
        int drug_id = DataExtractor.getDrugId(stockDrugName, stockDrugType, stockManufacturer, Date.valueOf(batchDate));

        int stockQuantity = DataExtractor.getDrugQuantity(drug_id);

        String drugName = Builder.buildDrugName(stockDrugName, stockDrugType, stockManufacturer);

        int quantity = Integer.parseInt(quantityTextField.getText());
        double price = Double.parseDouble(priceTextField.getText());
        double amount = Double.parseDouble(amountTextField.getText());


        if(quantity > stockQuantity){

            this.alertController.alertViewSetup(AlertViewController.AlertStyle.ERROR, "Cannot Perform Sale",
                    String.format("The quantity of the item '%s' is not sufficient to commence sale.\n" +
                            "Quantity of Drug in Stock: %d\nRequested Quantity: %d", drugName, stockQuantity, quantity),
                    true);
            this.alertStage.show();

            return;
        }

        stockQuantity = stockQuantity - quantity;
        Sale sale = new Sale(LocalDate.now(), quantity, drug_id, drugName, price, amount);

        if(DataExtractor.addSale(sale)){

            if(DataExtractor.updateDrugQuantity(stockDrugName, stockDrugType, stockManufacturer, batchDate, stockQuantity)){
                System.out.println("Drug Quantity has been updated");
            }

            clearFields();
            loadData();

        }

    }

    void clearFields(){

        drugNameTextField.clear();
        manufacturerComboBox.getItems().clear();
        drugTypeComboBox.getItems().clear();
        quantityTextField.clear();
        priceTextField.clear();
        amountTextField.clear();

    }

    public void calculateTotalSales(){

        double totalAmount = 0;
        for(Sale sale: salesTable.getItems()){
            totalAmount = totalAmount + sale.getAmount();
        }

        totalTextField.setText(String.format("%.2f", totalAmount));

    }

    @FXML private void tableContextMenuRequested(ContextMenuEvent event){

        if(salesTable.getItems().isEmpty()){
            salesTableContextMenu.hide();
            return;
        }

        salesTableContextMenu.show(salesTable, event.getScreenX(), event.getScreenY());

    }

    @FXML private void handleEdit() throws IOException {
        Sale drugSold = salesTable.getSelectionModel().getSelectedItem();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("editSales.fxml"));
        Parent parent = loader.load();

        EditSaleController editController = loader.getController();
        editController.inflateUI(drugSold, this.drugNameSet);

        Stage editStage = new Stage();
        editStage.setScene(new Scene(parent));
        editStage.initOwner(salesTable.getScene().getWindow());
        editStage.initModality(Modality.APPLICATION_MODAL);

        editStage.requestFocus();
        editStage.show();

        editStage.setOnHidden(stagehidden ->{
            loadData();
            salesTable.scrollTo(drugSold);
        });

    }


    @FXML private void removeItem(ActionEvent event){

        Sale saleToRemove = salesTable.getSelectionModel().getSelectedItem();

        int drugSoldId = saleToRemove.getDrugId();
        Drug drugSold = DataExtractor.getDrug(drugSoldId);
        int quantity = drugSold.getQuantity() + saleToRemove.getQuantity();

        alertController.alertViewSetup(AlertViewController.AlertStyle.WARNING, "Remove Sale Record",
                "Are you sure you want to remove sales record from the database ?" +
                        "\n\nNote that: Removing sales records is considered as returning an already bought drug." +
                        "\n\n Do you still want to continue ?");

        alertController.confirmButton.setText("Yes, Remove Selected Record");
        alertController.confirmButton.setOnAction(confirmButtonPressed ->{

            if(DataExtractor.removeSale(saleToRemove.getId()) && DataExtractor.updateDrugQuantity(drugSoldId, quantity)) {
                salesTable.getItems().remove(saleToRemove);

                DataExtractor.addLog("Sale Record Removal",
                        String.format("Sale record was removed from the database." +
                                "\n\nSale ID: %d" +
                                "\n Drug Purchased: %s" +
                                        "\n Batch Date of Drug: %s"+
                                "\n Quantity Purchased: %d" +
                                "\n Amount: %.2f",
                                saleToRemove.getId(), saleToRemove.getDrugName(),
                                drugSold.getManufactureDate(), saleToRemove.getQuantity(),
                                saleToRemove.getPrice()));

                alertStage.close();

                return;
            }

            showErrorAlert();

        });

        alertStage.show();

    }

    private void showErrorAlert() {

        alertController.alertViewSetup(AlertViewController.AlertStyle.ERROR, "Record Removal Error",
                "Could not remove sale record from database.");

        alertStage.show();
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
