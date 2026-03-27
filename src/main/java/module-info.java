module org.group45.choreday {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires org.apache.commons.dbcp2;
    requires lombok;
    requires jakarta.persistence;
    requires org.json;

    opens org.group45.choreday to javafx.fxml;
    opens org.group45.choreday.controllers to javafx.fxml;

    exports org.group45.choreday;
    exports org.group45.choreday.controllers;
    exports org.group45.choreday.utils;
    exports org.group45.choreday.services;
    exports org.group45.choreday.models;
}