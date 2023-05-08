package custom_cell;

import data.models.Builder;
import data.models.Drug;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class AlmostOutOfStockistViewCustomCell extends ListCell<Drug> {

    VBox parent;
    Label drugNameLabel;
    Label quantityLabel;

    public AlmostOutOfStockistViewCustomCell(){
        super();

        drugNameLabel = new Label();

        VBox.setMargin(drugNameLabel, new Insets(10));

        quantityLabel = new Label();
        VBox.setMargin(quantityLabel, new Insets(0, 10, 10, 10));

        parent = new VBox();
        parent.getChildren().addAll(drugNameLabel, quantityLabel);
        setGraphic(parent);
    }

    @Override
    protected void updateItem(Drug item, boolean empty) {
        super.updateItem(item, empty);

        if(!empty) {
            drugNameLabel.setText(Builder.buildDrugName(item.getDrugName(), item.getDrugType().toString(), item.getManufacturer()));
            quantityLabel.setText(String.format("Quantity Remaining: %d", item.getQuantity()));

            parent.setStyle("-fx-border-width: 0 0 0.5 0;" +
                    "-fx-border-color: #2a2e37;");
        }

        setGraphic(parent);

    }


}