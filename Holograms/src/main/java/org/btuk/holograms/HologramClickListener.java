package org.btuk.holograms;

import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class HologramClickListener implements Listener {

    private final HologramManager hologramManager;

    public HologramClickListener(HologramManager hologramManager) {
        this.hologramManager = hologramManager;
    }

    public void unregister() {
        eu.decentsoftware.holograms.event.HologramClickEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onHologramClick(HologramClickEvent event) {

        Player player = event.getPlayer();
        String hologramName = event.getHologram().getName();

        UUID hologramId;
        try {
            hologramId = UUID.fromString(hologramName);
        } catch (IllegalArgumentException e) {
            return;
        }

        var clickEvent = hologramManager.getHologramClickEvent(hologramId);

        if (clickEvent != null) {
            clickEvent.onClick(player);
        }
    }
}
