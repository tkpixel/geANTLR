package org.geantlr;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.util.Callback;

@Singleton
public class FxmlControllerFactory implements Callback<Class<?>, Object> {

    private final ApplicationContext context;

    @Inject
    public FxmlControllerFactory(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> clazz) {
        return context.getBean(clazz);
    }
}
