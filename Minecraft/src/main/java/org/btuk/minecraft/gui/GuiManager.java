package org.btuk.minecraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link Gui} manager, registers en unregisters Guis as wel as keeps track of players that are currently using a Gui.
 */
public final class GuiManager {

    private final Map<UUID, Gui> registeredGuis = new HashMap<>();

    private final Map<UUID, UUID> openGuis = new HashMap<>();

    private final Map<UUID, Deque<UUID>> guiHistory = new HashMap<>();

    /**
     * Adds the Gui to the list of registered Guis.
     * @param gui The Gui to add to the list of registered Guis.
     */
    public void registerGui(Gui gui) {
        if (gui != null) {
            registeredGuis.put(gui.getUuid(), gui);
        }
    }

    /**
     * Removes the Gui from the list of open Guis, and removes the Gui from the list of registered Guis.
     * @param gui Gui to unregister.
     */
    public void unregisterGui(Gui gui) {
        if (gui != null) {
            unregisterGuiByUuid(gui.getUuid());
        }
    }

    /**
     * Removes the Gui from the list of open Guis, and removes the Gui from the list of registered Guis.
     * @param uuid The uuid of the Gui to unregister.
     */
    public void unregisterGuiByUuid(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            openGuis.remove(player.getUniqueId(), uuid);
        }

        guiHistory.values().forEach(history -> history.removeIf(uuid::equals));
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

    public Gui getOpenGuiByPlayer(Player player) {
        return getOpenGuiByPlayerUuid(player.getUniqueId());
    }

    public Gui getOpenGuiByPlayerUuid(UUID playerUuid) {
        UUID guiUuid = getOpenGuiUuidByPlayerUuid(playerUuid);

        if (guiUuid == null) {
            return null;
        }

        return getGuiByUuid(guiUuid);
    }

    /**
     * Opens a Gui as a root Gui.
     * This clears the player's previous Gui history before opening the Gui.
     *
     * @param player The player whom to open the Gui for.
     * @param gui The Gui to open.
     */
    public void openRootGui(Player player, Gui gui) {
        clearHistory(player);
        gui.open(player);
    }

    /**
     * Opens a Gui and stores the currently open Gui as the previous Gui.
     *
     * @param player The player whom to open the Gui for.
     * @param gui The Gui to open.
     */
    public void navigateToGui(Player player, Gui gui) {
        UUID currentGuiUuid = getOpenGuiUuidByPlayer(player);

        if (currentGuiUuid != null && registeredGuis.containsKey(currentGuiUuid) && !currentGuiUuid.equals(gui.getUuid())) {
            guiHistory
                    .computeIfAbsent(player.getUniqueId(), ignored -> new ArrayDeque<>())
                    .push(currentGuiUuid);
        }

        gui.open(player);
    }

    /**
     * Opens the previous Gui for the player, if one exists in the player's history.
     *
     * @param player The player whom to return to the previous Gui.
     * @return true if a previous Gui was opened, false if no previous Gui exists.
     */
    private boolean goBack(Player player) {
        Deque<UUID> history = guiHistory.get(player.getUniqueId());

        if (history == null) {
            return false;
        }

        while (!history.isEmpty()) {
            UUID previousGuiUuid = history.pop();
            Gui previousGui = getGuiByUuid(previousGuiUuid);

            if (previousGui != null) {
                previousGui.open(player);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the player to the previous Gui if one exists.
     * If no previous Gui exists in the player's history, the current Gui's fallback return Gui is opened instead.
     *
     * @param player The player whom to return.
     * @param currentGui The Gui the player is returning from.
     * @return true if a Gui was opened, false if no previous or fallback Gui exists.
     */
    public boolean returnToPreviousGui(Player player, Gui currentGui) {
        if (goBack(player)) {
            return true;
        }

        if (currentGui == null || !currentGui.hasFallbackReturnGui()) {
            return false;
        }

        Gui fallbackReturnGui = currentGui.getFallbackReturnGui();
        openRootGui(player, fallbackReturnGui);
        return true;
    }

    public boolean hasPreviousGui(Player player) {
        Deque<UUID> history = guiHistory.get(player.getUniqueId());
        return history != null && !history.isEmpty();
    }

    private void clearHistory(Player player) {
        clearHistory(player.getUniqueId());
    }

    private void clearHistory(UUID playerUuid) {
        guiHistory.remove(playerUuid);
    }

    /**
     * Opens a Gui. Registers the Gui if it is not already registered, then opens the inventory, and adds the Gui
     * to the list of open Guis.
     * @param player The player whom to open the Gui for.
     * @param gui The Gui to open.
     */
    public void openGui(Player player, Gui gui) {
        if (!registeredGuis.containsKey(gui.getUuid()))
            registerGui(gui);
        openGui(player.getUniqueId(), gui.getUuid());
    }

    /**
     * Opens a Gui. Registers the Gui if it is not already registered, then opens the inventory, and adds the Gui
     * to the list of open Guis.
     * @param playerUuid The UUID of the player whom to open the Gui for.
     * @param guiUuid The UUID of the Gui to open.
     */
    private void openGui(UUID playerUuid, UUID guiUuid) {
        openGuis.put(playerUuid, guiUuid);
    }

    public void closeGui(Player player) {
        closeGui(player.getUniqueId());
    }

    /**
     * Removes the Player-Gui pair from the list of open Guis for the given player
     * @param playerUuid The player to remove the Open gui for
     */
    public void closeGui(UUID playerUuid) {
        openGuis.remove(playerUuid);
    }
}
