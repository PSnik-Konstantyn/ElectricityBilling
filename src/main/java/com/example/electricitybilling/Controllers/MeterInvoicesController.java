package com.example.electricitybilling.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class MeterInvoicesController {


    public MeterInvoicesController(String meterId, int moneyCounter) {
        this.meterId = meterId;
        this.moneyCounter = moneyCounter;
    }

    public MeterInvoicesController() {

    }

    public Label meterNumberLabel;
    public Label amountToPayLabel;
    public DatePicker datePicker;
    public TextField dayKwhTextField;
    public TextField nightKwhTextField;
    private String meterId;
    private int moneyCounter;

    public int getMoneyCounter() {
        return moneyCounter;
    }

    public void setMoneyCounter(int moneyCounter) {
        this.moneyCounter = moneyCounter;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    @FXML
    public void initial(Stage stage, String meterId) throws IOException {
        amountToPayLabel.setText("100$");
        meterNumberLabel.setText("100-12da");
    }

    public void enterData(ActionEvent actionEvent) {
    }

    public void pay(ActionEvent actionEvent) {
    }
}
