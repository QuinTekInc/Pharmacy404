package main;

import data.db.DataExtractor;
import data.db.DatabaseConnector;
import data.models.Builder;
import data.models.encryption.Cryptography;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Map;

public class Main extends Application {

    private void initAdminPassword(){

        Map.Entry<String, String> authEntry = DataExtractor.getAdminPassword();

        if(authEntry != null){
            return;
        }


        try {

            String key = Cryptography.generateKey();
            String adminPassword = Cryptography.encrypt("admin", key);

            DataExtractor.insertAdminPassword(adminPassword, key);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override public void start(Stage stage) throws Exception {

        DatabaseConnector.getInstance();
        initAdminPassword();

        StackPane borderPane = FXMLLoader.load(getClass().getResource("/main/mainWindow.fxml"));//ioexception here

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Pharmacy 404 - Designed by QuinTek Softwares Inc.");
        //call the database connector to run for the first time
        stage.getIcons().add(new Image(Builder.defaultAppImage));

        stage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

}