module org.geantlr {
    requires javafx.controls;
    requires javafx.fxml;
    requires jfx.incubator.richtext;

    opens org.geantlr to javafx.fxml;
    exports org.geantlr;
}
