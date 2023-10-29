module edu.asu.effortlogger {
    requires javafx.controls;
    requires spring.security.crypto;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    exports edu.asu.effortlogger;
    exports edu.asu.effortlogger.model;
}