package com.thepaperraven.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, PlayerData::new);
    }

    public static void removePlayerData(UUID playerUUID) {
        playerDataMap.remove(playerUUID);
    }
}
