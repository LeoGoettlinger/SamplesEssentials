package de.theredend2000.advancedhunt.platform.impl;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Spigot115PlatformAdapter extends Spigot114PlatformAdapter {
   @Override
   public boolean isAir(Material material) {
      return material == null ? true : material.isAir();
   }

   @Override
   public void sendClickableCopyText(Player player, String displayText, String copyText, String hoverText) {
      TextComponent component = new TextComponent(displayText);
      component.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, copyText));
      if (hoverText != null && !hoverText.isEmpty()) {
         component.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text(hoverText)}));
      }

      player.spigot().sendMessage(component);
   }
}
