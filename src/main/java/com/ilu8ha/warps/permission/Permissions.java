package com.ilu8ha.warps.permission;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;


public class Permissions {
    public static final String warp = "fmwarps.command.finewarps:warp";
    public static final String warps = "fmwarps.command.finewarps:warps";
    public static final String teleportRequest = "fmwarps.command.finewarps:teleportrequest";
    public static final String operator = "fmwarps.operator";
    public static final String warpSet = "fmwarps.warp.set";
    public static final String warpUpdate = "fmwarps.warp.update";
    public static final String warpRemove = "fmwarps.warp.remove";
    public static final String warpInfo = "fmwarps.warp.info";
    public static final String warpPrivacy = "fmwarps.warp.privacy";
    public static final String warpInvitation = "fmwarps.warp.invitation";
    public static final String teleportRequestAccept = "fmwarps.tpa.accept";
    public static final String teleportRequestDeny = "fmwarps.tpa.deny";
    public static final String teleportRequestSend = "fmwarps.tpa.send";



    public static void registerPermissions(){
        PermissionAPI.registerNode(warp, DefaultPermissionLevel.ALL, "Wrap access");
        PermissionAPI.registerNode(warps, DefaultPermissionLevel.ALL, "Warps access");
        PermissionAPI.registerNode(operator, DefaultPermissionLevel.OP, "Operator permission");
        PermissionAPI.registerNode(warpSet,DefaultPermissionLevel.ALL,"Create new warp");
        PermissionAPI.registerNode(warpUpdate, DefaultPermissionLevel.ALL, "Update warps");
        PermissionAPI.registerNode(warpRemove, DefaultPermissionLevel.ALL, "Remove warps");
        PermissionAPI.registerNode(warpInfo,DefaultPermissionLevel.ALL, "View warps info");
        PermissionAPI.registerNode(warpPrivacy, DefaultPermissionLevel.ALL, "Change privacy warp setting");
        PermissionAPI.registerNode(warpInvitation,DefaultPermissionLevel.ALL,"Invite/Uninvite other player");
        PermissionAPI.registerNode(teleportRequest, DefaultPermissionLevel.ALL, "Teleport request access");
        PermissionAPI.registerNode(teleportRequestSend, DefaultPermissionLevel.ALL, "Teleport request send");
        PermissionAPI.registerNode(teleportRequestAccept, DefaultPermissionLevel.ALL, "Teleport request accept");
        PermissionAPI.registerNode(teleportRequestDeny, DefaultPermissionLevel.ALL, "Teleport request deny");

        CustomStatus.registerStatusPermission();
    }
}
