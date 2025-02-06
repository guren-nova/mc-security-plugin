package com.novaplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class NovaSecurityPlugin extends JavaPlugin implements Listener {

    private final Set<String> lockedChests = new HashSet<>();

    @Override
    public void onEnable() {
        getLogger().info("Nova-SecurityPlugin enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        createRequiredFiles();
        loadLockedChests();
    }

    @Override
    public void onDisable() {
        getLogger().info("Nova-SecurityPlugin disabled.");
    }

    private void createRequiredFiles() {
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        File lockedChestsFile = new File(pluginFolder, "locked_chests.txt");
        if (!lockedChestsFile.exists()) {
            try {
                lockedChestsFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Failed to create locked_chests.txt: " + e.getMessage());
            }
        }

        File actionLogFile = new File(pluginFolder, "action_log.txt");
        if (!actionLogFile.exists()) {
            try {
                actionLogFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Failed to create action_log.txt: " + e.getMessage());
            }
        }
    }

    private void loadLockedChests() {
        File lockedChestsFile = new File(getDataFolder(), "locked_chests.txt");
        if (lockedChestsFile.exists()) {
        }
    }

    public void lockChest(Block block) {
        String location = block.getLocation().toString();
        lockedChests.add(location);
        saveLockedChests();
    }
    private void saveLockedChests() {
        try {
            File file = new File(getDataFolder(), "locked_chests.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(writer);
            for (String location : lockedChests) {
                printWriter.println(location);
            }
            printWriter.close();
        } catch (IOException e) {
            getLogger().warning("Error saving locked chests: " + e.getMessage());
        }
    }
    public void unlockChest(Block block) {
        String location = block.getLocation().toString();
        lockedChests.remove(location);
        saveLockedChests();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            logAction(player, "broke", block);
            preventAction(player, block);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // チェストやトラップチェストの設置ログ
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            logAction(player, "placed", block);
            preventAction(player, block);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block != null && (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.IRON_DOOR || block.getType() == Material.WOODEN_DOOR)) {
            logAction(player, "interacted with", block);
        }
    }

    private void logAction(Player player, String action, Block block) {
        try {
            File logFile = new File(getDataFolder(), "action_log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(logFile, true);
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println(player.getName() + " " + action + " " + block.getType() + " at " + block.getLocation());
            writer.close();
        } catch (IOException e) {
            getLogger().warning("Failed to log action: " + e.getMessage());
        }
    }

    private void preventAction(Player player, Block block) {
        if (isLockedChest(block)) {
            player.sendMessage("You cannot modify this chest, it is locked!");
            block.setType(Material.AIR); // 破壊・設置をキャンセル
            event.setCancelled(true); // イベントのキャンセル
        }
    }

    private boolean isLockedChest(Block block) {
        return lockedChests.contains(block.getLocation().toString());
    }
    private boolean isPlayerAuthorized(Player player) {
        return player.hasPermission("nova-securityplugin.use");
    }

}
