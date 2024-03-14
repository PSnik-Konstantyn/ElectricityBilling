package com.example.electricitybilling;

import javafx.scene.control.Alert;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Properties;

public class JDBCCommands {

    private final Connection connection;
    private final double pricePerKwhDay;
    private final double pricePerKwhNight;
    private final double nakrutkaDay;
    private final double nakrutkaNight;
    private double dayKhwNotPaid;
    private double nightKhwNotPaid;


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
        nakrutkaDay = Double.parseDouble(properties.getProperty("nakrutka.day"));
        nakrutkaNight = Double.parseDouble(properties.getProperty("nakrutka.night"));
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

            Khw newKnw = getTheNewestKhw(meterId);
            double latestDayKwh = newKnw.getDayKhw();
            double latestNightKwh = newKnw.getNightKhw();
            System.out.println(latestDayKwh + "    " + latestNightKwh);

            double needToPayDay = latestDayKwh - lastPaidDayKwh;
            double needToPayNight = latestNightKwh - lastPaidNightKwh;

            dayKhwNotPaid = needToPayDay;
            nightKhwNotPaid = needToPayNight;

            amountToPay = needToPayDay * pricePerKwhDay + needToPayNight * pricePerKwhNight;

            if (Objects.equals(lastPaidDate, Date.valueOf("1970-01-01"))) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Інформація про рахунок");
                alert.setHeaderText("Ви ще не оплачували рахунків!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Інформація про рахунок");
                alert.setHeaderText("Ваша остання дата платежу: " + lastPaidDate + "!\n" +
                        "За цей час ви використали:\n" +
                        "Денних КВ: " + needToPayDay + "\n" +
                        "Нічних КВ: " + needToPayNight);
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amountToPay;
    }

    public void addNewKwh(String meterId, double dayKwh, double nightKwh, Date billDate) {
        try {
            String getLastPaidDateQuery = "SELECT COALESCE(MAX(date), '1970-01-01') FROM Invoices WHERE meter_number = ?";
            PreparedStatement getLastPaidDateStatement = connection.prepareStatement(getLastPaidDateQuery);
            getLastPaidDateStatement.setString(1, meterId);
            ResultSet lastPaidDateResult = getLastPaidDateStatement.executeQuery();

            Date latestDate = null;
            if (lastPaidDateResult.next()) {
                latestDate = lastPaidDateResult.getDate(1);
            }

            if (!billDate.after(latestDate)) {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Оберіть іншу дату!");
//                alert.setHeaderText("Найновіший запис вже існує для цього лічильника. Оберіть іншу дату");
//                alert.showAndWait();
            } else {
                String sql = "SELECT day_kwh, night_kwh FROM Invoices WHERE meter_number = ? ORDER BY date DESC LIMIT 1";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, meterId);
                ResultSet resultSet = statement.executeQuery();
                double latestDayKwh = 0.0;
                double latestNightKwh = 0.0;
                if (resultSet.next()) {
                    latestDayKwh = resultSet.getDouble("day_kwh");
                    latestNightKwh = resultSet.getDouble("night_kwh");
                }
                if (latestDayKwh > dayKwh || latestNightKwh > nightKwh) {
                    if (latestDayKwh > dayKwh) {
                        dayKwh = latestDayKwh + nakrutkaDay;
                    }

                    if (latestNightKwh > nightKwh) {
                        nightKwh = latestNightKwh + nakrutkaNight;
                    }
//                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                    alert.setTitle("Жах!");
//                    alert.setHeaderText("Ви намагались ввести несправжні дати, за це вам накручено такі дані\n" +
//                            "Денні КВ: " + dayKwh + "\nНічні КВ: " + nightKwh);
//                    alert.showAndWait();
                }
                String insertQuery = "INSERT INTO Invoices (meter_number, day_kwh, night_kwh, date, was_paid) VALUES (?, ?, ?, ?, false)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, meterId);
                insertStatement.setDouble(2, dayKwh);
                insertStatement.setDouble(3, nightKwh);
                insertStatement.setDate(4, new java.sql.Date(billDate.getTime()));
                insertStatement.executeUpdate();

//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Перемога!");
//                alert.setHeaderText("Дані оновлено!");
//                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void payBill(String meterId, double amountToPay) {
        try {
            String getLastPaidDateQuery = "SELECT COALESCE(MAX(date), '1970-01-01') FROM Invoices WHERE meter_number = ?";
            PreparedStatement getLastPaidDateStatement = connection.prepareStatement(getLastPaidDateQuery);
            getLastPaidDateStatement.setString(1, meterId);
            ResultSet lastPaidDateResult = getLastPaidDateStatement.executeQuery();

            Date latestDate;
            if (lastPaidDateResult.next()) {
                latestDate = lastPaidDateResult.getDate(1);
                String markAsPaidQuery = "UPDATE Invoices SET was_paid = true WHERE meter_number = ? AND date = ?";
                PreparedStatement markAsPaidStatement = connection.prepareStatement(markAsPaidQuery);
                markAsPaidStatement.setString(1, meterId);
                markAsPaidStatement.setDate(2, latestDate);
                markAsPaidStatement.executeUpdate();
            }

            LocalDate currentDate = LocalDate.now();

            String insertQuery = "INSERT INTO BillsHistory (meter_number, current_day_kwh, current_night_kwh, current_price_per_kwh_day, current_price_per_kwh_night, date, amount_paid) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, meterId);
            insertStatement.setDouble(2, dayKhwNotPaid);
            insertStatement.setDouble(3, nightKhwNotPaid);
            insertStatement.setDouble(4, pricePerKwhDay);
            insertStatement.setDouble(5, pricePerKwhNight);
            insertStatement.setObject(6, currentDate);
            insertStatement.setDouble(7, amountToPay);

            insertStatement.executeUpdate();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Перемога!");
            alert.setHeaderText("Рахунок сплачено!");
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createZvitPDF(String meterId) throws IOException {
        String pdfFilePath = "/home/kostiantyn/IdeaProjects/ElectricityBilling/zvit.pdf";

        Files.deleteIfExists(Paths.get(pdfFilePath));

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Full payment history for meter " + meterId);
                contentStream.endText();

                String query = "SELECT * FROM BillsHistory WHERE meter_number = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, meterId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        int y = 680;
                        while (resultSet.next()) {
                            String entry;
                            String date = String.valueOf(resultSet.getDate("date"));
                            entry = "Date: " + date;
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;

                            entry = "Date: " + date;
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            entry = "Amount Paid: " + resultSet.getDouble("amount_paid");
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;

                            entry = "Day kWh: " + resultSet.getDouble("current_day_kwh");
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;

                            entry = "Night kWh: " + resultSet.getDouble("current_night_kwh");
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;

                            entry = "Price per kWh (Day): " + resultSet.getDouble("current_price_per_kwh_day");
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;

                            entry = "Price per kWh (Night): " + resultSet.getDouble("current_price_per_kwh_night");
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;

                            entry = " ";
                            contentStream.beginText();
                            contentStream.showText(entry);
                            contentStream.newLine();
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(100, y);
                            contentStream.showText(entry);
                            contentStream.endText();
                            y -= 20;
                        }
                    }
                }
            }

            document.save(pdfFilePath);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteViaID(String meterId) {
        String deleteQuery = "DELETE FROM Invoices WHERE meter_number = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setString(1, meterId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Khw getTheNewestKhw(String meterId) throws SQLException {
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
        return new Khw(latestDayKwh,latestNightKwh);
    }

}
