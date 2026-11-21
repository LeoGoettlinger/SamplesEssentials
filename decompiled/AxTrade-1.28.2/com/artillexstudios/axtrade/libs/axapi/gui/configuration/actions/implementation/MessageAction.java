package com.artillexstudios.axtrade.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PaperPlaceholderHandler;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PlaceholderParameters;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import org.bukkit.entity.Player;

public final class MessageAction extends Action<String> {
   public MessageAction(String data) {
      super(data);
   }

   public String transform(String data) {
      return data;
   }

   @Override
   public void run(Player player, HashMapContext context) {
      ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
      wrapper.message(
         StringUtils.format(
            PaperPlaceholderHandler.parseWithPlaceholderAPI(
               this.value(), new PlaceholderParameters().withParameters(Player.class, player).withParameters(Gui.class, context.get(GuiKeys.GUI))
            )
         )
      );
   }
}
