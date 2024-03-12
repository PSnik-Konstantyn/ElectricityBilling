package com.example.electricitybilling.Controllers;

import com.example.electricitybilling.JDBCCommands;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

public class MeterInvoicesController {


    public MeterInvoicesController(String meterId, int moneyCounter) throws SQLException, IOException {
        this.meterId = meterId;
        this.moneyCounter = moneyCounter;
    }

    public MeterInvoicesController() throws SQLException, IOException {

    }


    public Label meterNumberLabel;
    public Label amountToPayLabel;
    public DatePicker datePicker;
    public TextField dayKwhTextField;
    public TextField nightKwhTextField;
    private String meterId;
    private double moneyCounter;

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public double getMoneyCounter() {
        return moneyCounter;
    }

    public void setMoneyCounter(double moneyCounter) {
        this.moneyCounter = moneyCounter;
    }

    private final JDBCCommands jdbcCommands = new JDBCCommands();

    @FXML
    public void initial(String meterId) throws IOException {
        double billAmount = jdbcCommands.findAmountToPay(meterId);
        amountToPayLabel.setText(billAmount + " $");
        meterNumberLabel.setText(meterId);
        this.meterId = meterId;
    }

    public void enterData(ActionEvent actionEvent) {
        try {
            double dayKwh = Double.parseDouble(dayKwhTextField.getText());
            double nightKwh = Double.parseDouble(nightKwhTextField.getText());
            Date billDate = Date.valueOf(datePicker.getValue());
            jdbcCommands.addNewKwh(meterId, dayKwh, nightKwh, billDate);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/electricitybilling/meterInvoices.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) meterNumberLabel.getScene().getWindow();
            currentStage.close();
            MeterInvoicesController workController = loader.getController();
            workController.initial(meterId);

            Stage workStage = new Stage();
            workStage.setTitle("Invoices Window");
            workStage.setScene(new Scene(root, 800, 600));
            workStage.show();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка!");
            alert.setHeaderText("Введіть корректні дані.");
            alert.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void pay(ActionEvent actionEvent) {
    }
}
