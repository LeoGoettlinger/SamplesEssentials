package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.utils.DebugMessage;
import java.util.List;
import org.bukkit.entity.Player;

public class DebugInfoCommand extends AdvancedCommand {
   public DebugInfoCommand() {
      super(List.of("debug"));
      this.addLiteral("info");
      this.setUsage("/em debug info");
      this.setPermission("elitemobs.debug");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Dumps player skill/gear levels and active damage multipliers for diagnosis.");
   }

   @Override
   public void execute(CommandData commandData) {
      Player player = commandData.getPlayerSender();
      StringBuilder log = new StringBuilder();
      log.append("[EM-DebugInfo:").append(player.getName()).append("] ");
      Logger.sendMessage(player, "&6═════ EM DEBUG INFO ═════");
      Logger.sendMessage(player, "&7Player skills:");
      log.append("skills={");
      boolean first = true;

      for (SkillType type : SkillType.values()) {
         int level = PlayerData.getSkillLevel(player.getUniqueId(), type);
         Logger.sendMessage(player, "&7  " + type.name() + ": &f" + level);
         if (!first) {
            log.append(',');
         }

         log.append(type.name()).append('=').append(level);
         first = false;
      }

      log.append('}');
      int combatLevel = PlayerData.getPlayerLevel(player.getUniqueId());
      Logger.sendMessage(player, "&7Combat level: &f" + combatLevel);
      log.append(" combatLevel=").append(combatLevel);
      Logger.sendMessage(player, "&6── Active damage multipliers (in-memory) ──");
      Logger.sendMessage(player, "&7damageToEliteMobMultiplierV2:        &f" + String.format("%.3f", MobCombatSettingsConfig.getDamageToEliteMultiplier()));
      Logger.sendMessage(player, "&7damageToPlayerMultiplierV2:          &f" + String.format("%.3f", MobCombatSettingsConfig.getDamageToPlayerMultiplier()));
      Logger.sendMessage(
         player, "&7normalizedDamageToEliteMultiplier:   &f" + String.format("%.3f", MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier())
      );
      Logger.sendMessage(
         player, "&7normalizedDamageToPlayerMultiplier:  &f" + String.format("%.3f", MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier())
      );
      Logger.sendMessage(
         player, "&7scaledDamageToEliteMultiplier:       &f" + String.format("%.3f", MobCombatSettingsConfig.getScaledDamageToEliteMultiplier())
      );
      Logger.sendMessage(
         player, "&7scaledDamageToPlayerMultiplier:      &f" + String.format("%.3f", MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier())
      );
      log.append(
         String.format(
            " multipliers={defaultToElite=%.3f,defaultToPlayer=%.3f,normalizedToElite=%.3f,normalizedToPlayer=%.3f,scaledToElite=%.3f,scaledToPlayer=%.3f}",
            MobCombatSettingsConfig.getDamageToEliteMultiplier(),
            MobCombatSettingsConfig.getDamageToPlayerMultiplier(),
            MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier(),
            MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier(),
            MobCombatSettingsConfig.getScaledDamageToEliteMultiplier(),
            MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier()
         )
      );
      boolean debugEnabled = DebugMessage.isDebugEnabled(player);
      Logger.sendMessage(player, "&7Per-hit debug breakdown: &f" + (debugEnabled ? "&aENABLED" : "&cdisabled (run /em debug)"));
      log.append(" debug=").append(debugEnabled);
      Logger.sendMessage(player, "&6═════════════════════════");
      Logger.warn(log.toString());
   }
}
