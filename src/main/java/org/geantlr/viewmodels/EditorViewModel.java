package org.geantlr.viewmodels;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
