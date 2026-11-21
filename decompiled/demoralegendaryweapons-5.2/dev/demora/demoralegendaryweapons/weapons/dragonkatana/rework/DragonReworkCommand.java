package dev.demora.demoralegendaryweapons.weapons.dragonkatana.rework;

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

public class DragonReworkCommand implements CommandExecutor {
   private final DemoraLegendaryWeapons plugin;
   private FileConfiguration reworkConfig;

   public DragonReworkCommand(DemoraLegendaryWeapons plugin) {
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
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-dragon-only-players"));
         return true;
      } else if (!this.reworkConfig.getBoolean("enabled", false)) {
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-dragon-disabled"));
         return true;
      } else if (!this.hasRequiredItems(player)) {
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-dragon-no-items"));
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-dragon-requirements"));
         return true;
      } else {
         this.removeRequiredItems(player);
         AwakenedDragonKatana awakenedKatana = new AwakenedDragonKatana(this.plugin);
         ItemStack katana = awakenedKatana.createAwakenedDragonKatana();
         player.getInventory().addItem(new ItemStack[]{katana});
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.rework-dragon-created"));
         player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 0.5F);
         Location loc = player.getLocation();

         for (int i = 0; i < 20; i++) {
            try {
               loc.getWorld()
                  .spawnParticle(
                     Particle.DRAGON_BREATH,
                     loc.clone().add(Math.random() * 2.0 - 1.0, Math.random() * 2.0, Math.random() * 2.0 - 1.0),
                     5,
                     0.1,
                     0.1,
                     0.1,
                     0.02,
                     0.5F
                  );
            } catch (IllegalArgumentException var11) {
               loc.getWorld()
                  .spawnParticle(
                     Particle.DRAGON_BREATH, loc.clone().add(Math.random() * 2.0 - 1.0, Math.random() * 2.0, Math.random() * 2.0 - 1.0), 5, 0.1, 0.1, 0.1, 0.02
                  );
            }
         }

         return true;
      }
   }

   private boolean hasRequiredItems(Player player) {
      int dragonEgg = 0;
      int chorusFruit = 0;
      int enderPearl = 0;
      boolean hasDragonKatana = false;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null) {
            if (item.getType() == Material.DRAGON_EGG) {
               dragonEgg += item.getAmount();
            } else if (item.getType() == Material.CHORUS_FRUIT) {
               chorusFruit += item.getAmount();
            } else if (item.getType() == Material.ENDER_PEARL) {
               enderPearl += item.getAmount();
            } else if (this.isDragonKatana(item)) {
               hasDragonKatana = true;
            }
         }
      }

      return dragonEgg >= 1 && chorusFruit >= 64 && enderPearl >= 16 && hasDragonKatana;
   }

   private void removeRequiredItems(Player player) {
      int dragonEggToRemove = 1;
      int chorusFruitToRemove = 64;
      int enderPearlToRemove = 16;
      boolean dragonKatanaRemoved = false;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null) {
            if (item.getType() == Material.DRAGON_EGG && dragonEggToRemove > 0) {
               int amount = Math.min(item.getAmount(), dragonEggToRemove);
               item.setAmount(item.getAmount() - amount);
               dragonEggToRemove -= amount;
               if (item.getAmount() == 0) {
                  player.getInventory().remove(item);
               }
            } else if (item.getType() == Material.CHORUS_FRUIT && chorusFruitToRemove > 0) {
               int amount = Math.min(item.getAmount(), chorusFruitToRemove);
               item.setAmount(item.getAmount() - amount);
               chorusFruitToRemove -= amount;
               if (item.getAmount() == 0) {
                  player.getInventory().remove(item);
               }
            } else if (item.getType() == Material.ENDER_PEARL && enderPearlToRemove > 0) {
               int amount = Math.min(item.getAmount(), enderPearlToRemove);
               item.setAmount(item.getAmount() - amount);
               enderPearlToRemove -= amount;
               if (item.getAmount() == 0) {
                  player.getInventory().remove(item);
               }
            } else if (this.isDragonKatana(item) && !dragonKatanaRemoved) {
               player.getInventory().remove(item);
               dragonKatanaRemoved = true;
            }
         }
      }
   }

   private boolean isDragonKatana(ItemStack item) {
      return item != null && item.getType() == Material.NETHERITE_SWORD ? this.plugin.getDragonKatana().isDragonKatana(item) : false;
   }
}
