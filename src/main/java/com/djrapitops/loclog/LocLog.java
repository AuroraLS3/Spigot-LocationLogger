package com.djrapitops.loclog;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class LocLog extends JavaPlugin implements Listener {

    private Database database;
    private Timer locationLogTimer;

    @Override
    public void onEnable() {
        try {
            Files.createDirectories(getDataFolder().toPath());
            database = new Database(new File(getDataFolder(), "database.db"));
            database.init();
        } catch (IOException | SQLException e) {
            getLogger().log(Level.WARNING, "Failed to start", e);
            onDisable();
        }

        locationLogTimer = new Timer();
        locationLogTimer.scheduleAtFixedRate(new LocationLogger(), TimeUnit.SECONDS.toMillis(1L), TimeUnit.SECONDS.toMillis(1L));

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (locationLogTimer != null) locationLogTimer.cancel();
        try {
            if (database != null) database.close();
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to close database", e);
        }
        HandlerList.unregisterAll((Plugin) this);
    }

    class LocationLogger extends TimerTask {
        @Override
        public void run() {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                UUID identifier = onlinePlayer.getUniqueId();

                long time = System.currentTimeMillis();
                Location location = onlinePlayer.getLocation();
                int x = location.getBlockX();
                int z = location.getBlockZ();

                try {
                    database.savePlayerLocation(identifier, time, x, z);
                } catch (SQLException e) {
                    getLogger().log(Level.WARNING, "Failed to save location", e);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID identifier = event.getPlayer().getUniqueId();

        Block placedBlock = event.getBlockPlaced();
        String blockName = placedBlock.getType().name();
        Location location = placedBlock.getLocation();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        try {
            database.savePlayerPlacedBlock(identifier, x, z, blockName);
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to save location", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        UUID identifier = event.getPlayer().getUniqueId();

        Block brokenBlock = event.getBlock();
        String blockName = brokenBlock.getType().name();
        Location location = brokenBlock.getLocation();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        try {
            database.savePlayerBrokenBlock(identifier, x, z, blockName);
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to save location", e);
        }
    }
}