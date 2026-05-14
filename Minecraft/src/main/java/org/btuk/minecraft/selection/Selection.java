package org.btuk.minecraft.selection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.btuk.minecraft.misc.ItemUtils;
import org.btuk.outlines.Outlines;
import org.btuk.outlines.geometry.Outline;
import org.btuk.outlines.geometry.IntPoint2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Selection implements Listener {

    private final ItemStack selectionTool;
    protected final Outlines outlines;

    protected final Map<UUID, List<IntPoint2d>> activeSelections = new HashMap<>();
    protected final Map<UUID, UUID> playerOutlineIds = new HashMap<>();

    public Selection(JavaPlugin plugin, ItemStack selectionTool, Outlines outlines) {
        this.selectionTool = selectionTool;
        this.outlines = outlines;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void giveSelectionTool(Player player) {
        ItemUtils.setItemInSelectedSlot(player, selectionTool);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Action action = event.getAction();

        if (event.getItem() == null || !event.getItem().equals(selectionTool)) {
            return;
        }

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            resetSelection(playerId);
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            addPoint(player, playerId, event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        resetSelection(event.getPlayer().getUniqueId());
    }

    protected void addPoint(Player player, UUID playerId, PlayerInteractEvent event) {
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        activeSelections.computeIfAbsent(playerId, k -> new ArrayList<>()).add(new IntPoint2d(x, z));

        List<IntPoint2d> points = activeSelections.get(playerId);
        Outline newOutline = new Outline(Collections.unmodifiableList(points));

        UUID oldOutlineId = playerOutlineIds.get(playerId);
        if (oldOutlineId != null) {
            outlines.removePlayerOutline(playerId, oldOutlineId);
        }

        UUID newOutlineId = outlines.addPlayerOutline(playerId, newOutline);
        playerOutlineIds.put(playerId, newOutlineId);
    }

    protected void resetSelection(UUID playerId) {
        activeSelections.remove(playerId);
        UUID outlineId = playerOutlineIds.remove(playerId);

        if (outlineId != null) {
            outlines.removePlayerOutline(playerId, outlineId);
        }
    }
}