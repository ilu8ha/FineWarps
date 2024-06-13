package com.ilu8ha.warps.permission;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public enum MaxWarpCount {
    USER ("fmwarps.maxwarp.user"),
    VIP ("fmwarps.maxwarp.vip"),
    PREMIUM ("fmwarps.maxwarp.premium"),
    GRAND  ("fmwarps.maxwarp.grand"),
    SPONSOR ("fmwarps.maxwarp.sponsor"),
    ADMIN ("fmwarps.maxwarp.admin");
    private final String permission;
    MaxWarpCount(String permission){
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public static void registerPermissions(){
        PermissionAPI.registerNode(USER.permission, DefaultPermissionLevel.ALL, "User maximum warps count level");
        PermissionAPI.registerNode(VIP.permission, DefaultPermissionLevel.ALL, "VIP maximum warps count level");
        PermissionAPI.registerNode(PREMIUM.permission, DefaultPermissionLevel.ALL, "Premium maximum warps count level");
        PermissionAPI.registerNode(GRAND.permission, DefaultPermissionLevel.ALL, "Grand maximum warps count level");
        PermissionAPI.registerNode(SPONSOR.permission, DefaultPermissionLevel.ALL, "Sponsor maximum warps count level");
        PermissionAPI.registerNode(ADMIN.permission, DefaultPermissionLevel.ALL, "Admin maximum warps count level");
    }
}
