package sales;

import alert.AlertViewController;
import data.db.DataExtractor;
import data.db.DatabaseConnector;
import data.models.Builder;
import data.models.Drug;
import data.models.Sale;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class EditSaleController implements Initializable {

    @FXML private ToggleButton editToggleButton;
    @FXML private TextField idTextField;
    @FXML private TextField quantityTextField;
    @FXML private TextField drugNameTextField;
    @FXML private ComboBox<String> manufacturerComboBox;
    @FXML private ComboBox<String> drugTypeComboBox;
    @FXML private ComboBox<String> manufactureDateComboBox;
    @FXML private TextField priceTextField;
    @FXML private TextField amountText;
    @FXML private HBox buttonContainer;
    @FXML private Button cancelButton;
    @FXML private Button editButton;
    private Sale selectedSale;
    Drug drugSold;
    int oldQuantity;
    double oldAmount;
    String oldDrugName;
    Set<String> manufacturerSet = new HashSet<>();
    Set<String> drugTypeSet = new HashSet<>();
    Set<String> batchDateSet = new HashSet<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initTooltip();
        createAlert();

        editToggleButton.setSelected(false);
        initEditToggleButtonUnselected();
        editToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(!newValue){
                initEditToggleButtonUnselected();
                return;
            }

            cancelButton.setText("Cancel");
            HBox.setMargin(cancelButton, new Insets(0, 0, 0, 5));

            try{
                buttonContainer.getChildren().add(0, editButton);
            }catch (Exception ex){
                // do nothing
            }

        });

        textFieldListeners();
    }

    private void initTooltip(){
        drugNameTextField.setTooltip(new Tooltip("Drug Name"));
        drugTypeComboBox.setTooltip(new Tooltip("Drug Type"));
        manufacturerComboBox.setTooltip(new Tooltip("Manufacturer"));
        manufactureDateComboBox.setTooltip(new Tooltip("Batch or Manufacture Date"));
        quantityTextField.setTooltip(new Tooltip("Quantity"));
        priceTextField.setTooltip(new Tooltip("Retail Price"));
        amountText.setTooltip(new Tooltip("Total Amount"));
    }

    private void initEditToggleButtonUnselected(){

        buttonContainer.getChildren().remove(editButton);
        cancelButton.setText("Okay");
        HBox.setMargin(cancelButton, new Insets(0, 5, 0, 5));
    }

    public void inflateUI(Sale sale, Set<String> drugNames){

        drugSold = DataExtractor.getDrug(sale.getDrugId());
        TextFields.bindAutoCompletion(this.drugNameTextField, drugNames);

        idTextField.setText(String.valueOf(sale.getId()));
        quantityTextField.setText(String.valueOf(sale.getQuantity()));
        drugNameTextField.setText(drugSold.getDrugName());
        drugTypeComboBox.setValue(drugSold.getDrugType().toString());
        manufacturerComboBox.setValue(drugSold.getManufacturer());
        manufactureDateComboBox.setValue(drugSold.getManufactureDate().toString());
        priceTextField.setText(String.format("%.2f", sale.getPrice()));
        amountText.setText(String.format("%.2f", sale.getAmount()));

        //the only things that will be allowed for editing here are the productName
        //and quantity field
        idTextField.setDisable(true);
        amountText.setDisable(true);
        priceTextField.setDisable(true);

        this.oldQuantity = sale.getQuantity();
        this.oldDrugName = sale.getDrugName();
        this.oldAmount = sale.getAmount();

        quantityTextField.textProperty().addListener(observable -> handleCalculations());
        priceTextField.textProperty().addListener(observable -> handleCalculations());

        this.selectedSale = sale;
    }

    private void textFieldListeners() {

        drugNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            loadManufacturers(newValue);
            getDrugBatches(newValue, drugTypeComboBox.getValue(), manufacturerComboBox.getValue());
            try {
                loadAvailableDrugType(newValue, manufacturerComboBox.getValue());
                getDrugPrice(newValue, manufacturerComboBox.getValue(), drugTypeComboBox.getValue());
            } catch (Exception ex) {
                //do nothing
            }

        });

        manufacturerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            loadAvailableDrugType(drugNameTextField.getText(), newValue);
            getDrugBatches(drugNameTextField.getText(), drugTypeComboBox.getValue(), newValue);
            try {
                getDrugPrice(drugNameTextField.getText(), newValue, drugTypeComboBox.getValue());
            } catch (Exception ex) {
                //do fucking nothing as usual
            }

        });

        drugTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            getDrugPrice(drugNameTextField.getText(), manufacturerComboBox.getValue(), newValue);
            getDrugBatches(drugNameTextField.getText(), newValue, manufacturerComboBox.getValue());
        });
    }

    void handleCalculations(){

        try{

            double amount = Double.parseDouble(quantityTextField.getText()) * Double.parseDouble(priceTextField.getText());
            amountText.setText(String.format("%.2f", amount));

        }catch (Exception exception){
            //do nothing when catch an error
        }
    }

    @FXML private void confirmEdit(ActionEvent event){

       String drugName = drugNameTextField.getText();
       String drugType = drugTypeComboBox.getValue();
       String manufacturer = manufacturerComboBox.getValue();
       LocalDate manufactureDate = LocalDate.parse(manufactureDateComboBox.getValue());
       int newQuantity = Integer.parseInt(quantityTextField.getText());
       double amount = Double.parseDouble(amountText.getText());

       String builtDrugName = Builder.buildDrugName(drugName, drugType, manufacturer);

        this.selectedSale.setDrugName(builtDrugName);
        this.selectedSale.setQuantity(Integer.parseInt(quantityTextField.getText()));
        this.selectedSale.setAmount(amount);

        //computing the new quantity
        int value =  0;

        if(newQuantity > oldQuantity){
           value = newQuantity - oldQuantity;
           int newStockQuantity = drugSold.getQuantity() - value;

           if(newStockQuantity >= 0){

               drugSold.setQuantity(newQuantity);

           }else{

               this.alertController.alertViewSetup(AlertViewController.AlertStyle.ERROR,
                       "Cannot Update Sale",
                       String.format("The drug may be out of stock\n\n" +
                               "Previous Quantity Requested: %d\n" +
                               "New Quantity Requested: %d\n" +
                               "New Quantity of Drug in Stock after new Quantity Requested: %d", oldQuantity, newQuantity, newStockQuantity),true);
               this.alertStage.show();

               return;
           }

        }else{
            value = oldQuantity - newQuantity;
            drugSold.setQuantity(drugSold.getQuantity() + value);
        }

        if(DataExtractor.updateSale(this.selectedSale) && DataExtractor.updateDrugQuantity(drugName, drugType,
                manufacturer, manufactureDate,drugSold.getQuantity())){

            String logString = Builder.buildSaleUpdateLog(this.selectedSale.getId(), oldQuantity, newQuantity,
                    this.oldDrugName, drugName, this.oldAmount, amount);

            DataExtractor.addLog("Sale Update", logString);

            alertController.alertViewSetup(AlertViewController.AlertStyle.INFORMATION, "Information",
                    "Sale and Stock has successfully been updated", true);
            this.alertStage.setOnHiding(stageHiding ->  ((Button) event.getSource()).getScene().getWindow().hide());
            alertStage.show();

        }
    }

    @FXML private void cancelEdit(ActionEvent event){
        ((Button) event.getSource()).getScene().getWindow().hide();
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
