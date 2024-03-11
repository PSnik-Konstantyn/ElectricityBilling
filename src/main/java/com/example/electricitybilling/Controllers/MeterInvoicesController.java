package com.example.electricitybilling.Controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.io.IOException;

public class MeterInvoicesController {
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

    }
}
