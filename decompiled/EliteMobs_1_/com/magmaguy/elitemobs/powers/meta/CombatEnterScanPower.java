package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.HashSet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public abstract class CombatEnterScanPower extends MajorPower implements Listener {
   public static HashSet<CombatEnterScanPower> combatEnterScanPowers = new HashSet<>();
   public BukkitTask bukkitTask = null;
   private boolean isActive = false;

   public CombatEnterScanPower(PowersConfigFields powersConfigFields) {
      super(powersConfigFields);
      combatEnterScanPowers.add(this);
   }

   public static void shutdown() {
      combatEnterScanPowers.clear();
   }

   protected void activate(EliteEntity eliteEntity) {
      if (!this.isActive) {
         this.isActive = true;
         this.finishActivation(eliteEntity);
      }
   }

   protected abstract void finishActivation(EliteEntity var1);

   public void deactivate(EliteEntity eliteEntity) {
      if (this.bukkitTask != null && !this.bukkitTask.isCancelled()) {
         this.bukkitTask.cancel();
      }

      this.isActive = false;
      this.finishDeactivation(eliteEntity);
   }

   protected abstract void finishDeactivation(EliteEntity var1);

   protected boolean doExit(EliteEntity eliteEntity) {
      if (eliteEntity != null && eliteEntity.isValid()) {
         return false;
      } else {
         this.deactivate(eliteEntity);
         return true;
      }
   }

   public static class MajorCombatEnterScanningPowerEvents implements Listener {
      @EventHandler
      public void onCombatEnter(EliteMobEnterCombatEvent event) {
         for (CombatEnterScanPower combatEnterScanPower : CombatEnterScanPower.combatEnterScanPowers) {
            ElitePower elitePowerInstance = event.getEliteMobEntity().getPower(combatEnterScanPower);
            if (elitePowerInstance != null) {
               ((CombatEnterScanPower)elitePowerInstance).activate(event.getEliteMobEntity());
            }
         }
      }

      @EventHandler
      public void onCombatExit(EliteMobExitCombatEvent event) {
         for (CombatEnterScanPower combatEnterScanPower : CombatEnterScanPower.combatEnterScanPowers) {
            ElitePower elitePowerInstance = event.getEliteMobEntity().getPower(combatEnterScanPower);
            if (elitePowerInstance != null) {
               ((CombatEnterScanPower)elitePowerInstance).deactivate(event.getEliteMobEntity());
            }
         }
      }
   }
}
