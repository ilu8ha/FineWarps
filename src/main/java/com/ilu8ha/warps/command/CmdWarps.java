package com.ilu8ha.warps.command;

import com.ilu8ha.warps.FineWarps;
import com.ilu8ha.warps.permission.Permissions;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;

import java.util.List;

public class CmdWarps extends CmdBase implements ICommand{
    public CmdWarps(){
        super("warps", Permissions.warps,0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){
        try {
            List<String> warpList;
            String playerArg = null;
            Integer dimensionArg = null;
            if(args.length == 0){
                warpList = getAvailableWarpNames(sender, true);
            }else{
                Options options = new Options();

                Option dimension = new Option("d","Dimension");
                dimension.setArgs(1);
                dimension.setOptionalArg(false);
                options.addOption(dimension);

                Option playerName = new Option("p", "PlayerName");
                playerName.setArgs(1);
                playerName.setOptionalArg(false);
                options.addOption(playerName);

                CommandLineParser parser = new DefaultParser();
                CommandLine cmd = parser.parse(options, args);

                playerArg = cmd.getOptionValue("p");
                String dimensionArgStr = cmd.getOptionValue("d");
                if(dimensionArgStr!=null) dimensionArg = Integer.parseInt(dimensionArgStr);

                warpList = getAvailableWarpNames(sender, true, playerArg, dimensionArg);
            }

            if(warpList!=null && !warpList.isEmpty()){
                sender.sendMessage(new TextComponentString(String.format(TextFormatting.GREEN + "Total warp points found: %d", warpList.size())));
                for(String warpName : warpList){
                    sender.sendMessage(new TextComponentString(warpName));
                }
            } else if(warpList.isEmpty()){
                sender.sendMessage(new TextComponentString("No available warp points found"));
            } else {
                throw new CommandException("WarpList is not exist. This shouldn't have happened.");
            }
        } catch (CommandException | ParseException exception) {
            if(exception instanceof ParseException){
                sendSyntaxErrorResult(sender);
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + String.format(exception.getMessage())));
                FineWarps.logger.log(Level.ERROR, exception.getMessage(), ((CommandException) exception).getErrorObjects());
                exception.printStackTrace();
            }
        }
    }
}


