package com.ilu8ha.warps.permission;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public enum TpaRequestCooldown {
    USER ("fmwarps.tpa.cooldown.user"),
    VIP ("fmwarps.tpa.cooldown.vip"),
    PREMIUM ("fmwarps.tpa.cooldown.premium"),
    GRAND ("fmwarps.tpa.cooldown.grand"),
    SPONSOR ("fmwarps.tpa.cooldown.sponsor"),
    ADMIN ("fmwarps.tpa.cooldown.admin");

    private String permission;

    TpaRequestCooldown(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public static void registerPermission() {
        PermissionAPI.registerNode(USER.permission, DefaultPermissionLevel.ALL, "User cooldown to Tpa usage level");
        PermissionAPI.registerNode(VIP.permission, DefaultPermissionLevel.ALL, "VIP cooldown to Tpa usage level");
        PermissionAPI.registerNode(PREMIUM.permission, DefaultPermissionLevel.ALL, "Premium cooldown to Tpa usage level");
        PermissionAPI.registerNode(GRAND.permission, DefaultPermissionLevel.ALL, "Grand cooldown to Tpa usage level");
        PermissionAPI.registerNode(SPONSOR.permission, DefaultPermissionLevel.ALL, "Sponsor cooldown to Tpa usage level");
        PermissionAPI.registerNode(ADMIN.permission, DefaultPermissionLevel.ALL, "Admin cooldown to Tpa usage level");
    }
}
