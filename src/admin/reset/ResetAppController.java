package admin.reset;

import alert.AlertViewController;
import data.db.DataExtractor;
import data.db.DatabaseConnector;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ResetAppController implements Initializable {

    @FXML private PasswordField passwordField;
    @FXML private Button resetButton;
    BorderPane parentBorderPane;
    String adminPassword;

    @Override public void initialize(URL location, ResourceBundle resources) {

        Map.Entry<String, String> userPasswordMap = DataExtractor.getAdminPassword();

        try {
            adminPassword = Cryptography.decrypt(userPasswordMap.getValue(), userPasswordMap.getKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        resetButton.disableProperty().bind(passwordField.textProperty().isEmpty());

        createAlert();

    }

    @FXML void handleCancel(ActionEvent event) {
        ((Button) event.getSource()).getScene().getWindow().hide();
    }

    @FXML void handleReset(ActionEvent event) {

        String adminPassword = passwordField.getText();

        if(!adminPassword.equals(this.adminPassword)){

            DataExtractor.addLog("Security: Incorrect Password",
                    String.format("Cannot reset application due to incorrect admin password %s entered.\n " +
                            "Enter the correct administrator password and click the reset button to reset the application", adminPassword));

            alertController.alertViewSetup(AlertViewController.AlertStyle.ERROR, "App Reset",
                    "Cannot reset application. Incorrect administrator password entered", true);

            passwordField.clear();
            alertStage.show();

            return;

        }

        alertController.alertViewSetup(AlertViewController.AlertStyle.WARNING, "Reset Application",
                "Resetting the application will clear all data including drug stock and sales from " +
                        "the application's database and reset the administrator password back to it's default setting.\n\n" +
                        "NOTE THAT: This action is irreversible. Do you still wish to continue?" );

        alertController.confirmButton.setText("Confirm App Reset");

        alertController.confirmButton.setOnAction(confirmbButtonPressed ->{

            if(dropAllTables()){
                passwordField.getScene().getWindow().hide();
                getParentBorderPane().getScene().getWindow().hide();
                showSuccessAlert();
            }else{
                showErrorAlert();
            }

        });

        alertStage.show();

    }

    public BorderPane getParentBorderPane(){
        return parentBorderPane;
    }

    public void setParentBorderPane(BorderPane parentBorderPane) {
        this.parentBorderPane = parentBorderPane;
    }

    Stage alertStage;
    AlertViewController alertController;

    void createAlert(){

        FXMLLoader alertLoader = new FXMLLoader(getClass().getResource("/alert/alertView.fxml"));
        try {
            Parent alertParent = alertLoader.load();

            alertController = alertLoader.getController();

            Scene alertScene = new Scene(alertParent);
            alertStage = new Stage();
            alertStage.setScene(alertScene);
            alertStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void showSuccessAlert(){

        alertController.alertViewSetup(AlertViewController.AlertStyle.INFORMATION,
                "Reset Success", "Application has successfully been reset." +
                        "\n\nYou manually have to open the application", true);

        alertController.confirmButton.getStyleClass().remove("cancel-button");
        alertController.confirmButton.setText("Okay");

        alertStage.setOnHidden(alertStageHidden ->{
           System.exit(0);
        });

        alertStage.show();

    }

    private void showErrorAlert(){

        alertController.alertViewSetup(AlertViewController.AlertStyle.ERROR,
                "Reset Failed", "Could not reset application. ", true);

        alertController.confirmButton.setText("Close");
        alertController.confirmButton.getStyleClass().remove("cancel-button");

        alertStage.setOnHidden(stageHiding ->{
           passwordField.clear();
           passwordField.getScene().getWindow().hide();

        });

        alertStage.show();

    }


    public boolean dropAllTables(){

        boolean isDone = false;

        List<String> tables = new ArrayList<>(Arrays.asList("admin_password", "drugs", "sales", "logs"));

        for (String table:tables){
            Statement stmt = null;
            try {
                stmt = DatabaseConnector.getInstance().getConnection().createStatement();
                stmt.execute("delete from " + table);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        //closing the connection

        try {
            DatabaseConnector.getInstance().getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //creating new dummy Connection to dop table
        try {
            Class.forName("org.sqlite.JDBC");
            //do developer use Builder.buildDatabasePath()+"/pharmacy404.db"
            Connection connection = DriverManager.getConnection("jdbc:sqlite:pharmacy404.db");
            Statement statement = connection.createStatement();

            for(String table: tables){
                statement.execute("drop table "+ table);
            }

            isDone = true;

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
           e.printStackTrace();
        }


        return isDone;

    }



}
