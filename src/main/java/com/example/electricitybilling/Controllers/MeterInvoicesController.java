package com.example.electricitybilling.Controllers;

import com.example.electricitybilling.JDBCCommands;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
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

    private final JDBCCommands jdbcCommands = new JDBCCommands();

    @FXML
    public void initial(String meterId) throws IOException {
        double billAmount = jdbcCommands.findAmountToPay(meterId);
        amountToPayLabel.setText(billAmount + " $");
        meterNumberLabel.setText(meterId);

    }

    public void enterData(ActionEvent actionEvent) {
    }

    public void pay(ActionEvent actionEvent) {
    }
}
