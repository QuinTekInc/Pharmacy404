package admin;

import custom_cell.CustomDrugAmountTableCell;
import data.db.DataExtractor;
import data.models.Drug;
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

public class AdminDrugStockController implements Initializable {

    @FXML private TableView<Drug> stockTable;
    @FXML private TableColumn<Drug, String> drugNameCol;
    @FXML private TableColumn<Drug, String> drugTypeCol;
    @FXML private TableColumn<Drug, String> manufacturerCol;
    @FXML private TableColumn<Drug, Integer> quantityCol;
    @FXML private TableColumn<Drug, Double> priceCol;
    @FXML private TableColumn<Drug, Double> retailPriceCol;
    @FXML private RadioButton extendedViewRadioButton;
    @FXML private RadioButton simpleViewRadioButton;
    @FXML private TextField searchTextField;

    ObservableList<Drug> extendedDrugViewData;
    ObservableList<Drug> simpleDrugViewData;

    @FXML
    private Button viewInforButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadData();

        initializeTableColumns();
        stockTable.setItems(extendedDrugViewData);
        viewInforButton.disableProperty().bind(stockTable.getSelectionModel().selectedItemProperty().isNull());
        extendedViewRadioButton.setSelected(true);

        handleStockTableSearch(extendedDrugViewData);

        extendedViewRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                simpleViewRadioButton.setSelected(false);
                stockTable.setItems(extendedDrugViewData);
                handleStockTableSearch(stockTable.getItems());
            }
        });

        simpleViewRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                extendedViewRadioButton.setSelected(false);
                stockTable.setItems(simpleDrugViewData);
                handleStockTableSearch(stockTable.getItems());
            }
        });
    }

    private void initializeTableColumns(){
        drugNameCol.setCellValueFactory(new PropertyValueFactory<>("drugName"));
        drugTypeCol.setCellValueFactory(new PropertyValueFactory<>("drugType"));
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        retailPriceCol.setCellValueFactory(new PropertyValueFactory<>("retailPrice"));

        priceCol.setCellFactory(param -> new CustomDrugAmountTableCell());
        retailPriceCol.setCellFactory(param -> new CustomDrugAmountTableCell());
    }

    private void loadData(){
        extendedDrugViewData = DataExtractor.getDrugs();
        simpleDrugViewData = DataExtractor.getSimpleDrugViewData();
    }

    @FXML public void handleStockAdd(ActionEvent event) throws IOException {

        FXMLLoader loader  = new FXMLLoader(getClass().getResource("/stocks/addStock.fxml"));
        Parent parent = loader.load();

        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(stockTable.getScene().getWindow());

        stage.setOnHidden(stageHidden ->{

            loadData();
            if(simpleViewRadioButton.isSelected()){
              stockTable.setItems(simpleDrugViewData);
            }else{
               stockTable.setItems(extendedDrugViewData);
            }

        });

        stage.show();
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

    @FXML public void handleViewStockInfo(ActionEvent event) throws IOException {

        Drug selectedDrug = stockTable.getSelectionModel().getSelectedItem();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("adminViewDrugDetail.fxml"));
        Parent parent = loader.load();

        AdminViewDrugDetailController detailController = loader.getController();
        detailController.manualInitialize(selectedDrug, simpleViewRadioButton.isSelected());

        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(new Scene(parent));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnHidden(onHidden ->{

            loadData();
            if(simpleViewRadioButton.isSelected()){
                stockTable.setItems(simpleDrugViewData);
            }else{
                stockTable.setItems(extendedDrugViewData);
            }

        });

        stage.show();

    }

}
