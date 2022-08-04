package dev.timecoding.onebedperworld.command;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import dev.timecoding.onebedperworld.config.ConfigHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OBPWCommand implements CommandExecutor {

    private OneBedPerWorld plugin = OneBedPerWorld.getPlugin();
    private ConfigHandler handler = plugin.getConfigFile();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String wrongsyntax = getMessage("WrongSyntax").replace("%player%", sender.getName());
        if(args.length == 1){
            String args1 = args[0];
            if(args1.equalsIgnoreCase("reload")){
                if(sender.hasPermission(handler.getConfig().getString("Commands.reload.Permission"))) {
                    handler.reload();
                    sender.sendMessage(handler.getConfig().getString("Commands.reload.Message").replace("%player%", sender.getName()).replace("&", "§"));
                }else{
                    sender.sendMessage(handler.getConfig().getString("Messages.NoPermission").replace("&","§"));
                }
            }else if(args1.equalsIgnoreCase("help")){
                if(sender.hasPermission(handler.getConfig().getString("Commands.help.Permission"))) {
                    handler.reload();
                    sender.sendMessage(handler.getConfig().getString("Commands.help.Message").replace("%player%", sender.getName()).replace("&", "§"));
                }else{
                    sender.sendMessage(handler.getConfig().getString("Messages.NoPermission").replace("&","§"));
                }
            }else{
                if(sender.hasPermission(handler.getConfig().getString("Commands.help.Permission"))) {
                    sender.sendMessage(wrongsyntax.replace("%syntax%", "/obpw help"));
                }else{
                    sender.sendMessage(handler.getConfig().getString("Messages.NoPermission").replace("&","§"));
                }
            }
        }else{
            if(sender.hasPermission(handler.getConfig().getString("Commands.help.Permission"))) {
                sender.sendMessage(wrongsyntax.replace("%syntax%", "/obpw help"));
            }else{
                sender.sendMessage(handler.getConfig().getString("Messages.NoPermission").replace("&","§"));
            }
        }
        return false;
    }

    public String getMessage(String key){
        return handler.getConfig().getString("Messages."+key).replace("&", "§");
    }
}
