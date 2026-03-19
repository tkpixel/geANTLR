module org.geantlr {
    requires javafx.controls;
    requires javafx.fxml;
    requires jfx.incubator.richtext;

    requires atlantafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    opens org.geantlr to javafx.fxml;
    exports org.geantlr;
}
