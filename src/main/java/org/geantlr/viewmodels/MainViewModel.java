package org.geantlr.viewmodels;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jakarta.inject.Singleton;
import org.geantlr.services.DynamicGrammar;

@Singleton
public class MainViewModel {

    // Property to keep track of the active editors
    private final ObservableList<EditorViewModel> activeEditors = FXCollections.observableArrayList();

    // Property to hold the currently loaded dynamic grammar
    private final ObjectProperty<DynamicGrammar> dynamicGrammar = new SimpleObjectProperty<>(null);

    public ObservableList<EditorViewModel> getActiveEditors() {
        return activeEditors;
    }

    public void addEditor(EditorViewModel editor) {
        if (activeEditors.size() < 2) {
            activeEditors.add(editor);
        }
    }

    public void removeEditor(EditorViewModel editor) {
        activeEditors.remove(editor);
    }

    public DynamicGrammar getDynamicGrammar() {
        return dynamicGrammar.get();
    }

    public void setDynamicGrammar(DynamicGrammar grammar) {
        this.dynamicGrammar.set(grammar);
    }

    public ObjectProperty<DynamicGrammar> dynamicGrammarProperty() {
        return dynamicGrammar;
    }
}
