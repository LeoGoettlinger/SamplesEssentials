package dev.demora.demoralegendaryweapons.weapons.withersickles;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SickleActionBarManager {
   private final DemoraLegendaryWeapons plugin;
   private final WitherSickles witherSickles;
   private final SickleCooldownManager cooldownManager;
   private final SickleInventoryManager inventoryManager;

   public SickleActionBarManager(
      DemoraLegendaryWeapons plugin, WitherSickles witherSickles, SickleCooldownManager cooldownManager, SickleInventoryManager inventoryManager
   ) {
      this.plugin = plugin;
      this.witherSickles = witherSickles;
      this.cooldownManager = cooldownManager;
      this.inventoryManager = inventoryManager;
   }

   public void startActionBarTask() {
      (new BukkitRunnable() {
            private int tickCounter = 0;

            public void run() {
               this.tickCounter++;

               for (Player player : Bukkit.getOnlinePlayers()) {
                  ItemStack mainHand = player.getInventory().getItemInMainHand();
                  ItemStack offHand = player.getInventory().getItemInOffHand();
                  if (SickleActionBarManager.this.witherSickles.isWitherSickle(mainHand)) {
                     if (!SickleActionBarManager.this.witherSickles.isAdditionalSickle(offHand)
                        && this.tickCounter % 4 == 0
                        && !SickleActionBarManager.this.cooldownManager.isOnCooldown(player)) {
                        SickleActionBarManager.this.inventoryManager.equipOffhandSickle(player);
                     }

                     if (SickleActionBarManager.this.witherSickles.isAdditionalSickle(offHand)) {
                        player.sendActionBar(Component.text("\ue020"));
                     } else {
                        player.sendActionBar(Component.text("\ue021"));
                     }
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 5L);
   }
}
