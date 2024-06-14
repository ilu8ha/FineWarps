package com.ilu8ha.warps.permission;

import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.config.ConfigHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class CustomStatus {
    private static final String permissionNodeFormat = "fmwarps.playerstatus.%s";
    public static final List<CustomStatus> statusList = new ArrayList<>();
    private final String statusName;
    private final String permissionNode;
    private int tpaCooldown;
    private int warpCooldown;
    private int maxWarpCount;
    public CustomStatus(String statusName){
        this.statusName = statusName.toLowerCase();
        this.permissionNode = String.format(permissionNodeFormat, statusName.toLowerCase());
        this.tpaCooldown = ConfigHandler.defaultTpaCooldown;
        this.warpCooldown = ConfigHandler.defaultWarpCooldown;
        this.maxWarpCount = ConfigHandler.defaultMaxWarp;

        FineWarps.logger.log(Level.DEBUG, String.format("Added custom status %s", this.statusName));
        statusList.add(this);
    }

    public static void registerStatusPermission(){
        for(CustomStatus status : statusList){
            PermissionAPI.registerNode(status.permissionNode, DefaultPermissionLevel.NONE, String.format("Permission nod for %s status", status.statusName));
            FineWarps.logger.log(Level.DEBUG, String.format("Register permission %s for custom status %s", status.permissionNode ,status.statusName));
        }
    }
    public String getStatusName() {
        return statusName;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public int getTpaCooldown() {
        return tpaCooldown;
    }

    public int getWarpCooldown() {
        return warpCooldown;
    }

    public int getMaxWarpCount() {
        return maxWarpCount;
    }

    public void setTpaCooldown(int tpaCooldown) {
        this.tpaCooldown = tpaCooldown;
    }

    public void setWarpCooldown(int warpCooldown) {
        this.warpCooldown = warpCooldown;
    }

    public void setWarpCount(int warpCount) {
        this.maxWarpCount = warpCount;
    }
}
