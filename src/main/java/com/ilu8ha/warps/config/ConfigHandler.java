package com.ilu8ha.warps.config;

import com.ilu8ha.warps.FineWarps;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class ConfigHandler {
    public static File configFile;
    public static Configuration config;
    public static int maxWarpCountUser = 0;
    public static int maxWarpCountVip = 0;
    public static int maxWarpCountPremium = 0;
    public static int maxWarpCountGrand = 0;
    public static int maxWarpCountSponsor = 0;
    public static int maxWarpCountAdmin = 999;
    public static int cooldownWarpUser = 0;
    public static int cooldownWarpVip = 0;
    public static int cooldownWarpPremium = 0;
    public static int cooldownWarpGrand = 0;
    public static int cooldownWarpSponsor = 0;
    public static int cooldownWarpAdmin = 0;
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
    public static int cooldownTpaUser = 0;
    public static int cooldownTpaVip = 0;
    public static int cooldownTpaPremium = 0;
    public static int cooldownTpaGrand = 0;
    public static int cooldownTpaSponsor = 0;
    public static int cooldownTpaAdmin = 0;
    public static boolean isSpecialPermissionToTpaNeeded = false;
    public static boolean warpSafetyCheck = true;
    public static boolean tpaSafetyCheck = true;
    public static void init(File file){
        config = new Configuration(file);
        config.load();
        String category;

        category = "MaxWarpCount";
        config.addCustomCategoryComment(category,"Maximum warps per player status");
        maxWarpCountUser = config.getInt("maxWarpCountUser",category,0,0,999,"Maximum warps count for user");
        maxWarpCountVip = config.getInt("maxWarpCountVip",category,0,0,999,"Maximum warps count for VIP");
        maxWarpCountPremium = config.getInt("maxWarpCountPremium",category,0,0,999,"Maximum warps count for Premium");
        maxWarpCountGrand = config.getInt("maxWarpCountGrand",category,0,0,999,"Maximum warps count for Grand");
        maxWarpCountSponsor = config.getInt("maxWarpCountSponsor",category,0,0,999,"Maximum warps count for Sponsor");
        maxWarpCountAdmin = config.getInt("maxWarpCountAdmin",category,0,0,999,"Maximum warps count for Admin");

        category = "WarpUseCooldown";
        config.addCustomCategoryComment(category,"Warp cooldown in tick per player status");
        cooldownWarpUser = config.getInt("cooldownWarpUser", category, 0,0, 2147483647,"Cooldown in tick for user");
        cooldownWarpVip = config.getInt("cooldownWarpVip", category, 0,0, 2147483647,"Cooldown in tick for VIP");
        cooldownWarpPremium = config.getInt("cooldownWarpPremium", category, 0,0, 2147483647,"Cooldown in tick for Premium");
        cooldownWarpGrand = config.getInt("cooldownWarpGrand", category, 0,0, 2147483647,"Cooldown in tick for Grand");
        cooldownWarpSponsor = config.getInt("cooldownWarpSponsor", category, 0,0, 2147483647,"Cooldown in tick for Sponsor");
        cooldownWarpAdmin = config.getInt("cooldownWarpAdmin", category, 0,0, 2147483647,"Cooldown in tick for Admin");

        category = "General";
        config.addCustomCategoryComment("General","General settings");
        isCooldownSystemEnabled = config.getBoolean("isCooldownSystemEnabled",category,false, "Enable/disable warp cooldown");
        isSpecialPermissionNeeded = config.getBoolean("isSpecialPermissionNeeded", category, false,"Enable/disable require special permission to access for every DIM, Example fmwarps.accessdim.[DIM_ID]");
        isVisitedFirstSystemEnabled = config.getBoolean("isVisitedFirstSystemEnabled",category, false,"Enable/disable require to visit dimension before being able to warp there");
        spawnWorldId = config.getInt("spawnWorldId", category, 0, -2147483647, 2147483647,"Starting dimensionId. VisitFirstSystem checks are disable for this dimension");
        blacklistedDimension = config.get(category,"List of dimension without being able to warp there",blacklistedDimension).getIntList();
        unavailableWithoutPermissionWarpName = config.get(category,"List of warp names unavailable without operator permission", unavailableWithoutPermissionWarpName).getStringList();
        warpSafetyCheck = config.getBoolean("warpSafetyCheck", category, true, "Prevent teleporting if warp point on air or or haven't free space for player");

        category = "Tpa";
        config.addCustomCategoryComment(category,"Tpa settings");
        isVisitedFirstSystemToTpaEnabled = config.getBoolean("isVisitedFirstSystemToTpaEnabled", category, false, "Enable/disable require to visit player's dimension before being able to teleport");
        isSpecialPermissionToTpaNeeded = config.getBoolean("isSpecialPermissionToTpaNeeded", category, false,"Enable/disable require special permission to access for every DIM. Example fmwarps.accessdim.[DIM_ID]");
        isTpaCooldownSystemEnabled = config.getBoolean("isTpaCooldownSystemEnabled", category,false,"Enable/disable tpa cooldown");
        teleportRequestLifetime = config.getInt("teleportRequestLifetime",category, 1200,0,6000,"TeleportRequest lifetime in tick");
        canTpaToBlacklistedDimension = config.getBoolean("canTeleportToBlacklistedDimension", category, false, "Can telepot to player if he is in blacklisted dimension");
        tpaSafetyCheck = config.getBoolean("tpaSafetyCheck", category, true, "Teleportation is possible only then target player stay on ground");

        category = "TpaCooldown";
        config.addCustomCategoryComment(category,"Tpa cooldown in tick per player status");
        cooldownTpaUser = config.getInt("cooldownTpaUser", category, 0, 0, 2147483647, "Tpa cooldown in tick for user");
        cooldownTpaVip = config.getInt("cooldownTpaVip", category, 0, 0, 2147483647, "Tpa cooldown in tick for VIP");
        cooldownTpaPremium = config.getInt("cooldownTpaPremium", category, 0, 0, 2147483647, "Tpa cooldown in tick for Premium");
        cooldownTpaGrand = config.getInt("cooldownTpaGrand", category, 0, 0, 2147483647, "Tpa cooldown in tick for Grand");
        cooldownTpaSponsor = config.getInt("cooldownTpaSponsor", category, 0, 0, 2147483647, "Tpa cooldown in tick for Sponsor");
        cooldownTpaAdmin = config.getInt("cooldownTpaAdmin", category, 0, 0, 2147483647, "Tpa cooldown in tick for Admin");

        config.save();
    }

    public static void registerConfig(FMLPreInitializationEvent event){
        configFile = new File(String.valueOf(event.getModConfigurationDirectory()));
        init(new File(configFile.getPath(), FineWarps.MODID + ".cfg"));
    }
}
