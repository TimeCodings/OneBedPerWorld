package dev.timecoding.onebedperworld;

import dev.timecoding.onebedperworld.api.AutoUpdater;
import dev.timecoding.onebedperworld.api.Metrics;
import dev.timecoding.onebedperworld.command.OBPWCommand;
import dev.timecoding.onebedperworld.command.OBPWCompleter;
import dev.timecoding.onebedperworld.config.ConfigHandler;
import dev.timecoding.onebedperworld.config.DataSaveHandler;
import dev.timecoding.onebedperworld.listener.BedListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBedPerWorld extends JavaPlugin {

    private static OneBedPerWorld plugin;
    private DataSaveHandler dataSaveHandler = new DataSaveHandler(this);
    private ConfigHandler configHandler = new ConfigHandler(this);

    private AutoUpdater updater = null;

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"OneBedPerWorld "+ChatColor.GREEN+"up and running "+ChatColor.RED+"v"+getDescription().getVersion());
        configHandler.init();
        dataSaveHandler.init();
        plugin = this;
        getServer().getPluginManager().registerEvents(new BedListener(), plugin);
        getCommand("onebedperworld").setExecutor(new OBPWCommand());
        getCommand("onebedperworld").setTabCompleter(new OBPWCompleter());

        updater = new AutoUpdater(this);
        updater.checkForPluginUpdate();
        updater.checkForConfigUpdate();

        if(configHandler.getConfig().getBoolean("bStats")) {
            Metrics bstats = new Metrics(this, 16015);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"OneBedPerWorld "+ChatColor.RED+"got disabled! ("+ChatColor.RED+"v"+getDescription().getVersion()+")");
    }

    public void setConfigHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    public static OneBedPerWorld getPlugin() {
        return plugin;
    }

    public DataSaveHandler getDataSaveHandler() {
        return dataSaveHandler;
    }

    public ConfigHandler getConfigFile() {
        return configHandler;
    }
}
