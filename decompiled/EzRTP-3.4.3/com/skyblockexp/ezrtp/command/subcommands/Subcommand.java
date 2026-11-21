package com.skyblockexp.ezrtp.command.subcommands;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class Subcommand {
   protected final String name;
   protected final String permission;

   protected Subcommand(String name, String permission) {
      this.name = name;
      this.permission = permission;
   }

   public abstract boolean execute(@NotNull CommandSender var1, @NotNull String[] var2);

   @NotNull
   public abstract List<String> tabComplete(@NotNull CommandSender var1, @NotNull String[] var2);

   public String getName() {
      return this.name;
   }

   public String getPermission() {
      return this.permission;
   }

   protected boolean hasPermission(CommandSender sender) {
      return this.permission == null || sender.hasPermission(this.permission);
   }
}
