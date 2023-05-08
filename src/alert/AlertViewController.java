package alert;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;

public class AlertViewController {

    @FXML private AnchorPane alertContainer;
    @FXML public Button confirmButton;
    @FXML public Button cancelButton;
    @FXML public HBox buttonContainers;
    @FXML public Label alertTitleLabel;
    @FXML public Label alertMessageLabel;
    @FXML public ImageView alertTypeImageView;


    public void alertViewSetup(AlertStyle alertStyle, String title, String message){
        alertViewSetup(alertStyle, title, message, false);
    }


    public void alertViewSetup(AlertStyle alertStyle, String title, String message, boolean removeCancelButton){

        alertTypeImageView.setImage(new Image(alertStyle.toString()));
        alertTitleLabel.setText(title);
        alertMessageLabel.setText(message);

        if(removeCancelButton){
            buttonContainers.getChildren().remove(cancelButton);
            confirmButton.setOnAction(action -> confirmButton.getScene().getWindow().hide());
        }else{

            if(!buttonContainers.getChildren().contains(cancelButton)){
                buttonContainers.getChildren().add(cancelButton);
            }

        }

        if(alertStyle.equals(AlertStyle.WARNING)){
            confirmButton.getStyleClass().add("cancel-button");
            cancelButton.getStyleClass().remove("cancel-button");
        }

    }

    @FXML private void handleCancelButtonPressed(ActionEvent event){
        ((Button) event.getSource()).getScene().getWindow().hide();
    }

    public enum AlertStyle {

        INFORMATION("/alert/icons/icons8-info-96.png"),
        WARNING("/alert/icons/icons8-warning-96.png"),
        CONFIRMATION("/alert/icons/icons8-confirmation-96.png"),
        ERROR("/alert/icons/icons8-error-96.png");

        private final String iconLocation;

        private AlertStyle(String iconLocation){
            this.iconLocation = iconLocation;
        }

        @Override
        public String toString() {
            return iconLocation;
        }
    }

}
