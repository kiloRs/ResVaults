package com.thepaperraven.config;

import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationFile {
    private final File file;
    private final String name;
    private final FileConfiguration config;

        public ConfigurationFile(String name) {
            this(ResourceVaults.getPlugin(), "", name);
        }

        public ConfigurationFile(String folder, String name) {
            this(ResourceVaults.getPlugin(), folder, name);
        }

        public ConfigurationFile(Plugin plugin, String folder, String name) {
            this.config = YamlConfiguration.loadConfiguration(this.file = new File(plugin.getDataFolder() + folder, (this.name = name) + ".yml"));
        }
        public ConfigurationFile(Player player){
            this(ResourceVaults.getPlugin(),"/vaults/",player.getUniqueId() + "");
        }
        public ConfigurationFile(Vault vault){
            this(ResourceVaults.getPlugin(),"/vaults/", vault.getOwner().toString());
        }

        public boolean exists() {
            return this.file.exists();
        }

        public FileConfiguration getConfig() {
            return this.config;
        }

        public void save() {
            try {
                this.config.save(this.file);
            } catch (IOException var2) {
                Logger var10000 = ResourceVaults.getPlugin().getLogger();
                var10000.log(Level.SEVERE, "Could not save " + this.name + ".yml: " + var2.getMessage());
            }

        }

        public void delete() {
            if (this.file.exists() && !this.file.delete()) {
                ResourceVaults.getPlugin().getLogger().log(Level.SEVERE, "Could not delete " + this.name + ".yml.");
            }

        }

    public File getFile() {
        return this.file;
    }
}

