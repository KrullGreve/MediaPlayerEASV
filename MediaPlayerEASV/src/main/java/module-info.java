module org.example.mediaplayereasv {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.media;
    requires sqljdbc4;
    requires java.desktop;
    requires jdk.jfr;

    opens org.example.mediaplayereasv to javafx.fxml;
    exports org.example.mediaplayereasv;
}