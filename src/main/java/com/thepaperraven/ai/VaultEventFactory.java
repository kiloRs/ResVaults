package com.thepaperraven.ai;

import com.thepaperraven.events.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class VaultEventFactory {
    @Getter
    private final Plugin plugin;

    public VaultEventFactory(Plugin plugin) {
        this.plugin = plugin;
    }

    public static boolean fireCreateEvent(Player playerCreatingVault, Vault vault) {
        VaultCreateEvent event = new VaultCreateEvent(vault,playerCreatingVault);
        Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof Cancellable) || !event.isCancelled();
    }

    public static boolean fireDeleteEvent(Vault vault) {
        VaultDeleteEvent event = new VaultDeleteEvent(vault);
        Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof Cancellable) || !event.isCancelled();
    }

    public static boolean fireAddBalanceEvent(Vault vault, int balance) {
        VaultAddBalanceEvent event = new VaultAddBalanceEvent(vault, balance);
        Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof Cancellable) || !event.isCancelled();
    }

    public static boolean fireRemoveBalanceEvent(Vault vault, int removingBalance) {
        VaultRemoveBalanceEvent event = new VaultRemoveBalanceEvent(vault, removingBalance);
        Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof Cancellable) || !event.isCancelled();
    }

    public static boolean fireUpdateVault(Vault vault, NamespacedKey[] changes) {
        VaultUpdatePDCEvent event = new VaultUpdatePDCEvent(vault, Arrays.stream(changes).toList());
        Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof Cancellable) || !event.isCancelled();
    }
}
