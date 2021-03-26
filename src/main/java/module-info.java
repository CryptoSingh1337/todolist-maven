module org.lunatics {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.datatransfer;
    requires java.desktop;

    opens org.lunatics to javafx.fxml;
    opens org.lunatics.additem to javafx.fxml;
    opens org.lunatics.edititem to javafx.fxml;
    exports org.lunatics;
    exports org.lunatics.additem;
    exports org.lunatics.edititem;
}