package admin.auth;

import alert.AlertViewController;
import data.db.DataExtractor;
import data.models.encryption.Cryptography;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ChangeAdminPasswordController implements Initializable {

    @FXML private PasswordField currentPasswordTextField;
    @FXML private PasswordField newPasswordTextField;
    @FXML private PasswordField confirmPasswordTextField;
    @FXML private Button confirmNewPasswordButton;

    private String currentPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Map.Entry<String, String> adminPasswordEntry = DataExtractor.getAdminPassword();
        try {
            currentPassword = Cryptography.decrypt(adminPasswordEntry.getValue(), adminPasswordEntry.getKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        confirmNewPasswordButton.disableProperty().bind(
                confirmPasswordTextField.textProperty().isNotEqualTo(newPasswordTextField.getText()).not());

        createAlert();
    }

    Stage alertStage;
    AlertViewController alertController;

    BorderPane mainBorderPane;
    AdminAuthenticationController adminAuthController;

    private void createAlert(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/alert/alertView.fxml"));
        try {
            Parent alertParent = loader.load();
            Scene alertScene = new Scene(alertParent);

            alertController = loader.getController();

            alertStage = new Stage();
            alertStage.setScene(alertScene);
            alertStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void manualInitialize(BorderPane mainBorderPane, AdminAuthenticationController authController){
        this.mainBorderPane = mainBorderPane;
        this.adminAuthController= authController;
    }

    @FXML private void handleConfirmNewPassword(ActionEvent event) throws Exception {

        String currentPassword = currentPasswordTextField.getText();

        if(!currentPassword.equals(this.currentPassword)){
            System.out.println("Incorrect current password");
            return;
        }


        String newPassword = newPasswordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();



        String key = Cryptography.generateKey();
        String encryptedPassword = Cryptography.encrypt(newPassword, key);//exception occurs here

        if(DataExtractor.updateAdminPassword(encryptedPassword, key)){

            alertController.alertViewSetup(AlertViewController.AlertStyle.INFORMATION,
                    "Password Update",
                    "Your password has be updated to the database successfully", true);

            alertStage.setOnHiding(stageHiding ->{
                adminAuthController.setAdminPassword(newPasswordTextField.getText());
                this.mainBorderPane.setCenter(adminAuthController.authParent);
            });

            alertStage.show();
        }

    }

    @FXML private void handleCancel(ActionEvent event){
        ((Button) event.getSource()).getScene().getWindow().hide();
    }

}
