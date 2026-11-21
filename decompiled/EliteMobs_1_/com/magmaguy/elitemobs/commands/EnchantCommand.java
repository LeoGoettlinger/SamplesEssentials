package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import java.util.List;

public class EnchantCommand extends AdvancedCommand {
   public EnchantCommand() {
      super(List.of("enchant"));
      this.setUsage("/em enchant");
      this.setPermission("elitemobs.enchant.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the enchantment menu or teleports the player to the Adventurer's Guild Hub");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
         new ItemEnchantmentMenu(commandData.getPlayerSender());
      }
   }
}
