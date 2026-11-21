package com.lishid.openinv.command;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.runnable.WrappedRunnable;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.AccessEqualMode;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import java.util.Collections;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerLookupCommand implements TabExecutor {
   @NotNull
   protected final OpenInv plugin;
   @NotNull
   protected final LanguageManager lang;
   @NotNull
   protected final Config config;
   @NotNull
   protected final PlayerLoader playerLoader;

   public PlayerLookupCommand(@NotNull OpenInv plugin, @NotNull LanguageManager lang, @NotNull Config config, @NotNull PlayerLoader playerLoader) {
      this.plugin = plugin;
      this.lang = lang;
      this.config = config;
      this.playerLoader = playerLoader;
   }

   public boolean onCommand(@NotNull final CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull final String[] args) {
      final boolean accessInv = this.isAccessInventory(command);
      final String targetId = this.getTargetIdentifer(sender, command, args.length > 0 ? args[0] : null, accessInv);
      if (targetId == null) {
         return true;
      } else {
         (new WrappedRunnable() {
            @Override
            public void run() {
               OfflinePlayer target = PlayerLookupCommand.this.getTarget(targetId);
               if (target != null && (target.hasPlayedBefore() || target.isOnline())) {
                  Runnable sync = () -> {
                     if (!(sender instanceof Player player && !player.isValid())) {
                        PlayerLookupCommand.PlayerAccess onlineTarget = PlayerLookupCommand.this.access(sender, target, accessInv);
                        if (onlineTarget != null) {
                           PlayerLookupCommand.this.handle(sender, onlineTarget, accessInv, args);
                        }
                     }
                  };
                  if (sender instanceof Player senderPlayer) {
                     PlayerLookupCommand.this.plugin.getScheduler().runTaskAtEntity(senderPlayer, sync);
                  } else {
                     PlayerLookupCommand.this.plugin.getScheduler().runTask(sync);
                  }
               } else {
                  PlayerLookupCommand.this.lang.sendMessage(sender, "messages.error.invalidPlayer");
               }
            }
         }).runTaskAsynchronously(this.plugin);
         return true;
      }
   }

   protected abstract boolean isAccessInventory(@NotNull Command var1);

   @Nullable
   protected abstract String getTargetIdentifer(@NotNull CommandSender var1, @NotNull Command var2, @Nullable String var3, boolean var4);

   @Nullable
   protected abstract OfflinePlayer getTarget(@NotNull String var1);

   @Nullable
   protected PlayerLookupCommand.PlayerAccess access(@NotNull CommandSender sender, @NotNull OfflinePlayer target, boolean invPerms) {
      Player onlineTarget = this.accessAsPlayer(sender, target);
      if (onlineTarget == null) {
         return null;
      } else {
         return this.deniedCommand(sender, onlineTarget, invPerms) ? null : this.accessGeneralized(sender, onlineTarget, invPerms);
      }
   }

   @Nullable
   protected Player accessAsPlayer(@NotNull CommandSender sender, @NotNull OfflinePlayer target) {
      Player onlineTarget;
      if (!target.isOnline()) {
         if (this.config.isOfflineDisabled() || !Permissions.ACCESS_OFFLINE.hasPermission(sender)) {
            this.lang.sendMessage(sender, "messages.error.permissionPlayerOffline");
            return null;
         }

         onlineTarget = this.playerLoader.load(target);
      } else {
         if (!Permissions.ACCESS_ONLINE.hasPermission(sender)) {
            this.lang.sendMessage(sender, "messages.error.permissionPlayerOnline");
            return null;
         }

         onlineTarget = target.getPlayer();
      }

      if (onlineTarget == null) {
         this.lang.sendMessage(sender, "messages.error.invalidPlayer");
         return null;
      } else {
         return onlineTarget;
      }
   }

   protected abstract boolean deniedCommand(@NotNull CommandSender var1, @NotNull Player var2, boolean var3);

   @Nullable
   protected PlayerLookupCommand.PlayerAccess accessGeneralized(@NotNull CommandSender sender, @NotNull Player onlineTarget, boolean invPerms) {
      boolean ownContainer = sender.equals(onlineTarget);
      Permissions edit;
      if (invPerms) {
         edit = ownContainer ? Permissions.INVENTORY_EDIT_SELF : Permissions.INVENTORY_EDIT_OTHER;
      } else {
         edit = ownContainer ? Permissions.ENDERCHEST_EDIT_SELF : Permissions.ENDERCHEST_EDIT_OTHER;
      }

      boolean viewOnly = !edit.hasPermission(sender);
      if (ownContainer) {
         return new PlayerLookupCommand.PlayerAccess(onlineTarget, viewOnly);
      } else if (sender instanceof Player player && !Permissions.ACCESS_CROSSWORLD.hasPermission(sender) && !onlineTarget.getWorld().equals(player.getWorld())) {
         this.lang.sendMessage(sender, "messages.error.permissionCrossWorld", new Replacement("%target%", onlineTarget.getDisplayName()));
         return null;
      } else {
         AccessEqualMode accessMode = AccessEqualMode.getByPerm(sender, this.config);
         int level = 4;

         while (level > 0) {
            String permission = "openinv.access.level." + level;
            if (!onlineTarget.hasPermission(permission)) {
               if (!sender.hasPermission(permission)) {
                  level--;
                  continue;
               }
            } else {
               if (!sender.hasPermission(permission) || accessMode == AccessEqualMode.DENY) {
                  this.lang.sendMessage(sender, "messages.error.permissionExempt", new Replacement("%target%", onlineTarget.getDisplayName()));
                  return null;
               }

               if (accessMode == AccessEqualMode.VIEW) {
                  viewOnly = true;
               }
            }
            break;
         }

         return new PlayerLookupCommand.PlayerAccess(onlineTarget, viewOnly);
      }
   }

   protected abstract void handle(@NotNull CommandSender var1, @NotNull PlayerLookupCommand.PlayerAccess var2, boolean var3, @NotNull String[] var4);

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      return command.testPermissionSilent(sender) && args.length == 1 ? TabCompleter.completeOnlinePlayer(sender, args[0]) : Collections.emptyList();
   }

   protected record PlayerAccess(Player player, boolean viewOnly) {
   }
}
