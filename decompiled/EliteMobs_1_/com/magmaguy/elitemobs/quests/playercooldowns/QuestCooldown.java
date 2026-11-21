package com.magmaguy.elitemobs.quests.playercooldowns;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.io.Serializable;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;

public class QuestCooldown implements Serializable {
   private static final long serialVersionUID = 8936646690733915274L;
   private final String permission;
   private final boolean permanent;
   private long targetUnixTime = 0L;
   private transient BukkitTask bukkitTask = null;
   private transient PermissionAttachment permissionAttachment;

   public QuestCooldown(int delayInMinutes, String permission, UUID player) {
      this.permanent = delayInMinutes < 1;
      if (!this.permanent) {
         this.targetUnixTime = System.currentTimeMillis() + 60000L * delayInMinutes;
      }

      this.permission = permission;
   }

   public void startCooldown(UUID player) {
      this.stop();
      Player session = Bukkit.getPlayer(player);
      if (session != null && !this.isExpired()) {
         this.permissionAttachment = session.addAttachment(MetadataHandler.PLUGIN);
         this.permissionAttachment.setPermission(this.permission, true);
         session.setMetadata(this.permission, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
         if (!this.permanent) {
            this.scheduleExpiration(session);
         }
      }
   }

   boolean isExpired() {
      return !this.permanent && this.targetUnixTime <= System.currentTimeMillis();
   }

   private void scheduleExpiration(Player session) {
      long remaining = this.targetUnixTime - System.currentTimeMillis();
      long delay = Math.max(1L, remaining / 50L + (remaining % 50L > 0L ? 1L : 0L));
      this.bukkitTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
         this.bukkitTask = null;
         if (Bukkit.getPlayer(session.getUniqueId()) != session) {
            this.stop();
         } else if (!this.isExpired()) {
            this.scheduleExpiration(session);
         } else {
            PlayerQuestCooldowns owner = PlayerData.getPlayerQuestCooldowns(session.getUniqueId());
            if (owner != null) {
               owner.expire(this, session);
            } else {
               this.stop();
            }
         }
      }, delay);
   }

   void stop() {
      if (this.bukkitTask != null) {
         this.bukkitTask.cancel();
      }

      this.bukkitTask = null;
      PermissionAttachment owned = this.permissionAttachment;
      this.permissionAttachment = null;
      if (owned != null) {
         owned.remove();
      }
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public long getTargetUnixTime() {
      return this.targetUnixTime;
   }

   @Generated
   public BukkitTask getBukkitTask() {
      return this.bukkitTask;
   }
}
