package net.bteuk.minecraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link Gui} manager, registers en unregisters Guis as wel as keeps track of players that are currently using a Gui.
 */
public final class GuiManager {

    private final Map<UUID, Gui> registeredGuis = new HashMap<>();

    private final Map<UUID, UUID> openGuis = new HashMap<>();

    private GuiManager() {
        // Private constructor so this class can not be instantiated.
    }

    public void registerGui(Gui gui) {
        if (gui != null) {
            registeredGuis.put(gui.getUuid(), gui);
        }
    }

    public void unregisterGui(Gui gui) {
        if (gui != null) {
            unregisterGuiByUuid(gui.getUuid());
        }
    }

    public void unregisterGuiByUuid(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            openGuis.remove(player.getUniqueId(), uuid);
        }
        registeredGuis.remove(uuid);
    }

    public Gui getGuiByUuid(UUID uuid) {
        return registeredGuis.get(uuid);
    }

    public UUID getOpenGuiUuidByPlayer(Player player) {
        return getOpenGuiUuidByPlayerUuid(player.getUniqueId());
    }

    public UUID getOpenGuiUuidByPlayerUuid(UUID uuid) {
        return openGuis.get(uuid);
    }

    public void openGui(Player player, Gui gui) {
        openGui(player.getUniqueId(), gui.getUuid());
    }

    public void openGui(UUID playerUuid, UUID guiUuid) {
        openGuis.put(playerUuid, guiUuid);
    }

    public void closeGui(Player player) {
        closeGui(player.getUniqueId());
    }

    public void closeGui(UUID playerUuid) {
        openGuis.remove(playerUuid);
    }
}
