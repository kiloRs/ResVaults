package com.thepaperraven.config;

import com.thepaperraven.ResourceVaults;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class GeneralConfiguration {

    private String PREFIX = ChatColor.GRAY + "[" + ChatColor.YELLOW + "ResourceVaults" + ChatColor.GRAY + "] ";

    private static final FileConfiguration config = ResourceVaults.getPlugin().getConfig();

    private static final String ALLOWED_MATERIALS_PATH = "allowed-materials";
    private static final String SIGN_TYPE_PATH = "default-sign-type";
    private static final String VAULT_MATERIAL = "default-vault-material";
    private static final String SIGN_TEXT_COLOR_PATH = "sign.text-color";
    private static final String SIGN_TEXT_GLOW = "sign.text-glow";
    private static final String ADMIN_PERMISSION_PATH = "admin-permission";

    private final List<Material> allowedMaterials;
    private Material defaultSignType;
    private final ChatColor signTextColor;
    private final String adminPermission;
    private final Material defaultVaultMaterial;
    private boolean signsGlow;

    public GeneralConfiguration(ConfigurationSection config) {
        if (config.isList(ALLOWED_MATERIALS_PATH)) {
            this.allowedMaterials = config.getStringList(ALLOWED_MATERIALS_PATH).stream()
                    .map(Material::getMaterial)
                    .toList();
        }
        else {
            ArrayList<Material> m = new ArrayList<>();
            m.add(Material.WHEAT);
            m.add(Material.STONE);
            m.add(Material.LEATHER);
            this.allowedMaterials = m;
        }
        this.defaultSignType = Material.getMaterial(config.getString(SIGN_TYPE_PATH,Material.BIRCH_WALL_SIGN.name().toUpperCase()));
        if (this.defaultSignType == null) {
            this.defaultSignType = Material.OAK_WALL_SIGN;
        }

        this.signsGlow = config.getBoolean(SIGN_TEXT_GLOW,false);
        this.signTextColor = ChatColor.valueOf(config.getString(SIGN_TEXT_COLOR_PATH, "BLACK"));
        this.defaultVaultMaterial = Material.matchMaterial(config.getString(VAULT_MATERIAL,Material.STONE.name().toUpperCase()));


        this.adminPermission = config.getString(ADMIN_PERMISSION_PATH, "rv.admin");

        PREFIX = ChatColor.translateAlternateColorCodes('&',config.getString("general.prefix","&l[&eResourceVaults&r]"));
        save();
    }

    public List<Material> getAllowedMaterials() {
        return allowedMaterials;
    }

    public Material getDefaultSignType() {
        return defaultSignType;
    }

    public ChatColor getSignTextColor() {
        return signTextColor;
    }
    public boolean doSignsGlow(){
        return signsGlow;
    }

    public String getAdminPermission() {
        return adminPermission;
    }

    public static boolean doesPlayerHave(Player player) {
        return player.hasPermission(ResourceVaults.getConfiguration().getAdminPermission());
    }
    public void save() {
        // Save allowed materials
        List<String> allowedMaterialNames = new ArrayList<>();
        for (Material material : allowedMaterials) {
            allowedMaterialNames.add(material.name());
        }
        config.set("general.allowed-materials", allowedMaterialNames);

        config.set("general.default-vault-material",Material.STONE.name().toUpperCase());
        // Save default sign type
        config.set("general.default-sign-type", defaultSignType.name());

        // Save sign text color
        config.set("general.sign-text-color", signTextColor.name());

        // Save admin permission
        config.set("general.admin-permission", adminPermission);

        // Save the YAML configuration to file
        try {
            config.save(new File(ResourceVaults.getPlugin().getDataFolder(),"config.yml"));
            ResourceVaults.getLogger("Saving Fresh Configurations!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

