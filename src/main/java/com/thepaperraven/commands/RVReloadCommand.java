package com.thepaperraven.commands;

import com.thepaperraven.ResourceVaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RVReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("rvreload") && sender.isOp()){
            if (args.length == 1) {
                boolean state = args[0].equalsIgnoreCase("false");
                ResourceVaults.reloadPlugin(!state);
                sender.sendMessage("Reloading RV!");
                return true;
            }
        }

        return false;
    }
}
