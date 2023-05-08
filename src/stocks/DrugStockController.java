package stocks;

import custom_cell.CustomDrugAmountTableCell;
import data.db.DataExtractor;
import data.models.Drug;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DrugStockController implements Initializable {

    @FXML private TableView<Drug> stockTable;
    @FXML private TableColumn<Drug, String> drugNameCol;
    @FXML private TableColumn<String, String> drugTypeCol;
    @FXML private TableColumn<Drug, String> manufacturerCol;
    @FXML private TableColumn<Drug, Integer> quantityCol;
    @FXML private TableColumn<Drug, Double> priceCol;
    @FXML private Button viewInfoButton;
    @FXML private RadioButton simpleViewRadioButton;
    @FXML private RadioButton extendedViewRadioButton;

    @FXML private TextField searchTextField;

    ObservableList<Drug> extendedDrugsData = FXCollections.observableArrayList();
    ObservableList<Drug> simpleDrugsData = FXCollections.observableArrayList();


    @FXML
    public void initialize(URL location, ResourceBundle rb){

        loadData();

        initTableCols();
        stockTable.setItems(extendedDrugsData);
        extendedViewRadioButton.setSelected(true);
        viewInfoButton.disableProperty().bind(stockTable.getSelectionModel().selectedItemProperty().isNull());

        handleStockTableSearch(extendedDrugsData);

        extendedViewRadioButton.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){
                simpleViewRadioButton.setSelected(false);
                stockTable.setItems(extendedDrugsData);
                handleStockTableSearch(stockTable.getItems());
            }

        });

        simpleViewRadioButton.selectedProperty().addListener((observable, oldValue, newValue) ->{

            if(newValue){
                extendedViewRadioButton.setSelected(false);
                stockTable.setItems(simpleDrugsData);
                handleStockTableSearch(stockTable.getItems());
            }

        });

    }

    private void initTableCols(){
        drugNameCol.setCellValueFactory(new PropertyValueFactory<>("drugName"));
        drugTypeCol.setCellValueFactory(new PropertyValueFactory<>("drugType"));
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("retailPrice"));
        priceCol.setCellFactory(param -> new CustomDrugAmountTableCell());
    }

    public void manualInitialize(){
        loadData();

        if(simpleViewRadioButton.isSelected()){
            stockTable.setItems(simpleDrugsData);
        }else{
            stockTable.setItems(extendedDrugsData);
        }

    }

    private void loadData(){

        extendedDrugsData = DataExtractor.getDrugs();
        simpleDrugsData = DataExtractor.getSimpleDrugViewData();


    }

    public void handleStockTableSearch(ObservableList<Drug> items){

        FilteredList<Drug> drugsFilter = new FilteredList<>(items, pred -> true);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) ->{
            drugsFilter.setPredicate(drug -> {

                if(newValue.isEmpty()){
                    return true;
                }

                String toLowerCase = newValue.toLowerCase();

                return drug.getDrugName().toLowerCase().contains(toLowerCase) ||
                        drug.getManufacturer().toLowerCase().contains(toLowerCase) ||
                        drug.getDrugType().toString().toLowerCase().contains(toLowerCase);

            });
        });


        SortedList<Drug> drugsSorted = new SortedList<>(drugsFilter);
        drugsSorted.comparatorProperty().bind(stockTable.comparatorProperty());
        stockTable.setItems(drugsSorted);

    }



    @FXML void handleStockAdd(ActionEvent event) throws IOException {

        Parent parent = FXMLLoader.load(getClass().getResource("addStock.fxml"));

        Scene scene = new Scene(parent);
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(scene);
        stage.setTitle("Add Drug Stock");
        stage.setOnHidden(stageHidden ->{

            loadData();
            if(simpleViewRadioButton.isSelected()){
                stockTable.setItems(simpleDrugsData);
            }else{
                stockTable.setItems(extendedDrugsData);
            }

        });
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

    }

    @FXML
    void handleViewStockInfo(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("viewDrugDetails.fxml"));
        Parent parent = loader.load();

        DrugDetailController ddcontroller = loader.getController();
        ddcontroller.inflateUI(stockTable.getSelectionModel().getSelectedItem());

        Scene scene = new Scene(parent);
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(scene);
        stage.setTitle("Add Drug Stock");
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setOnHidden(stageHidden ->{

            loadData();

            if(simpleViewRadioButton.isSelected()){
                stockTable.setItems(simpleDrugsData);
            }else{
                stockTable.setItems(extendedDrugsData);
            }

        });

        stage.show();
    }

}
