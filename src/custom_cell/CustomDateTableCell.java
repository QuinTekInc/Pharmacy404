package custom_cell;

import data.models.Builder;
import data.models.Sale;
import javafx.scene.control.TableCell;

import java.time.LocalDate;

public class CustomDateTableCell extends TableCell<Sale, LocalDate> {

    @Override protected void updateItem(LocalDate item, boolean empty){
        super.updateItem(item, empty);

        if(item == null){
            setText(null);
            return;
        }

        if(item.equals(LocalDate.now())){
            setText(String.format("%s(TODAY)", Builder.buildDateString(item)));
            return;
        }

        int day = item.getDayOfMonth();
        int month = item.getMonth().getValue();
        int year = item.getYear();

        LocalDate today = LocalDate.now();

        if(today.getDayOfMonth() - 1 == day && today.getMonth().getValue() == month && today.getYear()== year){
            setText(String.format("%s(YESTERDAY)", Builder.buildDateString(item)));
            return;
        }

        setText(Builder.buildDateString(item));

    }

}
