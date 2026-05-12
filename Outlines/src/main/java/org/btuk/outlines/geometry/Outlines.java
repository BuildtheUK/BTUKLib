package org.btuk.outlines.geometry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Outlines {

    private final Map<UUID, Map<UUID, Outline>> playerOutlines = new HashMap<>();

    public Collection<Outline> getPlayerOutlines(UUID player) {
        return getPlayerOutlinesMap(player).values();
    }

    public Outline getPlayerOutline(UUID player, UUID outline) {
        Map<UUID, Outline> playerOutlines = getPlayerOutlinesMap(player);
        return playerOutlines.get(outline);
    }

    public UUID addPlayerOutline(UUID player, Outline outline) {
        UUID outlineId = UUID.randomUUID();
        getPlayerOutlinesMap(player).put(outlineId, outline);
        return outlineId;
    }

    private Map<UUID, Outline> getPlayerOutlinesMap(UUID player) {
        return playerOutlines.computeIfAbsent(player, k -> new HashMap<>());
    }
}
