package admin.auth;

import admin.AdminPanelController;
import data.db.DataExtractor;
import data.models.encryption.Cryptography;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import main.MainWindowController;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminAuthenticationController implements Initializable {

    @FXML
    public VBox authParent;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button enterButton;

    MainWindowController mainController;

    private String adminPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Map.Entry<String, String> authEntry = DataExtractor.getAdminPassword();
        try {
            adminPassword = Cryptography.decrypt(authEntry.getValue(), authEntry.getKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        enterButton.disableProperty().bind(passwordField.textProperty().isEmpty());
    }

    public void manualIntialize(MainWindowController mainController){

       this.mainController = mainController;

    }

    @FXML
    void handleEnterPressed(ActionEvent event) throws IOException {

        if(passwordField.getText().equals(getAdminPassword())){

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/adminPanel.fxml"));
            Parent parent = loader.load();

            AdminPanelController panelController = loader.getController();
            panelController.setMainBorderPane(mainController);
            panelController.setAdminAuthController(this);

            this.mainController.mainBorderPane.setCenter(parent);
            passwordField.clear();

            return;
        }

        if(DataExtractor.addLog("Admin Security -Wrong Password Entered", String.format("The admin password: '%s' user entered is incorrect", passwordField.getText()))){
            System.out.println("Log Added");
        }

    }

    public String getAdminPassword(){
        return adminPassword;
    }

    public void setAdminPassword(String password){
        this.adminPassword = password;
    }

}
