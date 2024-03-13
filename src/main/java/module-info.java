module com.example.electricitybilling {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;
    requires java.desktop;


    opens com.example.electricitybilling to javafx.fxml;
    exports com.example.electricitybilling;
    exports com.example.electricitybilling.Controllers;
    opens com.example.electricitybilling.Controllers to javafx.fxml;
}