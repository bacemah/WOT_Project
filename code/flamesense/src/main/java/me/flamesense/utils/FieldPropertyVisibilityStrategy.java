package me.flamesense.utils;

import jakarta.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldPropertyVisibilityStrategy implements PropertyVisibilityStrategy {
    public FieldPropertyVisibilityStrategy() {
    }

    public boolean isVisible(Field field)
    {
        return true;
    }

    public boolean isVisible(Method method) {

        return true;
    }
}
