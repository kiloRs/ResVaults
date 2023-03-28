package com.thepaperraven.commands;

import com.jeff_media.jefflib.NumberUtils;
import com.thepaperraven.ai.gui.VaultGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MyVaults implements CommandExecutor {
    private final Plugin plugin;

    public MyVaults(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)){
            return false;
        }

        if (label.equalsIgnoreCase("myvaults")){
            if (args.length == 0){
                VaultGUI.openPage(1,player);
                return true;
            }
            else if (args.length == 1){
                String pageNumber = args[0];
                if (NumberUtils.isInteger(pageNumber)){
                    int page = Integer.parseInt(pageNumber);
                    VaultGUI.openPage(page,player);
                }
                return false;
            }
            return false;
        }
        return false;
    }
}
