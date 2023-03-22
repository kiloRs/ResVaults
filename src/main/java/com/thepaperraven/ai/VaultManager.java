package com.thepaperraven.ai;

import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.thepaperraven.ai.VaultKeys.*;

public class VaultManager {
    private final ResourceVaults plugin;
    private static final Map<UUID, List<Vault>> playerVaults = new HashMap<>();

    public VaultManager(ResourceVaults plugin) {
        this.plugin = plugin;

    }

    public static Vault getVaultById(int index, Player player) {
        List<Vault> orDefault = playerVaults.getOrDefault(player.getUniqueId(), new ArrayList<>());

        if (orDefault.isEmpty()){
            return null;
        }
        if (orDefault.get(index - 1) == null){
            return null;
        }
        return orDefault.get(index - 1);
    }


    public List<Vault> getVaults(UUID playerId) {
        return playerVaults.getOrDefault(playerId, new ArrayList<>());
    }

    public void addVault(UUID playerId, Vault vault) {
        List<Vault> vaults = playerVaults.getOrDefault(playerId, new ArrayList<>());
        vaults.add(vault);
        playerVaults.put(playerId, vaults);
    }

    public boolean removeVault(UUID playerId, Vault vault) {
        List<Vault> vaults = playerVaults.getOrDefault(playerId, new ArrayList<>());

        if (!vaults.contains(vault)){
            ResourceVaults.log("PLayer: " + Bukkit.getPlayer(playerId).getName() + " does not have: " + vault.getIndex());
            return false;
        }

        vaults.remove(vault);
        playerVaults.put(playerId, vaults);
        return true;
    }

    public void loadVaultsFromFile(UUID playerId) {
        List<Vault> vaults = new ArrayList<>();
        File playerFile = getPlayerFile(playerId);

        if (playerFile.exists()) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(playerFile);
            ConfigurationSection vaultsSection = yamlConfiguration.getConfigurationSection("vaults");

            if (vaultsSection != null) {
                for (String vaultKey : vaultsSection.getKeys(false)) {
                    ConfigurationSection vaultSection = vaultsSection.getConfigurationSection(vaultKey);
                    Location location = vaultSection.getSerializable("Location", Location.class);
                    if (location == null) {
                        continue;
                    }
                    String materialType = vaultSection.getString("Type");
                    int index = vaultSection.getInt("Index");
                    Vault vault = new Vault(index,playerId,location,Material.matchMaterial(materialType));
                    vaults.add(vault);
                }
            }
        }

        playerVaults.put(playerId, vaults);
    }

    public void saveVaultsToFile(UUID playerId) {
        List<Vault> vaults = playerVaults.getOrDefault(playerId, new ArrayList<>());
        File playerFile = getPlayerFile(playerId);

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        ConfigurationSection vaultsSection = yamlConfiguration.createSection("vaults");

        int vaultIndex = 1;
        for (Vault vault : vaults) {
            ConfigurationSection vaultSection = vaultsSection.createSection(Integer.toString(vaultIndex));
            vaultSection.set("Location", vault.getLocation());
            vaultSection.set("Type", vault.getMaterialType().toString());
            vaultSection.set("Index", vault.getIndex());

            // save the PDC data of the vault into the chest
            vault.getContainer().set(VaultKeys.getOwnerKey(), DataType.UUID, vault.getOwnerId());
            vault.getContainer().set(VaultKeys.getLocationKey(), DataType.LOCATION, vault.getLocation());
            vault.getContainer().set(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING, vault.getMaterialType().toString());
            vault.getContainer().set(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, vault.getIndex());
            vault.getChest().update();

            vaultIndex++;
        }

        try {
            yamlConfiguration.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getPlayerFile(UUID playerId) {
        File playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdir();
        }
        return new File(playersFolder, playerId.toString() + ".yml");
    }

    public void removePlayerVaults(UUID playerId) {
        playerVaults.remove(playerId);
        File playerFile = getPlayerFile(playerId);
        playerFile.delete();
    }


    public Vault getVault(Block block) {
        if (isVault(block)) {
            Vault vault = Vault.fromBlock(block);
            if (vault != null && vault.isActive()){
                List<Vault> orDefault = playerVaults.getOrDefault(vault.getOwnerId(), new ArrayList<>());
                orDefault.add(vault);
                playerVaults.putIfAbsent(vault.getOwnerId(), orDefault);
            }
            return vault;
        }
        return null;
    }

    public static Block findAttachedChest(Block block) {
        BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN, BlockFace.SELF };
        for (BlockFace face : blockFaces) {
            Block adjacentBlock = block.getRelative(face);
            if (adjacentBlock.getType() == Material.CHEST) {
                return adjacentBlock;
            }
        }
        return null;
    }
    private boolean isAdjacent(Block block1, Block block2) {
        if (block1.getWorld() != block2.getWorld()) {
            return false;
        }
        Location loc1 = block1.getLocation();
        Location loc2 = block2.getLocation();
        return !(loc1.distance(loc2) > 1);
    }

