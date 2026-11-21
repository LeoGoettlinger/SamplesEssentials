package com.artillexstudios.axtrade.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PaperPlaceholderHandler;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PlaceholderParameters;
import org.bukkit.entity.Player;

public final class PlayerCommandAction extends Action<String> {
   public PlayerCommandAction(String data) {
      super(data);
   }

   public String transform(String data) {
      return data;
   }

   @Override
   public void run(Player player, HashMapContext context) {
      player.performCommand(
         PaperPlaceholderHandler.parseWithPlaceholderAPI(
            this.value(), new PlaceholderParameters().withParameters(Player.class, player).withParameters(Gui.class, context.get(GuiKeys.GUI))
         )
      );
   }
}
