package admin.analysis.views.summary;

import data.db.DataExtractor;
import data.models.Summary;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import  javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ResourceBundle;

public class WeeklySaleSummaryController implements Initializable {

    @FXML private TableView<Summary.WeeklySalesSummary> weeklySalesTable;
    @FXML private TableColumn<Summary.WeeklySalesSummary, Integer> weekColumn;
    @FXML private TableColumn<Summary.WeeklySalesSummary, Double> totalAmountColumn;

    ObservableList<Summary.WeeklySalesSummary> summaryList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        summaryList = DataExtractor.getWeeklySalesSummary();
        initializeTableColumns();
        weeklySalesTable.setItems(summaryList);
    }

    private void initializeTableColumns(){

        weekColumn.setCellValueFactory(new PropertyValueFactory<>("weekNumber"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        weekColumn.setCellFactory(param -> new TableCell<Summary.WeeklySalesSummary, Integer>(){
            @Override protected void updateItem(Integer item, boolean isEmpty){

                super.updateItem(item, isEmpty);
                if(item == null){
                    setText(null);
                    return;
                }

                setText(String.format("Week %d", item));
                setPadding(new Insets(0, 10, 0, 10));
            }
        });

        totalAmountColumn.setCellFactory(param -> new TableCell<Summary.WeeklySalesSummary, Double>(){

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if(item == null){
                    setText(null);
                    return;
                }

                setText(String.format("%.2f", item));
                setTextAlignment(TextAlignment.RIGHT);
                setAlignment(Pos.CENTER_RIGHT);
                setPadding(new Insets(0, 10, 0, 10));
            }
        });
    }

}
