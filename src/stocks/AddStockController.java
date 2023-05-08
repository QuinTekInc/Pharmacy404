package stocks;

import alert.AlertViewController;
import data.db.DataExtractor;
import data.db.DatabaseConnector;
import data.models.Drug;
import data.models.DrugType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.ResourceBundle;

public class AddStockController implements Initializable {

    @FXML private TextField drugNameTextField;
    @FXML private TextField manufacturerTextField;
    @FXML private ComboBox<String> drugTypeComboBox;
    @FXML private DatePicker manufacturingDate;
    @FXML private DatePicker expiringDate;
    @FXML private TextField quantityText;
    @FXML private TextField wholesalePriceTextField;
    @FXML private TextField retailPriceTextField;

    @FXML private Button addButton;

    @FXML public void initialize(URL location, ResourceBundle rb){

        initTooltip();
        createAlert();

        DrugType[] drugTypes = DrugType.values();

        for(DrugType drugType: drugTypes){
            drugTypeComboBox.getItems().add(drugType.toString());
        }

        addButtonDisableListeners();

    }

    //the function is for when we wanna add new drug batch
    public void manualInitialize(Drug drug){

        drugNameTextField.setText(drug.getDrugName());
        drugTypeComboBox.setValue(drug.getDrugType().toString());
        manufacturerTextField.setText(drug.getManufacturer());
        wholesalePriceTextField.setText(String.format("%.2f", drug.getPrice()));

        addButton.setOnAction(addButtonPressed ->{

            drug.setPrice(Double.parseDouble(wholesalePriceTextField.getText()));
            drug.setQuantity(Integer.parseInt(quantityText.getText()));
            drug.setManufactureDate(manufacturingDate.getValue());
            drug.setExpiringDate(expiringDate.getValue());

            if(DataExtractor.updateDrugInfo(drug)){
                alertController.alertViewSetup(AlertViewController.AlertStyle.INFORMATION,
                        "Drug Drug Update",
                        "Drug Batch has been updated to the database successfully", true);
                alertController.confirmButton.setText("Okay");
                alertStage.setOnHidden(alertStageHidden -> addButton.getScene().getWindow().hide());
                alertStage.show();
            }

        });

    }

    private void initTooltip(){

        drugNameTextField.setTooltip(new Tooltip("Drug Name"));
        drugTypeComboBox.setTooltip(new Tooltip("Drug Type"));
        manufacturerTextField.setTooltip(new Tooltip("Manufacturer"));
        manufacturingDate.setTooltip(new Tooltip("Manufacture or Batch Date of Date"));
        expiringDate.setTooltip(new Tooltip("Expiration Date"));
        quantityText.setTooltip(new Tooltip("Quantity"));
        wholesalePriceTextField.setTooltip(new Tooltip("Wholesale Price"));
        retailPriceTextField.setTooltip(new Tooltip("Retail Price"));

    }
    private void addButtonDisableListeners(){

        drugNameTextField.textProperty().addListener(observable -> addButton.setDisable(isFieldEmpty()));
        manufacturerTextField.textProperty().addListener(observable -> addButton.setDisable(isFieldEmpty()));
        drugTypeComboBox.valueProperty().addListener(observable -> addButton.setDisable(isFieldEmpty()));
        manufacturingDate.valueProperty().addListener(observable -> addButton.setDisable(isFieldEmpty()));
        expiringDate.valueProperty().addListener(observable -> addButton.setDisable(isFieldEmpty()));
        quantityText.textProperty().addListener(observable -> addButton.setDisable(isFieldEmpty()));

        wholesalePriceTextField.textProperty().addListener((observable, oldValue, newValue) ->{
            addButton.setDisable(isFieldEmpty());

            if(newValue.isEmpty()){
                wholesalePriceTextField.setTooltip(new Tooltip("Wholesale Price"));
                return;
            }

            calculateProfit();
        });

        retailPriceTextField.textProperty().addListener((observable, oldValue, newValue) ->{
            addButton.setDisable(isFieldEmpty());

            if(newValue.isEmpty()){
                retailPriceTextField.setTooltip(new Tooltip("Retail Price"));
                return;
            }

            calculateProfit();
        });

    }

