package com.ilu8ha.warps;

import com.ilu8ha.warps.command.CmdTpa;
import com.ilu8ha.warps.command.CmdWarp;
import com.ilu8ha.warps.command.CmdWarps;
import com.ilu8ha.warps.config.ConfigHandler;
import com.ilu8ha.warps.permission.Permissions;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

import java.util.*;


@Mod(modid = FineWarps.MODID, name = FineWarps.MODNAME, version = FineWarps.VERSION, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber(modid = FineWarps.MODID)
public class FineWarps {
    public static final String MODID = "fmwarps";
    public static final String MODNAME = "FineWarps";
    public static final String VERSION = "1.0";
    public static Logger logger;
    public static List<Warp> warpsData;
    public static HashMap<String, String > userNameCache;
    public static HashMap<String, List<Integer>> visitedDimensionData;
    public static HashMap<String, Integer> cooldownWarpUse;
    public static HashMap<String, Integer> cooldownTpaUse;
    public static List<TeleportRequest> teleportRequests;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ConfigHandler.registerConfig(event);
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Permissions.registerPermissions();
    }
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){

    }
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event){
        warpsData = new ArrayList<>();
        userNameCache = new HashMap<>();
        teleportRequests = new ArrayList<>();

        event.registerServerCommand(new CmdWarp());
        event.registerServerCommand(new CmdWarps());
        event.registerServerCommand(new CmdTpa());

        if(!DataFileManager.loadWarpsDataFromFile()){
            warpsData.clear();
        }
        if(!DataFileManager.loadUserNameCacheDataFromFile()){
            userNameCache.clear();
        }

        if(ConfigHandler.isVisitedFirstSystemEnabled || ConfigHandler.isVisitedFirstSystemToTpaEnabled) {
            visitedDimensionData = new HashMap<>();
        }
        if(ConfigHandler.isCooldownSystemEnabled){
            cooldownWarpUse = new HashMap<>();
        }
        if(ConfigHandler.isTpaCooldownSystemEnabled){
            cooldownTpaUse = new HashMap<>();
        }
    }
    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event){
        DataFileManager.saveWarpsDataToFile();
        warpsData.clear();
        userNameCache.clear();
        if(ConfigHandler.isVisitedFirstSystemEnabled || ConfigHandler.isVisitedFirstSystemToTpaEnabled){
            visitedDimensionData.clear();
        }
        if(ConfigHandler.isCooldownSystemEnabled){
            cooldownWarpUse.clear();
        }
        if(ConfigHandler.isTpaCooldownSystemEnabled){
            cooldownTpaUse.clear();
        }
    }
    public static boolean isDimensionInBlacklist(int dimId){
        for (int id : ConfigHandler.blacklistedDimension) {
            if (id == dimId) return true;
        }
        return false;
    }
}

