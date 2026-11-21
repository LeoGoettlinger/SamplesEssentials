package dev.demora.demoralegendaryweapons.weapons.midassword;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class MidasSwordListener implements Listener {
   private final MidasSword midasSword;

   public MidasSwordListener(MidasSword midasSword) {
      this.midasSword = midasSword;
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player victim = event.getEntity();
      Player killer = victim.getKiller();
      if (killer != null) {
         ItemStack weapon = killer.getInventory().getItemInMainHand();
         if (this.midasSword.isMidasSword(weapon)) {
            this.midasSword.increaseDamage(weapon);
            this.midasSword.createKillEffects(victim, killer);

            for (int i = 0; i < 5; i++) {
               victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.GOLD_BLOCK));
            }

            String msg = DemoraLegendaryWeapons.getInstance()
               .getMessageWithPrefix("weapon-messages.midas-upgrade")
               .replace("{damage}", String.valueOf(this.midasSword.getDamageLevel(weapon)));
            killer.sendMessage(msg);
         }
      }
   }
}
