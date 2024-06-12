package com.ilu8ha.warps.command;

import com.ilu8ha.warps.cofh.core.util.helpers.EntityHelper;
import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.TeleportRequest;
import com.ilu8ha.warps.config.ConfigHandler;
import com.ilu8ha.warps.permission.Permissions;
import com.ilu8ha.warps.permission.TpaRequestCooldown;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
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
                throw new CommandException(String.format("Unexpected sender %s", sender.getName()),sender);
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
                    switch (args[1]){
                        case ("here"):
                            createNewTeleportationRequest(playerSender, args[0], false);
                            return;
                        default:
                            sendSyntaxErrorResult(sender);
                            return;
                    }
                default:
                    sendSyntaxErrorResult(sender);
            }

        }catch (CommandException exception){
            if(exception.getErrorObjects()[0] != null && exception.getErrorObjects()[0] instanceof TeleportRequest){
                TeleportRequest request = (TeleportRequest) exception.getErrorObjects()[0];
                request.getAffectedPlayer().sendMessage(new TextComponentString(TextFormatting.RED + exception.getMessage()));
                request.canceled();
                FineWarps.teleportRequests.remove(request);

            }else sender.sendMessage(new TextComponentString(TextFormatting.RED + exception.getMessage()));
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

    private void processRequest(EntityPlayer player, boolean accepted) throws CommandException{
        if(accepted){
            if(!hasPermission(player, Permissions.teleportRequestAccept)) throw new CommandException("You can't accept requests");
            TeleportRequest request = FineWarps.teleportRequests.stream()
                    .filter(i->i.getPlayerAcceptor().equals(player))
                    .findFirst().orElse(null);
            if(request == null) throw new CommandException("You haven't incoming teleportation request");
            EntityPlayer affectedPlayer, targetPlayer;
            affectedPlayer = request.getAffectedPlayer();
            targetPlayer = request.getTargetPlayer();
            checkTeleportRequestConditions(request);
            request.done();
            teleport(affectedPlayer, targetPlayer);
            FineWarps.teleportRequests.remove(request);
        }else{
            if(!hasPermission(player, Permissions.teleportRequestDeny)) throw new CommandException("You can't deny requests");
            TeleportRequest request = FineWarps.teleportRequests.stream()
                    .filter(tr-> tr.getPlayerRequester().equals(player) || tr.getPlayerAcceptor().equals(player))
                    .findFirst().orElse(null);
            if(request == null) throw new CommandException("You haven't teleportation request");
            request.canceled();
            FineWarps.teleportRequests.remove(request);
        }
    }
    private void checkTeleportRequestConditions(TeleportRequest request) throws CommandException{
        if(hasPermission(request.getAffectedPlayer(), Permissions.operator)){
            return;
        }
        if((ConfigHandler.isSpecialPermissionToTpaNeeded
                    && !hasPermission(request.getAffectedPlayer(), specialPermissionNode + request.getTargetPlayer().dimension)
                    && !hasPermission(request.getAffectedPlayer(), Permissions.operator))
                || (!ConfigHandler.canTpaToBlacklistedDimension
                    && FineWarps.isDimensionInBlacklist(request.getTargetPlayer().dimension)
                    && !hasPermission(request.getAffectedPlayer(), Permissions.operator))){
            throw new CommandException(String.format("You can't teleport to DIM%d", request.getTargetPlayer().dimension), request);
        }
        if(ConfigHandler.isVisitedFirstSystemToTpaEnabled
                && ConfigHandler.spawnWorldId != request.getTargetPlayer().dimension
                && !(FineWarps.visitedDimensionData.get(request.getAffectedPlayer().getUniqueID().toString()).contains(request.getTargetPlayer().dimension)
                && !hasPermission(request.getAffectedPlayer(), Permissions.operator))){
            throw new CommandException(String.format("You need visit dimension DIM%d first", request.getTargetPlayer().dimension), request);
        }
    }
    private void teleport(EntityPlayer affectedPlayer, EntityPlayer targetPlayer){
        if(affectedPlayer.dimension != targetPlayer.dimension){
            EntityHelper.transferPlayerToDimension((EntityPlayerMP)affectedPlayer, targetPlayer.dimension,((EntityPlayerMP) affectedPlayer).server.getPlayerList());
        }
        affectedPlayer.setPositionAndUpdate(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ);
    }

    private void createNewTeleportationRequest(EntityPlayer sender, String acceptorPlayerName, boolean toAcceptor) throws CommandException{
        EntityPlayer acceptorPlayer = checkTeleportSendConditionsAndReturnEntityPlayerAcceptor(sender,acceptorPlayerName);
        TeleportRequest request = new TeleportRequest(acceptorPlayer, sender, toAcceptor);
        FineWarps.teleportRequests.add(request);
        if(ConfigHandler.isTpaCooldownSystemEnabled && !hasPermission(sender, Permissions.operator)){
            FineWarps.cooldownTpaUse.put(sender.getUniqueID().toString(), getTpaCooldownTimerForPlayerStatus(sender));
        }
    }
    private EntityPlayer checkTeleportSendConditionsAndReturnEntityPlayerAcceptor(EntityPlayer player, String acceptorPlayerName) throws CommandException{
        if(!FineWarps.teleportRequests.isEmpty()
                && (FineWarps.teleportRequests.stream().anyMatch(tp -> tp.getPlayerRequester().equals(player) || tp.getPlayerAcceptor().equals(player)))){
            throw new CommandException("You already have an active request");
        }
        if(!hasPermission(player, Permissions.teleportRequestSend)) throw new CommandException("You can't send teleportation requests");

        if(ConfigHandler.isTpaCooldownSystemEnabled
                && (!hasPermission(player, Permissions.operator) && FineWarps.cooldownTpaUse.containsKey(player.getUniqueID().toString()))){
            int timer = FineWarps.cooldownTpaUse.get(player.getUniqueID().toString()) / 20;
            throw new CommandException(String.format("Requesting teleportation cooldown has not yet expired. Tty via %s s.",timer));
        }
        if(player.getName().equals(acceptorPlayerName)) throw new CommandException("You can't send request to yourself");
        if(Arrays.asList(FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames()).contains(acceptorPlayerName)){
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(acceptorPlayerName);
        }else {
            throw new CommandException(String.format("Player %s not found", acceptorPlayerName));
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