//    public boolean createVault(Player player, Block block, Material materialType) {
//        if (block.getType() != Material.CHEST) {
//            return false;
//        }
//        Chest chest = (Chest) block.getState();
//        PersistentDataContainer pdc = chest.getPersistentDataContainer();
//
//        if (pdc.has(VaultKeys.getOwnerKey(), DataType.UUID)) {
//            return false;
//        }
//
//        List<Vault> vaults = playerVaults.getOrDefault(player.getUniqueId(), new ArrayList<>());
//        int vaultIndex = vaults.size() + 1;
//        Vault vault = new Vault(player.getUniqueId(), block.getLocation(), materialType, vaultIndex);
//
//        vault.getContainer().set(VaultKeys.getOwnerKey(), DataType.UUID, player.getUniqueId());
//        vault.getContainer().set(VaultKeys.getLocationKey(), DataType.LOCATION, block.getLocation());
//        vault.getContainer().set(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING, materialType.toString());
//        vault.getContainer().set(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, vaultIndex);
//        chest.update();
//
//        vaults.add(vault);
//        playerVaults.put(player.getUniqueId(), vaults);
//        saveVaultsToFile(player.getUniqueId());
//
//        return true;
//    }

    public boolean removeVault(Player player, Vault vault) {
        List<Vault> vaults = playerVaults.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (!vaults.contains(vault)) {
            return false;
        }

        vaults.remove(vault);
        playerVaults.put(player.getUniqueId(), vaults);
        vault.getChest().getBlockInventory().clear();
        vault.getContainer().remove(VaultKeys.getOwnerKey());
        vault.getContainer().remove(VaultKeys.getLocationKey());
        vault.getContainer().remove(VaultKeys.getMaterialTypeKey());
        vault.getContainer().remove(VaultKeys.getIndexKey());
        vault.getChest().update();
        saveVaultsToFile(player.getUniqueId());

        return true;
    }

    public boolean addItemsToVault(Vault vault, ItemStack item, int amount) {
        Material materialType = item.getType();
        if (vault.getMaterialType() != materialType) {
            return false;
        }
        int index = vault.getIndex() - 1;
        ItemStack[] contents = vault.getChest().getBlockInventory().getContents();
        int maxStackSize = materialType.getMaxStackSize();
        int remaining = amount;
        while (index < contents.length && remaining > 0) {
            ItemStack itemStack = contents[index];
            if (itemStack == null) {
                itemStack = new ItemStack(materialType);
            } else if (itemStack.getType() != materialType) {
                return false;
            }
            int freeSpace = maxStackSize - itemStack.getAmount();
            if (freeSpace > 0) {
                int add = Math.min(freeSpace, remaining);
                itemStack.setAmount(itemStack.getAmount() + add);
                remaining -= add;
                contents[index] = itemStack;
            }
            index++;
        }
        vault.getChest().getBlockInventory().setContents(contents);
        vault.getChest().update();
        return true;
    }
    public Vault createVault(Player player, Block chestBlock, Material material){
        return this.createVault(player,chestBlock,material, playerVaults.size());
    }
    public Vault createVault(Player player, Block chestBlock, Material materialType, int index) {

        if (VaultManager.isVault(chestBlock)){
            return Vault.fromBlock(chestBlock);
        }
        UUID ownerId = player.getUniqueId();
        Location location = chestBlock.getLocation();
        Vault newVault = new Vault(index,ownerId, location, materialType);
        addVault(ownerId, newVault);
        return newVault;
    }
    public boolean hasVaults(UUID playerId) {
        return playerVaults.containsKey(playerId) && !playerVaults.get(playerId).isEmpty();
    }

    public int getVaultCount(UUID playerId) {
        if (!playerVaults.containsKey(playerId)) {
            return 0;
        }

        return playerVaults.get(playerId).size();
    }

    public List<ItemStack> getItemsFromVault(Vault vault) {
        List<ItemStack> items = new ArrayList<>();

        if (!playerVaults.containsKey(vault.getOwnerId())) {
            return items;
        }

        int vaultIndex = vault.getIndex();
        if (vaultIndex < 0 || vaultIndex >= playerVaults.get(vault.getOwnerId()).size()) {
            return items;
        }

        Vault v = playerVaults.get(vault.getOwnerId()).get(vaultIndex);
        for (ItemStack item : v.getInventory().getContents()) {
            if (item != null) {
                items.add(item.clone());
            }
        }

        return items;
    }

    public boolean isVaultFull(Vault vault) {
        Inventory inventory = vault.getInventory();
        int vaultIndex = vault.getIndex();
        int maxStackSize = inventory.getMaxStackSize();
        for (int i = vaultIndex * maxStackSize; i < (vaultIndex + 1) * maxStackSize; i++) {
            if (inventory.getItem(i) == null) {
                return false;
            }
        }
        return true;
    }
    public void saveEach(UUID uuid,boolean log){
        if (log){
            ResourceVaults.log("Saving Vaults to File from PDC!");
        }
        saveVaultsToFile(uuid);
    }

    public static boolean isVault(Block block) {
        if (block.getState() instanceof Chest chest) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            for (BlockFace face : faces) {
                Block relative = block.getRelative(face);
                if (relative.getState() instanceof Sign sign) {
                    if (sign.getLine(0).equals("[Resources]")) {
                        if (chest.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID) &&
                                chest.getPersistentDataContainer().has(getLocationKey(), DataType.LOCATION) &&
                                chest.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING) &&
                                chest.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER)) {
                            return true;
                        }
                    }
                }
            }
        } else if (block.getState() instanceof Sign sign) {
            if (sign.getLine(0).equals("[Resources]")) {
                BlockFace oppositeFace = ((Directional) block.getBlockData()).getFacing().getOppositeFace();
                Block attachedBlock = block.getRelative(oppositeFace);
                if (attachedBlock.getState() instanceof Chest chest) {
                    return chest.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID) &&
                            chest.getPersistentDataContainer().has(getLocationKey(), DataType.LOCATION) &&
                            chest.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING) &&
                            chest.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER);
                }
            }
        }
        return false;
    }
}