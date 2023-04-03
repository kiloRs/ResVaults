package com.thepaperraven.utils;

import com.thepaperraven.ResourceVaults;
import org.bukkit.entity.Player;

public class PermissionUtil {

    public static boolean hasPermission(Player player){
        return player.hasPermission(ResourceVaults.getConfiguration().getAdminPermission());
    }
}
