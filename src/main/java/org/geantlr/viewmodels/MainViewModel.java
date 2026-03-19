package org.geantlr.viewmodels;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MainViewModel {

    // Property to keep track of the number of active editors (1 or 2)
    private final IntegerProperty activeEditorsCount = new SimpleIntegerProperty(1);

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
}
