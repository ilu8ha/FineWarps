package com.ilu8ha.warps.command;

import com.ilu8ha.warps.cofh.core.util.helpers.EntityHelper;
import com.ilu8ha.warps.DataFileManager;
import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.Warp;
import com.ilu8ha.warps.config.ConfigHandler;
import com.ilu8ha.warps.permission.MaxWarpCount;
import com.ilu8ha.warps.permission.Permissions;
import com.ilu8ha.warps.permission.WarpUseCooldown;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CmdWarp extends CmdBase implements ICommand {
    public CmdWarp(){
        super("finewarps:warp",Permissions.warp,0);
    }
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        String warpNameParam , actionParam, playerParam;
        Warp warp;
        EntityPlayer playerSender;
        switch (args.length){
            case (0):
                sendHelpResult(sender);
                return;
            case (1):
                if(args[0].equals("help")){
                    sendHelpResult(sender);
                }else {
                    warpNameParam = args[0];
                    if(!(sender instanceof EntityPlayer)){
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only players can use it"));
                        return;
                    }
                    playerSender = (EntityPlayer) sender;
                    warp = getWarpIfExistOrGetNull(warpNameParam);
                    if(canTeleport(playerSender, warp)){
                        teleportPlayerToWarp(playerSender,warp);
                    }
                }
                return;
            case (2):
                warpNameParam = args[0];
                actionParam = args[1];
                warp = getWarpIfExistOrGetNull(warpNameParam);
                switch (actionParam){
                    case ("set"):
                        if(warp != null){
                            sender.sendMessage(new TextComponentString(String.format(TextFormatting.RED + "Warp with name %s already exist", warpNameParam)));
                            return;
                        }
                        if(!(sender instanceof EntityPlayer)){
                            sender.sendMessage(new TextComponentString("Only players can use this"));
                            return;
                        }
                        playerSender = (EntityPlayer) sender;
                        if(canCreateNewWarp(playerSender, warpNameParam)){
                            warp = new Warp(warpNameParam, playerSender);
                            FineWarps.warpsData.add(warp);
                            if(DataFileManager.saveWarpsDataToFile()){
                                playerSender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("You successfully created new warp point %s",warpNameParam)));
                            }else {
                                DataFileManager.reloadWarpsData();
                                playerSender.sendMessage(new TextComponentString(TextFormatting.RED + "Something went wrong!"));
                            }
                        }
                        return;
                    case ("remove"):
                        if(warp == null){
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format(TextFormatting.RED + "Warp with name %s doesn't exist", warpNameParam)));
                            return;
                        }
                        if(canInteractWithWarpInAction(sender, warp, Permissions.warpRemove)){
                            FineWarps.warpsData.remove(warp);
                            if(DataFileManager.saveWarpsDataToFile()){
                                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("You successfully remove warp point %s",warpNameParam)));
                            } else {
                                DataFileManager.reloadWarpsData();
                                sender.sendMessage(new TextComponentString(String.format(TextFormatting.RED + "Something went wrong!")));
                            }
                        }
                        return;
                    case ("public"):
                        if(warp == null){
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Warp with name %s doesn't exist", warpNameParam)));
                            return;
                        }
                        if(canInteractWithWarpInAction(sender, warp, Permissions.warpPrivacy)){
                            if(warp.isPrivate()){
                                if(warp.setPrivacy(false)){
                                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Warp %s is now Public", warpNameParam)));
                                }else {
                                    sender.sendMessage(new TextComponentString(String.format(TextFormatting.RED + "Something went wrong!")));
                                }
                            }else {
                                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Warp %s is already Public",warpNameParam)));
                            }

                        }
                        return;
                    case ("private"):
                        if(warp == null){
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Warp with name %s doesn't exist", warpNameParam)));
                            return;
                        }
                        if(canInteractWithWarpInAction(sender, warp, Permissions.warpPrivacy)){
                            if(!warp.isPrivate()){
                                if(warp.setPrivacy(true)){
                                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Warp %s is now Private", warpNameParam)));
                                }else {
                                    sender.sendMessage(new TextComponentString(String.format(TextFormatting.RED + "Something went wrong!")));
                                }
                            }else {
                                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Warp %s is already Private",warpNameParam)));
                            }

                        }
                        return;
                    case ("update"):
                        if(warp == null){
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Warp with name %s doesn't exist", warpNameParam)));
                            return;
                        }
                        if(!(sender instanceof EntityPlayer)){
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only player can use this"));
                            return;
                        }
                        playerSender = (EntityPlayer) sender;
                        if(canInteractWithWarpInAction(sender,warp,Permissions.warpUpdate)){
                            if(warp.update(playerSender)){
                                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Warp %s position update",warpNameParam)));
                            }else {
                                sender.sendMessage(new TextComponentString(String.format(TextFormatting.RED + "Something went wrong!")));
                            }
                        }
                        return;
                    case ("info"):
                        if(warp == null){
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Warp with name %s doesn't exist", warpNameParam)));
                            return;
                        }
                        if(canViewWarpInfo(sender,warp)){
                            String[]warpInfo = warp.getInfo();
                            for(String warpInfoStr : warpInfo){
                                sender.sendMessage(new TextComponentString(warpInfoStr));
                            }
                        }
                        return;
                    default:
                        sendSyntaxErrorResult(sender);
                        return;

                }
            case (3):
                warpNameParam = args[0];
                actionParam = args[1];
                playerParam = args[2];
                warp = getWarpIfExistOrGetNull(warpNameParam);
                if(warp == null){
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format(TextFormatting.RED + "Warp with name %s doesn't exist", warpNameParam)));
                    return;
                }
                switch (actionParam){
                    case ("invite"):
                        updateInvitedList(sender,warp, playerParam, true);
                        return;
                    case ("uninvite"):
                        updateInvitedList(sender,warp, playerParam, false);
                        return;
                    default:
                        sendSyntaxErrorResult(sender);
                        break;
                }
                break;
            default:
                sendSyntaxErrorResult(sender);
        }
    }

    private Warp getWarpIfExistOrGetNull(String warpName){
        return FineWarps.warpsData.stream().filter(i -> i.getName().equals(warpName)).findFirst().orElse(null);
    }

    private boolean canTeleport(EntityPlayer player, @Nullable Warp warp){
        if(warp == null){
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Warp doesn't exist"));
            return false;
        }
        if(hasPermission(player, Permissions.operator)){
            return true;
        }
        if(ConfigHandler.isCooldownSystemEnabled && FineWarps.cooldownWarpUse.containsKey(player.getUniqueID().toString())){
            int time = FineWarps.cooldownWarpUse.get(player.getUniqueID().toString()) / 20;
            player.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Warp cooldown has not yet expired. Try via %s.S",time)));
            return false;
        }
        if((ConfigHandler.isSpecialPermissionNeeded && !hasPermission(player, specialPermissionNode +warp.getDimensionId()))
                || FineWarps.isDimensionInBlacklist(warp.getDimensionId())){
            player.sendMessage(new TextComponentString(TextFormatting.RED + String.format("You can't warp to dimension DIM%s", warp.getDimensionId())));
            return false;
        }
        if(ConfigHandler.isVisitedFirstSystemEnabled
                && ConfigHandler.spawnWorldId != warp.getDimensionId()
                && !(FineWarps.visitedDimensionData.get(player.getUniqueID().toString()).contains(warp.getDimensionId()))){
            player.sendMessage(new TextComponentString(TextFormatting.RED + String.format("You need visit dimension DIM%s first", warp.getDimensionId())));
            return false;
        }
        if(!warp.getOwner_UUID().equals(player.getUniqueID().toString())
                && warp.isPrivate()
                && !warp.getInvitedPlayerUUID().contains(player.getUniqueID().toString())){
            player.sendMessage(new TextComponentString(TextFormatting.RED + String.format("You haven't access to warp %s",warp.getName())));
            return false;
        }
        if(player.dimension != warp.getDimensionId() &&!DimensionManager.isDimensionRegistered(warp.getDimensionId())) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Dimension %s is unreachable",warp.getDimensionId())));
            return false;
        }
        if(ConfigHandler.warpSafetyCheck){
            BlockPos targetPos = new BlockPos(warp.getPosX(),warp.getPosY(), warp.getPosZ());
            World targetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(warp.getDimensionId());
            Chunk targetChunk = targetWorld.getChunk(targetPos);
            if(!isWarpPositionSafely(targetChunk,targetPos,targetWorld)){
                player.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Warp %s position is unsafe", warp.getName())));
                return false;
            }
        }

        return true;
    }

    private boolean canCreateNewWarp(EntityPlayer player, String warpName){
        if(hasPermission(player, Permissions.operator)){
            return true;
        }
        if(!hasPermission(player, Permissions.warpSet)){
            player.sendMessage(new TextComponentString(TextFormatting.RED + "You can't create new warp"));
            return false;
        }
        if(Arrays.asList(ConfigHandler.unavailableWithoutPermissionWarpName).contains(warpName)){
            player.sendMessage(new TextComponentString(TextFormatting.RED + "You can't create new warp with this name"));
            return false;
        }
        if(!player.onGround){
            player.sendMessage(new TextComponentString(TextFormatting.RED + "You can't create new warp while flying"));
            return false;
        }
        if(!hasUnusedWarp(player)){
            player.sendMessage(new TextComponentString(TextFormatting.RED + "You haven't unused warp point"));
            return false;
        }
        if(FineWarps.isDimensionInBlacklist(player.dimension)){
            player.sendMessage(new TextComponentString(TextFormatting.RED + "You can't create warp in this dimension"));
            return false;
        }
        if(ConfigHandler.warpSafetyCheck){
            World targetWorld = player.world;
            BlockPos targetPos = player.getPosition();
            Chunk targetChunk = targetWorld.getChunk(targetPos);
            if(!isWarpPositionSafely(targetChunk, targetPos, targetWorld)){
                player.sendMessage(new TextComponentString(TextFormatting.RED + "You cannot create a warp cause position is unsafe. Warp point must be placed on a solid block with two block empty space upside"));
                return false;
            }

        }
        return true;
    }

    private int getMaxWarpCountForPlayerStatus(EntityPlayer player){
        if(hasPermission(player, MaxWarpCount.USER.getPermission())){
            return ConfigHandler.maxWarpCountUser;
        }else if(hasPermission(player, MaxWarpCount.VIP.getPermission())){
            return ConfigHandler.maxWarpCountVip;
        } else if (hasPermission(player, MaxWarpCount.PREMIUM.getPermission())) {
            return ConfigHandler.maxWarpCountPremium;
        } else if (hasPermission(player, MaxWarpCount.GRAND.getPermission())) {
            return ConfigHandler.maxWarpCountGrand;
        } else if (hasPermission(player,MaxWarpCount.SPONSOR.getPermission())) {
            return ConfigHandler.maxWarpCountSponsor;
        } else if (hasPermission(player, MaxWarpCount.ADMIN.getPermission())) {
            return ConfigHandler.maxWarpCountAdmin;
        } else return ConfigHandler.maxWarpCountUser;
    }
    private boolean hasUnusedWarp(EntityPlayer player){
        int playerMaxWarpCount = getMaxWarpCountForPlayerStatus(player);
        int playerWarpCount;
        if(playerMaxWarpCount>0){
            playerWarpCount = (int) FineWarps.warpsData.stream().filter(i->i.getOwner_UUID().equals(player.getUniqueID().toString())).count();
            return playerWarpCount - playerMaxWarpCount < 0;
        }
        return false;
    }

    private int getCooldownTimerForPlayerStatus(EntityPlayer player) {
        if(hasPermission(player, WarpUseCooldown.USER.getPermission())){
            return ConfigHandler.cooldownWarpUser;
        }else if(hasPermission(player,WarpUseCooldown.VIP.getPermission())){
            return ConfigHandler.cooldownWarpVip;
        } else if (hasPermission(player, WarpUseCooldown.PREMIUM.getPermission())) {
            return ConfigHandler.cooldownWarpPremium;
        } else if (hasPermission(player, WarpUseCooldown.GRAND.getPermission())) {
            return ConfigHandler.cooldownWarpGrand;
        } else if(hasPermission(player, WarpUseCooldown.SPONSOR.getPermission())){
            return ConfigHandler.cooldownWarpSponsor;
        } else if (hasPermission(player, WarpUseCooldown.ADMIN.getPermission())) {
            return ConfigHandler.cooldownWarpAdmin;
        } else return ConfigHandler.cooldownWarpUser;
    }

    private void teleportPlayerToWarp(EntityPlayer player, Warp warp){
        if(player.dimension != warp.getDimensionId()) {
            EntityHelper.transferPlayerToDimension((EntityPlayerMP) player, warp.getDimensionId(), ((EntityPlayerMP) player).server.getPlayerList());
        }
        player.rotationYaw = warp.getYaw();
        player.rotationPitch = warp.getPitch();
        player.fallDistance = 0;
        player.setPositionAndUpdate(warp.getPosX(),warp.getPosY() + 0.5D,warp.getPosZ());
        warp.Visited();
        if(ConfigHandler.isCooldownSystemEnabled && !hasPermission(player, Permissions.operator)){
            FineWarps.cooldownWarpUse.putIfAbsent(player.getUniqueID().toString(), getCooldownTimerForPlayerStatus(player));
        }
    }

    private boolean canInteractWithWarpInAction(ICommandSender sender, Warp warp, String actionPermission){
        if(hasPermission(sender, Permissions.operator)){
            return true;
        }
        if(!hasPermission(sender, actionPermission) || !(sender instanceof EntityPlayer)){
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "You can't interact with warps "));
            return false;
        }

        if(!((EntityPlayer) sender).getUniqueID().toString().equals(warp.getOwner_UUID())){
            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("You can't interact with warp %s", warp.getName())));
            return false;
        }
        return true;
    }
    private boolean canViewWarpInfo(ICommandSender sender, Warp warp){
        if(hasPermission(sender,Permissions.operator)){
            return true;
        }
        if(!hasPermission(sender, Permissions.warpInfo) || !(sender instanceof EntityPlayer)){
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "You can't view warps info"));
            return false;
        }
        if(warp.isPrivate()
                && !((EntityPlayer) sender).getUniqueID().toString().equals(warp.getOwner_UUID())
                && !(warp.getInvitedPlayerUUID().contains(((EntityPlayer) sender).getUniqueID().toString()))){
            sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("You can't interact with warp %s", warp.getName())));
            return false;
        }
        return true;
    }

    private void updateInvitedList(ICommandSender sender,Warp warp, String playerName, boolean invite){
        if(canInteractWithWarpInAction(sender, warp, Permissions.warpInvitation)){
            String playerOtherUUID;
            boolean playerOnline = false;
            List<String> invitedList = warp.getInvitedPlayerUUID();
            if(sender.getName().equals(playerName)) {
                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are owner of this warp"));
                return;
            }
            if(sender.getServer().getPlayerList().getPlayerByUsername("playerName") == null){
                playerOtherUUID = getPlayerUUIDFromNameFromUserCacheOrGetNull(playerName);
                if(playerOtherUUID == null){
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format("Player %s doesn't exist", playerName)));
                    return;
                }
            }else {
                playerOtherUUID = sender.getServer().getPlayerList().getPlayerByUsername(playerName).getUniqueID().toString();
                playerOnline = true;
            }
            if(invite){
                if(invitedList.contains(playerOtherUUID)){
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Player %s already invited to %s", playerName, warp.getName())));
                }else if(warp.addInvitedPlayer(playerOtherUUID)){
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Player %s successfully invited to %s", playerName, warp.getName())));
                    if(playerOnline) sender.getServer().getPlayerList()
                            .getPlayerByUUID(UUID.fromString(playerOtherUUID))
                            .sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("You have been invited to warp %s!", warp.getName())));
                }else{
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Something went wrong!"));
                }
            }else {
                if(!invitedList.contains(playerOtherUUID)){
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Player %s not invited to %s",playerName,warp.getName())));
                }else if(warp.removeInvitedPlayer(playerOtherUUID)){
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("Player %s successfully uninvited from %s", playerName, warp.getName())));
                    if(playerOnline) sender.getServer().getPlayerList()
                            .getPlayerByUUID(UUID.fromString(playerOtherUUID))
                            .sendMessage(new TextComponentString(TextFormatting.GREEN + String.format("You are not longer invited to warp %s :(", warp.getName())));
                }else {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Something went wrong!"));
                }
            }
        }
    }

    private String getPlayerUUIDFromNameFromUserCacheOrGetNull(String playerName){
        for(Map.Entry<String, String> entry: FineWarps.userNameCache.entrySet()){
            if(entry.getValue().equals(playerName)){
                return entry.getKey();

            }
        }
        return null;
    }
    private boolean isWarpPositionSafely(Chunk targetChunk, BlockPos targetPos, World targetWorld){
        return ((targetChunk.getBlockState(targetPos.down(1)).isSideSolid(targetWorld, targetPos.down(1), EnumFacing.UP))
                && !(targetChunk.getBlockState(targetPos).getMaterial().blocksMovement())
                && !(targetChunk.getBlockState(targetPos.up(1)).getMaterial().blocksMovement()));
    }
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos var4){
        String actionParam, playerParam, warpParam;
        Warp warp;
        switch (args.length){
            case (1):
                warpParam = args[0];
                try {
                    return getAvailableWarpNames(sender,false).stream().filter(i -> i.toLowerCase().startsWith(warpParam.toLowerCase())).collect(Collectors.toList());
                } catch (Exception e){
                    return  Collections.emptyList();
                }

            case (2):
                warpParam = args[0];
                actionParam = args[1];
                warp = getWarpIfExistOrGetNull(warpParam);
                if(warp != null && !warpParam.equals("help") && warp.getOwner_name().equals(sender.getName())) {
                    return new ArrayList<>(Arrays.asList("update","remove","public","private","info","invite","uninvite"))
                            .stream().filter(i->i.toLowerCase().startsWith(actionParam.toLowerCase()))
                            .collect(Collectors.toList());
                } else if (warp == null && !warpParam.equals("help")) {
                    return new ArrayList<>(Arrays.asList("set"));
                }else if(warp!=null && !warpParam.equals("help")){
                    return new ArrayList<>(Arrays.asList("info"));
                }else{
                    return Collections.emptyList();
                }

            case (3):
                warpParam = args[0];
                actionParam = args[1];
                playerParam = args[2];
                if(actionParam.equals("invite")){
                    return Arrays.stream(server.getOnlinePlayerNames())
                            .filter(i->i.toLowerCase().startsWith(playerParam.toLowerCase()) && !i.equals(sender.getName()))
                            .collect(Collectors.toList());
                }
                else if(actionParam.equals("uninvite")){
                    warp = getWarpIfExistOrGetNull(warpParam);
                    if(warp != null){
                        return warp.getInvitedPlayerUUID().stream()
                                .map(i-> FineWarps.userNameCache.get(i))
                                .filter(i->i.toLowerCase().startsWith(playerParam.toLowerCase()))
                                .collect(Collectors.toList());
                    }
                }
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("warp");
    }
}
