package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ClassWeaponAffinity implements Listener {
   private static final int CLASS_WEAPON_BONUS_PERCENT = 10;
   private static final double CLASS_WEAPON_MULTIPLIER = 1.1;
   private static final int OFF_CLASS_WEAPON_PENALTY_PERCENT = 20;
   private static final double OFF_CLASS_WEAPON_MULTIPLIER = 0.8;
   private static final long WARNING_INTERVAL_MILLIS = 300000L;
   private final Map<UUID, Long> lastWarnings = new HashMap<>();
   private final Set<UUID> chatWarnedThisSession = new HashSet<>();

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onEliteDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
      if (!CombatDamageContext.isDamageTransferActive()) {
         Player player = event.getPlayer();
         if (AdvancedCombatModule.isInitialized()) {
            AdvancedCombatModule module = AdvancedCombatModule.get();
            if (module.mechanicsActive(player) && module.hasActiveClass(player)) {
               SkillType weaponSkill = WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand());
               if (weaponSkill != null) {
                  boolean classWeapon = module.activeClassWeaponAffinities(player).contains(weaponSkill);
                  event.setDamage(event.getDamage() * (classWeapon ? 1.1 : 0.8));
                  if (!classWeapon) {
                     this.warnOffClassWeapon(player, module);
                  }
               }
            }
         }
      }
   }

   private void warnOffClassWeapon(Player player, AdvancedCombatModule module) {
      long now = System.currentTimeMillis();
      Long previous = this.lastWarnings.get(player.getUniqueId());
      if (previous == null || now - previous >= 300000L) {
         ClassFormDefinition form = module.profile(player.getUniqueId())
            .flatMap(profile -> profile.optionalActiveLineage())
            .map(active -> module.catalog().require(active.activeFormId()))
            .orElse(null);
         if (form != null) {
            this.lastWarnings.put(player.getUniqueId(), now);
            String header = ClassPresentationTheme.gradient("#7A1F2B:#C2414A", "Off-class weapon");
            String bonus = bonusDescription(form);
            ActionBarCompositor.show(player, ActionBarCompositor.Source.AFFINITY_WARNING, ChatColorConverter.convert(header + " &8» &c-20% damage&7. " + bonus));
            if (this.chatWarnedThisSession.add(player.getUniqueId())) {
               player.sendMessage(ChatColorConverter.convert(header + " &8» &7This weapon does not match your class: &c-20% damage&7. " + bonus));
            }
         }
      }
   }

   public static String description(ClassFormDefinition form) {
      return bonusDescription(form) + " &7Other weapons deal &c20% less damage&7.";
   }

   private static String bonusDescription(ClassFormDefinition form) {
      return "&f"
         + form.weaponAffinities().stream().map(SkillType::getDisplayName).collect(Collectors.joining(" &7and &f"))
         + " &7deal &a10% more damage &7with &f"
         + form.displayName()
         + "&7.";
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.lastWarnings.remove(event.getPlayer().getUniqueId());
      this.chatWarnedThisSession.remove(event.getPlayer().getUniqueId());
   }

   void shutdown() {
      this.lastWarnings.clear();
      this.chatWarnedThisSession.clear();
   }
}
