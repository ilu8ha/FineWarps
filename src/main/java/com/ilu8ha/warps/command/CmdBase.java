package com.ilu8ha.warps.command;

import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.Warp;
import com.ilu8ha.warps.permission.Permissions;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CmdBase extends CommandBase implements ICommand{
    private final String name;
    private final String permission;
    public final int level;
    protected final String specialPermissionNode = "fmwarps.accessdim.";
    public CmdBase(String name, String permission, int level){
        this.level = level;
        this.name = name;
        this.permission = permission;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return TextFormatting.GREEN + "Use /warp help for details";
    }
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
        return hasPermission(sender, this.permission);
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings){

    }

    @Override
    public int getRequiredPermissionLevel() {return level;}



    protected boolean hasPermission(ICommandSender sender, String permissionNode){
        try {
            if(sender.getCommandSenderEntity() instanceof EntityPlayer){
                return PermissionAPI.hasPermission((CommandBase.getCommandSenderAsPlayer(sender)), permissionNode);
            }else return sender instanceof MinecraftServer;
        }catch (PlayerNotFoundException exception){
            return false;
        }
    }
    protected void sendHelpResult(ICommandSender sender){
        TextComponentString usage = new TextComponentString("FineWarp usage:");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setBold(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/warp [warp_name]");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/warp [warp_name] <set/remove/public/private/update/info>");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/warp [warp_name] <invite/uninvite> <player>");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/warps - list of available warps");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/warps [params]");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("  -p - player");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("  -d - dimension");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/tpa [player]");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/tpa [player] <here>");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);

        usage = new TextComponentString("/tpa <accept/deny>");
        usage.getStyle().setColor(TextFormatting.GRAY);
        usage.getStyle().setItalic(true);
        sender.sendMessage(usage);
    }
    protected void sendSyntaxErrorResult(ICommandSender sender){
        sender.sendMessage(new TextComponentString(TextFormatting.RED +"Syntax error"));
        sender.sendMessage(new TextComponentString(getUsage(sender)));
    }
    protected List<String> getAvailableWarpNames(ICommandSender sender,boolean longNameFormat, @Nullable String ownerName, @Nullable Integer dimId) throws CommandException{
        Stream<Warp> ret = FineWarps.warpsData.stream();
        if(ownerName != null && FineWarps.userNameCache.containsValue(ownerName)){
            ret = ret.filter(warp -> warp.getOwner_name().equals(ownerName));
        } else if (ownerName != null) {
            throw new CommandException(String.format("Player %s doesn't exist", ownerName), ownerName);
        }
        if(dimId != null && DimensionManager.isDimensionRegistered(dimId)){
            ret = ret.filter(warp -> warp.getDimensionId() == dimId);
        } else if(dimId != null) {
            throw new CommandException(String.format("Dimension %s doesn't exist",dimId), dimId);
        }
        if(hasPermission(sender, Permissions.operator)){
            if(longNameFormat){
                return ret.map(warp -> formatWarpName(warp,sender)).collect(Collectors.toList());
            }
            else {
                return ret.map(Warp::getName).collect(Collectors.toList());
            }

        }else if(sender instanceof EntityPlayer){
            if(ownerName == null){
                ret = ret.filter(warp ->
                        !warp.isPrivate()
                        || (warp.getOwner_UUID().equals(((EntityPlayer) sender).getUniqueID().toString()))
                        || (warp.getInvitedPlayerUUID().contains(((EntityPlayer) sender).getUniqueID().toString())));
            }else if (!ownerName.equals(sender.getName())) {
                ret = ret.filter(warp ->
                        !warp.isPrivate()
                        || (warp.getInvitedPlayerUUID().contains(((EntityPlayer) sender).getUniqueID().toString())));
            }
            if(longNameFormat){
                return ret.map(warp -> formatWarpName(warp,sender)).collect(Collectors.toList());
            }else {
                return ret.map(Warp::getName).collect(Collectors.toList());
            }
        } else {
            throw new CommandException(String.format("Unexpected sender %s", sender.getName()), sender);
        }
    }
    protected List<String> getAvailableWarpNames(ICommandSender sender, boolean longNameFormat) throws CommandException{
        return getAvailableWarpNames(sender, longNameFormat,null,null);
    }
    private String formatWarpName(Warp warp, ICommandSender sender){
        TextFormatting stringColor = TextFormatting.WHITE;
        if(sender instanceof EntityPlayer){
            String playerUUID = ((EntityPlayer)sender).getUniqueID().toString();
            if(warp.getOwner_UUID().equals(playerUUID)){
                stringColor = TextFormatting.DARK_GREEN;
            }else if(warp.getInvitedPlayerUUID().contains(playerUUID)){
                stringColor = TextFormatting.DARK_AQUA;
            }
            else if (!warp.isPrivate()){
                stringColor = TextFormatting.BLUE;
            }
        }
        return String.format("%s%s [%d, %d, %d : DIM%d] %s%s",
                stringColor,
                warp.getName(),
                (int) warp.getPosX(),
                (int) warp.getPosY(),
                (int) warp.getPosZ(),
                warp.getDimensionId(),
                warp.getOwner_name().equals(sender.getName()) ? "You" : warp.getOwner_name(),
                TextFormatting.RESET);
    }
}
