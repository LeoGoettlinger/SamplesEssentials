package com.viaversion.viaversion.bukkit.commands;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public record BukkitCommandSender(CommandSender sender) implements ViaCommandSender {
   @Override
   public boolean hasPermission(String permission) {
      return this.sender.hasPermission(permission);
   }

   @Override
   public void sendMessage(String msg) {
      this.sender.sendMessage(msg);
   }

   @Override
   public UUID getUUID() {
      return this.sender instanceof Entity entity ? entity.getUniqueId() : new UUID(0L, 0L);
   }

   @Override
   public String getName() {
      return this.sender.getName();
   }
}
