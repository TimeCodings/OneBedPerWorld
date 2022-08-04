package dev.timecoding.onebedperworld.command;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import dev.timecoding.onebedperworld.config.ConfigHandler;
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
        if(command.getName().equalsIgnoreCase("obpw") || command.getName().equalsIgnoreCase("onebedperworld")){
            if(sender.hasPermission(handler.getConfig().getString("Commands.reload.Permission"))){
                completer.add("reload");
            }
            if(sender.hasPermission(handler.getConfig().getString("Commands.help.Permission"))){
                completer.add("help");
            }
        }
        return completer;
    }
}
