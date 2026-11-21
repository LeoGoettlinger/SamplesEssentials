package com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axafkzone.libs.axapi.placeholders.PaperPlaceholderHandler;
import com.artillexstudios.axafkzone.libs.axapi.placeholders.PlaceholderParameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ConsoleCommandAction extends Action<String> {
   public ConsoleCommandAction(String data) {
      super(data);
   }

   public String transform(String data) {
      return data;
   }

   @Override
   public void run(Player player, HashMapContext context) {
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         PaperPlaceholderHandler.parseWithPlaceholderAPI(
            this.value(), new PlaceholderParameters().withParameters(Player.class, player).withParameters(Gui.class, context.get(GuiKeys.GUI))
         )
      );
   }
}
