package dev.demora.demoralegendaryweapons.weapons.withersickles.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AwakenedSickleActionBarManager {
   private final DemoraLegendaryWeapons plugin;
   private final AwakenedWitherSickles awakenedSickles;
   private final AwakenedSickleInventoryManager inventoryManager;
   private final AwakenedSickleCooldownManager cooldownManager;

   public AwakenedSickleActionBarManager(
      DemoraLegendaryWeapons plugin,
      AwakenedWitherSickles awakenedSickles,
      AwakenedSickleCooldownManager cooldownManager,
      AwakenedSickleInventoryManager inventoryManager
   ) {
      this.plugin = plugin;
      this.awakenedSickles = awakenedSickles;
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
                  if (AwakenedSickleActionBarManager.this.awakenedSickles.isAwakenedWitherSickle(mainHand)) {
                     if (!AwakenedSickleActionBarManager.this.awakenedSickles.isAdditionalAwakenedSickle(offHand)
                        && this.tickCounter % 4 == 0
                        && !AwakenedSickleActionBarManager.this.cooldownManager.isOnCooldown(player)) {
                        AwakenedSickleActionBarManager.this.inventoryManager.equipOffhandSickle(player);
                     }

                     if (AwakenedSickleActionBarManager.this.awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
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
