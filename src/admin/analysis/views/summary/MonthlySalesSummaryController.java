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
import javafx.scene.control.cell.PropertyValueFactory;
import main.MainWindowController;
import java.net.URL;
import java.util.ResourceBundle;

public class MonthlySalesSummaryController implements Initializable {
    @FXML public TableView<Summary.MonthlySalesSummary> summaryTable;
    @FXML public TableColumn<Summary.MonthlySalesSummary, Integer> monthColumn;
    @FXML public TableColumn<Summary.MonthlySalesSummary, Double> totalAmountColumn;

private ObservableList<Summary.MonthlySalesSummary> summaryList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        summaryList = DataExtractor.getMonthlySalesSummary();
        initializeTableColumns();
        summaryTable.setItems(summaryList);
    }

    private void initializeTableColumns(){

        monthColumn.setCellValueFactory(new PropertyValueFactory<>("monthNumber"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        monthColumn.setCellFactory(param -> new TableCell<Summary.MonthlySalesSummary, Integer>(){
            @Override protected void updateItem(Integer item, boolean isEmpty){

                super.updateItem(item, isEmpty);
                if(item == null){
                    setText(null);
                    return;
                }

                setText(MainWindowController.getMonth(item - 1));
                setPadding(new Insets(0, 10, 0, 10));
            }
        });

        totalAmountColumn.setCellFactory(param -> new TableCell<Summary.MonthlySalesSummary, Double>(){

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if(item == null){
                    setText(null);
                    return;
                }

                setText(String.format("%.2f", item));
                setAlignment(Pos.CENTER_RIGHT);
                setPadding(new Insets(0, 10, 0, 10));
            }
        });
    }



}
