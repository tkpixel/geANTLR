package org.geantlr.viewmodels;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.geantlr.services.DynamicGrammar;

public class MainViewModel {

    // Property to keep track of the number of active editors (1 or 2)
    private final IntegerProperty activeEditorsCount = new SimpleIntegerProperty(1);

    // Property to hold the currently loaded dynamic grammar
    private final ObjectProperty<DynamicGrammar> dynamicGrammar = new SimpleObjectProperty<>(null);

    public int getActiveEditorsCount() {
        return activeEditorsCount.get();
    }

    public void setActiveEditorsCount(int count) {
        if (count < 1) count = 1;
        if (count > 2) count = 2;
        this.activeEditorsCount.set(count);
    }

    public IntegerProperty activeEditorsCountProperty() {
        return activeEditorsCount;
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
