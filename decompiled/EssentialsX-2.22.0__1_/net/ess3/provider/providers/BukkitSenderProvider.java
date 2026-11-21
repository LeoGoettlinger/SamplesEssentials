package net.ess3.provider.providers;

import java.util.Set;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.CommandSender.Spigot;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class BukkitSenderProvider implements CommandSender {
   protected final ConsoleCommandSender base;
   private final BukkitSenderProvider.MessageHook hook;

   public BukkitSenderProvider(ConsoleCommandSender base, BukkitSenderProvider.MessageHook hook) {
      this.base = base;
      this.hook = hook;
   }

   public void sendMessage(String message) {
      this.hook.sendMessage(message);
   }

   public void sendMessage(String[] messages) {
      for (String msg : messages) {
         this.sendMessage(msg);
      }
   }

   public void sendMessage(UUID uuid, String message) {
      this.sendMessage(message);
   }

   public void sendMessage(UUID uuid, String[] messages) {
      this.sendMessage(messages);
   }

   public Server getServer() {
      return this.base.getServer();
   }

   public String getName() {
      return this.base.getName();
   }

   public Spigot spigot() {
      try {
         Class.forName("org.bukkit.command.CommandSender$Spigot");
         return ModernCommandSenderSpigotCreator.stupidDumbHackToMakeTheJvmHappy(this);
      } catch (ClassNotFoundException var2) {
         return null;
      }
   }

   public boolean isPermissionSet(String name) {
      return this.base.isPermissionSet(name);
   }

   public boolean isPermissionSet(Permission perm) {
      return this.base.isPermissionSet(perm);
   }

   public boolean hasPermission(String name) {
      return this.base.hasPermission(name);
   }

   public boolean hasPermission(Permission perm) {
      return this.base.hasPermission(perm);
   }

   public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
      return this.base.addAttachment(plugin, name, value);
   }

   public PermissionAttachment addAttachment(Plugin plugin) {
      return this.base.addAttachment(plugin);
   }

   public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
      return this.base.addAttachment(plugin, name, value, ticks);
   }

   public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
      return this.base.addAttachment(plugin, ticks);
   }

   public void removeAttachment(PermissionAttachment attachment) {
      this.base.removeAttachment(attachment);
   }

   public void recalculatePermissions() {
      this.base.recalculatePermissions();
   }

   public Set<PermissionAttachmentInfo> getEffectivePermissions() {
      return this.base.getEffectivePermissions();
   }

   public boolean isOp() {
      return this.base.isOp();
   }

   public void setOp(boolean value) {
      this.base.setOp(value);
   }

   public interface MessageHook {
      void sendMessage(String message);
   }
}
