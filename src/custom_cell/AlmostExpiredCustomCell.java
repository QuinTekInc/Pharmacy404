package custom_cell;

import data.db.DataExtractor;
import data.models.Builder;
import data.models.Drug;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class AlmostExpiredCustomCell extends ListCell<Drug> {

    VBox parent;
    Label drugNameLabel;
    Label expiryDateLabel;
    Label daysRemainingLabel;

    public AlmostExpiredCustomCell() {
        super();

        drugNameLabel = new Label();

        VBox.setMargin(drugNameLabel, new Insets(5));

        expiryDateLabel = new Label();
        VBox.setMargin(expiryDateLabel, new Insets(0, 5, 5, 5));

        daysRemainingLabel = new Label();
        VBox.setMargin(daysRemainingLabel, new Insets(0, 5, 10, 5));


        parent = new VBox();
        parent.getChildren().addAll(drugNameLabel, expiryDateLabel, daysRemainingLabel);
        setGraphic(parent);
    }

    @Override
    protected void updateItem(Drug item, boolean empty) {
        super.updateItem(item, empty);

        if(item == null){
            setText(null);
            setGraphic(null);
            return;
        }

        String drugName = Builder.buildDrugName(item.getDrugName(), item.getDrugType().toString(), item.getManufacturer());
        drugNameLabel.setText(drugName);

        LocalDate expiringDate = item.getExpiringDate();
        expiryDateLabel.setText(String.format("Expiry Date: %s", Builder.buildDateString(expiringDate)));


        daysRemainingLabel.setText(getTimeRemaining(item.getExpiringDate()));

        parent.setStyle("-fx-border-width: 0 0 0.5 0;" +
                "-fx-border-color: derive(#2a2e37, 50%);");

        setGraphic(parent);

    }

    private String getTimeRemaining(LocalDate expiringDate){

        LocalDate today = LocalDate.now();

        int expiringYear = expiringDate.getYear();
        int thisYear = today.getYear();

        if(expiringYear == thisYear){


            long numberOfDays;

            if(expiringDate.getMonthValue() > today.getMonthValue()){
                numberOfDays = ChronoUnit.DAYS.between(today, expiringDate);
            }else{
                numberOfDays = ChronoUnit.DAYS.between(expiringDate, today);
            }

            if(numberOfDays > 28 && numberOfDays <= 366){
                long numberOfMonths = numberOfDays / 30;
                return String.format("Days Remaining: %d Month(s)", numberOfMonths);
            }else{
                return String.format("Days Remaining: %d Day(s)", numberOfDays);
            }


        }else if(expiringYear < thisYear){

            long numberOfDays = ChronoUnit.DAYS.between(today, expiringDate);
            String numberOfDaysString = String.valueOf(numberOfDays);

            numberOfDays = Long.parseLong(numberOfDaysString.replace("-", ""));

            if(numberOfDays >= 365){

                long numberOfYears = ChronoUnit.YEARS.between(today, expiringDate);
                return String.format("Day Remaining: %d Year(s)", numberOfYears);
            }if(numberOfDays > 30){

                long numberOfMonths = ChronoUnit.MONTHS.between(today, expiringDate);
                return String.format("Day Remaining: %d Months(s)", numberOfMonths);

            }else{
                return String.format("Days Remaining: %d Day(s)", numberOfDays);
            }

        }

        return "Expired";


    }


}
