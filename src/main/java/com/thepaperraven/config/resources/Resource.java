package com.thepaperraven.config.resources;

import com.thepaperraven.ai.ResourceVaults;
import io.lumine.mythic.lib.api.util.EnumUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Resource {
    WHEAT(new ChestResource(Material.WHEAT)),STONE(new ChestResource(Material.STONE)),LEATHER(new ChestResource(Material.LEATHER)),NONE(null);
    Resource(@Nullable ChestResource resource){
        if (resource == null){
            return;
        }
        this.resource = resource;
        this.name = null;
        this.value = 0;
        this.material = resource.getMaterial();
        this.active = true;
        if (ResourceVaults.getResourcesConfiguration().exists() && ResourceVaults.getResourcesConfiguration().getConfig().contains("Resources")){
            if (ResourceVaults.getResourcesConfiguration().getConfig().contains("Resources." + material.getKey().getKey() + ".Name")) {
                this.name = ResourceVaults.getResourcesConfiguration().getConfig().getString("Resources." +material.getKey().getKey() + ".Name",resource.getName());
            }
            if (ResourceVaults.getResourcesConfiguration().getConfig().contains ("Resources." +material.getKey().getKey() + ".Value")) {
                this.value = ResourceVaults.getResourcesConfiguration().getConfig().getInt("Resources." +material.getKey().getKey() + ".Value",0);
            }
            if (ResourceVaults.getResourcesConfiguration().getConfig().isBoolean("Resources." +material.getKey().getKey() + ".Enable")){
                this.active = ResourceVaults.getResourcesConfiguration().getConfig().getBoolean("Resources." +material.getKey().getKey() + ".Enable",true);
            }
            if (ResourceVaults.getResourcesConfiguration().getConfig().contains("Resources." +material.getKey().getKey() + ".Color")){
                this.color = EnumUtils.getIfPresent(ChatColor.class,ResourceVaults.getResourcesConfiguration().getConfig().getString("Resources." +material.getKey().getKey() +".Color")).orElse(ChatColor.AQUA);
            }
        }
        else {
            ResourceVaults.log("No Instance of Resources Section in Configurations!");
        }
    }


    @Getter
    private ChestResource resource;
    @Getter
    private String name;
    @Getter
    private int value;
    @Getter
    private Material material;
    @Getter
    private boolean active;
    @Getter
    private ChatColor color = ChatColor.WHITE;

    @NotNull
    public static Resource get(Material material) {
        for (Resource value : Resource.values()) {
            if (value.getMaterial()==material){
                return value;
            }
        }
        return NONE;
    }
}

