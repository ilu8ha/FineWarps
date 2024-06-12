package com.ilu8ha.warps;

import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;


import java.text.SimpleDateFormat;
import java.util.*;

public final class Warp {
    private final String name;
    private final String owner_name;
    private final String owner_UUID;
    private int dimensionId;
    private double posX;
    private double posY;
    private double posZ;
    private float pitch;
    private float yaw;
    private boolean isPrivate;
    private final List<String> invitedPlayerUUID;
    private final Date dateOfCreation;
    private int visits;
    public Warp(String name, EntityPlayer owner){
        this.name = name;
        this.owner_name = owner.getName();
        this.owner_UUID = owner.getUniqueID().toString();
        this.dimensionId = owner.dimension;
        this.posX = owner.posX;
        this.posY = owner.posY;
        this.posZ = owner.posZ;
        this.pitch = owner.rotationPitch;
        this.yaw = owner.rotationYaw;
        this.isPrivate = true;
        this.invitedPlayerUUID = new ArrayList<>();
        this.visits = 0;
        this.dateOfCreation = Calendar.getInstance().getTime();
    }
    public String getName() {
        return name;
    }
    public String getOwner_name() {
        return owner_name;
    }
    public String getOwner_UUID() {
        return owner_UUID;
    }
    public int getDimensionId() { return dimensionId; }
    public double getPosX() {
        return posX;
    }
    public double getPosY() {
        return posY;
    }
    public Date getCreatedDate() { return dateOfCreation; }
    public int getVisits() { return visits; }
    public double getPosZ() {
        return posZ;
    }
    public boolean isPrivate() {
        return isPrivate;
    }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }

    public List<String> getInvitedPlayerUUID() {
        List<String> ret = new ArrayList<>();
        if(!invitedPlayerUUID.isEmpty()){
            ret.addAll(invitedPlayerUUID);
        }
        return ret;
    }
    public boolean addInvitedPlayer(String playerUUID){
        if(!invitedPlayerUUID.contains(playerUUID)){
            invitedPlayerUUID.add(playerUUID);
            return sync();
        }
        return false;
    }
    public boolean removeInvitedPlayer(String playerUUID){
        if(invitedPlayerUUID.contains(playerUUID)){
            invitedPlayerUUID.remove(playerUUID);
            return sync();
        }
        return false;
    }

    public boolean setPrivacy(boolean privacy){
        isPrivate = privacy;
        return sync();
    }

    private boolean sync(){
        if(DataFileManager.saveWarpsDataToFile()){
            return true;
        }
        else {
            DataFileManager.reloadWarpsData();
            return false;
        }
    }
    public String[] getInfo(){
        String[] ret = new String[7];
        ret[0] = TextFormatting.GREEN + "Warp info: " + TextFormatting.AQUA + this.name;
        ret[1] = TextFormatting.GREEN + "Owner: " + TextFormatting.AQUA + this.owner_name;
        ret[2] = TextFormatting.GREEN + "Dimension: " + TextFormatting.AQUA + "DIM" + this.dimensionId + TextFormatting.GREEN + " Position: " + TextFormatting.AQUA + "x: " + ((int) posX) + " y: " + ((int) posY) + " z: " + ((int) posZ);
        ret[3] = TextFormatting.GREEN + "Privacy: " + TextFormatting.AQUA;
        if(this.isPrivate) ret[3]+="Private";
        else ret[3]+="Public";
        ret[4] = TextFormatting.GREEN + "Date of creation: " + TextFormatting.AQUA + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(this.dateOfCreation);
        ret[5] = TextFormatting.GREEN + "Visits: " + TextFormatting.AQUA + this.visits;
        ret[6] = TextFormatting.GREEN + "Invited players: "+TextFormatting.RESET;
        try {
            for (String playerId : this.invitedPlayerUUID) {
                if (FineWarps.userNameCache.containsKey(playerId)) {
                    ret[6] += ("[" + TextFormatting.AQUA + FineWarps.userNameCache.get(playerId) + TextFormatting.RESET + "] ");
                } else {
                    throw new PlayerNotFoundException(String.format("User with uuid %s not found in user cache. This shouldn't have happened. Problem in warp %s", playerId, this.name), playerId);

                }
            }
        }catch (PlayerNotFoundException exception){
            FineWarps.logger.log(Level.ERROR, exception.getMessage());
            exception.printStackTrace();
            this.removeInvitedPlayer((String)exception.getErrorObjects()[0]);
            return getInfo();
        }
        return ret;
    }
    public boolean update(EntityPlayer player){
        this.posX = player.posX;
        this.posY = player.posY;
        this.posZ = player.posZ;
        this.yaw = player.rotationYaw;
        this.pitch = player.rotationPitch;
        this.dimensionId = player.dimension;

        return sync();
    }

    public void Visited(){
        this.visits++;
        if(!DataFileManager.saveWarpsDataToFile()){
            DataFileManager.reloadWarpsData();
        }
    }
}
