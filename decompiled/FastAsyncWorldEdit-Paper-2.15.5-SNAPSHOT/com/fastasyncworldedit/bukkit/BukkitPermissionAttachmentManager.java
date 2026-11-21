package com.fastasyncworldedit.bukkit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;

public class BukkitPermissionAttachmentManager {
   private final WorldEditPlugin plugin;
   private final Map<Player, PermissionAttachment> attachments = Collections.synchronizedMap(new WeakHashMap<>());
   private PermissionAttachment noopAttachment;

   public BukkitPermissionAttachmentManager(WorldEditPlugin plugin) {
      this.plugin = plugin;
   }

   @Nullable
   public PermissionAttachment getOrAddAttachment(@Nullable Player p) {
      if (p instanceof OfflinePlayer) {
         p = p.getPlayer();
      }

      if (p == null || !p.isOnline()) {
         return null;
      } else if (p.hasMetadata("NPC")) {
         if (this.noopAttachment == null) {
            this.noopAttachment = new PermissionAttachment(this.plugin, new PermissibleBase(null));
         }

         return this.noopAttachment;
      } else {
         return this.attachments.computeIfAbsent(p, k -> k.addAttachment(this.plugin));
      }
   }

   public void removeAttachment(@Nullable final Player p) {
      if (p != null) {
         PermissionAttachment attach = this.attachments.remove(p);
         if (attach != null) {
            attach.remove();
         }
      }
   }
}
