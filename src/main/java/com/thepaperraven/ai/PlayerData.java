package com.thepaperraven.ai;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerData {
    private final Map<Integer, VaultInstance> vaults;
    private final UUID uuid;
    private boolean loadOnCreate;
    // Other fields and methods for the PlayerData class

    public PlayerData(UUID uuid){
        this(uuid,true);
    }
    public PlayerData(UUID uuid, boolean loadOnCreate) {
        this.uuid = uuid;
        this.loadOnCreate = loadOnCreate;
        this.vaults = new HashMap<Integer, VaultInstance>();
    }

    public void addVault(VaultInstance vaultInstance){
        this.addVault(getNextIndex(),vaultInstance);
    }
    public void addVault(int index,VaultInstance vaultInstance) {
        this.vaults.put(index, vaultInstance);
    }

    public VaultInstance getVault(int index) {
        return this.vaults.get(index);
    }

    public boolean hasVault(int index) {
        return this.vaults.containsKey(index);
    }

    public int getNextIndex() {
        int index = 1;
        while (vaults.containsKey(index)) {
            index++;
        }
        return index;
    }
}
