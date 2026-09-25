package com.zenxy.hologram.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramView {

    private final UUID playerUuid;
    private final String hologramId;
    // Map line index -> entity ID allocated/spawned for this player
    private final Map<Integer, Integer> lineEntityIds = new ConcurrentHashMap<>();
    // Map line index -> last formatted text sent to player (to prevent duplicate packet updates)
    private final Map<Integer, String> lastSentTexts = new ConcurrentHashMap<>();
    private int interactionEntityId = -1;

    public HologramView(UUID playerUuid, String hologramId) {
        this.playerUuid = playerUuid;
        this.hologramId = hologramId;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getHologramId() {
        return hologramId;
    }

    public Map<Integer, Integer> getLineEntityIds() {
        return lineEntityIds;
    }

    public void setLineEntityId(int lineIndex, int entityId) {
        lineEntityIds.put(lineIndex, entityId);
    }

    public Integer getLineEntityId(int lineIndex) {
        return lineEntityIds.get(lineIndex);
    }

    public void removeLineEntityId(int lineIndex) {
        lineEntityIds.remove(lineIndex);
        lastSentTexts.remove(lineIndex);
    }

    public String getLastSentText(int lineIndex) {
        return lastSentTexts.get(lineIndex);
    }

    public void setLastSentText(int lineIndex, String text) {
        lastSentTexts.put(lineIndex, text);
    }

    public int getInteractionEntityId() {
        return interactionEntityId;
    }

    public void setInteractionEntityId(int interactionEntityId) {
        this.interactionEntityId = interactionEntityId;
    }

    public Collection<Integer> getAllEntityIds() {
        List<Integer> ids = new ArrayList<>(lineEntityIds.values());
        if (interactionEntityId != -1) {
            ids.add(interactionEntityId);
        }
        return ids;
    }
}
