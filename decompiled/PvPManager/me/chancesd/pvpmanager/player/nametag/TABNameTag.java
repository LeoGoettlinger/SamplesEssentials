package me.chancesd.pvpmanager.player.nametag;

import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import org.jetbrains.annotations.Nullable;

public class TABNameTag extends NameTag {
   private boolean needsRestore;
   private final TABHook tab;

   public TABNameTag(TABHook hook, CombatPlayer p) {
      super(p);
      this.tab = hook;
   }

   private boolean changeTABPrefix(String tabPrefix, String tabSuffix) {
      TabPlayer tabPlayer = this.getTABPlayer();
      if (tabPlayer == null) {
         return false;
      } else {
         NameTagManager teamManager = TabAPI.getInstance().getNameTagManager();
         if (teamManager != null && this.tab.showAboveHead()) {
            if (!tabPrefix.isEmpty()) {
               teamManager.setPrefix(tabPlayer, tabPrefix);
            }

            if (!tabSuffix.isEmpty()) {
               teamManager.setSuffix(tabPlayer, tabSuffix);
            }
         }

         TabListFormatManager tabManager = TabAPI.getInstance().getTabListFormatManager();
         if (tabManager != null && this.tab.showInPlayerlist()) {
            if (!tabPrefix.isEmpty()) {
               tabManager.setPrefix(tabPlayer, tabPrefix);
            }

            if (!tabSuffix.isEmpty()) {
               tabManager.setSuffix(tabPlayer, tabSuffix);
            }
         }

         this.needsRestore = true;
         return true;
      }
   }

   private boolean restoreTABPrefix() {
      TabPlayer tabPlayer = this.getTABPlayer();
      if (tabPlayer == null) {
         return false;
      } else {
         NameTagManager teamManager = TabAPI.getInstance().getNameTagManager();
         if (teamManager != null && this.tab.showAboveHead()) {
            teamManager.setPrefix(tabPlayer, null);
            teamManager.setSuffix(tabPlayer, null);
         }

         TabListFormatManager tabManager = TabAPI.getInstance().getTabListFormatManager();
         if (tabManager != null && this.tab.showInPlayerlist()) {
            tabManager.setPrefix(tabPlayer, null);
            tabManager.setSuffix(tabPlayer, null);
         }

         if (Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
            this.setPvP(this.pvPlayer.hasPvPEnabled());
         }

         this.needsRestore = false;
         return true;
      }
   }

   @Nullable
   private TabPlayer getTABPlayer() {
      return TabAPI.getInstance().getPlayer(this.pvPlayer.getUUID());
   }

   @Override
   public final void setInCombat() {
      this.changeTABPrefix(this.combatPrefix, this.combatSuffix);
   }

   @Override
   public final void restoreNametag() {
      if (this.needsRestore) {
         this.restoreTABPrefix();
      }
   }

   @Override
   public final void setPvP(boolean state) {
      if (this.getTABPlayer() != null) {
         if (state) {
            if (this.pvpOnPrefix.isEmpty()) {
               this.restoreNametag();
            } else {
               this.changeTABPrefix(this.pvpOnPrefix, "");
            }
         } else if (this.pvpOffPrefix.isEmpty()) {
            this.restoreNametag();
         } else {
            this.changeTABPrefix(this.pvpOffPrefix, "");
         }
      }
   }

   @Override
   public void cleanup() {
   }
}
