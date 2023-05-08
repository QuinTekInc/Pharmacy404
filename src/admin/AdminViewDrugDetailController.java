package admin;

import com.jfoenix.controls.JFXToggleButton;
import data.db.DataExtractor;
import data.models.Drug;
import data.models.DrugType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.tools.Tool;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminViewDrugDetailController implements Initializable {

    @FXML private VBox adminDrugDetailParent;

    @FXML private Label titleLabel;
    @FXML private HBox buttonsParent;
    @FXML private TextField idTextField;
    @FXML private TextField drugNameTextField;
    @FXML private ComboBox<String> drugTypeComboBox;
    @FXML private TextField manufacturerTextField;
    @FXML private DatePicker manufacturingDate;
    @FXML private DatePicker expiringDate;

    @FXML private HBox datePickerContainer;

    @FXML private TextField quantityText;
    @FXML private TextField wholesalePriceTextField;
    @FXML private TextField retailPriceTextField;
    @FXML private JFXToggleButton editToggleButton;
    @FXML private Button editButton;
    @FXML private Button cancelButton;
    private Drug selectedDrug;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        editToggleButton.setSelected(false);
        initEditToggleButtonUnselectedView();
        initTooltip();
        toggleButtonListener();
        textFieldListeners();

    }

    private void initTooltip(){

        idTextField.setTooltip(new Tooltip("Drug ID"));
        drugNameTextField.setTooltip(new Tooltip("Drug Name"));
        drugTypeComboBox.setTooltip(new Tooltip("Drug Type"));
        manufacturerTextField.setTooltip(new Tooltip("Manufacturer"));
        manufacturingDate.setTooltip(new Tooltip("Batch or Manufacture Date"));
        quantityText.setTooltip(new Tooltip("Quantity Remaining"));
        wholesalePriceTextField.setTooltip(new Tooltip("Wholesale Price"));
        retailPriceTextField.setTooltip(new Tooltip("Retail Price"));

    }

    private void calculateProfit(){

        if(wholesalePriceTextField.getText().isEmpty() || retailPriceTextField.getText().isEmpty()){
            return;
        }

        try{

            StringBuilder tooltipStringBuilder = new StringBuilder();

            double wholesalePrice = Double.parseDouble(wholesalePriceTextField.getText());
            tooltipStringBuilder.append(String.format("\nWholesale Price: %.2f", wholesalePrice));

            double retailPrice = Double.parseDouble(retailPriceTextField.getText());
            tooltipStringBuilder.append(String.format("\nRetail Builder: %.2f", retailPrice));

            double profit = retailPrice - wholesalePrice;
            tooltipStringBuilder.append(String.format("\nProfit: %.2f", profit));

            double profitPercent = (profit/wholesalePrice) * 100;
            tooltipStringBuilder.append(String.format("\nProfit Percent: %.1f", profitPercent)).append("%");

            Tooltip wholesalePriceTooltip = new Tooltip();
            wholesalePriceTooltip.setText("Wholesale Price" + tooltipStringBuilder);
            wholesalePriceTextField.setTooltip(wholesalePriceTooltip);

            Tooltip retailPriceTooltip = new Tooltip();
            retailPriceTooltip.setText("Retail Price" + tooltipStringBuilder);
            retailPriceTextField.setTooltip(retailPriceTooltip);


        }catch (Exception ex){
            //do nothing
        }

    }

    public void manualInitialize(Drug drug, boolean isSimpleMode){

        this.selectedDrug = drug;

        quantityText.setText(String.valueOf(selectedDrug.getQuantity()));
        drugNameTextField.setText(String.valueOf(selectedDrug.getDrugName()));
        drugTypeComboBox.setValue(selectedDrug.getDrugType().toString());
        manufacturerTextField.setText(selectedDrug.getManufacturer());
        wholesalePriceTextField.setText(String.format("%.2f", selectedDrug.getPrice()));
        retailPriceTextField.setText(String.format("%.2f", selectedDrug.getRetailPrice()));

        if(isSimpleMode){
            editToggleButton.setDisable(true);
            editToggleButton.setVisible(false);
            this.adminDrugDetailParent.getChildren().add(cancelButton);
            VBox.setMargin(cancelButton, new Insets(10));
            adminDrugDetailParent.getChildren().removeAll(idTextField, datePickerContainer, buttonsParent);

        }else{

            idTextField.setText(String.valueOf(selectedDrug.getId()));
            manufacturingDate.setValue(selectedDrug.getManufactureDate());
            expiringDate.setValue(selectedDrug.getExpiringDate());

        }

    }

    public void outOfStockViewManualInitialize(Drug selectedDrug, String title){

        int stockQuantity = selectedDrug.getQuantity();
        idTextField.setText(String.valueOf(selectedDrug.getId()));
        quantityText.setText(String.valueOf(selectedDrug.getQuantity()));
        drugNameTextField.setText(selectedDrug.getDrugName());
        drugTypeComboBox.setValue(selectedDrug.getDrugType().toString());
        manufacturerTextField.setText(selectedDrug.getManufacturer());
        manufacturingDate.setValue(selectedDrug.getManufactureDate());
        expiringDate.setValue(selectedDrug.getExpiringDate());
        wholesalePriceTextField.setText(String.format("%.2f", selectedDrug.getPrice()));

        titleLabel.setText(title);
        editToggleButton.setVisible(false);
        editToggleButton.setDisable(true);
        quantityText.setDisable(false);

        //getting index of the quantity text
        int quantityTextFieldIndex = this.adminDrugDetailParent.getChildren().indexOf(quantityText);
        Label noteLabel = new Label("The value you set will be added to the old value and updated to the database");
        VBox.setMargin(noteLabel, new Insets(3, 10, 10, 10));
        adminDrugDetailParent.getChildren().add(quantityTextFieldIndex + 1, noteLabel);

        editButton.disableProperty().bind(quantityText.textProperty().isEmpty());

        editButton.setOnAction(editButton_pressed ->{

            int newQuantity = stockQuantity + Integer.parseInt(quantityText.getText());
            if(DataExtractor.updateDrugQuantity(selectedDrug.getId(), newQuantity)){
                editButton.getScene().getWindow().hide();
            }

        });

    }

    public void initEditToggleButtonUnselectedView(){
        editButton.setDisable(true);
        buttonsParent.getChildren().remove(editButton);
        cancelButton.setText("Okay");
        HBox.setMargin(cancelButton, new Insets(0));
    }

    public void toggleButtonListener() {

        editToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(!newValue){
               initEditToggleButtonUnselectedView();
            }else{
                editButton.setDisable(false);
                cancelButton.setText("Cancel");
                HBox.setMargin(cancelButton, new Insets(0, 0, 0, 5));
                try{
                    buttonsParent.getChildren().add(0, editButton);
                }catch (Exception ex){
                    // do nothing
                }
            }

        });

    }

    private void textFieldListeners(){


        wholesalePriceTextField.textProperty().addListener((observable, oldValue, newValue) ->{
            editButton.setDisable(isFieldEmpty());

            if(newValue.isEmpty()){
                return;
            }

            calculateProfit();

        });

        retailPriceTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue.isEmpty()){
                return;
            }

            calculateProfit();

        });

    }

    private boolean isFieldEmpty(){

        return drugNameTextField.getText().isEmpty() ||
                manufacturerTextField.getText().isEmpty()||
                drugTypeComboBox.getValue().isEmpty()||
                manufacturingDate.getValue() == null ||
                expiringDate.getValue() == null ||
                quantityText.getText().isEmpty() ||
                wholesalePriceTextField.getText().isEmpty();

    }


    @FXML public void editButtonClicked(ActionEvent event){

        this.selectedDrug.setDrugName(drugNameTextField.getText());
        this.selectedDrug.setDrugType(DrugType.drugValueOf(drugTypeComboBox.getValue()));
        this.selectedDrug.setManufacturer(manufacturerTextField.getText());
        this.selectedDrug.setManufactureDate(manufacturingDate.getValue());
        this.selectedDrug.setExpiringDate(expiringDate.getValue());
        this.selectedDrug.setQuantity(Integer.parseInt(quantityText.getText()));
        this.selectedDrug.setPrice(Double.parseDouble(wholesalePriceTextField.getText()));

        if(DataExtractor.updateDrugInfo(this.selectedDrug)){
            System.out.println("Drug Updated Successfully");
            this.wholesalePriceTextField.getScene().getWindow().hide();
        }

    }

    @FXML private void closeButtonPressed(ActionEvent event){
        this.wholesalePriceTextField.getScene().getWindow().hide();
    }

}
