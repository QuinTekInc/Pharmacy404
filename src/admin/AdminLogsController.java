package admin;

import data.db.DataExtractor;
import data.db.DatabaseConnector;
import data.models.Log;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdminLogsController implements Initializable {

    @FXML private ListView<Log> logListView;
    @FXML private TextField dateTextField;
    @FXML private TextField logTitleTextField;
    @FXML private TextArea detailsTextArea;
    @FXML private ComboBox<String> logsFilterComboBox;
    String[] filters = {"All Filters", "Security", "Sales"};
    ObservableList<Log> logs = FXCollections.observableArrayList();

    @Override public void initialize(URL loc, ResourceBundle rb){

        logsFilterComboBox.getItems().addAll(filters);
        logsFilterComboBox.getSelectionModel().select(0);

        filterLogsList();

        logListView.setCellFactory(param -> new LogCell());

        logListView.setOnMouseClicked(mouse_clicked->{
            Log selectedLog = logListView.getSelectionModel().getSelectedItem();
            if(selectedLog != null){
                dateTextField.setText(selectedLog.getDate().toString());
                logTitleTextField.setText(selectedLog.getTitle());
                detailsTextArea.setText(selectedLog.getDetails());
            }
        });

        try {
            loadLogData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void filterLogsList(){

        logsFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) ->{
            switch (newValue){
                case "All Filters":
                    logListView.setItems(logs);
                    break;

                case "Sales":
                    loadSaleRelatedFilter();
                    break;

                case "Security":
                    loadSecurityRelatedFilter();
                    break;
            }
        });

    }

    void loadSecurityRelatedFilter(){

        ObservableList<Log> securityLogFilter = FXCollections.observableArrayList();

        for(Log log: logs){
            String title = log.getTitle();

            if(title.toLowerCase().contains("security") || title.toLowerCase().contains("password")){
                securityLogFilter.add(log);
            }
        }

        logListView.setItems(securityLogFilter);

    }

    void loadSaleRelatedFilter(){

        ObservableList<Log> salesLogFilter = FXCollections.observableArrayList();

        for(Log log: logs){
            String title = log.getTitle();

            if(title.toLowerCase().contains("sale") || title.toLowerCase().contains("sales")){
                salesLogFilter.add(log);
            }
        }

        logListView.setItems(salesLogFilter);

    }

    private void loadLogData() throws SQLException {

        logs.clear();

        String query = "select * from logs";
        PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while(rs.next()){
            int id = rs.getInt("id");
            LocalDate date = LocalDate.parse(String.valueOf(rs.getDate("log_date")));
            String title = rs.getString("title");
            String details = rs.getString("details");

            logs.add(new Log(id, date, title, details));
        }


        logListView.setItems(logs);

    }


    @FXML
    void deleteSelectedLog(ActionEvent event) throws SQLException {

        if(DataExtractor.deleteLog(logListView.getSelectionModel().getSelectedItem().getId())){
            clearFields();
            loadLogData();
        }

    }

    @FXML
    void handleClearLogs(ActionEvent event) throws SQLException {

        if (DataExtractor.clearLogs()) {
            clearFields();
            loadLogData();
        }

    }

    private void clearFields(){
        dateTextField.clear();
        logTitleTextField.clear();
        detailsTextArea.clear();
    }

    private static class LogCell extends ListCell<data.models.Log>{

        VBox parent;
        Label dateLabel;
        Label titleLabel;

        public LogCell(){
            super();

            parent = new VBox();
            parent.setMaxHeight(50);
            parent.setMinHeight(50);
            parent.setPrefHeight(50);

            dateLabel = new Label();
            titleLabel = new Label();

            parent.getChildren().addAll(dateLabel, titleLabel);
            setGraphic(parent);
        }

        @Override
        protected void updateItem(data.models.Log item, boolean isEmpty) {
            super.updateItem(item, isEmpty);

            if(item == null){
                setText(null);
                setGraphic(null);
                return;
            }

            String logDateString = item.getDate().toString();
            String logTitle = item.getTitle();

            if(!logDateString.isEmpty() && !logTitle.isEmpty()){
                dateLabel.setText(logDateString);
                titleLabel.setText(logTitle);

                parent.setStyle("-fx-border-width: 0 0 0.5 0;" +
                        "-fx-border-color: #b2b2b2");

            }


            setGraphic(parent);
        }
    }
}
