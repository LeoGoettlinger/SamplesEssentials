package com.itzazeem.asbounty.manager;

import com.itzazeem.asbounty.AsBounty;
import com.itzazeem.asbounty.utils.MessageUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SignManager implements Listener {
   private final AsBounty plugin;
   private final Map<UUID, SignManager.SignSession> activeSessions;
   private final Map<UUID, Location> signLocations;

   public SignManager(AsBounty plugin) {
      this.plugin = plugin;
      this.activeSessions = new HashMap<>();
      this.signLocations = new HashMap<>();
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public void openSignEditor(Player player, BiConsumer<Player, String> callback) {
      UUID uuid = player.getUniqueId();
      this.closeSession(player);
      SignManager.SignSession session = new SignManager.SignSession(callback);
      this.activeSessions.put(uuid, session);
      Location signLoc = player.getLocation().getBlock().getLocation();
      Block block = signLoc.getBlock();
      session.oldBlockType = block.getType();
      session.oldBlockData = block.getBlockData();
      block.setType(Material.OAK_SIGN);
      this.signLocations.put(uuid, signLoc);
      List<String> signLines = this.plugin.getConfig().getStringList("sign-gui");
      String[] lines = new String[4];

      for (int i = 0; i < 4; i++) {
         if (i < signLines.size() && signLines.get(i) != null) {
            lines[i] = MessageUtils.colorize(signLines.get(i));
         } else {
            lines[i] = "";
         }
      }

      if (block.getState() instanceof Sign sign) {
         SignSide frontSide = sign.getSide(Side.FRONT);

         for (int ix = 0; ix < 4; ix++) {
            frontSide.setLine(ix, lines[ix]);
         }

         sign.update(true, false);
      }

      this.scheduleOpenSign(player, uuid, lines, 2L);
   }

   private void scheduleOpenSign(Player player, UUID uuid, String[] lines, long delay) {
      Runnable openTask = () -> {
         if (player.isOnline()) {
            Location currentLoc = this.signLocations.get(uuid);
            if (currentLoc != null && currentLoc.getWorld() != null) {
               Block currentBlock = currentLoc.getBlock();
               if (currentBlock.getState() instanceof Sign sign) {
                  try {
                     player.sendSignChange(currentLoc, lines);
                  } catch (Throwable var12) {
                  }

                  try {
                     player.openSign(sign, Side.FRONT);
                  } catch (Throwable var11) {
                     try {
                        player.openSign(sign);
                     } catch (Throwable var10) {
                     }
                  }
               }
            }
         }
      };

      try {
         if (player.getScheduler() != null) {
            player.getScheduler().runDelayed(this.plugin, scheduledTask -> openTask.run(), null, delay);
            return;
         }
      } catch (Throwable var9) {
      }

      try {
         Bukkit.getScheduler().runTaskLater(this.plugin, openTask, delay);
      } catch (UnsupportedOperationException var8) {
         openTask.run();
      }
   }

   public void closeSession(Player player) {
      UUID uuid = player.getUniqueId();
      SignManager.SignSession session = this.activeSessions.remove(uuid);
      if (session != null) {
         Location loc = this.signLocations.remove(uuid);
         if (loc != null && loc.getWorld() != null) {
            Block block = loc.getBlock();
            if (block != null && block.getType() == Material.OAK_SIGN) {
               if (session.oldBlockType != null && session.oldBlockType != Material.AIR) {
                  block.setType(session.oldBlockType);
                  if (session.oldBlockData != null) {
                     block.setBlockData(session.oldBlockData);
                  }
               } else {
                  block.setType(Material.AIR);
               }
            }
         }
      }
   }

   @EventHandler
   public void onSignChange(SignChangeEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      if (this.activeSessions.containsKey(uuid)) {
         SignManager.SignSession session = this.activeSessions.get(uuid);
         event.setCancelled(true);
         String input = event.getLine(0);
         if (input == null || input.isEmpty()) {
            input = event.getLine(1);
         }

         if (input != null && !input.isEmpty()) {
            this.activeSessions.remove(uuid);
            Location loc = this.signLocations.remove(uuid);
            if (loc != null && loc.getWorld() != null) {
               Block block = loc.getBlock();
               if (block != null && block.getType() == Material.OAK_SIGN) {
                  if (session.oldBlockType != null && session.oldBlockType != Material.AIR) {
                     block.setType(session.oldBlockType);
                     if (session.oldBlockData != null) {
                        block.setBlockData(session.oldBlockData);
                     }
                  } else {
                     block.setType(Material.AIR);
                  }
               }
            }

            if (session.callback != null && !input.isEmpty()) {
               session.callback.accept(player, input);
            }
         } else {
            this.closeSession(player);
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      if (this.activeSessions.containsKey(uuid)
         && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
         && event.getClickedBlock() != null
         && event.getClickedBlock().getType() == Material.OAK_SIGN) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      if (this.activeSessions.containsKey(uuid)) {
         Location signLoc = this.signLocations.get(uuid);
         if (signLoc != null && signLoc.getWorld() != null && event.getBlock().getLocation().equals(signLoc)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.closeSession(event.getPlayer());
   }

   private static class SignSession {
      final BiConsumer<Player, String> callback;
      Material oldBlockType;
      BlockData oldBlockData;

      SignSession(BiConsumer<Player, String> callback) {
         this.callback = callback;
         this.oldBlockType = Material.AIR;
      }
   }
}
