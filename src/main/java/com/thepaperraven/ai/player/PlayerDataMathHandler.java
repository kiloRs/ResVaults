package com.thepaperraven.ai.player;

import org.bukkit.Material;

public interface PlayerDataMathHandler {
    public int getTotalItems();
    public int getTotalItems(Material material);
    public int getTotalVaults();
    public int getTotalVaults(Material material);
}
