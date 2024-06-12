package com.ilu8ha.warps;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.Level;
import java.io.*;
import java.util.*;


public class DataFileManager {
    private static final File dataHomePath = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory().getPath() + "/playerdata/FMWarpsData");
    private static final File warpsDataFile = new File(dataHomePath.getPath() + "/FMWarps.json" );
    private static final File visitedDimensionDataPath = new File(dataHomePath.getPath() + "/VisitedDimension" );
    private static final File userNameCacheDataFile = new File(dataHomePath.getPath() + "/userNameCache.json");
    public static boolean loadWarpsDataFromFile(){
        if(isHomeDirectorySafe() && isWarpsDataFileSafe()){
            try(FileReader reader = new FileReader(warpsDataFile)){
                Gson gson = new Gson();
                Warp[] warps = gson.fromJson(reader, Warp[].class);
                if(warps!= null && warps.length>0){
                    FineWarps.warpsData.clear();
                    FineWarps.warpsData.addAll(Arrays.asList(warps));
                }
                return true;
            }catch (IOException exception){
                FineWarps.logger.log(Level.ERROR,"Something went wrong when reading file");
                exception.printStackTrace();
                return false;
            }
        }else {
            FineWarps.logger.log(Level.ERROR, "Can't read warpData from file cause filesystem was corrupted");
            return false;
        }
    }
    public static boolean saveWarpsDataToFile(){
        if(isHomeDirectorySafe() && isWarpsDataFileSafe()){
            try(FileWriter writer = new FileWriter(warpsDataFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(FineWarps.warpsData, writer);
                return true;
            }catch (IOException exception){
                FineWarps.logger.log(Level.ERROR,"Something went wrong when saving to file");
                exception.printStackTrace();
                return false;
            }
        }else {
            FineWarps.logger.log(Level.ERROR, "Can't save warpData to file cause filesystem was corrupted");
            return false;
        }
    }

    public static void reloadWarpsData(){
        loadWarpsDataFromFile();
    }

    public static boolean loadVisitedDimensionDataFromFile(UUID playerId){
        File dataFile = new File(visitedDimensionDataPath + "/" + playerId + ".json");
        if(isVisitedDimensionDataFileSafe(dataFile)){
            try(FileReader reader = new FileReader(dataFile)){
                Gson gson = new Gson();
                List <Integer> visitedDimensionDataValue = gson.fromJson(reader, List.class);
                if(visitedDimensionDataValue != null){
                    FineWarps.visitedDimensionData.remove(playerId.toString());
                    FineWarps.visitedDimensionData.put(playerId.toString(), visitedDimensionDataValue);
                    return true;
                } else{
                    FineWarps.visitedDimensionData.put(playerId.toString(), new ArrayList<>());
                    return false;
                }
            }
            catch (IOException exception){
                exception.printStackTrace();
                FineWarps.logger.log(Level.ERROR,"Something went wrong when reading file" + dataFile.getName());
                return false;
            }
        }else {
            FineWarps.logger.log(Level.ERROR, String.format("Can't read VisitedDimensionData for %s from file cause filesystem was corrupted", playerId));
            return false;
        }
    }
    public static boolean saveVisitedDimensionDataToFile(UUID playerId, List<Integer> visitedDimensionData){
        File dataFile = new File(visitedDimensionDataPath + "/" + playerId + ".json");
        if(isVisitedDimensionDataFileSafe(dataFile)){
            try(FileWriter writer = new FileWriter(dataFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(visitedDimensionData, writer);
                return true;
            }catch (IOException exception){
                FineWarps.logger.log(Level.ERROR,"Something went wrong when saving to file" + dataFile.getName());
                exception.printStackTrace();
                return false;
            }
        }else {
            FineWarps.logger.log(Level.ERROR,String.format("Can't save VisitedDimensionData for %s to file cause filesystem was corrupted", playerId));
            return false;
        }
    }
    public static boolean loadUserNameCacheDataFromFile(){
        if(isUserNameCacheDataFileSafe()){
            try(FileReader reader = new FileReader(userNameCacheDataFile)){
                Gson gson = new Gson();
                HashMap<String, String> userNameCacheData = gson.fromJson(reader,HashMap.class);
                if(userNameCacheData!= null && !userNameCacheData.isEmpty()) {
                    FineWarps.userNameCache.clear();
                    FineWarps.userNameCache.putAll(userNameCacheData);
                }
                return true;
            }catch (IOException exception) {
                exception.printStackTrace();
                FineWarps.logger.log(Level.ERROR,"Something went wrong when reading UserNameCacheData file");
                return false;
            }
        }else {
            FineWarps.logger.log(Level.ERROR, "Can't read UserNameCacheData from file cause filesystem was corrupted");
            return false;
        }
    }

    public static boolean saveUserNameCacheDataToFile(){
        if(isUserNameCacheDataFileSafe()){
            try(FileWriter writer = new FileWriter(userNameCacheDataFile)){
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(FineWarps.userNameCache,writer);
                return true;
            } catch (IOException exception) {
                FineWarps.logger.log(Level.ERROR,"Something went wrong when saving userNameCache to file");
                exception.printStackTrace();
                return false;
            }
        }else {
            FineWarps.logger.log(Level.ERROR, "Can't save UserNameCacheData to file cause filesystem was corrupted");
            return false;
        }
    }

    private static boolean isHomeDirectorySafe(){
        try {
            if (!dataHomePath.exists() && !dataHomePath.mkdirs()) {
                throw new IOException("Failed to create warp data directory");
            }
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }
    private static boolean isWarpsDataFileSafe(){
        try {
            if (!warpsDataFile.exists() && !warpsDataFile.createNewFile()) {
                throw new IOException("Failed to create warp data file");
            }
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private static boolean isVisitedDimensionDataDirectorySafe(){
        try {
            if (!isHomeDirectorySafe() || (!visitedDimensionDataPath.exists() && !visitedDimensionDataPath.mkdirs())){
                throw new IOException("Failed to create warp data directory");
            }
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private static boolean isVisitedDimensionDataFileSafe(File dataFile){
        try {

            if(!isVisitedDimensionDataDirectorySafe() || (!dataFile.exists() && !dataFile.createNewFile())){
                throw new IOException("Filed to create visitedDimensionData file " + dataFile.getName());
            }
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }
    private static boolean isUserNameCacheDataFileSafe(){
        try {
            if(!isHomeDirectorySafe() || (!userNameCacheDataFile.exists() && !userNameCacheDataFile.createNewFile())){
                throw new IOException("Failed to create userNameCacheData file");
            }
            return true;
        }catch (IOException exception){
            exception.printStackTrace();
            return false;
        }

    }}
