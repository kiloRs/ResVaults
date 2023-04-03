package com.thepaperraven;


import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.PDC;
import com.thepaperraven.ai.vault.Vault;
import com.thepaperraven.config.PlayerConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.thepaperraven.ResourceVaults.getPlayerData;

public class VaultManager {

    public static boolean registerVault(Player player, Vault vault) {
        UUID playerUUID = player.getUniqueId();
        PlayerData playerData = getPlayerData(playerUUID);

        if (playerData.getVaults().containsKey(vault.getVaultIndex())){
            ResourceVaults.log("Vault already exists!");
            return false;
        }



        savePlayerVaults(player);

        return true;
    }

    public static void unregisterVault(Player player, Vault vault) {
        UUID playerUUID = player.getUniqueId();
        PlayerData playerData = getPlayerData(playerUUID);

        playerData.removeVault(vault.getVaultIndex());
        savePlayerVaults(player);
    }


    public static void savePlayerVaults(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = getPlayerData(uuid);
        File playerFile = playerData.getConfig().getFile();
        YamlConfiguration playerConfig = PlayerConfiguration.loadConfiguration(playerFile);
        int vaultIndex = 1;
        for (Vault vault : playerData.getVaults().values()) {
            String vaultPath = "vaults." + vaultIndex;
            playerConfig.set(vaultPath + ".material", vault.getMaterial().name());
            playerConfig.set(vaultPath + ".location.left", vault.getContainer().getLeft().getLocation().toBlockLocation().serialize());
            if (vault.getContainer().hasSecondChest()){
                playerConfig.set(vaultPath + ".location.right",vault.getContainer().getRight().getLocation().toBlockLocation().serialize());
            }
            vaultIndex++;
        }
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean hasResourcesSign(@NotNull Block block) {
        BlockFace frontFace = getBlockFaceAttachedTo(block);
        if (frontFace == null) {
            return false;
        }
        Block signBlock = block.getRelative(frontFace);
        if (signBlock.getState() instanceof Sign sign) {
            return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Resources]");
        }
        return false;
    }
    private static BlockFace getBlockFaceAttachedTo(@NotNull Block block) {
        if (block.getBlockData() instanceof Directional directional) {
            return directional.getFacing();
        }
        return null;
    }

    public static void loadPlayerVaults(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = PlayerData.get(uuid).getConfig().getFile();
        if (!playerFile.exists()) {
            return;
        }
        YamlConfiguration playerConfig = PlayerConfiguration.loadConfiguration(playerFile);
        if (!playerConfig.contains("vaults")) {
            return;
        }
        PlayerData playerData = getPlayerData(uuid);
        ConfigurationSection vaultsSection = playerConfig.getConfigurationSection("vaults");

        if (vaultsSection == null){
            ResourceVaults.error("NO VAULTS SAVED IN FILE FOR " + player.getName() + "!");
            return;
        }
        for (String vaultId : vaultsSection.getKeys(false)) {
            ConfigurationSection vaultSection = vaultsSection.getConfigurationSection(vaultId);
            Material material = Material.matchMaterial(vaultSection.getString("material",ResourceVaults.getConfiguration().getDefaultVaultMaterial().name()));
            if (material == null) {
                continue;
            }
            Location location = vaultSection.getLocation("location.left",null);
            if (location == null){
                continue;
            }
            if (!(location.getBlock().getState() instanceof Container container)){
                continue;
            }
            Location secondLocation = null;
            if (vaultSection.isLocation("location.right")){
                secondLocation = vaultSection.getLocation("location.right");
            }
            Vault vault;
            if (secondLocation != null){
            vault = new Vault(uuid,Integer.parseInt(vaultId),material, PDC.getPDCOfDoubleChest(location,secondLocation));
            }
            else {
                vault = new Vault(uuid,Integer.parseInt(vaultId),material,PDC.get(container));
            }
            playerData.getVaults().put(Integer.parseInt(vaultId), vault);
        }
    }

}
