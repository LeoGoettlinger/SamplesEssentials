package com.blaxk.spawnelytra.setup;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.integration.BedrockSupport;
import com.blaxk.spawnelytra.util.MessageUtil;
import com.blaxk.spawnelytra.util.SchedulerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class SetupSession {
   private static final Color GOLD = Color.fromRGB(255, 215, 0);
   private static final DustOptions GOLD_MARKER = new DustOptions(GOLD, 1.8F);
   private static final DustOptions GOLD_EDGE = new DustOptions(GOLD, 1.2F);
   private static final DustOptions GOLD_FILL = new DustOptions(GOLD, 1.0F);
   private final Main plugin;
   private final Player player;
   private Location pos1;
   private Location pos2;
   private String activationMode;
   private boolean showBoostActivated;
   private boolean showPressToBoost;
   private SchedulerUtil.TaskHandle previewTask;
   private SchedulerUtil.TaskHandle animateTask;

   public SetupSession(Main plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      String worldName = player.getWorld().getName();
      this.activationMode = plugin.getConfig().getString("worlds." + worldName + ".activation_mode", "double_jump");
   }

   public void begin() {
      this.showBoostActivated = this.plugin.getConfig().getBoolean("messages.show_boost_activated", true);
      this.showPressToBoost = this.plugin.getConfig().getBoolean("messages.show_press_to_boost", true);
      MessageUtil.send(this.player, "setup_started");
      String exitLabel = MessageUtil.plain("setup_exit_label");
      String exitHover = MessageUtil.plain("setup_exit_hover");
      String exitText;
      if (BedrockSupport.isBedrockPlayer(this.player)) {
         exitText = "<#aaa8a8>" + exitLabel + ": /spawnelytra setup exit";
      } else {
         exitText = "<#aaa8a8>[<click:run_command:'/spawnelytra setup exit'><hover:show_text:'" + exitHover + "'>" + exitLabel + "</hover></click>]";
      }

      MessageUtil.sendRaw(this.player, MiniMessage.miniMessage().deserialize(exitText));
   }

   public void end() {
      if (this.previewTask != null) {
         this.previewTask.cancel();
         this.previewTask = null;
      }

      if (this.animateTask != null) {
         this.animateTask.cancel();
         this.animateTask = null;
      }
   }

   public boolean hasBothPositions() {
      return this.pos1 != null && this.pos2 != null && this.pos1.getWorld().equals(this.pos2.getWorld());
   }

   public Location getPos1() {
      return this.pos1;
   }

   public Location getPos2() {
      return this.pos2;
   }

   public void setPos1(Location loc) {
      this.pos1 = loc.clone();
      this.spawnMarker(loc);
      this.player.playSound(loc, Sound.UI_BUTTON_CLICK, 0.6F, 1.4F);
      MessageUtil.send(this.player, "setup_pos1_set");
      if (this.pos2 != null) {
         this.startRectangleAnimation();
      } else {
         MessageUtil.send(this.player, "setup_pos1_next_instruction");
      }
   }

   public void setPos2(Location loc) {
      if (this.pos1 != null && !this.pos1.getWorld().equals(loc.getWorld())) {
         MessageUtil.send(this.player, "setup_world_mismatch");
      } else {
         this.pos2 = loc.clone();
         this.spawnMarker(loc);
         this.player.playSound(loc, Sound.UI_BUTTON_CLICK, 0.6F, 1.4F);
         MessageUtil.send(this.player, "setup_pos2_set");
         this.startRectangleAnimation();
         SchedulerUtil.runAtEntityLater(this.plugin, this.player, 20L, () -> this.plugin.getSetupManager().showOptions(this.player));
      }
   }

   private void spawnMarker(Location loc) {
      this.player.spawnParticle(Particle.DUST, loc, 15, 0.15, 0.15, 0.15, 0.0, GOLD_MARKER);
   }

   private void startRectangleAnimation() {
      if (this.hasBothPositions()) {
         if (this.animateTask != null) {
            this.animateTask.cancel();
         }

         Location a = this.pos1.clone();
         Location b = this.pos2.clone();
         final double minX = Math.min(a.getX(), b.getX());
         final double minY = Math.min(a.getY(), b.getY());
         final double minZ = Math.min(a.getZ(), b.getZ());
         final double maxX = Math.max(a.getX(), b.getX());
         final double maxY = Math.max(a.getY(), b.getY());
         final double maxZ = Math.max(a.getZ(), b.getZ());
         this.animateTask = SchedulerUtil.runAtEntityTimer(this.plugin, this.player, 0L, 1L, new Runnable() {
            int tick;
            final int total = 40;

            @Override
            public void run() {
               if (!SetupSession.this.player.isOnline()) {
                  SetupSession.this.animateTask.cancel();
               } else {
                  double progress = Math.min(1.0, this.tick / 40.0);
                  double currentY = minY + (maxY - minY) * progress;
                  SetupSession.this.drawFilledPlane(minX, minZ, maxX, maxZ, currentY, 1.0);
                  SetupSession.this.drawRectangleOnY(minX, minZ, maxX, maxZ, currentY, 1.0);
                  this.tick++;
                  if (40 < this.tick) {
                     SetupSession.this.animateTask.cancel();
                     SetupSession.this.startPreview(minX, minY, minZ, maxX, maxY, maxZ);
                     SetupSession.this.player.playSound(SetupSession.this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8F, 1.4F);
                  }
               }
            }
         });
      }
   }

   private void startPreview(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      if (this.previewTask != null) {
         this.previewTask.cancel();
      }

      this.previewTask = SchedulerUtil.runAtEntityTimer(this.plugin, this.player, 0L, 10L, () -> {
         if (!this.player.isOnline()) {
            this.previewTask.cancel();
         } else {
            this.drawBoxEdges(1.0, minX, minY, minZ, maxX, maxY, maxZ);
         }
      });
   }

   private void drawBoxEdges(double progress, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      double y2 = minY + (maxY - minY) * progress;
      DustOptions edgeDust = GOLD_EDGE;

      for (double y = minY; y <= y2; y += 0.5) {
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), minX, y, minZ), 1, 0.0, 0.0, 0.0, 0.0, edgeDust);
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), minX, y, maxZ), 1, 0.0, 0.0, 0.0, 0.0, edgeDust);
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), maxX, y, minZ), 1, 0.0, 0.0, 0.0, 0.0, edgeDust);
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), maxX, y, maxZ), 1, 0.0, 0.0, 0.0, 0.0, edgeDust);
      }

      this.drawRectangleOnY(minX, minZ, maxX, maxZ, minY, progress);
      this.drawRectangleOnY(minX, minZ, maxX, maxZ, y2, progress);
   }

   private void drawRectangleOnY(double minX, double minZ, double maxX, double maxZ, double y, double progress) {
      DustOptions gold = GOLD_EDGE;
      double xProgress = minX + (maxX - minX) * progress;
      double zProgress = minZ + (maxZ - minZ) * progress;

      for (double x = minX; x <= xProgress; x += 0.5) {
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), x, y, minZ), 1, 0.0, 0.0, 0.0, 0.0, gold);
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), x, y, maxZ), 1, 0.0, 0.0, 0.0, 0.0, gold);
      }

      for (double z = minZ; z <= zProgress; z += 0.5) {
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), minX, y, z), 1, 0.0, 0.0, 0.0, 0.0, gold);
         this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), maxX, y, z), 1, 0.0, 0.0, 0.0, 0.0, gold);
      }
   }

   private void drawFilledPlane(double minX, double minZ, double maxX, double maxZ, double y, double spacing) {
      DustOptions gold = GOLD_FILL;
      if (spacing <= 0.0) {
         spacing = 1.0;
      }

      for (double x = minX; x <= maxX; x += spacing) {
         for (double z = minZ; z <= maxZ; z += spacing) {
            this.player.spawnParticle(Particle.DUST, new Location(this.player.getWorld(), x, y, z), 1, 0.0, 0.0, 0.0, 0.0, gold);
         }
      }
   }

   public void setActivationMode(String activationMode) {
      this.activationMode = activationMode;
   }

   public String getActivationMode() {
      return this.activationMode;
   }

   public boolean isShowBoostActivated() {
      return this.showBoostActivated;
   }

   public void setShowBoostActivated(boolean showBoostActivated) {
      this.showBoostActivated = showBoostActivated;
   }

   public boolean isShowPressToBoost() {
      return this.showPressToBoost;
   }

   public void setShowPressToBoost(boolean showPressToBoost) {
      this.showPressToBoost = showPressToBoost;
   }
}
