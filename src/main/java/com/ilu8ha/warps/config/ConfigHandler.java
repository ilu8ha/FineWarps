package com.ilu8ha.warps.config;

import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.permission.CustomStatus;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import java.io.File;



public class ConfigHandler {
    public static File configFile;
    public static Configuration config;
    public static boolean isCooldownSystemEnabled = false;
    public static boolean isVisitedFirstSystemEnabled = false;
    public static boolean isSpecialPermissionNeeded = false;
    public static int[] blacklistedDimension = new int[]{};
    public static String[] unavailableWithoutPermissionWarpName = new String[]{"list","warps", "warp", "help", "spawn", "shop", "adminshop"};
    public static int spawnWorldId = 0;
    public static boolean isVisitedFirstSystemToTpaEnabled = false;
    public static boolean isTpaCooldownSystemEnabled = false;
    public static int teleportRequestLifetime = 1200;
    public static boolean canTpaToBlacklistedDimension = false;
    public static boolean isSpecialPermissionToTpaNeeded = false;
    public static boolean warpSafetyCheck = true;
    public static boolean tpaSafetyCheck = true;
    public static int defaultMaxWarp = 1;
    public static int defaultTpaCooldown = 1600;
    public static int defaultWarpCooldown = 1600;
    public static boolean isCustomStatusesEnabled = false;
    private static String [] customStatuses = new String[]{};
    private static String [] customStatusesMaxWarp = new String[]{};
    private static String [] customStatusesWarpCooldown = new String[]{};
    private static String [] customStatusesTpaCooldown = new String[]{};

    public static void init(File file){
        config = new Configuration(file);
        config.load();
        String category;

        category = "General";
        config.addCustomCategoryComment("General","General settings");
        isCooldownSystemEnabled = config.getBoolean("isCooldownSystemEnabled",category,false, "Enable/disable warp cooldown");
        isSpecialPermissionNeeded = config.getBoolean("isSpecialPermissionNeeded", category, false,"Enable/disable require special permission to access for every DIM, Example fmwarps.accessdim.[DIM_ID]");
        isVisitedFirstSystemEnabled = config.getBoolean("isVisitedFirstSystemEnabled",category, false,"Enable/disable require to visit dimension before being able to warp there");
        spawnWorldId = config.getInt("spawnWorldId", category, 0, -2147483647, 2147483647,"Starting dimensionId. VisitFirstSystem checks are disable for this dimension");
        blacklistedDimension = config.get(category,"List of dimension without being able to warp there",blacklistedDimension).getIntList();
        unavailableWithoutPermissionWarpName = config.get(category,"List of warp names unavailable without operator permission", unavailableWithoutPermissionWarpName).getStringList();
        warpSafetyCheck = config.getBoolean("warpSafetyCheck", category, true, "Prevent teleporting if warp point on air or or haven't free space for player");
        defaultMaxWarp = config.getInt("defaultMaxWarp", category, 1, 0,2147483647, "Maximum warp count per player by default");
        defaultWarpCooldown = config.getInt("defaultWarpCooldown", category,1600,0,2147483647, "Default cooldown between teleporting on warp point");

        category = "Tpa";
        config.addCustomCategoryComment(category,"Tpa settings");
        isVisitedFirstSystemToTpaEnabled = config.getBoolean("isVisitedFirstSystemToTpaEnabled", category, false, "Enable/disable require to visit player's dimension before being able to teleport");
        isSpecialPermissionToTpaNeeded = config.getBoolean("isSpecialPermissionToTpaNeeded", category, false,"Enable/disable require special permission to access for every DIM. Example fmwarps.accessdim.[DIM_ID]");
        isTpaCooldownSystemEnabled = config.getBoolean("isTpaCooldownSystemEnabled", category,false,"Enable/disable tpa cooldown");
        teleportRequestLifetime = config.getInt("teleportRequestLifetime",category, 1200,0,6000,"TeleportRequest lifetime in tick");
        canTpaToBlacklistedDimension = config.getBoolean("canTeleportToBlacklistedDimension", category, false, "Can telepot to player if he is in blacklisted dimension");
        tpaSafetyCheck = config.getBoolean("tpaSafetyCheck", category, true, "Teleportation is possible only then target player stay on ground");
        defaultTpaCooldown = config.getInt("defaultTpaCooldown", category,1600,0,2147483647, "Default cooldown between sending a new teleport request");


        category = "CustomStatus";
        config.addCustomCategoryComment(category, "Custom Statuses module.");
        isCustomStatusesEnabled = config.getBoolean("isCustomStatusesEnabled", category, false, "Is custom statuses module enabled. Have no reason to enable it with default forge permissionHandler");
        customStatuses = config.get(category, "List of custom statusNames, case insensitive. Each status will have its own permission registered, permission format fmwarps.playerstatus.[statusName]", customStatuses).getStringList();
        customStatusesMaxWarp = config.get(category, "Maximum warp point for status owner. Format [statusName(case insensitive)]:[number(must be positive)]", customStatusesMaxWarp).getStringList();
        customStatusesWarpCooldown = config.get(category, "Cooldown between teleporting on warp point for status owner. Format [statusName(case insensitive)]:[number in tick(must be positive)]", customStatusesWarpCooldown).getStringList();
        customStatusesTpaCooldown = config.get(category, "Cooldown between sending a new teleport request for status owner. Format [statusName(case insensitive)]:[number in tick(must be positive)]", customStatusesTpaCooldown).getStringList();
        registerCustomStatus();

        config.save();
    }

