package com.sk89q.worldedit.bukkit;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractNonPlayerActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.adapter.bukkit.TextAdapter;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BukkitCommandSender extends AbstractNonPlayerActor {
   private static final UUID DEFAULT_ID = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");
   private final CommandSender sender;
   private final WorldEditPlugin plugin;

   public BukkitCommandSender(WorldEditPlugin plugin, CommandSender sender) {
      Preconditions.checkNotNull(plugin);
      Preconditions.checkNotNull(sender);
      Preconditions.checkArgument(!(sender instanceof Player), "Cannot wrap a player");
      this.plugin = plugin;
      this.sender = sender;
   }

   @Override
   public UUID getUniqueId() {
      return DEFAULT_ID;
   }

   @Override
   public String getName() {
      return this.sender.getName();
   }

   @Deprecated
   @Override
   public void printRaw(String msg) {
      for (String part : msg.split("\n")) {
         this.sender.sendMessage(part);
      }
   }

   @Deprecated
   @Override
   public void print(String msg) {
      for (String part : msg.split("\n")) {
         this.sender.sendMessage("§d" + part);
      }
   }

   @Deprecated
   @Override
   public void printDebug(String msg) {
      for (String part : msg.split("\n")) {
         this.sender.sendMessage("§7" + part);
      }
   }

   @Deprecated
   @Override
   public void printError(String msg) {
      for (String part : msg.split("\n")) {
         this.sender.sendMessage("§c" + part);
      }
   }

   @Override
   public void print(Component component) {
      TextAdapter.sendMessage(this.sender, WorldEditText.format(component, this.getLocale()));
   }

   @Override
   public String[] getGroups() {
      return new String[0];
   }

   @Override
   public boolean hasPermission(String perm) {
      return true;
   }

   @Override
   public void setPermission(String permission, boolean value) {
   }

   @Override
   public void checkPermission(String permission) throws AuthorizationException {
   }

   @Override
   public Locale getLocale() {
      return WorldEdit.getInstance().getConfiguration().defaultLocale;
   }

   public CommandSender getSender() {
      return this.sender;
   }

   @Override
   public SessionKey getSessionKey() {
      return new SessionKey() {
         @Nullable
         @Override
         public String getName() {
            return BukkitCommandSender.this.sender.getName();
         }

         @Override
         public boolean isActive() {
            return !(BukkitCommandSender.this.sender instanceof Entity entity) ? true : entity.isValid() && !entity.isDead();
         }

         @Override
         public boolean isPersistent() {
            return true;
         }

         @Override
         public UUID getUniqueId() {
            return BukkitCommandSender.DEFAULT_ID;
         }
      };
   }
}
