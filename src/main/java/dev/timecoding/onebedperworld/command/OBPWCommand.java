package dev.timecoding.onebedperworld.command;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import dev.timecoding.onebedperworld.config.ConfigHandler;
import dev.timecoding.onebedperworld.config.DataSaveHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OBPWCommand implements CommandExecutor {

    private OneBedPerWorld plugin = OneBedPerWorld.getPlugin();
    private ConfigHandler handler = plugin.getConfigFile();
    private DataSaveHandler data = plugin.getDataSaveHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String wrongsyntax = getMessage("WrongSyntax").replace("%player%", sender.getName());
        if(args.length == 1){
            String args1 = args[0];
            if(args1.equalsIgnoreCase("reload") || args1.equalsIgnoreCase("rl")){
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
        }else if(args.length == 4) {
            String sync = args[0];
            String pname = args[1];
            String type = args[2];
            String world = args[3];
            if(sync.equalsIgnoreCase("deletecache") || sync.equalsIgnoreCase("delcache")){
                if(sender.hasPermission(handler.getConfig().getString("Commands.deletecache.Permission"))) {
                    if(Bukkit.getOfflinePlayer(pname).getPlayer() != null) {
                        String uuid = Bukkit.getOfflinePlayer(pname).getUniqueId().toString();
                        Player p = Bukkit.getOfflinePlayer(pname).getPlayer();
                        if(type.equalsIgnoreCase("bed") || type.equalsIgnoreCase("anchor")){
                            if(Bukkit.getWorld(world) != null){
                                if(type.equalsIgnoreCase("bed")){
                                    if(data.bedLocationExists(p, Bukkit.getWorld(world))){
                                        data.deleteBedLocation(p, Bukkit.getWorld(world));
                                        sender.sendMessage(handler.getConfig().getString("Commands.deletecache.Message").replace("%player%", sender.getName()).replace("%type%", type.toUpperCase()).replace("%world%", world).replace("&", "§"));
                                    }else{
                                        sender.sendMessage(ChatColor.RED+"No Bed-Data found for this world!");
                                    }
                                }else if(type.equalsIgnoreCase("anchor")){
                                    if(data.anchorLocationExists(p, Bukkit.getWorld(world))){
                                        data.deleteAnchorLocation(p, Bukkit.getWorld(world));
                                        sender.sendMessage(handler.getConfig().getString("Commands.deletecache.Message").replace("%player%", sender.getName()).replace("%type%", type.toUpperCase()).replace("%world%", world).replace("&", "§"));
                                    }else{
                                        sender.sendMessage(ChatColor.RED+"No Anchor-Data found for this world!");
                                    }
                                }
                            }else{
                                sender.sendMessage(ChatColor.RED+"This world does not exists!");
                            }
                        }else{
                            sender.sendMessage(ChatColor.RED+"Please use the type BED or ANCHOR!");
                        }
                    }else{
                        sender.sendMessage(ChatColor.RED+"This player does not exists!");
                    }
                }else{
                    sender.sendMessage(handler.getConfig().getString("Messages.NoPermission").replace("&","§"));
                }
            }else{
                sender.sendMessage(wrongsyntax.replace("%syntax%", "/obpw deletecache <Player> <Type> <World>"));
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
