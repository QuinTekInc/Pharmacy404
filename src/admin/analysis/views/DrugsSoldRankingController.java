package admin.analysis.views;

import data.db.DataExtractor;
import data.models.DrugSalesRanking;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class DrugsSoldRankingController implements Initializable {

    @FXML private BorderPane rankingsBorderPane;
    @FXML private AnchorPane tableViewParent;
    @FXML private TableView<DrugSalesRanking> rankTable;
    @FXML private TableColumn<DrugSalesRanking, String> drugNameColumn;
    @FXML private TableColumn<DrugSalesRanking, Integer> quantitySoldColumn;
    @FXML private TextField searchTextField;
    @FXML private Button tableViewButton;
    @FXML private Button graphicalViewButton;

    private ObservableList<DrugSalesRanking> rankings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rankings = DataExtractor.getDrugRankings();
        rankings.sort(Comparator.comparing(DrugSalesRanking::getQuantitySold));

        tableViewButton.getStyleClass().add("selected-button");
        initializeTableColumns();
        rankTable.setItems(rankings);
        handleTableSearch();
        createGraphicNode();

        rankingsBorderPane.centerProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue.equals(tableViewParent)){
                tableViewButton.getStyleClass().add("selected-button");
                graphicalViewButton.getStyleClass().remove("selected-button");
            } else if (newValue.equals(graphAnchorPane)) {
                graphicalViewButton.getStyleClass().add("selected-button");
                tableViewButton.getStyleClass().remove("selected-button");
            }

            tableViewButton.toFront();
            graphicalViewButton.toFront();

        });

    }

    public void handleTableSearch(){
        FilteredList<DrugSalesRanking> rankingsFilter = new FilteredList<>(rankings, pred ->true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            rankingsFilter.setPredicate(drugRanking ->{

                if(newValue.isEmpty()){
                    return true;
                }

                String newValueLowerCase = newValue.toLowerCase();

                return drugRanking.getDrugName().contains(newValueLowerCase);

            });
        });

        SortedList<DrugSalesRanking> rankingsSorted = new SortedList<>(rankingsFilter);
        rankingsSorted.comparatorProperty().bind(rankTable.comparatorProperty());
        rankTable.setItems(rankingsSorted);
    }

    AnchorPane graphAnchorPane;
    private void createGraphicNode(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("drugSoldRankingGraphs.fxml"));
        try {
            graphAnchorPane = loader.load();
            DrugSoldRankingGraphsController graphController = loader.getController();
            graphController.manualInitialize(buildData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private ObservableList<XYChart.Data<String, Number>> buildData(){

        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();

        for(DrugSalesRanking ranking: rankings){
            data.add(new XYChart.Data<>(ranking.getDrugName(), ranking.getQuantitySold()));
        }

        return data;
    }
    private void initializeTableColumns(){
        drugNameColumn.setCellValueFactory(new PropertyValueFactory<>("drugName"));
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));

        quantitySoldColumn.setCellFactory(param -> new TableCell<DrugSalesRanking, Integer>(){

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if(item == null){
                    setText(null);
                    return;
                }

                setText(item.toString());
                setAlignment(Pos.CENTER_RIGHT);
                setTextAlignment(TextAlignment.RIGHT);
                setMaxWidth(Double.MAX_VALUE);

            }
        });
    }


    @FXML private void handleTableViewButtonPressed(ActionEvent event){

        if(!rankingsBorderPane.getCenter().equals(tableViewParent)){
            rankingsBorderPane.setCenter(tableViewParent);
        }
    }

    @FXML private void handleGraphicButtonPressed(ActionEvent event){

        if(!rankingsBorderPane.getCenter().equals(this.graphAnchorPane)){
            rankingsBorderPane.setCenter(graphAnchorPane);
        }

    }

}
