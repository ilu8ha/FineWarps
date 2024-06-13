package com.ilu8ha.warps.permission;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public enum WarpUseCooldown {
    USER ("fmwarps.cooldown.user"),
    VIP ("fmwarps.cooldown.vip"),
    PREMIUM ("fmwarps.cooldown.premium"),
    GRAND  ("fmwarps.cooldown.grand"),
    SPONSOR ("fmwarps.cooldown.sponsor"),
    ADMIN ("fmwarps.cooldown.admin");
    private final String permission;
    WarpUseCooldown(String permission){
        this.permission = permission;
    }
    public String getPermission() {
        return permission;
    }

    public static void registerPermission(){
        PermissionAPI.registerNode(USER.permission, DefaultPermissionLevel.ALL, "User cooldown to warp usage level");
        PermissionAPI.registerNode(VIP.permission, DefaultPermissionLevel.ALL, "VIP cooldown to warp usage level");
        PermissionAPI.registerNode(PREMIUM.permission, DefaultPermissionLevel.ALL, "Premium cooldown to warp usage level");
        PermissionAPI.registerNode(GRAND.permission, DefaultPermissionLevel.ALL, "Grand cooldown to warp usage level");
        PermissionAPI.registerNode(SPONSOR.permission, DefaultPermissionLevel.ALL, "Sponsor cooldown to warp usage level");
        PermissionAPI.registerNode(ADMIN.permission, DefaultPermissionLevel.ALL, "Admin cooldown to warp usage level");
    }
}
