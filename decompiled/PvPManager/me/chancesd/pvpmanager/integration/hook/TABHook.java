package me.chancesd.pvpmanager.integration.hook;

import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.nametag.NameTag;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Conf;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import org.bukkit.entity.Player;

public class TABHook extends BaseDependency {
   private final boolean showInPlayerlist;
   private final boolean showAboveHead;

   public TABHook(Hook hook) {
      super(hook);
      if (!Utils.isVersionAtLeast(Utils.stripTags(hook.getVersion()), "4.0.0")) {
         throw new DependencyException("Can't hook into TAB, update TAB to 4.0.0 or above", hook);
      } else {
         this.showInPlayerlist = this.getConfigBoolean("Show In Tab", true);
         this.showAboveHead = this.getConfigBoolean("Show Above Player", true);
         this.registerLoadEvent();
      }
   }

   private void registerLoadEvent() {
      EventBus eventBus = TabAPI.getInstance().getEventBus();
      if (eventBus != null && Conf.TOGGLE_NAMETAG_ENABLED.asBool() && !PvPManager.getInstance().isReloading()) {
         eventBus.register(PlayerLoadEvent.class, event -> {
            TabPlayer tabPlayer = event.getPlayer();
            if (event.isJoin()) {
               CombatPlayer combatPlayer = PvPManager.getInstance().getPlayerManager().getUnchecked((Player)tabPlayer.getPlayer());
               if (combatPlayer == null) {
                  return;
               }

               ScheduleUtils.runAsyncLater(() -> {
                  NameTag nameTag = combatPlayer.getNameTag();
                  if (nameTag != null) {
                     nameTag.setPvP(combatPlayer.hasPvPEnabled());
                  }
               }, 1L, TimeUnit.SECONDS);
            }
         });
      }
   }

   @Override
   public String onEnableMessage() {
      return this.getName() + " Found! Using TAB to change nametags instead of scoreboards";
   }

   public boolean showAboveHead() {
      return this.showAboveHead;
   }

   public boolean showInPlayerlist() {
      return this.showInPlayerlist;
   }
}
