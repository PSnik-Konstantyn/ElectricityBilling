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
import java.sql.SQLException;

public class StartController {
    public TextField enterId;
    public Button startButton;

    private JDBCCommands jdbcCommands = new JDBCCommands();

    public StartController() throws SQLException, IOException {
    }

    @FXML
    public void clickStart() {
        String enteredId = enterId.getText();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/electricitybilling/meterInvoices.fxml"));
            System.out.println("good");
            Parent root = loader.load();
            System.out.println("good2");
            Stage currentStage = (Stage) startButton.getScene().getWindow();
            currentStage.close();
            MeterInvoicesController workController = loader.getController();
            workController.initial(new Stage(), enteredId);

            Stage workStage = new Stage();
            workStage.setTitle("Invoices Window");
            workStage.setScene(new Scene(root, 800, 600));
            workStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



