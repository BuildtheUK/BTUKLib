package org.btuk.minecraft.gui;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;

public class GuiOpenContext {

    @Getter
    private final Player player;

    private final Map<String, Object> parameters;

    public GuiOpenContext(Player player, Map<String, Object> parameters) {
        this.player = player;
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