module org.geantlr {
    requires javafx.controls;
    requires javafx.fxml;
    requires jfx.incubator.richtext;

    requires atlantafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.antlr.antlr4.runtime;
    requires antlr4;
    requires antlr4.c3;

    requires io.micronaut.inject;
    requires jakarta.inject;
    requires io.micronaut.core;
    requires io.micronaut.context;

    opens org.geantlr to javafx.fxml;
    opens org.geantlr.views to javafx.fxml;
    exports org.geantlr;
    exports org.geantlr.views;
    exports org.geantlr.viewmodels;
    exports org.geantlr.services;
}
