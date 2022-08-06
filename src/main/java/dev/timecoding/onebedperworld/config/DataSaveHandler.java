package dev.timecoding.onebedperworld.config;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataSaveHandler {

    private OneBedPerWorld plugin;

    public DataSaveHandler(OneBedPerWorld plugin){
        this.plugin = plugin;
    }

    private File f = null;
    private YamlConfiguration cfg = null;

    public void init(){
        f = new File("plugins//OneBedPerWorld", "data.yml");
        cfg = YamlConfiguration.loadConfiguration(f);
        cfg.options().copyDefaults(true);
        save();
    }

    public void save(){
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean bedLocationExists(Player player, World world){
        String wname = world.getName();
        String puuid = player.getUniqueId().toString();
        String base = "Players."+puuid+"."+wname;
        if(cfg.get(base) != null) {
            return true;
        }
        return false;
    }

    public void setBedLocation(Player player, Block bed){
        Location loc = bed.getLocation();
        String puuid = player.getUniqueId().toString();
        String base = "Players."+puuid+"."+bed.getWorld().getName();
        String locformat = loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ();
        cfg.set(base, locformat);
        save();
    }

    public Location readBedLocation(Player player, World world){
        String wname = world.getName();
        String puuid = player.getUniqueId().toString();
        String base = "Players."+puuid+"."+wname;
        if(bedLocationExists(player,world)){
            String read = getString(base);
            ArrayList<String> splitter = new ArrayList<String>(Arrays.asList(read.split(" ")));
            return new Location(Bukkit.getWorld(wname), Double.valueOf(splitter.get(0)), Double.valueOf(splitter.get(1)), Double.valueOf(splitter.get(2)));
        }
        return null;
    }

    public void deleteBedLocation(Player player, World world){
        if(bedLocationExists(player, world)){
            cfg.set("Players."+player.getUniqueId().toString()+"."+world.getName(), null);
            save();
        }
    }

    public List<String> readAllBedWorldNames(Player player){
        String uuid = player.getUniqueId().toString();
        List<String> list = new ArrayList<>();
        for(String value : cfg.getValues(true).keySet()){
            if(value.startsWith("Players."+uuid+".")){
                list.add(value.replace("Players."+uuid+".", ""));
            }
        }
        return list;
    }

    public List<World> readAllBedWorlds(Player player){
        List<World> list = new ArrayList<>();
        for(String name : readAllBedWorldNames(player)){
            list.add(worldNameToWorld(name));
        }
        return list;
    }

    public World worldNameToWorld(String name){
        return Bukkit.getWorld(name);
    }

    public boolean anchorLocationExists(Player player, World world){
        String wname = world.getName();
        String puuid = player.getUniqueId().toString();
        String base = "Anchors."+puuid+"."+wname;
        if(cfg.get(base) != null) {
            return true;
        }
        return false;
    }

    public void setAnchorLocation(Player player, Block bed){
        Location loc = bed.getLocation();
        String puuid = player.getUniqueId().toString();
        String base = "Anchors."+puuid+"."+bed.getWorld().getName();
        String locformat = loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ();
        cfg.set(base, locformat);
        save();
    }

    public void deleteAnchorLocation(Player player, World world){
        if(anchorLocationExists(player, world)){
            cfg.set("Anchors."+player.getUniqueId().toString()+"."+world.getName(), null);
            save();
        }
    }

    public Location readAnchorLocation(Player player, World world){
        String wname = world.getName();
        String puuid = player.getUniqueId().toString();
        String base = "Anchors."+puuid+"."+wname;
        if(anchorLocationExists(player,world)){
            String read = getString(base);
            ArrayList<String> splitter = new ArrayList<String>(Arrays.asList(read.split(" ")));
            return new Location(Bukkit.getWorld(wname), Double.valueOf(splitter.get(0)), Double.valueOf(splitter.get(1)), Double.valueOf(splitter.get(2)));
        }
        return null;
    }

    public List<String> readAllAnchorWorldNames(Player player){
        String uuid = player.getUniqueId().toString();
        List<String> list = new ArrayList<>();
        for(String value : cfg.getValues(true).keySet()){
            if(value.startsWith("Anchors."+uuid+".")){
                list.add(value.replace("Anchors."+uuid+".", ""));
            }
        }
        return list;
    }

    public List<World> readAllAnchorWorlds(Player player){
        List<World> list = new ArrayList<>();
        for(String name : readAllAnchorWorldNames(player)){
            list.add(worldNameToWorld(name));
        }
        return list;
    }

    public RespawnAnchor getRespawnAnchor(Location loc){
        if(loc.getBlock().getType() == Material.RESPAWN_ANCHOR){
            return (RespawnAnchor) loc.getBlock().getBlockData();
        }
        return null;
    }

    private String getString(String key){
        return cfg.getString(key);
    }

}
