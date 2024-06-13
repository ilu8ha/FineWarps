package com.ilu8ha.warps.command.exception;

import com.ilu8ha.warps.TeleportRequest;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class TpaException extends Exception{
    @Nullable
    private TeleportRequest teleportRequest;

    private ICommandSender messageReceiver;

    private boolean needCanceledRequest;


    public TpaException(String message, boolean needCanceledRequest, ICommandSender messageReceiver, @Nullable TeleportRequest teleportRequest){
        super(message);
        this.needCanceledRequest = needCanceledRequest;
        this.messageReceiver = messageReceiver;
        this.teleportRequest = teleportRequest;
    }

    @Nullable
    public TeleportRequest getTeleportRequest() {
        return teleportRequest;
    }

    public ICommandSender getMessageReceiver() {
        return messageReceiver;
    }

    public boolean isNeedCanceledRequest() {
        return needCanceledRequest;
    }
}
