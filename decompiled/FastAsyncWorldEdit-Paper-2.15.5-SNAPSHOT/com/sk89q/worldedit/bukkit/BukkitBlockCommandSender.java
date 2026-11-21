package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractCommandBlockActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.adapter.bukkit.TextAdapter;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;

public class BukkitBlockCommandSender extends AbstractCommandBlockActor {
   private static final String UUID_PREFIX = "CMD";
   private final BlockCommandSender sender;
   private final WorldEditPlugin plugin;
   private final UUID uuid;

   public BukkitBlockCommandSender(WorldEditPlugin plugin, BlockCommandSender sender) {
      super(BukkitAdapter.adapt(((BlockCommandSender)Preconditions.checkNotNull(sender)).getBlock().getLocation()));
      Preconditions.checkNotNull(plugin);
      this.plugin = plugin;
      this.sender = sender;
      this.uuid = UUID.nameUUIDFromBytes(("CMD" + sender.getName()).getBytes(StandardCharsets.UTF_8));
   }

   @Override
   public String getName() {
      return this.sender.getName();
   }

   @Deprecated
   @Override
   public void printRaw(String msg) {
      TaskManager.taskManager().sync(() -> {
         for (String part : msg.split("\n")) {
            this.sender.sendMessage(part);
         }

         return null;
      });
   }

   @Deprecated
   @Override
   public void print(String msg) {
      TaskManager.taskManager().sync(() -> {
         for (String part : msg.split("\n")) {
            this.print(TextComponent.of(part, TextColor.LIGHT_PURPLE));
         }

         return null;
      });
   }

   @Deprecated
   @Override
   public void printDebug(String msg) {
      TaskManager.taskManager().sync(() -> {
         for (String part : msg.split("\n")) {
            this.print(TextComponent.of(part, TextColor.GRAY));
         }

         return null;
      });
   }

   @Deprecated
   @Override
   public void printError(String msg) {
      TaskManager.taskManager().sync(() -> {
         for (String part : msg.split("\n")) {
            this.print(TextComponent.of(part, TextColor.RED));
         }

         return null;
      });
   }

   @Override
   public void print(Component component) {
      TaskManager.taskManager().sync(() -> {
         TextAdapter.sendMessage(this.sender, WorldEditText.format(component, this.getLocale()));
         return null;
      });
   }

   @Override
   public Locale getLocale() {
      return WorldEdit.getInstance().getConfiguration().defaultLocale;
   }

   @Override
   public UUID getUniqueId() {
      return this.uuid;
   }

   @Override
   public String[] getGroups() {
      return new String[0];
   }

   @Override
   public void checkPermission(String permission) throws AuthorizationException {
      if (!this.hasPermission(permission)) {
         throw new AuthorizationException();
      }
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.sender.hasPermission(permission);
   }

   @Override
   public boolean togglePermission(String permission) {
      return true;
   }

   @Override
   public void setPermission(String permission, boolean value) {
   }

   public BlockCommandSender getSender() {
      return this.sender;
   }

   @Override
   public SessionKey getSessionKey() {
      return new SessionKey() {
         private volatile boolean active = true;

         private void updateActive() {
            Block block = BukkitBlockCommandSender.this.sender.getBlock();
            if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
               this.active = false;
            } else {
               Material type = block.getType();
               this.active = type == Material.COMMAND_BLOCK || type == Material.CHAIN_COMMAND_BLOCK || type == Material.REPEATING_COMMAND_BLOCK;
            }
         }

         @Override
         public String getName() {
            return BukkitBlockCommandSender.this.sender.getName();
         }

         @Override
         public boolean isActive() {
            if (Bukkit.isPrimaryThread()) {
               this.updateActive();
            } else {
               Bukkit.getScheduler().callSyncMethod(BukkitBlockCommandSender.this.plugin, () -> {
                  this.updateActive();
                  return null;
               });
            }

            return this.active;
         }

         @Override
         public boolean isPersistent() {
            return true;
         }

         @Override
         public UUID getUniqueId() {
            return BukkitBlockCommandSender.this.uuid;
         }
      };
   }
}
