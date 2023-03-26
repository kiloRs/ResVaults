package com.thepaperraven.ai;

import lombok.Getter;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Getter
public class PlayerData {
    private final Map<Integer, VaultInstance> vaults;
    private final Map<Material, Map<Integer, VaultInstance>> mapByMaterial;
    private final UUID uuid;
    private boolean loadOnCreate;

    // Other fields and methods for the PlayerData class
    public PlayerData(UUID uuid) {
        this(uuid, true);
    }

    public PlayerData(UUID uuid, boolean loadOnCreate) {
        this.uuid = uuid;
        this.loadOnCreate = loadOnCreate;
        this.vaults = new HashMap<>();
        this.mapByMaterial = new HashMap<>();
    }

    public void addVault(VaultInstance vaultInstance) {
        int index = getNextIndex();
        addVault(index, vaultInstance);
    }

    public void addVault(int index, VaultInstance vaultInstance) {
        vaults.put(index, vaultInstance);
        Material material = vaultInstance.getAllowedMaterial();
        Map<Integer, VaultInstance> materialVaults = mapByMaterial.getOrDefault(material, new HashMap<>());
        materialVaults.put(index, vaultInstance);
        mapByMaterial.put(material, materialVaults);
    }

    public VaultInstance getVault(int index) {
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

        ResourceVaults.error("No Index found for " + instance.getOwnerUUID());

        return returnValue;
    }

    public List<VaultInstance> getVaults() {
        return new ArrayList<>(vaults.values());
    }

    public List<VaultInstance> getVaultsByMaterial(Material material) {
        Map<Integer, VaultInstance> materialVaults = mapByMaterial.getOrDefault(material, new HashMap<>());
        return new ArrayList<>(materialVaults.values());
    }

    public int getTotalVaultCount() {
        return vaults.size();
    }

    public int getTotalVaultCount(Material material) {
        Map<Integer, VaultInstance> materialVaults = mapByMaterial.getOrDefault(material, new HashMap<>());
        return materialVaults.size();
    }

    public int getTotalMaterialAmount(Material material) {
        int totalAmount = 0;
        Map<Integer, VaultInstance> materialVaults = mapByMaterial.getOrDefault(material, new HashMap<>());
        for (VaultInstance vault : materialVaults.values()) {
            int amount = vault.getAmount();
            totalAmount += amount;
        }
        return totalAmount;
    }

    public void updateVaultMetadata(VaultInstance vault, VaultMetadata metadata) {
        int index = getIndexOf(vault);
        VaultInstance updatedVault = new Vault(metadata, vault.getSignLocation(), vault.getChestLocations(), vault.getAllowedMaterial());
        vaults.put(index, updatedVault);

        Material material = vault.getAllowedMaterial();
        Map<Integer, VaultInstance> materialVaults = mapByMaterial.get(material);
        if (materialVaults != null) {
            materialVaults.put(index, updatedVault);
        }
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
            Material material = vault.getAllowedMaterial();
            if (mapByMaterial.containsKey(material)) {
                mapByMaterial.get(material).remove(index);
            }
        }
    }
}
