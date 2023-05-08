package admin.analysis.views.summary;

import data.db.DataExtractor;
import data.models.Builder;
import data.models.Summary.DailySalesSummary;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextAlignment;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DailySalesSummaryController implements Initializable {

    @FXML private DatePicker datePicker;
    @FXML private TableView<DailySalesSummary> summaryTable;
    @FXML private TableColumn<DailySalesSummary, LocalDate> dateSaleColumn;
    @FXML private TableColumn<DailySalesSummary, Double> totalAmountColumn;

    private ObservableList<DailySalesSummary> summaryList;

    @Override public void initialize(URL loc, ResourceBundle rb){

        summaryList = DataExtractor.getDailySalesSummary();

        dateSaleColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        dateSaleColumn.setCellFactory(param -> new DateTableCell());
        totalAmountColumn.setCellFactory(param -> new AmountTableCell());
        summaryTable.setItems(summaryList);
        initTableSearch();

    }

    private void initTableSearch() {
        FilteredList<DailySalesSummary> summaryFilter = new FilteredList<>(summaryList, pred -> true);
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {

            summaryFilter.setPredicate(salesSummary -> {
                if(newValue == null){
                    return true;
                }

                return salesSummary.getSaleDate().equals(newValue);
            });
        });

        SortedList<DailySalesSummary> summarySorted = new SortedList<>(summaryFilter);
        summarySorted.comparatorProperty().bind(summaryTable.comparatorProperty());
        summaryTable.setItems(summarySorted);
    }

    private static class DateTableCell extends TableCell<DailySalesSummary, LocalDate>{

        @Override
        protected void updateItem(LocalDate item, boolean empty) {
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

    private static class AmountTableCell extends TableCell<DailySalesSummary, Double>{

        @Override protected void updateItem(Double item, boolean empty){
            super.updateItem(item, empty);

            if(item == null){
                setText(null);
                return;
            }

            setText(String.format("%.2f", item));
            setMaxWidth(Double.MAX_VALUE);
            setTextAlignment(TextAlignment.RIGHT);
            setPadding(new Insets(0, 5, 0,5));
            setAlignment(Pos.CENTER_RIGHT);

        }
    }

    @FXML private void clearDatePickerValue(ActionEvent event){
        datePicker.setValue(null);
    }

}
