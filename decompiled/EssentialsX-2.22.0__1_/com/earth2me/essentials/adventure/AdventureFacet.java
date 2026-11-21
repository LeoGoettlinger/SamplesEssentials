package com.earth2me.essentials.adventure;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface AdventureFacet {
   void close();

   ComponentHolder deserializeMiniMessage(String message);

   void send(CommandSender sender, ComponentHolder component);

   void send(Player player, ComponentHolder component);

   String legacyToMini(String message);

   String legacyToMini(String message, boolean useCustomTags);

   String legacyToMiniWithUrls(String message);

   String escapeTags(String input);

   String stripTags(String input);

   ComponentHolder legacyToAdventure(String message);

   ComponentHolder text(String message);

   String miniToLegacy(String message);

   String adventureToLegacy(ComponentHolder component);

   ComponentHolder append(ComponentHolder base, ComponentHolder... addition);
}
