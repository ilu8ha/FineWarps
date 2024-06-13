package com.ilu8ha.warps;

import com.ilu8ha.warps.config.ConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TeleportRequest {
    private final EntityPlayer playerRequester;
    private final EntityPlayer playerAcceptor;
    private final boolean toAcceptor;
    private int timer;
    public TeleportRequest(EntityPlayer playerAcceptor, EntityPlayer playerRequester, boolean toAcceptor){
        this.playerAcceptor = playerAcceptor;
        this.playerRequester = playerRequester;
        this.toAcceptor = toAcceptor;
        this.timer = ConfigHandler.teleportRequestLifetime;

        TextComponentString str = new TextComponentString(" request teleport ");
        str.getStyle().setItalic(true).setColor(TextFormatting.GOLD);
        TextComponentString playerPref = new TextComponentString(playerRequester.getName());
        playerPref.getStyle().setItalic(true).setColor(TextFormatting.AQUA);
        playerPref.appendSibling(str);
        TextComponentString suffixStr;
        if(toAcceptor){
            suffixStr = new TextComponentString("to you");

        }else {
            suffixStr = new TextComponentString("to them");
        }
        suffixStr.getStyle().setColor(TextFormatting.GOLD).setBold(true).setItalic(true);
        playerPref.appendSibling(suffixStr);

        playerAcceptor.sendMessage(playerPref);
        playerAcceptor.sendMessage(new TextComponentString(TextFormatting.GOLD + "Use [/tpa accept] or [/tpa deny]"));

        playerRequester.sendMessage(new TextComponentString(String.format(TextFormatting.GOLD + "Request to %s sent", playerAcceptor.getName())));
    }
    public EntityPlayer getPlayerRequester() {
        return playerRequester;
    }

    public EntityPlayer getPlayerAcceptor() {
        return playerAcceptor;
    }

    public boolean isToAcceptor() {
        return toAcceptor;
    }

    public int getTimer() {
        return timer;
    }

    public void tick(){
        timer--;
    }
    public void canceled(){
        this.canceled(playerRequester);
        this.canceled(playerAcceptor);
    }
    public void canceled(EntityPlayer player){
        if(playerRequester.equals(player)){
            playerAcceptor.sendMessage(new TextComponentString(String.format("%sTeleport request from %s%s%s no longer exists",
                    TextFormatting.GOLD,
                    TextFormatting.AQUA,
                    playerRequester.getName(),
                    TextFormatting.GOLD)));
        } else {
            playerRequester.sendMessage(new TextComponentString(String.format("%sTeleport request to %s%s%s no longer exists",
                    TextFormatting.GOLD,
                    TextFormatting.AQUA,
                    playerAcceptor.getName(),
                    TextFormatting.GOLD)));
        }
    }
    public void done(){
        getAffectedPlayer().sendMessage(new TextComponentString(String.format("%s Teleporting...", TextFormatting.GOLD)));
        getTargetPlayer().sendMessage(new TextComponentString(String.format("%s%s%s teleported to you",
                TextFormatting.AQUA,
                getAffectedPlayer().getName(),
                TextFormatting.GOLD)));
    }
    public EntityPlayer getAffectedPlayer(){
        return toAcceptor? playerRequester : playerAcceptor;
    }
    public EntityPlayer getTargetPlayer(){
        return toAcceptor? playerAcceptor : playerRequester;

    }
}
