package org.btuk.minecraft.gui;

import java.util.Map;

public class GuiOpenContext {

    private final Map<String, Object> parameters;

    public GuiOpenContext(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);

        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Parameter '" + key + "' has invalid type.");
        }

        return type.cast(value);
    }
}