    public static void registerConfig(FMLPreInitializationEvent event){
        configFile = new File(String.valueOf(event.getModConfigurationDirectory()));
        init(new File(configFile.getPath(), FineWarps.MODID + ".cfg"));
    }

    private static void registerCustomStatus(){
        if(isCustomStatusesEnabled){
            for(String statusName : customStatuses){
                if(CustomStatus.statusList.stream().noneMatch(i->i.getStatusName().equals(statusName.toLowerCase()))){
                    new CustomStatus(statusName.toLowerCase());
                }
            }

            for(String str : customStatusesMaxWarp){
                if(str.matches("^\\w+:(0|[1-9]\\d*)$")){
                    try{
                        String[] parts = str.split(":");
                        String statusName = parts[0];
                        int property = Integer.parseInt(parts[1]);
                        CustomStatus status = CustomStatus.statusList.stream().filter(i->i.getStatusName().equals(statusName.toLowerCase())).findAny().orElse(null);
                        if(status!=null){
                            status.setWarpCount(property);
                            FineWarps.logger.log(Level.DEBUG, String.format("Maximum warp for status %s set to %d",statusName, property));
                        }
                    }catch (NumberFormatException exception)
                    {

                    }
                }
            }
            for(String str : customStatusesWarpCooldown){
                if(str.matches("^\\w+:(0|[1-9]\\d*)$")){
                    String[] parts = str.split(":");
                    String statusName = parts[0];
                    int property = Integer.parseInt(parts[1]);
                    CustomStatus status = CustomStatus.statusList.stream().filter(i->i.getStatusName().equals(statusName.toLowerCase())).findAny().orElse(null);
                    if(status!=null){
                        status.setWarpCooldown(property);
                        FineWarps.logger.log(Level.DEBUG, String.format("Warp cooldown for status %s set to %d",statusName, property));
                    }
                }
            }
            for(String str : customStatusesTpaCooldown){
                if(str.matches("^\\w+:(0|[1-9]\\d*)$")){
                    String[] parts = str.split(":");
                    String statusName = parts[0];
                    int property = Integer.parseInt(parts[1]);
                    CustomStatus status = CustomStatus.statusList.stream().filter(i->i.getStatusName().equals(statusName.toLowerCase())).findAny().orElse(null);
                    if(status!=null){
                        status.setTpaCooldown(property);
                        FineWarps.logger.log(Level.DEBUG, String.format("Tpa cooldown for status %s set to %d",statusName, property));
                    }
                }
            }
        }
    }
}
