package com.ilu8ha.warps;

import com.ilu8ha.warps.config.ConfigHandler;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = FineWarps.MODID)
public class EventListener {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        if(ConfigHandler.isCooldownSystemEnabled && !FineWarps.cooldownWarpUse.isEmpty()){
            Iterator<Map.Entry<String, Integer>> iterator = FineWarps.cooldownWarpUse.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Integer> entry = iterator.next();
                if(entry.getValue() > 0){
                    entry.setValue(entry.getValue() - 1);
                }else iterator.remove();
            }
        }
        if(ConfigHandler.isTpaCooldownSystemEnabled && !FineWarps.cooldownTpaUse.isEmpty()){
            Iterator<Map.Entry<String, Integer>> iterator = FineWarps.cooldownTpaUse.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Integer> entry = iterator.next();
                if(entry.getValue() > 0){
                    entry.setValue(entry.getValue() - 1);
                }else iterator.remove();
            }
        }
        if(!FineWarps.teleportRequests.isEmpty()){
            Iterator<TeleportRequest> iterator = FineWarps.teleportRequests.iterator();
            while (iterator.hasNext()){
                TeleportRequest tr = iterator.next();
                if(tr.getTimer()>0){
                    tr.tick();
                } else {
                    tr.canceled();
                    iterator.remove();
                }
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        if(ConfigHandler.isVisitedFirstSystemEnabled || ConfigHandler.isVisitedFirstSystemToTpaEnabled){
            int dimId = event.player.getEntityWorld().provider.getDimension();
            UUID playerId = event.player.getUniqueID();
            if(dimId != ConfigHandler.spawnWorldId && !FineWarps.isDimensionInBlacklist(dimId)) {
                List<Integer> visitedDimensionList = FineWarps.visitedDimensionData.get(playerId.toString());
                if(!visitedDimensionList.contains(dimId)){
                    visitedDimensionList.add(dimId);
                    event.player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Now you can teleport at this dimension"));
                    DataFileManager.saveVisitedDimensionDataToFile(playerId,visitedDimensionList);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        if(ConfigHandler.isVisitedFirstSystemEnabled || ConfigHandler.isVisitedFirstSystemToTpaEnabled){
            DataFileManager.loadVisitedDimensionDataFromFile(event.player.getUniqueID());
        }
        if(!FineWarps.userNameCache.containsKey(event.player.getUniqueID().toString())
                ||!FineWarps.userNameCache.get(event.player.getUniqueID().toString()).equals(event.player.getName())) {
            FineWarps.userNameCache.put(event.player.getUniqueID().toString(), event.player.getName());
            if(!DataFileManager.saveUserNameCacheDataToFile()){
                DataFileManager.loadUserNameCacheDataFromFile();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(ConfigHandler.isVisitedFirstSystemEnabled || ConfigHandler.isVisitedFirstSystemToTpaEnabled){
            FineWarps.visitedDimensionData.remove(event.player.getUniqueID().toString());
        }
        if(!FineWarps.teleportRequests.isEmpty()){
            List<TeleportRequest> requests = FineWarps.teleportRequests.stream()
                    .filter(i->(i.getPlayerAcceptor().equals(event.player))
                            || (i.getPlayerRequester().equals(event.player)))
                    .collect(Collectors.toList());
            if(!requests.isEmpty()){
                requests.forEach(tr->tr.canceled(event.player));
                FineWarps.teleportRequests.removeAll(requests);
            }
        }
    }
}