    private void calculateProfit(){

       if(wholesalePriceTextField.getText().isEmpty() || retailPriceTextField.getText().isEmpty()){
           return;
       }

       wholesalePriceTextField.setTooltip(null);
       retailPriceTextField.setTooltip(null);

        StringBuilder stringBuilder = new StringBuilder();


       double wholesalePrice = Double.parseDouble(wholesalePriceTextField.getText());
       stringBuilder.append(String.format("Wholesale Price: %.2f\n", wholesalePrice));
       double retailPrice = Double.parseDouble(retailPriceTextField.getText());
       stringBuilder.append(String.format("Retail Price: %.2f\n", retailPrice));

       double profit = retailPrice - wholesalePrice;
       stringBuilder.append(String.format("Profit: %.2f\n", profit));

       double profitPercent = (profit / wholesalePrice) * 100;
       stringBuilder.append(String.format("Profit Percent: %.1f", profitPercent)).append("%");

       Tooltip wholeSalePriceTooltip = new Tooltip();
       wholeSalePriceTooltip.setText("Wholesale Price\n" + stringBuilder);

       wholesalePriceTextField.setTooltip(wholeSalePriceTooltip);

       Tooltip retailPriceTooltip = new Tooltip();
       retailPriceTooltip.setText("Retail Price\n" + stringBuilder);
       retailPriceTextField.setTooltip(retailPriceTooltip);


    }

    private boolean isFieldEmpty(){

        return drugNameTextField.getText().isEmpty() ||
                manufacturerTextField.getText().isEmpty()||
                drugTypeComboBox.getValue().isEmpty()||
                manufacturingDate.getValue() == null ||
                expiringDate.getValue() == null ||
                quantityText.getText().isEmpty() ||
                wholesalePriceTextField.getText().isEmpty()||
                retailPriceTextField.getText().isEmpty();

    }

    @FXML private void handleAdd(ActionEvent event) throws IOException {

        int quantity = Integer.parseInt(quantityText.getText());
        String drugName = drugNameTextField.getText();
        DrugType drugType = DrugType.drugValueOf(drugTypeComboBox.getValue());
        String manufacturer = manufacturerTextField.getText();
        LocalDate manufactureDate = manufacturingDate.getValue();
        LocalDate expirationDate = expiringDate.getValue();
        double wholeSalePrice = Double.parseDouble(wholesalePriceTextField.getText());
        double retailPrice = Double.parseDouble(retailPriceTextField.getText());

        Drug newDrug = new Drug(quantity, drugName, drugType, manufacturer);
        newDrug.setManufactureDate(manufactureDate);
        newDrug.setExpiringDate(expirationDate);
        newDrug.setPrice(wholeSalePrice);
        newDrug.setRetailPrice(retailPrice);

        boolean isDrugExistFlag = isDrugExist(newDrug);
        System.out.println(isDrugExistFlag);

        if(isDrugExist(newDrug)){

            alertController.alertViewSetup(AlertViewController.AlertStyle.WARNING, "Drug Already exist",
                    "The drug you are trying to add already exists in the database. " +
                            "Would you like to update the quantity instead ?");

            alertController.confirmButton.setText("Yes, Update");
            alertController.cancelButton.setText("No, Cancel");

           //get previous drug in the data

            alertController.confirmButton.setOnAction(confirmButtonPressed ->{

                Drug stockDrug = DataExtractor.getDrug(DataExtractor.getDrugId(drugName, drugType.toString(),
                        manufacturer, Date.valueOf(manufactureDate)));

                int newQuantity = stockDrug.getQuantity() + newDrug.getQuantity();

                //this is where we update the drug quantity
                if(DataExtractor.updateDrugQuantity(stockDrug.getId(), newQuantity)){
                    System.out.println("Stock Has been updated");
                    clearFields();
                    alertStage.hide();
                }
            });

            alertStage.show();
            return;

        }

        if(DataExtractor.addDrug(newDrug)){
            clearFields();
        }

    }

    public boolean isDrugExist(Drug drug){

        String query = "select * from drugs where drug_name = ? and drug_type = ? and " +
                "manufacturer = ? and manufacture_date = ? and expiration_date = ?";

        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drug.getDrugName());
            ps.setString(2, drug.getDrugType().toString());
            ps.setString(3, drug.getManufacturer());
            ps.setDate(4, Date.valueOf(drug.getManufactureDate()));
            ps.setDate(5, Date.valueOf(drug.getExpiringDate()));

            ResultSet rs = ps.executeQuery();

            return rs.next();

        }catch (SQLException sqlex){
            throw new RuntimeException(sqlex);
        }

    }

    public void clearFields(){
        quantityText.clear();
        drugNameTextField.clear();
        drugTypeComboBox.setValue(null);
        manufacturerTextField.clear();
        manufacturingDate.setValue(null);
        expiringDate.setValue(null);
        wholesalePriceTextField.clear();
    }

    @FXML private void handleCancel(ActionEvent event){
        ((Button) event.getSource()).getScene().getWindow().hide();
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
