module org.group45.choreday {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.group45.choreday to javafx.fxml;
    exports org.group45.choreday;
}