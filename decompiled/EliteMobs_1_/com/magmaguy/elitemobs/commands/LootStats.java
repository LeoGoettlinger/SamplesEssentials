package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LootStats extends AdvancedCommand {
   public LootStats() {
      super(List.of("loot"));
      this.addLiteral("stats");
      this.setUsage("/em loot stats");
      this.setPermission("elitemobs.loot.stats");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Provides EliteMobs stats for the currently held item.");
   }

   @Override
   public void execute(CommandData commandData) {
      Player player = commandData.getPlayerSender();
      ItemStack item = player.getInventory().getItemInMainHand();
      double attackSpeed = EliteItemManager.getAttackSpeed(item);
      double damage = EliteItemManager.getBaseDamage(item);
      double dps = EliteItemManager.getDPS(item);
      double itemLevel = EliteItemManager.getWeaponLevel(item);
      double bonusEDPS = EliteItemManager.getTotalDPS(item);
      player.sendMessage(CommandMessagesConfig.getLootStatsHeader());
      player.sendMessage(CommandMessagesConfig.getLootStatsAttackSpeed() + attackSpeed);
      player.sendMessage(CommandMessagesConfig.getLootStatsDamage() + damage);
      player.sendMessage(CommandMessagesConfig.getLootStatsEdps() + dps);
      player.sendMessage(CommandMessagesConfig.getLootStatsLevel() + itemLevel);
      player.sendMessage(CommandMessagesConfig.getLootStatsBonusEdps() + bonusEDPS);
   }
}
