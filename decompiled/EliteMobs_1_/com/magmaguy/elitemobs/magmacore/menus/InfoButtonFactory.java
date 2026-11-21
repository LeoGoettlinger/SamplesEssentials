package com.magmaguy.elitemobs.magmacore.menus;

import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakChatStyle;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public final class InfoButtonFactory {
   private InfoButtonFactory() {
   }

   public static MenuButton nightbreakInfoButton(
      String skullName,
      String displayName,
      List<String> displayLore,
      final String headlineMessage,
      final String wikiLabel,
      final String wikiLinkDisplay,
      final String wikiLinkHover,
      final String wikiUrl,
      final String contentLabel,
      final String contentLinkDisplay,
      final String contentLinkHover,
      final String contentUrl,
      final String discordLabel,
      final String discordLinkDisplay,
      final String discordLinkHover,
      final String discordUrl,
      final String downloadAllClickMessage,
      final String downloadAllClickHover,
      final String downloadAllCommand
   ) {
      return new MenuButton(ItemStackGenerator.generateSkullItemStack(skullName, displayName, displayLore)) {
         @Override
         public void onClick(Player p) {
            p.closeInventory();
            Logger.sendSimpleMessage(p, NightbreakChatStyle.separator());
            Logger.sendSimpleMessage(p, headlineMessage);
            p.spigot()
               .sendMessage(
                  new BaseComponent[]{SpigotMessage.simpleMessage(wikiLabel), SpigotMessage.hoverLinkMessage(wikiLinkDisplay, wikiLinkHover, wikiUrl)}
               );
            p.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(contentLabel), SpigotMessage.hoverLinkMessage(contentLinkDisplay, contentLinkHover, contentUrl)
                  }
               );
            p.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(discordLabel), SpigotMessage.hoverLinkMessage(discordLinkDisplay, discordLinkHover, discordUrl)
                  }
               );
            if (NightbreakAccount.hasToken() && downloadAllClickMessage != null) {
               p.spigot().sendMessage(SpigotMessage.commandHoverMessage(downloadAllClickMessage, downloadAllClickHover, downloadAllCommand));
            }

            Logger.sendSimpleMessage(p, NightbreakChatStyle.separator());
         }
      };
   }
}
