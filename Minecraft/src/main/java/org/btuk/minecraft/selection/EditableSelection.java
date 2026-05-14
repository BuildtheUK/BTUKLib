package org.btuk.minecraft.selection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.btuk.holograms.HologramManager;
import org.btuk.outlines.Outlines;
import org.btuk.outlines.geometry.IntPoint2d;
import org.btuk.outlines.geometry.Outline;

public class EditableSelection extends Selection {

    private final Map<UUID, List<UUID>> playerHolograms = new HashMap<>();

    private final HologramManager hologramManager;

    public EditableSelection(JavaPlugin plugin, ItemStack selectionTool, Outlines outlines, HologramManager hologramManager) {
        super(plugin, selectionTool, outlines);
        this.hologramManager = hologramManager;
    }

    public void toggleEditable(Player player, boolean editable) {
        if (editable) {
            playerHolograms.putIfAbsent(player.getUniqueId(), new ArrayList<>());
        } else {
            List<UUID> holograms = playerHolograms.remove(player.getUniqueId());
            removeHolograms(holograms);
        }
    }

    @Override
    protected void addPoint(Player player, UUID playerId, PlayerInteractEvent event) {
        super.addPoint(player, playerId, event);
        List<UUID> holograms = playerHolograms.get(playerId);
        if (holograms == null) {
            return;
        }
        for (UUID hologramId : holograms) {
            hologramManager.removeHologram(hologramId);
        }
        holograms.clear();
        List<IntPoint2d> points = activeSelections.get(playerId);
        if (points == null) {
            return;
        }
        Location playerLocation = player.getLocation();
        for (IntPoint2d point : points) {
            World world = playerLocation.getWorld();
            Location location = new Location(player.getWorld(), point.x(), 1 + world.getHighestBlockYAt(point.x(), point.z()), point.z());
            holograms.add(hologramManager.createHologram(location, player));
        }
    }

    @Override
    protected void resetSelection(UUID playerId) {
        super.resetSelection(playerId);
        List<UUID> holograms = playerHolograms.get(playerId);
        if (holograms == null) {
            return;
        }
        removeHolograms(holograms);
        activeSelections.remove(playerId);
    }

    private void removeHolograms(List<UUID> holograms) {
        if (holograms == null) {
            return;
        }
        for (UUID hologramId : holograms) {
            hologramManager.removeHologram(hologramId);
        }
        holograms.clear();
    }

    private void movePoint(UUID playerId, int index, Location newLocation) {
        int x = newLocation.getBlockX();
        int z = newLocation.getBlockZ();

        List<IntPoint2d> selection = activeSelections.get(playerId);
        if (selection == null || selection.size() <= index) {
            return;
        }

        selection.set(index, new IntPoint2d(x, z));
        Outline newOutline = new Outline(Collections.unmodifiableList(selection));

        UUID oldOutlineId = playerOutlineIds.get(playerId);
        if (oldOutlineId != null) {
            outlines.removePlayerOutline(playerId, oldOutlineId);
        }

        UUID newOutlineId = outlines.addPlayerOutline(playerId, newOutline);
        playerOutlineIds.put(playerId, newOutlineId);
    }
}
