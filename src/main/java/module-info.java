module sed.sample.sqlitepersontableviewexampleusingtasks {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    opens sed.sample.sqlitepersontableviewexampleusingtasks to javafx.fxml;
    exports sed.sample.sqlitepersontableviewexampleusingtasks;
    requires java.sql;
}
