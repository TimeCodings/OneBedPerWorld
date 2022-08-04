package dev.timecoding.onebedperworld.config;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigHandler {

    private OneBedPerWorld plugin;

    public ConfigHandler(OneBedPerWorld plugin){
        this.plugin = plugin;
    }

    private File f = null;
    public YamlConfiguration cfg = null;

    public void init(){
        plugin.saveDefaultConfig();
        f = new File("plugins//OneBedPerWorld", "config.yml");
        cfg = YamlConfiguration.loadConfiguration(f);
        cfg.options().copyDefaults(true);
    }

    public void save(){
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload(){
        cfg = YamlConfiguration.loadConfiguration(f);
    }

    public YamlConfiguration getConfig(){
        return cfg;
    }

    public void overwrite(String key, Object value){
        cfg.set(key, value);
        save();
    }

    public boolean keyExists(String key){
        if(cfg.get(key) != null){
            return true;
        }
        return false;
    }

}
