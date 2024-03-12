package com.example.electricitybilling;

import javafx.scene.control.Alert;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class JDBCCommands {

    private final Connection connection;
    private double pricePerKwhDay;
    private double pricePerKwhNight;

    public JDBCCommands() throws SQLException, IOException {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("application.properties")) {
            properties.load(fileInputStream);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        pricePerKwhDay = Double.parseDouble(properties.getProperty("price.per.kwh.day"));
        pricePerKwhNight = Double.parseDouble(properties.getProperty("price.per.kwh.night"));
        connection = DriverManager.getConnection(url, username, password);
    }

    public boolean checkLichilnik(String meterId) {
        try {
            String query = "SELECT COUNT(*) FROM Invoices WHERE meter_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, meterId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    public double findAmountToPay(String meterId) {
        double amountToPay = 0.0;
        if (!checkLichilnik(meterId)) {
            return 0;
        }
        try {
            String getLastPaidDateQuery = "SELECT COALESCE(MAX(date), '1970-01-01') FROM Invoices WHERE meter_number = ? AND was_paid = true";
            PreparedStatement getLastPaidDateStatement = connection.prepareStatement(getLastPaidDateQuery);
            getLastPaidDateStatement.setString(1, meterId);
            ResultSet lastPaidDateResult = getLastPaidDateStatement.executeQuery();

            Date lastPaidDate = null;
            if (lastPaidDateResult.next()) {
                lastPaidDate = lastPaidDateResult.getDate(1);
            }
            System.out.println(lastPaidDate);

            String getLastUsageQuery = "SELECT COALESCE(MAX(day_kwh), 0), COALESCE(MAX(night_kwh), 0) FROM Invoices WHERE meter_number = ? AND was_paid = true";
            PreparedStatement getLastUsageStatement = connection.prepareStatement(getLastUsageQuery);
            getLastUsageStatement.setString(1, meterId);
            ResultSet lastUsageResult = getLastUsageStatement.executeQuery();

            double lastPaidDayKwh = 0.0;
            double lastPaidNightKwh = 0.0;
            if (lastUsageResult.next()) {
                lastPaidDayKwh = lastUsageResult.getDouble(1);
                lastPaidNightKwh = lastUsageResult.getDouble(2);
            }

            System.out.println(lastPaidDayKwh + "    " + lastPaidNightKwh);

            String getLatestUsageQuery = "SELECT COALESCE(day_kwh, 0), COALESCE(night_kwh, 0) FROM Invoices WHERE meter_number = ? ORDER BY date DESC LIMIT 1";
            PreparedStatement getLatestUsageStatement = connection.prepareStatement(getLatestUsageQuery);
            getLatestUsageStatement.setString(1, meterId);
            ResultSet latestUsageResult = getLatestUsageStatement.executeQuery();

            double latestDayKwh = 0.0;
            double latestNightKwh = 0.0;
            if (latestUsageResult.next()) {
                latestDayKwh = latestUsageResult.getDouble(1);
                latestNightKwh = latestUsageResult.getDouble(2);
            }
            System.out.println(latestDayKwh + "    " + latestNightKwh);

            double needToPayDay = latestDayKwh - lastPaidDayKwh;
            double needToPayNight = latestNightKwh - lastPaidNightKwh;

            amountToPay = needToPayDay * pricePerKwhDay + needToPayNight * pricePerKwhNight;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Інформація про рахунок");
            alert.setHeaderText("Ваша остання дата платежу: " + lastPaidDate + "!\n" +
                    "За цей час ви використали:\n" +
                    "Денних КВ: " + needToPayDay + "\n" +
                    "Нічних КВ: " + needToPayNight);
            alert.showAndWait();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amountToPay;
    }

}
