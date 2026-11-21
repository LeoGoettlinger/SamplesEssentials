package com.Sw3ys.bigenderchest.commands;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import com.Sw3ys.bigenderchest.storage.ChestStore;
import com.Sw3ys.bigenderchest.storage.YamlChestStore;
import com.Sw3ys.bigenderchest.util.Sched;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ECCommand implements CommandExecutor {
   private final EnderChestManager manager;

   public ECCommand(EnderChestManager manager) {
      this.manager = manager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
         if (!sender.hasPermission("enderchest.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
         } else {
            BiggerEnderChest.get().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "BiggerEnderChest config reloaded.");
            return true;
         }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("import")) {
         if (!sender.hasPermission("enderchest.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
         } else {
            BiggerEnderChest plugin = BiggerEnderChest.get();
            ChestStore target = this.manager.getStore();
            if (!target.isAuthoritative()) {
               sender.sendMessage(ChatColor.RED + "Import requires storage.type: mysql. Enable MySQL, restart, then run /ec import.");
               return true;
            } else {
               sender.sendMessage(ChatColor.YELLOW + "Migrating local YAML ender chests into MySQL...");
               Sched.async(
                  plugin,
                  () -> {
                     YamlChestStore yaml = new YamlChestStore(plugin);
                     List<UUID> ids = yaml.listStoredUUIDs();
                     int migrated = 0;
                     int skipped = 0;
                     int failed = 0;

                     for (UUID id : ids) {
                        try {
                           ChestStore.Stored data = yaml.read(id);
                           if (target.importWrite(id, data.size, data.items)) {
                              migrated++;
                           } else {
                              skipped++;
                           }
                        } catch (Exception var11x) {
                           failed++;
                           plugin.getLogger().warning("Import failed for " + id + ": " + var11x.getMessage());
                        }
                     }

                     if (!(sender instanceof Player pl && !pl.isOnline())) {
                        sender.sendMessage(
                           ChatColor.GREEN
                              + "Import complete: "
                              + migrated
                              + " migrated to MySQL"
                              + (skipped > 0 ? ", " + skipped + " skipped (online)" : "")
                              + (failed > 0 ? ChatColor.RED + ", " + failed + " failed" : "")
                              + ChatColor.GREEN
                              + ". Existing local YAML files were left untouched."
                        );
                     }
                  }
               );
               return true;
            }
         }
      } else if (sender instanceof Player p) {
         if (!p.hasPermission("enderchest.world.bypass")) {
            List<String> disabledWorlds = BiggerEnderChest.get().getConfig().getStringList("disabled-worlds");
            World world = p.getWorld();
            if (disabledWorlds.contains(world.getName())) {
               String message = BiggerEnderChest.get().getConfig().getString("messages.world-disabled", "&cYou cannot use Ender Chests in this world.");
               p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
               return true;
            }
         }

         if (!p.hasPermission("enderchest.cooldown.bypass")) {
            boolean enabled = BiggerEnderChest.get().getConfig().getBoolean("cooldown.enabled", true);
            int seconds = BiggerEnderChest.get().getConfig().getInt("cooldown.seconds", 5);
            if (enabled) {
               Map<UUID, Long> cooldowns = BiggerEnderChest.get().getCooldowns();
               long now = System.currentTimeMillis();
               if (cooldowns.containsKey(p.getUniqueId())) {
                  long expires = cooldowns.get(p.getUniqueId());
                  if (now < expires) {
                     long remaining = (expires - now + 999L) / 1000L;
                     String message = BiggerEnderChest.get()
                        .getConfig()
                        .getString("messages.cooldown", "&cPlease wait %seconds%s before opening your Ender Chest again.");
                     p.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%seconds%", String.valueOf(remaining))));
                     return true;
                  }
               }

               cooldowns.put(p.getUniqueId(), now + seconds * 1000L);
            }
         }

         this.manager.open(p, p.getUniqueId());
         return true;
      } else {
         return true;
      }
   }
}
