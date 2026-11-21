package dev.demora.demoralegendaryweapons.weapons.withersickles.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SicklesReworkCommand implements CommandExecutor {
   private final DemoraLegendaryWeapons plugin;
   private FileConfiguration reworkConfig;

   public SicklesReworkCommand(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.loadReworkConfig();
   }

   private void loadReworkConfig() {
      File reworkFile = new File(this.plugin.getDataFolder(), "rework.yml");
      if (!reworkFile.exists()) {
         try {
            this.plugin.getDataFolder().mkdirs();
            InputStream defaultConfig = this.plugin.getResource("rework.yml");
            if (defaultConfig != null) {
               Files.copy(defaultConfig, reworkFile.toPath());
               defaultConfig.close();
            }
         } catch (IOException var3) {
            this.plugin.getLogger().severe("Не удалось создать rework.yml: " + var3.getMessage());
         }
      }

      this.reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-sickles-only-players"));
         return true;
      } else if (!this.reworkConfig.getBoolean("awakened_wither_sickles.enabled", false)) {
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-sickles-disabled"));
         return true;
      } else if (!this.hasRequiredItems(player)) {
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-sickles-no-items"));
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-sickles-requirements"));
         return true;
      } else {
         this.removeRequiredItems(player);
         AwakenedWitherSickles awakenedSickles = new AwakenedWitherSickles(this.plugin);
         ItemStack sickle = awakenedSickles.createAwakenedWitherSickle();
         player.getInventory().addItem(new ItemStack[]{sickle});
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-sickles-created"));
         player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.8F);
         player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0F, 1.2F);
         Location loc = player.getLocation();

         for (int i = 0; i < 30; i++) {
            loc.getWorld()
               .spawnParticle(
                  Particle.FLAME, loc.clone().add(Math.random() * 2.0 - 1.0, Math.random() * 2.0, Math.random() * 2.0 - 1.0), 5, 0.1, 0.1, 0.1, 0.02
               );
            loc.getWorld()
               .spawnParticle(
                  Particle.SOUL_FIRE_FLAME, loc.clone().add(Math.random() * 2.0 - 1.0, Math.random() * 2.0, Math.random() * 2.0 - 1.0), 3, 0.1, 0.1, 0.1, 0.01
               );
         }

         return true;
      }
   }

   private boolean hasRequiredItems(Player player) {
      int netheriteIngot = 0;
      int witherSkeletonSkull = 0;
      int blazePowder = 0;
      boolean hasWitherSickle = false;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null) {
            if (item.getType() == Material.NETHERITE_INGOT) {
               netheriteIngot += item.getAmount();
            } else if (item.getType() == Material.WITHER_SKELETON_SKULL) {
               witherSkeletonSkull += item.getAmount();
            } else if (item.getType() == Material.BLAZE_POWDER) {
               blazePowder += item.getAmount();
            } else if (this.isWitherSickle(item)) {
               hasWitherSickle = true;
            }
         }
      }

      return netheriteIngot >= 1 && witherSkeletonSkull >= 1 && blazePowder >= 64 && hasWitherSickle;
   }

   private void removeRequiredItems(Player player) {
      int netheriteToRemove = 1;
      int skullToRemove = 1;
      int blazeToRemove = 64;
      boolean sickleRemoved = false;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null) {
            if (item.getType() == Material.NETHERITE_INGOT && netheriteToRemove > 0) {
               int amount = Math.min(item.getAmount(), netheriteToRemove);
               item.setAmount(item.getAmount() - amount);
               netheriteToRemove -= amount;
               if (item.getAmount() == 0) {
                  player.getInventory().remove(item);
               }
            } else if (item.getType() == Material.WITHER_SKELETON_SKULL && skullToRemove > 0) {
               int amount = Math.min(item.getAmount(), skullToRemove);
               item.setAmount(item.getAmount() - amount);
               skullToRemove -= amount;
               if (item.getAmount() == 0) {
                  player.getInventory().remove(item);
               }
            } else if (item.getType() == Material.BLAZE_POWDER && blazeToRemove > 0) {
               int amount = Math.min(item.getAmount(), blazeToRemove);
               item.setAmount(item.getAmount() - amount);
               blazeToRemove -= amount;
               if (item.getAmount() == 0) {
                  player.getInventory().remove(item);
               }
            } else if (this.isWitherSickle(item) && !sickleRemoved) {
               player.getInventory().remove(item);
               sickleRemoved = true;
            }
         }
      }
   }

   private boolean isWitherSickle(ItemStack item) {
      return item != null && item.getType() == Material.STONE_HOE ? this.plugin.getWitherSickles().isWitherSickle(item) : false;
   }
}
