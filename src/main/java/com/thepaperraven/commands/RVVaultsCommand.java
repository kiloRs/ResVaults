//package com.thepaperraven.commands;
//
//import com.thepaperraven.ai.gui.VaultsGUI;
//import com.thepaperraven.ai.player.PlayerData;
//import com.thepaperraven.ai.vault.Vault;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.Map;
//
//public class RVVaultsCommand implements CommandExecutor {
//
//    private final String commandName = "rvvaults";
//
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player player)) {
//            sender.sendMessage("This command can only be run by a player.");
//            return true;
//        }
//
//        Map<Integer, Vault> vaults = PlayerData.get(player.getUniqueId()).getVaults();
//
//        int numVaults = vaults.size();
//        int numPages = (int) Math.ceil((double) numVaults / VaultsGUI.SIZE);
//        if (numPages == 0) numPages = 1;
//
//        VaultsGUI gui = new VaultsGUI(player, numPages);
//        gui.setPageIcon(VaultsGUI.PREVIOUS_PAGE, getIcon("previous-page"));
//        gui.setPageIcon(VaultsGUI.CURRENT_PAGE, getIcon("current-page"));
//        gui.setPageIcon(VaultsGUI.NEXT_PAGE, getIcon("next-page"));
//
//        int page = 1;
//        if (args.length > 0) {
//            try {
//                page = Integer.parseInt(args[0]);
//            } catch (NumberFormatException e) {
//                player.sendMessage(ChatColor.RED + "Invalid page number: " + args[0]);
//                return true;
//            }
//            if (page < 1 || page > numPages) {
//                player.sendMessage(ChatColor.RED + "Invalid page number: " + page);
//                return true;
//            }
//        }
//        gui.setPage(page);
//
//        int start = (page - 1) * VaultsGUI.SIZE;
//        int end = start + VaultsGUI.SIZE;
//        if (end > numVaults) end = numVaults;
//
//        int slot = 0;
//        for (int i = start; i < end; i++) {
//            gui.setVault(slot++, vaults.get(i));
//        }
//
//        player.openInventory(gui.getInventory());
//        return true;
//    }
//
//
//}
