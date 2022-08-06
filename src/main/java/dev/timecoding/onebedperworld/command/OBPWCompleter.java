package dev.timecoding.onebedperworld.command;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import dev.timecoding.onebedperworld.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class OBPWCompleter implements TabCompleter {
    private OneBedPerWorld plugin = OneBedPerWorld.getPlugin();
    private ConfigHandler handler = plugin.getConfigFile();
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completer = new ArrayList<>();
        String delperm = handler.getConfig().getString("Commands.deletecache.Permission");
        if(command.getName().equalsIgnoreCase("obpw") || command.getName().equalsIgnoreCase("onebedperworld")){
            if(args.length == 1) {
                if (sender.hasPermission(handler.getConfig().getString("Commands.reload.Permission"))) {
                    completer.add("reload");
                }
                if (sender.hasPermission(handler.getConfig().getString("Commands.help.Permission"))) {
                    completer.add("help");
                }
                if (sender.hasPermission(handler.getConfig().getString("Commands.deletecache.Permission"))) {
                    completer.add("deletecache");
                }
            }
            if(args.length == 3){
                if(sender.hasPermission(delperm) && args[0].equalsIgnoreCase("deletecache") || sender.hasPermission(delperm) && args[0].equalsIgnoreCase("delcache")){
                    completer.add("BED");
                    completer.add("ANCHOR");
                }
            }
            if(args.length == 4){
                if(sender.hasPermission(delperm) && args[0].equalsIgnoreCase("deletecache") || sender.hasPermission(delperm) && args[0].equalsIgnoreCase("delcache")){
                    for(World w : Bukkit.getWorlds()){
                        completer.add(w.getName());
                    }
                }
            }
        }
        return completer;
    }
}
