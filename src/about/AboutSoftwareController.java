package about;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutSoftwareController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private Text detailsText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        detailsText.setText(buildAbout());
    }

    private String buildAbout(){
        StringBuilder builder = new StringBuilder("About Pharmacy 404\n\n");

        builder.append("Pharmacy 404 is standalone Point of sales(PoS) software aimed at providing the basic " +
                "needs small scale pharmaceutical shops.\n");
        builder.append("Its features include:\n");
        builder.append("1. Drug stock and sales tracking\n\n");
        builder.append("2. Local Database, which stores information about stock and sales that occur in this pharmacy.");
        builder.append("    The CRUD(Create, Record, Update and Delete) feature of this application allows users to " +
                "save and update records, and delete unwanted records from the database\n\n");
        builder.append("3. Good user interface.\n\n");
        builder.append("4. Intuitive summarization of data using graphs, texts and tables for administrators and users to make decisions" +
                "           regarding the operations in the pharmacy\n\n");
        builder.append("5. Admin Panel protection: This features prevents users from gaining access to the application's " +
                "admin to perform illegal operations in the application. " +
                "Password to the administrator panel of this application is encrypted\n\n");
        builder.append("6. Logging: This feature allow administrators to view some minor operations that users have made " +
                "in the application. This includes updating of a sale record and also, " +
                "trying to access the administrator panel of the application");
        builder.append("\n\n\n");
        builder.append("Version: 0.1 alpha\n");
        builder.append("Build Tools: Java and Javafx Version 8(OpenJDK 1.8.0_361 Embedded Javafx)\n");
        builder.append("3rd Party Libraries Used: JFoenix\n");
        builder.append("            ControlsFX\n");
        builder.append("            JDBC SQLite\n\n");

        builder.append("Release Date: Saturday, 24th April, 2023");
        builder.append("\n");
        builder.append("Released By: QuinTek Inc.\n");
        builder.append("License: Opensource");
        builder.append("\n\n\n");
        builder.append("Please note that: This copy of our software is not the stable long term release of our application." +
                "Feel free to contact us on the features you would like to be added to the application.\n\n");
        builder.append("Contact Quintek Inc.:\n");
        builder.append("    Email: quinsefalloyd@gmail.com\n");
        builder.append("    Phone(WhatsApp): +233 24 737 5161 or +233 50 072 1537");

        return builder.toString();
    }


    @FXML private void handleBackToTop(ActionEvent event){
        scrollPane.setVvalue(0);
    }


}
