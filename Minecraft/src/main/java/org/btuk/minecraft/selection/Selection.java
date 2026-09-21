package org.btuk.minecraft.selection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.btuk.minecraft.misc.ComponentUtils;
import org.btuk.minecraft.misc.ItemUtils;
import org.btuk.outlines.Outlines;
import org.btuk.outlines.geometry.Outline;
import org.btuk.outlines.geometry.IntPoint2d;

import java.util.function.BiPredicate;
import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Selection implements Listener {

    protected final JavaPlugin plugin;
    protected final ItemStack selectionTool;
    protected final Outlines outlines;

    protected final Map<UUID, List<IntPoint2d>> activeSelections = new HashMap<>();
    protected final Map<UUID, UUID> playerOutlineIds = new HashMap<>();

    protected boolean resetOnWorldChange = true;

    protected BiPredicate<Player, IntPoint2d> pointValidator = (p, pt) -> true;

    protected BiConsumer<Player, List<IntPoint2d>> selectionUpdateHook = (p, l) -> {};

    public Selection(JavaPlugin plugin, ItemStack selectionTool, Outlines outlines) {
        this.plugin = plugin;
        this.selectionTool = selectionTool;
        this.outlines = outlines;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void giveSelectionTool(Player player) {
        ItemUtils.setItemInSelectedSlot(player, selectionTool);
    }

    public List<IntPoint2d> getPlayerSelection(UUID playerId) {
        return activeSelections.get(playerId);
    }

    public void setPointValidator(BiPredicate<Player, IntPoint2d> pointValidator) {
        this.pointValidator = pointValidator;
    }

    public void setSelectionUpdateHook(BiConsumer<Player, List<IntPoint2d>> selectionUpdateHook) {
        this.selectionUpdateHook = selectionUpdateHook;
    }

    public void setResetOnWorldChange(boolean resetOnWorldChange) {
        this.resetOnWorldChange = resetOnWorldChange;
    }

    public void startSelection(Player player, List<IntPoint2d> points) {
        UUID playerId = player.getUniqueId();
        resetSelection(playerId);
        List<IntPoint2d> selection = activeSelections.computeIfAbsent(playerId, k -> new ArrayList<>());
        for (IntPoint2d point : points) {
            if (pointValidator.test(player, point)) {
                selection.add(point);
            }
        }
        replaceOutline(playerId, selection);
        player.sendMessage(ComponentUtils.success("Started selection with " + selection.size() + " points."));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Action action = event.getAction();

        if (event.getItem() == null || !event.getItem().equals(selectionTool)) {
            return;
        }

        event.setCancelled(true);

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            resetSelection(playerId);
            addPoint(player, playerId, event);
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

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (resetOnWorldChange) {
            resetSelection(event.getPlayer().getUniqueId());
        }
    }

    protected void addPoint(Player player, UUID playerId, PlayerInteractEvent event) {
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        IntPoint2d point = new IntPoint2d(x, z);
        if (!pointValidator.test(player, point)) {
            return;
        }

        List<IntPoint2d> points = activeSelections.computeIfAbsent(playerId, k -> new ArrayList<>());

        points.add(point);
        replaceOutline(playerId, points);
        selectionUpdateHook.accept(player, points);
        if (points.size() == 1) {
            player.sendMessage(ComponentUtils.success("Started new selection."));
        } else {
            player.sendMessage(ComponentUtils.success("Added point to selection."));
        }
    }

    protected void resetSelection(UUID playerId) {
        activeSelections.remove(playerId);
        UUID outlineId = playerOutlineIds.remove(playerId);

        if (outlineId != null) {
            outlines.removePlayerOutline(playerId, outlineId);
        }
    }

    protected void replaceOutline(UUID playerId, List<IntPoint2d> points) {
        Outline newOutline = new Outline(Collections.unmodifiableList(points));

        UUID oldOutlineId = playerOutlineIds.get(playerId);
        if (oldOutlineId != null) {
            outlines.removePlayerOutline(playerId, oldOutlineId);
        }

        UUID newOutlineId = outlines.addPlayerOutline(playerId, newOutline);
        playerOutlineIds.put(playerId, newOutlineId);
    }
}