module org.group45.choreday {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires org.apache.commons.dbcp2;

    opens org.group45.choreday to javafx.fxml;
    opens org.group45.choreday.screen to javafx.fxml;

    exports org.group45.choreday;
    exports org.group45.choreday.screen;
    exports org.group45.choreday.utils;
}