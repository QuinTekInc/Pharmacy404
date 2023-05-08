package admin.analysis.views.summary;

import data.db.DataExtractor;
import data.models.Summary;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextAlignment;

public class YearlySaleSummaryController implements Initializable {

    @FXML private TableView<Summary.YearlySalesSummary> summaryTable;
    @FXML private TableColumn<Summary.YearlySalesSummary, Integer> yearColumn;
    @FXML private TableColumn<Summary.YearlySalesSummary, Double> totalAmountColumn;

    private ObservableList<Summary.YearlySalesSummary> summaryList;

    @Override public void initialize(URL location, ResourceBundle resources) {

        summaryList = DataExtractor.getYearlySaleSummary();
        initializeColumns();

        summaryTable.setItems(summaryList);

    }

    private void initializeColumns(){
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearNumber"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        totalAmountColumn.setCellFactory(param -> {
            return new TableCell<Summary.YearlySalesSummary, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);

                    if(item == null){
                        setText(null);
                        return;
                    }

                    setText(String.format("%.2f", item));
                    setPadding(new Insets(0, 5, 0, 5));
                    setMaxWidth(Double.MAX_VALUE);
                    setTextAlignment(TextAlignment.RIGHT);
                    setAlignment(Pos.CENTER_RIGHT);

                }
            };
        });

        yearColumn.setCellFactory(param -> new YearColumnCustomCell(summaryList));
    }

    private static class YearColumnCustomCell extends TableCell<Summary.YearlySalesSummary, Integer> {

        ObservableList<Summary.YearlySalesSummary> summaryList;

        YearColumnCustomCell(ObservableList<Summary.YearlySalesSummary> summaryList){
            super();
            this.summaryList = summaryList;
        }

        @Override protected void updateItem(Integer item, boolean isEmpty){

            if(item == null){
                setText(null);
                return;
            }

            setText(formatYearString(item));

        }

        private String formatYearString(int year){

            String sentence = "";

            for(Summary.YearlySalesSummary summary: summaryList){
                if(summary.getYearNumber() == year){
                    int index = summaryList.indexOf(summary) + 1;
                    sentence = String.format("Year %d: %d", index, year);
                    break;
                }
            }

            return sentence;
        }

    }

}
