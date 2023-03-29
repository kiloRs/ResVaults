package com.thepaperraven.ai;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.config.PlayerConfiguration;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class PlayerData {
    private final Map<Integer, VaultInstance> vaults;
    private final Map<Integer, VaultInventory> inventories = new HashMap<>();
    @Getter
    private final UUID uuid;
    private final boolean loadOnCreate;
    @Getter
    private PlayerConfiguration config;
    @Getter
    private final Player player;
    private final VaultAmounts vaultAmounts;

    // Other fields and methods for the PlayerData class
    public PlayerData(UUID uuid) {
        this(uuid, true);
    }

    public PlayerData(@NotNull UUID uuid, boolean loadOnCreate) {
        this.uuid = uuid;
        this.loadOnCreate = loadOnCreate;
        this.vaults = new HashMap<>();
        this.config = new PlayerConfiguration(uuid);
        this.player = Bukkit.getPlayer(uuid);
        this.vaultAmounts = new VaultAmounts(this.getPlayer());

    }

    public static PlayerData get(UUID uniqueId) {
        return new PlayerData(uniqueId);
    }

    public void addVault(VaultInstance vaultInstance) {
        int index = getNextIndex();
        addVault(index, vaultInstance);
    }

    public void addVault(int index, VaultInstance vaultInstance) {
        if (index == 0){
            index++;
        }
        vaults.put(index, vaultInstance);
        inventories.put(index,vaultInstance.getVaultInventory());
    }

    public VaultInstance getVault(int index) {
        if (index == 0){
            return vaults.get(1);
        }
        return vaults.get(index);
    }

    public boolean hasVault(int index) {
        return vaults.containsKey(index);
    }

    public int getNextIndex() {
        int index = 1;
        while (vaults.containsKey(index)) {
            index++;
        }
        return index;
    }

    public int getIndexOf(VaultInstance instance) {
        if (!vaults.containsValue(instance)) {
            return 0;
        }
        int returnValue = 0;
        for (Map.Entry<Integer, VaultInstance> entry : vaults.entrySet()) {
            Integer integer = entry.getKey();
            VaultInstance vaultInstance = entry.getValue();
            if (vaultInstance.equals(instance)) {
                returnValue = integer;
                continue;
            }
        }
        if (returnValue > 0) {
            return returnValue;
        }

        ResourceVaults.error("No Index found for " + instance.getMetadata().getOwnerUUID());

        return returnValue;
    }

    public Map<Integer,VaultInstance> getVaults() {
        return vaults;
    }

    public List<Vault> getVaultsByMaterial(Material material) {
        List<Vault> matching = new ArrayList<>();
        for (Map.Entry<Integer, VaultInstance> entry : vaults.entrySet()) {
            Integer integer = entry.getKey();
            VaultInstance vaultInstance = entry.getValue();
            if (vaultInstance.getMetadata().getAllowedMaterial()==material){
                matching.set(integer, ((Vault) vaultInstance));
            }
        }
        return matching;
    }


    /**
     * @param index The VaultMetadata index (note: always starts at 1, instead of 0 which is natural, so please enter the Vaults internal index.
     */
    public void removeVault(int index) {
        if (index>0) {
            index = index - 1;
        }
        if (vaults.containsKey(index)) {
            VaultInstance vault = vaults.remove(index);
            ResourceVaults.log("Removing Vault: " + vault.getMetadata().getVaultIndex() + " from " + Bukkit.getPlayer(vault.getMetadata().getOwnerUUID()).getName());
        }
    }

    public void saveVault(Vault vault) {
        int index = vault.getMetadata().getVaultIndex();
        config = new PlayerConfiguration(uuid);

        // Save vault owner
        config.set("vaults." + index + ".owner", vault.getMetadata().getOwnerUUID().toString());

        // Save vault material
        config.set("vaults." + index + ".material", vault.getMetadata().getAllowedMaterial().getKey().getKey());

        // Save chest locations
        List<String> chestLocStrings = new ArrayList<>();
        for (Location loc : vault.getChestLocations()) {
            chestLocStrings.add(locationToString(loc));
        }
        config.set("vaults." + index + ".chestLocations", chestLocStrings);

        // Save sign location
        config.set("vaults." + index + ".signLocation", locationToString(vault.getSignLocation()));

        // Save locked state
        config.set("vaults." + index + ".lockedState", vault.isLocked());

        try {
            config.save(config.getFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not save to file: " + config.getFile().getName() + "!");
        }
    }

    public Vault loadVault(int index) {
        FileConfiguration config = getConfig();
        if (!config.contains("vaults." + index)) {
            return null;
        }

        // Load vault owner
        UUID owner = UUID.fromString(config.getString("vaults." + index + ".owner",""));
        if (!owner.equals(getUuid()) && getUuid() != null) {
            return null;
        }

        // Load vault material
        Material material = Material.getMaterial(config.getString("vaults." + index + ".material","WHEAT"));
        if (material == null) {
            return null;
        }

        // Load chest locations
        List<String> chestLocStrings = config.getStringList("vaults." + index + ".chestLocations");
        List<Location> chestLocs = new ArrayList<>();
        for (String locString : chestLocStrings) {
            Location loc = stringToLocation(locString);
            if (loc == null) {
                return null;
            }
            chestLocs.add(loc);
        }

        // Load sign location
        Location signLoc = stringToLocation(config.getString("vaults." + index + ".signLocation",""));
        if (signLoc == null) {
            return null;
        }

        // Load locked state
        boolean lockedState = config.getBoolean("vaults." + index + ".lockedState",true);

        VaultMetadata metadata = new VaultMetadata(material, uuid, index);
        Vault vault = new Vault(metadata, chestLocs, signLoc);
        //Locks or Unlocks the Vault
        vault.setLocked(lockedState);
        return vault;
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLocation(String locString) {
        String[] split = locString.split(",");
        if (split.length != 4) {
            return null;
        }
        World world = Bukkit.getWorld(split[0]);
        if (world == null) {
            return null;
        }
        int x = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        int z = Integer.parseInt(split[3]);
        return new Location(world, x, y, z);
    }
    public boolean isVaultSaved(int index) {
        if (vaults.containsKey(index)) {
            Vault vault = (Vault) vaults.get(index);
            if (config.contains("vaults." + index)) {
                ConfigurationSection section = config.getConfigurationSection("vaults." + index);
                if (section != null) {
                    Vault savedVault = loadVault(index);
                    return savedVault != null && savedVault.equals(vault);
                }
            }
        }
        return false;
    }

    public File getFile(){
        return new PlayerConfiguration(this.uuid).getFile();
    }

}
