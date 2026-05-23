package org.btuk.holograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log
public final class HologramManager {

    private final Map<UUID, Hologram> holograms = new HashMap<>();

    private final Map<UUID, HologramClickEvent> hologramClickEvents = new HashMap<>();

    private final HologramClickListener hologramClickListener;

    public HologramManager(JavaPlugin plugin) {
        Plugin decentHolograms = Bukkit.getPluginManager().getPlugin("DecentHolograms");

        if (decentHolograms == null || !decentHolograms.isEnabled()) {
            throw new IllegalStateException("DecentHolograms is not installed or enabled");
        }

        hologramClickListener = new HologramClickListener(this);
        Bukkit.getPluginManager().registerEvents(hologramClickListener, plugin);
        log.info("Enabled hologram manager");
    }

    public void disable() {
        hologramClickListener.unregister();
        for (Hologram hologram : holograms.values()) {
            hologram.delete();
        }
        hologramClickEvents.clear();
        holograms.clear();
    }

    public UUID createHologram(Location location, Player player) {
        UUID uuid = createHologram(location);
        Hologram hologram = holograms.get(uuid);
        hologram.setShowPlayer(player);
        log.info("Created hologram");
        return uuid;
    }

    public void hideHologram(UUID uuid) {
        Hologram hologram = holograms.get(uuid);
        if (hologram == null) {
            throw new IllegalStateException("Hologram with ID " + uuid + " does not exist");
        }
        hologram.hideAll();
    }

    public void addHologramClickEvent(UUID uuid, HologramClickEvent event) {
        hologramClickEvents.put(uuid, event);
    }

    public void removeHologramClickEvent(UUID uuid) {
        hologramClickEvents.remove(uuid);
    }

    public HologramClickEvent getHologramClickEvent(UUID uuid) {
        return hologramClickEvents.get(uuid);
    }

    public void removeHologram(UUID uuid) {
        Hologram hologram = holograms.remove(uuid);
        if (hologram != null) {
            hologram.delete();
        }
        removeHologramClickEvent(uuid);
    }

    private UUID createHologram(Location location) {
        UUID hologramId = UUID.randomUUID();
        if (DHAPI.getHologram(hologramId.toString()) != null) {
            throw new IllegalStateException("Hologram with ID " + hologramId + " already exists");
        }

        Hologram hologram = DHAPI.createHologram(hologramId.toString(), location, Collections.singletonList("&b&lClick to move corner"));
        hologram.setDefaultVisibleState(false);
        holograms.put(hologramId, hologram);
        return hologramId;
    }
}
