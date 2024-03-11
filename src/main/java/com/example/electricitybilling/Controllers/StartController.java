package com.example.electricitybilling.Controllers;

import com.example.electricitybilling.JDBCCommands;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class StartController {
    public TextField enterId;
    public Button startButton;
    public Button registrationButton;

    private JDBCCommands jdbcCommands = new JDBCCommands();

    public StartController() throws SQLException, IOException {
    }

    @FXML
    public void clickStart() throws SQLException {
        String enteredId = enterId.getText();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("meterInvoices.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) startButton.getScene().getWindow();
            currentStage.close();
            MeterInvoicesController workController = loader.getController();
            workController.initial(new Stage(), enteredId);

            Stage workStage = new Stage();
            workStage.setTitle("Invoices Window");
            workStage.setScene(new Scene(root, 1000, 700));
            workStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        }

}



