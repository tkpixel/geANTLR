module org.geantlr {
    requires javafx.controls;
    requires javafx.fxml;
    requires jfx.incubator.richtext;

    requires io.micronaut.inject;
    requires io.micronaut.context;
    requires io.micronaut.core;

    opens org.geantlr to javafx.fxml;
    exports org.geantlr;
}
