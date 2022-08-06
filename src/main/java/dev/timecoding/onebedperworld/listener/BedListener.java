package dev.timecoding.onebedperworld.listener;

import dev.timecoding.onebedperworld.OneBedPerWorld;
import dev.timecoding.onebedperworld.config.ConfigHandler;
import dev.timecoding.onebedperworld.config.DataSaveHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;
import java.util.List;

public class BedListener implements Listener {

    private OneBedPerWorld plugin = OneBedPerWorld.getPlugin();
    private DataSaveHandler handler = plugin.getDataSaveHandler();
    private ConfigHandler cfg = plugin.getConfigFile();
    private boolean enabled = cfg.getConfig().getBoolean("Enabled");

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        Player p = e.getPlayer();

        boolean enabled = cfg.getConfig().getBoolean("TeleportToSpawn.Enabled");
        List<String> worlds = cfg.getConfig().getStringList("TeleportToSpawn.Worlds");
        if(enabled){
            if(worlds.contains(p.getWorld().getName())){
                World currentworld = p.getWorld();

                int x = 0;
                if(cfg.getConfig().get("TeleportToSpawn.Modify."+currentworld.getName()+".X") != null){
                    x = cfg.getConfig().getInt("TeleportToSpawn.Modify."+currentworld.getName()+".X");
                }
                int y = 0;
                if(cfg.getConfig().get("TeleportToSpawn.Modify."+currentworld.getName()+".Y") != null){
                    y = cfg.getConfig().getInt("TeleportToSpawn.Modify."+currentworld.getName()+".Y");
                }
                int z = 0;
                if(cfg.getConfig().get("TeleportToSpawn.Modify."+currentworld.getName()+".Z") != null){
                    z = cfg.getConfig().getInt("TeleportToSpawn.Modify."+currentworld.getName()+".Z");
                }

                p.teleport(new Location(currentworld.getSpawnLocation().getWorld(), currentworld.getSpawnLocation().getX()+x, currentworld.getSpawnLocation().getY()+y, currentworld.getSpawnLocation().getZ()+z));
            }
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e){
        List<String> validresults = cfg.getConfig().getStringList("ValidResults");
        Location loc = e.getBed().getLocation();
        Player p = e.getPlayer();
        World w = p.getWorld();
        if(enabled) {
            boolean triggerrespawnsystem = cfg.getConfig().getBoolean("DisableBedExplosions.TriggerRespawnSystem");
            boolean bedexplosionenabled = cfg.getConfig().getBoolean("DisableBedExplosions.Enabled");
            List<String> bedexlist = cfg.getConfig().getStringList("DisableBedExplosions.Worlds");
            if(bedexplosionenabled && bedexlist.contains(loc.getWorld().getName())){
                e.setUseBed(Event.Result.ALLOW);
            }
            if (!onBlacklist(loc.getWorld().getName())) {
                for (String s : validresults) {
                    if (e.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.valueOf(s) || bedexlist.contains(loc.getWorld().getName()) && triggerrespawnsystem) {
                        p.sendMessage(getMessage("SpawnPointSet").replace("%world%", w.getName()).replace("%player%", p.getName()).replace("%coordinates%", loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                        handler.setBedLocation(p, e.getBed());
                    }
                }
            }else{
                p.sendMessage(getMessage("SleepBlacklist").replace("%world%", w.getName()).replace("%player%", p.getName()));
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (enabled) {
            Player p = e.getPlayer();
            World w = p.getWorld();

            boolean respawnIfDestroyed = cfg.getConfig().getBoolean("RespawnIfDestroyed");
            boolean triggerevent = cfg.getConfig().getBoolean("BlacklistedWorlds.TriggerEvent");
            boolean blockdefaultrespawn = cfg.getConfig().getBoolean("BedNotExists.BlockDefaultRespawn");

            boolean priority = cfg.getConfig().getBoolean("RespawnAnchor.Priority");
            boolean samecoordinate = cfg.getConfig().getBoolean("BedNotExists.SameCoordinate");

            boolean fallbackworldenabled = cfg.getConfig().getBoolean("BedNotExists.RespawnWorld.Enabled");
            boolean onlyiffailed = cfg.getConfig().getBoolean("BedNotExists.RespawnWorld.OnlyIfTTRFailed");
            String respawnworld = cfg.getConfig().getString("BedNotExists.RespawnWorld.World");

            boolean prefernether = cfg.getConfig().getBoolean("RespawnAnchor.PreferNether");

            boolean anchorenabled = cfg.getConfig().getBoolean("RespawnAnchor.Enabled");

            boolean anchorok = false;

            Location filoc = null;
            if(handler.anchorLocationExists(p, w)) {
                if(prefernether){
                    for(String aw : handler.readAllAnchorWorldNames(p)){
                        if(Bukkit.getWorld(aw) != null && Bukkit.getWorld(aw).getEnvironment().equals(World.Environment.NETHER)){
                            Location nloc = handler.readAnchorLocation(p, handler.worldNameToWorld(aw));
                            if(nloc != null && nloc.getBlock().getType().equals(Material.RESPAWN_ANCHOR)){
                                filoc = nloc;
                                anchorok = true;
                            }
                        }
                    }
                }
                Location loc = null;
                if(!prefernether || !anchorok) {
                    filoc = handler.readAnchorLocation(p, w);
                    if (filoc != null && filoc.getBlock().getType().equals(Material.RESPAWN_ANCHOR)) {
                        RespawnAnchor anchor = handler.getRespawnAnchor(filoc);
                        if (anchor.getCharges() >= 1) {
                            anchor.setCharges((anchor.getCharges()-1));
                            Block b = filoc.getBlock();
                            b.setBlockData((BlockData) anchor);
                            filoc = respawnToNext(filoc);
                            anchorok = true;
                        }
                    }
                }
            }


            Location loc = null;
            if(!priority || priority && !anchorok) {
                if (!onBlacklist(w.getName())) {
                    if (handler.readAllBedWorldNames(p).size() != 0) {
                        if (handler.bedLocationExists(p, w)) {
                            loc = handler.readBedLocation(p, w);
                            if (!loc.getBlock().getType().toString().endsWith("BED") && !respawnIfDestroyed) {
                                p.sendMessage(getMessage("BedDestroyed").replace("%world%", w.getName()).replace("%player%", p.getName()).replace("%coordinates%", loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                                loc = performBedNotExists(p);
                                if (loc != null) {
                                    e.setRespawnLocation(loc);
                                    if (onlyiffailed && handler.readBedLocation(p, loc.getWorld()) == null && fallbackworldenabled || !onlyiffailed && fallbackworldenabled || onlyiffailed && handler.readBedLocation(p, loc.getWorld()).equals(loc) && fallbackworldenabled) {
                                        p.sendMessage(getMessage("WorldRespawnPointFound").replace("%world%", loc.getWorld().getName()).replace("%player%", p.getName()));
                                    } else {
                                        p.sendMessage(getMessage("SpareBedFound").replace("%world%", loc.getWorld().getName()).replace("%player%", p.getName()));
                                    }
                                } else if (blockdefaultrespawn) {
                                    e.setRespawnLocation(p.getLocation());
                                }
                            } else {
                                loc = respawnToNext(loc);
                                e.setRespawnLocation(loc);
                                p.sendMessage(getMessage("Respawn").replace("%world%", w.getName()).replace("%player%", p.getName()).replace("%coordinates%", loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                            }
                        } else {
                            if (Bukkit.getWorld(respawnworld) != null && fallbackworldenabled) {
                                p.sendMessage(getMessage("WorldRespawnPointFound").replace("%world%", w.getName()).replace("%player%", p.getName()));
                                e.setRespawnLocation(Bukkit.getWorld(respawnworld).getSpawnLocation());
                            } else {
                                p.sendMessage(getMessage("BedInWorldNotExists").replace("%world%", w.getName()).replace("%player%", p.getName()));
                                if (samecoordinate) {
                                    e.setRespawnLocation(p.getLocation());
                                }
                            }
                        }
                    } else {
                        if (Bukkit.getWorld(respawnworld) != null && fallbackworldenabled) {
                            p.sendMessage(getMessage("WorldRespawnPointFound").replace("%world%", w.getName()).replace("%player%", p.getName()));
                            e.setRespawnLocation(Bukkit.getWorld(respawnworld).getSpawnLocation());
                        } else {
                            p.sendMessage(getMessage("NoBedExists").replace("%player%", p.getName()));
                            if (samecoordinate) {
                                e.setRespawnLocation(p.getLocation());
                            }
                        }
                    }
                } else {
                    p.sendMessage(getMessage("OnBlacklist").replace("%world%", w.getName()).replace("%player%", p.getName()));
                    loc = performBedNotExists(p);
                    if (loc != null) {
                        e.setRespawnLocation(loc);
                        if (onlyiffailed && handler.readBedLocation(p, loc.getWorld()) == null || !onlyiffailed || onlyiffailed && handler.readBedLocation(p, loc.getWorld()).equals(loc)) {
                            p.sendMessage(getMessage("WorldRespawnPointFound").replace("%world%", loc.getWorld().getName()).replace("%player%", p.getName()));
                        } else {
                            p.sendMessage(getMessage("SpareBedFound").replace("%world%", loc.getWorld().getName()).replace("%player%", p.getName()));
                        }
                    } else if (blockdefaultrespawn) {
                        e.setRespawnLocation(p.getLocation());
                    }
                }
            }
            if(loc != null && !priority){
                e.setRespawnLocation(loc);
            }else if(anchorok && priority){
                e.setRespawnLocation(filoc);
            }else if(loc == null && anchorok){
                e.setRespawnLocation(filoc);
            }else if(!anchorok && priority && loc != null){
                e.setRespawnLocation(loc);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        World w = p.getWorld();
        Block b = e.getBlockPlaced();
        if(b.getType() == Material.RESPAWN_ANCHOR) {
            boolean anchorenabled = cfg.getConfig().getBoolean("RespawnAnchor.Enabled");
            boolean naturally = cfg.getConfig().getBoolean("RespawnAnchor.Naturally");
            String anchorcancelmessage = getMessage("CantUseRA").replace("%player%", p.getName()).replace("%world%", w.getName());
            if (anchorenabled) {
                boolean inside = cfg.getConfig().getBoolean("RespawnAnchor.InsideNether");
                boolean outside = cfg.getConfig().getBoolean("RespawnAnchor.OutsideNether.Enabled");
                if(!inside && w.getEnvironment().equals(World.Environment.NETHER)){
                    e.setCancelled(true);
                }
                List<String> list = cfg.getConfig().getStringList("RespawnAnchor.OutsideNether.Worlds");
                if(outside){
                    if(!list.contains(w.getName())){
                        if(inside && !w.getEnvironment().equals(World.Environment.NETHER)) {
                            e.setCancelled(true);
                        }
                    }
                }else if(list.contains(w.getName())){
                    e.setCancelled(true);
                }

                if(e.isCancelled() && !naturally){
                    p.sendMessage(anchorcancelmessage);
                }else if(e.isCancelled() && naturally){
                    e.setCancelled(false);
                }
            } else if(!naturally){
                e.setCancelled(true);
                p.sendMessage(anchorcancelmessage);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        Action a = e.getAction();
        World w = p.getWorld();
        boolean inside = cfg.getConfig().getBoolean("RespawnAnchor.InsideNether");
        boolean outside = cfg.getConfig().getBoolean("RespawnAnchor.OutsideNether.Enabled");
        if(a.equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType() == Material.RESPAWN_ANCHOR){
            int charge = handler.getRespawnAnchor(e.getClickedBlock().getLocation()).getCharges();
            boolean anchorenabled = cfg.getConfig().getBoolean("RespawnAnchor.Enabled");
            boolean outsideenabled = cfg.getConfig().getBoolean("RespawnAnchor.OutsideNether.Enabled");

            List<String> worlds = cfg.getConfig().getStringList("RespawnAnchor.OutsideNether.Worlds");
            if(!inside && w.getEnvironment().equals(World.Environment.NETHER)){
                e.setCancelled(true);
            }
            List<String> list = cfg.getConfig().getStringList("RespawnAnchor.OutsideNether.Worlds");
            if(outside){
                if(!list.contains(w.getName())){
                    if(inside && !w.getEnvironment().equals(World.Environment.NETHER)) {
                        e.setCancelled(true);
                    }
                }
            }else if(list.contains(w.getName())){
                e.setCancelled(true);
            }
            if(anchorenabled && charge == 4) {
                boolean b = false;
                Location loc = e.getClickedBlock().getLocation();
                Block block = loc.getBlock();
                if(outsideenabled && worlds.contains(w.getName())) {
                    e.setCancelled(true);
                    b = true;
                }else if(inside && w.getEnvironment().equals(World.Environment.NETHER)) {
                    b = true;
                }else if(!outsideenabled && !worlds.contains(w.getName())){
                    b = true;
                }
                if(b){
                    p.sendMessage(getMessage("SpawnPointSet").replace("%world%", w.getName()).replace("%player%", p.getName()).replace("%coordinates%", loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    handler.setAnchorLocation(p, e.getClickedBlock().getLocation().getBlock());
                }
            }
        }
    }

    public boolean onBlacklist(String worldname){
        boolean toggle = cfg.getConfig().getBoolean("BlacklistedWorlds.ToWhitelist");
        boolean enabled = cfg.getConfig().getBoolean("BlacklistedWorlds.Enabled");
        if(enabled) {
            if (cfg.getConfig().getStringList("BlacklistedWorlds.Worlds").contains(worldname) && !toggle
                    || !cfg.getConfig().getStringList("BlacklistedWorlds.Worlds").contains(worldname) && toggle) {
                return true;
            }
        }
        return false;
    }

    public Location performBedNotExists(Player p) {
        boolean blockdefaultrespawn = cfg.getConfig().getBoolean("BedNotExists.BlockDefaultRespawn");
        boolean trytorespawn = cfg.getConfig().getBoolean("BedNotExists.TryToRespawn.Enabled");
        List<String> tryworlds = cfg.getConfig().getStringList("BedNotExists.TryToRespawn.Worlds");
        boolean respawnIfDestroyed = cfg.getConfig().getBoolean("RespawnIfDestroyed");
        boolean allworlds = cfg.getConfig().getBoolean("BedNotExists.TryToRespawn.AllWorlds");
        boolean onlyiffailed = cfg.getConfig().getBoolean("BedNotExists.RespawnWorld.OnlyIfTTRFailed");
        String respawnworld = cfg.getConfig().getString("BedNotExists.RespawnWorld.World");
        if (blockdefaultrespawn) {
            boolean commandenabled = cfg.getConfig().getBoolean("BedNotExists.Command.Enabled");
            if(commandenabled){
                boolean consolecommandenabbled = cfg.getConfig().getBoolean("BedNotExists.Command.Console");
                String commandtext = cfg.getConfig().getString("BedNotExists.Command.Text").replace("/", "").replace("%player%", p.getName());
                if(consolecommandenabbled){
                    Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), commandtext);
                }else{
                    p.performCommand(commandtext);
                }
            }
            Location loc = null;
            boolean found = false;
            if (trytorespawn) {
                    World first = null;
                    List<String> getter = null;
                    if(allworlds){
                        getter = handler.readAllBedWorldNames(p);
                    }else{
                        getter = tryworlds;
                    }
                    for (String w : getter) {
                        if (Bukkit.getWorld(w) != null && !found) {
                            loc = handler.readBedLocation(p, handler.worldNameToWorld(w));
                            if(loc != null) {
                                if (loc.getBlock().getType().toString().endsWith("BED") || !loc.getBlock().getType().toString().endsWith("BED") && respawnIfDestroyed) {
                                    loc = respawnToNext(loc);
                                    found = true;
                                } else {
                                    loc = null;
                                }
                            }
                        }
                    }
                    if(!found){
                        p.sendMessage(getMessage("NoOtherWorldBedFound").replace("%player%", p.getName()));
                    }
            }
            if(onlyiffailed && !found || !onlyiffailed){
                World newworld = Bukkit.getWorld(respawnworld);
                loc = newworld.getSpawnLocation();
            }

            return loc;
        }
        return null;
    }

    public Location respawnToNext(Location loc){
        boolean respawntonext = cfg.getConfig().getBoolean("RespawnNextToBed");
        if (respawntonext) {
            List<Location> modified = new ArrayList<>();
            modified.add(new Location(loc.getWorld(), loc.getBlockX() + 1, loc.getBlockY(), loc.getBlockZ()));
            modified.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() + 1));
            modified.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - 1));
            modified.add(new Location(loc.getWorld(), loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ()));
            for (Location locs : modified) {
                if (locs.getBlock().getType() == Material.AIR || locs.getBlock().getType() == Material.GRASS) {
                    loc = locs;
                }
            }
        }
        return loc;
    }

    public String getMessage(String key){
        return cfg.getConfig().getString("Messages."+key).replace("&", "§");
    }
}
