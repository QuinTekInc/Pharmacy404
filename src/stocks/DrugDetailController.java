package stocks;

import data.models.Drug;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class DrugDetailController {

    @FXML private TextField idTextField;
    @FXML private TextField drugNameTextField;
    @FXML private TextField drugTypeTextField;
    @FXML private TextField manufacturerTextField;
    @FXML private DatePicker manufacturingDate;
    @FXML private DatePicker expiringDate;
    @FXML private TextField quantityText;
    @FXML private TextField retailPriceTextField;

    public void inflateUI(Drug selectedDrug){

        idTextField.setText(String.valueOf(selectedDrug.getId()));
        quantityText.setText(String.valueOf(selectedDrug.getQuantity()));
        drugNameTextField.setText(String.valueOf(selectedDrug.getDrugName()));
        drugTypeTextField.setText(selectedDrug.getDrugType().toString());
        manufacturerTextField.setText(selectedDrug.getManufacturer());
        manufacturingDate.setValue(selectedDrug.getManufactureDate());
        expiringDate.setValue(selectedDrug.getExpiringDate());
        retailPriceTextField.setText(String.format("%.2f", selectedDrug.getRetailPrice()));
    }

    public void okayButtonPressed(ActionEvent event) {
        ((Button) event.getSource()).getScene().getWindow().hide();
    }
}