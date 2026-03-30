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

    requires io.micronaut.micronaut_inject;
    requires jakarta.inject;
    requires io.micronaut.core;
    requires io.micronaut.context;

    requires google.adk;
    requires google.adk.contrib.langchain4j;
    requires langchain4j.core;
    requires io.reactivex.rxjava3;
    requires langchain4j.ollama;
    requires google.genai;
    requires java.net.http;
    requires java.logging;
    requires net.sourceforge.plantuml;

    opens org.geantlr to javafx.fxml;
    opens org.geantlr.views to javafx.fxml;
    exports org.geantlr;
    exports org.geantlr.views;
    exports org.geantlr.viewmodels;
    exports org.geantlr.services;
}
