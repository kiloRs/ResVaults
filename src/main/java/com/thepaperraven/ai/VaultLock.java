package com.thepaperraven.ai;

import org.bukkit.block.Block;

import java.util.List;

public interface VaultLock {

    public List<Block> lockedBlocks();
    void lock();

    void unlock();

    boolean isLocked();

}