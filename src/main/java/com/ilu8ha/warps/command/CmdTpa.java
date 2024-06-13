package com.ilu8ha.warps.command;

import com.ilu8ha.warps.cofh.core.util.helpers.EntityHelper;
import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.TeleportRequest;
import com.ilu8ha.warps.command.exception.TpaException;
import com.ilu8ha.warps.config.ConfigHandler;
import com.ilu8ha.warps.permission.Permissions;
import com.ilu8ha.warps.permission.TpaRequestCooldown;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class CmdTpa extends CmdBase implements ICommand {
    public CmdTpa() {super("tpa", Permissions.teleportRequest, 0);}
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){
        try {
            if(!(sender instanceof EntityPlayer)){
                throw new TpaException(String.format("Unexpected sender %s", sender.getName()),sender,null);
            }
            EntityPlayer playerSender = (EntityPlayer) sender;
            switch (args.length){
                case (1):
                    switch (args[0]){
                        case ("accept"):
                            processRequest(playerSender, true);
                            return;
                        case ("deny"):
                            processRequest(playerSender, false);
                            return;
                        default:
                            createNewTeleportationRequest(playerSender, args[0], true);
                            return;
                    }
                case (2):
                    if(args[1].equals("here")){
                        createNewTeleportationRequest(playerSender, args[0], false);
                        return;
                    }
                    sendSyntaxErrorResult(sender);
                    return;
                default:
                    sendSyntaxErrorResult(sender);
            }

        }catch (TpaException exception){
            exception.getMessageReceiver().sendMessage(new TextComponentString(TextFormatting.RED + exception.getMessage()));
            if(exception.getTeleportRequest() != null){
                exception.getTeleportRequest().canceled();
                FineWarps.teleportRequests.remove(exception.getTeleportRequest());
            }
        }


    }
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos var4){
        if(!(sender instanceof EntityPlayer)) return Collections.emptyList();
        EntityPlayer playerSender = (EntityPlayer) sender;

        switch (args.length){
            case (1):
                TeleportRequest request = FineWarps.teleportRequests.stream()
                        .filter(i->i.getPlayerAcceptor().equals(playerSender)
                                || i.getPlayerRequester().equals(playerSender))
                        .findAny().orElse(null);
                if(request != null){
                    if(playerSender.equals(request.getPlayerAcceptor())){
                        return new ArrayList<>(Arrays.asList("accept", "deny")).stream()
                                .filter(i->i.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList());
                    } else return new ArrayList<>(Arrays.asList("deny"));
                }else {
                    return Arrays.stream(server.getOnlinePlayerNames())
                            .filter(i->i.toLowerCase().startsWith(args[0].toLowerCase()) && !i.equals(sender.getName()))
                            .collect(Collectors.toList());
                }
            case (2):
                if(!args[0].equals("accept") && !args[0].equals("deny")){
                    return new ArrayList<>(Arrays.asList("here"));
                }
                else return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }

    private void processRequest(EntityPlayer player, boolean accepted) throws TpaException{
        if(accepted){
            if(!hasPermission(player, Permissions.teleportRequestAccept)) throw new TpaException("You can't accept requests", player, null);
            TeleportRequest request = FineWarps.teleportRequests.stream()
                    .filter(i->i.getPlayerAcceptor().equals(player))
                    .findFirst().orElse(null);
            if(request == null) throw new TpaException("You haven't incoming teleportation request", player, null);
            checkTeleportRequestConditions(request);
            teleport(request);
            request.done();
            FineWarps.teleportRequests.remove(request);
        }else{
            if(!hasPermission(player, Permissions.teleportRequestDeny)) throw new TpaException("You can't deny requests", player, null);
            TeleportRequest request = FineWarps.teleportRequests.stream()
                    .filter(tr-> tr.getPlayerRequester().equals(player) || tr.getPlayerAcceptor().equals(player))
                    .findFirst().orElse(null);
            if(request == null) throw new TpaException("You haven't teleportation request", player, null);
            request.canceled();
            FineWarps.teleportRequests.remove(request);
        }
    }
    private void checkTeleportRequestConditions(TeleportRequest request) throws TpaException{
        if(hasPermission(request.getAffectedPlayer(), Permissions.operator)){
            return;
        }
        if((ConfigHandler.isSpecialPermissionToTpaNeeded
                    && !hasPermission(request.getAffectedPlayer(), specialPermissionNode + request.getTargetPlayer().dimension))
                || (!ConfigHandler.canTpaToBlacklistedDimension
                    && FineWarps.isDimensionInBlacklist(request.getTargetPlayer().dimension))){
            throw new TpaException(String.format("You can't teleport to DIM%d", request.getTargetPlayer().dimension), request.getAffectedPlayer(), request);
        }
        if(ConfigHandler.isVisitedFirstSystemToTpaEnabled
                && ConfigHandler.spawnWorldId != request.getTargetPlayer().dimension
                && !(FineWarps.visitedDimensionData.get(request.getAffectedPlayer().getUniqueID().toString()).contains(request.getTargetPlayer().dimension)
                && !hasPermission(request.getAffectedPlayer(), Permissions.operator))){
            throw new TpaException(String.format("You need visit dimension DIM%d first", request.getTargetPlayer().dimension), request.getAffectedPlayer(), request);
        }
        if(ConfigHandler.tpaSafetyCheck){
            if(request.isToAcceptor()){
                if(!request.getPlayerAcceptor().onGround){
                    throw new TpaException("You must stay on ground", request.getPlayerAcceptor(), null);
                }
            }else {
                if(!request.getPlayerRequester().onGround){
                    throw new TpaException("You must stay on ground", request.getPlayerRequester(), request);
                }
            }
        }
    }
    private void teleport(TeleportRequest request) {
        EntityPlayer targetPlayer = request.getTargetPlayer();
        EntityPlayer affectedPlayer = request.getAffectedPlayer();
        if(affectedPlayer.dimension != targetPlayer.dimension){
            EntityHelper.transferPlayerToDimension((EntityPlayerMP)affectedPlayer, targetPlayer.dimension,((EntityPlayerMP) affectedPlayer).server.getPlayerList());
        }
        affectedPlayer.fallDistance = 0;
        affectedPlayer.setPositionAndUpdate(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ);
    }


    private void createNewTeleportationRequest(EntityPlayer sender, String acceptorPlayerName, boolean toAcceptor) throws TpaException{
        EntityPlayer acceptorPlayer = checkTeleportSendConditionsAndReturnEntityPlayerAcceptor(sender,acceptorPlayerName);
        TeleportRequest request = new TeleportRequest(acceptorPlayer, sender, toAcceptor);
        FineWarps.teleportRequests.add(request);
        if(ConfigHandler.isTpaCooldownSystemEnabled && !hasPermission(sender, Permissions.operator)){
            FineWarps.cooldownTpaUse.put(sender.getUniqueID().toString(), getTpaCooldownTimerForPlayerStatus(sender));
        }
    }
    private EntityPlayer checkTeleportSendConditionsAndReturnEntityPlayerAcceptor(EntityPlayer player, String acceptorPlayerName) throws TpaException{
        if(!FineWarps.teleportRequests.isEmpty()
                && (FineWarps.teleportRequests.stream().anyMatch(tp -> tp.getPlayerRequester().equals(player) || tp.getPlayerAcceptor().equals(player)))){
            throw new TpaException("You already have an active request", player, null);
        }
        if(!hasPermission(player, Permissions.teleportRequestSend)) throw new TpaException("You can't send teleportation requests", player, null);
        if(ConfigHandler.isTpaCooldownSystemEnabled
                && (!hasPermission(player, Permissions.operator) && FineWarps.cooldownTpaUse.containsKey(player.getUniqueID().toString()))){
            int timer = FineWarps.cooldownTpaUse.get(player.getUniqueID().toString()) / 20;
            throw new TpaException(String.format("Requesting teleportation cooldown has not yet expired. Tty via %s s.",timer), player, null);
        }
        if(player.getName().equals(acceptorPlayerName)) throw new TpaException("You can't send request to yourself", player,null);
        if(Arrays.asList(FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames()).contains(acceptorPlayerName)){
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(acceptorPlayerName);
        }else {
            throw new TpaException(String.format("Player %s not found", acceptorPlayerName), player, null);
        }
    }

    private int getTpaCooldownTimerForPlayerStatus(EntityPlayer player){
        if(hasPermission(player, TpaRequestCooldown.USER.getPermission())){
            return ConfigHandler.cooldownTpaUser;
        }else if(hasPermission(player,TpaRequestCooldown.VIP.getPermission())){
            return ConfigHandler.cooldownTpaVip;
        } else if (hasPermission(player, TpaRequestCooldown.PREMIUM.getPermission())) {
            return ConfigHandler.cooldownTpaPremium;
        } else if (hasPermission(player, TpaRequestCooldown.GRAND.getPermission())) {
            return ConfigHandler.cooldownTpaGrand;
        } else if(hasPermission(player, TpaRequestCooldown.SPONSOR.getPermission())){
            return ConfigHandler.cooldownTpaSponsor;
        } else if (hasPermission(player, TpaRequestCooldown.ADMIN.getPermission())) {
            return ConfigHandler.cooldownWarpAdmin;
        } else return ConfigHandler.cooldownTpaUser;
    }
}

