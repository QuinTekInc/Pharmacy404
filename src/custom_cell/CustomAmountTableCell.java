package custom_cell;

import data.models.Sale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;

public class CustomAmountTableCell extends TableCell<Sale, Double> {

    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);

        if(item == null){
            setText(null);
            return;
        }

        setText(String.format("%.2f", item));
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(0, 5, 0,5));
        setAlignment(Pos.CENTER_RIGHT);

    }
}
