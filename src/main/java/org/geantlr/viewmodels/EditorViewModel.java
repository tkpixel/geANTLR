package org.geantlr.viewmodels;

import io.micronaut.context.annotation.Prototype;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Prototype
public class EditorViewModel {

    private final StringProperty textContent = new SimpleStringProperty("");

    public StringProperty textContentProperty() {
        return textContent;
    }

    public String getTextContent() {
        return textContent.get();
    }

    public void setTextContent(String text) {
        this.textContent.set(text);
    }
}
