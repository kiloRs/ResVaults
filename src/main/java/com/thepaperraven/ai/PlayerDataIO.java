package com.thepaperraven.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeff_media.morepersistentdatatypes.DataType;
import io.lumine.mythic.lib.gson.JsonElement;
import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.gson.JsonParser;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataIO {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private final PlayerData playerData;

    public PlayerDataIO(PlayerData playerData) {
        this.playerData = playerData;
    }

    public void save() {
        File playerDataFile = getPlayerDataFile();
        try (FileWriter writer = new FileWriter(playerDataFile)) {
            gson.toJson(playerData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerData load() {
        File playerDataFile = getPlayerDataFile();
        if (playerDataFile.exists()) {
            try (FileReader reader = new FileReader(playerDataFile)) {
                return gson.fromJson(reader, PlayerData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return playerData;
    }
    public static PlayerData loadOnLogin(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".json");

        if (playerFile.exists()) {
            try {
                JsonObject playerJson = JsonParser.parseReader(new FileReader(playerFile)).getAsJsonObject();
                Map<Integer, Vault> vaults = new HashMap<>();

                for (Map.Entry<String, JsonElement> entry : playerJson.entrySet()) {
                    if (entry.getKey().equals("uuid")) {
                        continue; // skip the uuid key
                    }

                    int vaultIndex = Integer.parseInt(entry.getKey());

                    JsonObject vaultJson = entry.getValue().getAsJsonObject();

                    // Load the Vault data from the JSON object
                    Material type = Material.valueOf(vaultJson.get("type").getAsString());
                    Location signLocation = deserializeLocation(vaultJson.get("signLocation").getAsString());
                    Location chestLocation = deserializeLocation(vaultJson.get("chestLocation").getAsString());

                    Location doubleChestLocation = null;
                    if (vaultJson.has("doubleChestLocation")) {
                        doubleChestLocation = deserializeLocation(vaultJson.get("doubleChestLocation").getAsString());
                    }

                    boolean locked = vaultJson.get("locked").getAsBoolean();
                    int count = vaultJson.get("count").getAsInt();

                    // Load the Vault's PDC
                    Block chestBlock = chestLocation.getBlock();
                    if (chestBlock.getState() instanceof Chest) {
                        Chest chest = (Chest) chestBlock.getState();
                        PersistentDataContainer pdc = chest.getPersistentDataContainer();

                        if (pdc.has(VaultKeys.getIndexKey(), PersistentDataType.INTEGER)) {
                            int index = pdc.get(VaultKeys.getIndexKey(), PersistentDataType.INTEGER);

                            if (index == vaultIndex) {
                                // The chest has the correct index, load the Vault data
                                UUID ownerUUID = uuid;
                                if (pdc.has(VaultKeys.getOwnerKey(), DataType.UUID)) {
                                    ownerUUID = pdc.getOrDefault(VaultKeys.getOwnerKey(), DataType.UUID,UUID.fromString(playerFile.getName()));
                                }

                                boolean isLocked = false;
                                if (pdc.has(VaultKeys.getLocked(), DataType.BOOLEAN)) {
                                    isLocked = pdc.getOrDefault(VaultKeys.getLocked(), DataType.BOOLEAN,false);
                                }

                                Vault vault = new Vault(vaultIndex, ownerUUID, chestLocation, signLocation, doubleChestLocation, isLocked, type);
                                vault.setCount(count);
                                vaults.put(vaultIndex, vault);
                            } else {
                                // The chest has the wrong index, mark the Vault inactive
                                Vault vault = new Vault(vaultIndex, uuid, chestLocation, signLocation, doubleChestLocation, locked, type);
                                vault.setActive(false);
                                vaults.put(vaultIndex, vault);
                            }
                        } else {
                            // The chest doesn't have the index key, mark the Vault inactive
                            Vault vault = new Vault(vaultIndex, uuid, chestLocation, signLocation, doubleChestLocation, locked, type);
                            vault.setActive(false);
                            vaults.put(vaultIndex, vault);
                        }
                    } else {
                        // The chest location doesn't point to a chest block, mark the Vault inactive
                        Vault vault = new Vault(vaultIndex, uuid, chestLocation, signLocation, doubleChestLocation, locked, type);
                        vault.setActive(false);
                        vaults.put(vaultIndex, vault);
                    }
                }

                return new PlayerData(uuid, vaults);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new PlayerData(uuid);
    }
    private File getPlayerDataFile() {
        String fileName = playerUUID.toString() + ".json";
        File dataFolder = new File("plugins/YourPluginName/PlayerData");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return new File(dataFolder, fileName);
    }

}