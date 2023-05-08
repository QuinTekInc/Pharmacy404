package admin;

import admin.auth.AdminAuthenticationController;
import admin.auth.ChangeAdminPasswordController;
import admin.reset.ResetAppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.MainWindowController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminPanelController implements Initializable {

    @FXML private BorderPane adminBorderPane;
    @FXML private VBox sideVBox;
    @FXML private AnchorPane adminPaneGreetingsPane;

    MainWindowController mainController;

    AdminAuthenticationController adminAuthController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adminBorderPane.setLeft(null);

        sideVBox.getStyleClass().add("admin-side-vbox");

        adminBorderPane.centerProperty().addListener(observable -> {
            if(!adminBorderPane.getCenter().equals(adminPaneGreetingsPane)){
                adminBorderPane.setLeft(sideVBox);
            }
        });

    }

    private AdminAuthenticationController getAdminAuthController() {
        return this.adminAuthController;
    }

    public void setAdminAuthController(AdminAuthenticationController adminAuthController) {
        this.adminAuthController = adminAuthController;
    }

    public MainWindowController getMainController(){
        return mainController;
    }

    public void setMainBorderPane(MainWindowController mainController){
        this.mainController = mainController;
    }


    @FXML private void loadAnalysis(ActionEvent event) throws IOException {

        Parent analysisPane = FXMLLoader.load(getClass().getResource("/admin/analysis/analysis.fxml"));

        try{
            adminBorderPane.setCenter(analysisPane);
        }catch (Exception ex){
            //do nothing
        }

    }

    @FXML private void loadSales(ActionEvent event) throws IOException {

        Parent sales = FXMLLoader.load(getClass().getResource("adminSales.fxml"));

        try{
            adminBorderPane.setCenter(sales);
        }catch (Exception ex){
            //do nothing
        }

    }

    @FXML private void loadStocks(ActionEvent event) throws IOException {

        Parent stock = FXMLLoader.load(getClass().getResource("adminDrugStock.fxml"));

        try{
            adminBorderPane.setCenter(stock);
        }catch (Exception ex){
            //do nothing
        }

    }

    @FXML private void loadLogs(ActionEvent event) throws IOException {

        Parent logsParent = FXMLLoader.load(getClass().getResource("adminLogs.fxml"));

        try {
            adminBorderPane.setCenter(logsParent);
        }catch (Exception ex){
            //do nothing
        }
    }

    @FXML private void changeAdminPassword(ActionEvent event)throws IOException{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/auth/changeAdminPassword.fxml"));
        Parent passwordParent = loader.load();

        ChangeAdminPasswordController controller = loader.getController();
        controller.manualInitialize(getMainController().mainBorderPane, getAdminAuthController());

        Scene scene = new Scene(passwordParent);
        Stage passwordStage = new Stage(StageStyle.UTILITY);
        passwordStage.setScene(scene);
        passwordStage.initOwner(((Button) event.getSource()).getScene().getWindow());
        passwordStage.initModality(Modality.APPLICATION_MODAL);
        passwordStage.show();


    }

    @FXML private void resetApp(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/reset/resetApp.fxml"));
        Parent parent = loader.load();

        ResetAppController controller = loader.getController();
        controller.setParentBorderPane(adminBorderPane);

        Scene scene = new Scene(parent);
        Stage resetStage = new Stage(StageStyle.UTILITY);
        resetStage.setScene(scene);
        resetStage.initModality(Modality.APPLICATION_MODAL);
        resetStage.setTitle("Pharmacy 404 - Admin/Reset Application");

        resetStage.show();

    }

    @FXML private void backButtonPressed(ActionEvent event){
        getMainController().mainBorderPane.setCenter(getMainController().getCurrentCenterNode());
    }


}
