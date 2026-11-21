package de.theredend2000.advancedhunt.lib.xseries.paper;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.MiniMessage;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class AdventureAPIFactory {
   private static final MiniMessage DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage();

   public static void setDisplayNameFromString(ItemMeta meta, String miniMessageText) {
      Component displayName = DEFAULT_MINI_MESSAGE.deserialize(miniMessageText);
      meta.displayName(displayName);
   }

   public static void setLoreFromString(ItemMeta meta, List<String> miniMessageLoreLines) {
      List<Component> lore = miniMessageLoreLines.stream()
         .map(line -> (Component)(line.isEmpty() ? Component.empty() : DEFAULT_MINI_MESSAGE.deserialize(line)))
         .collect(Collectors.toList());
      meta.lore(lore);
   }

   public static void setDisplayName(ItemMeta meta, Component displayname) {
      meta.displayName(displayname);
   }

   public static void setLore(ItemMeta meta, List<? extends Component> lore) {
      meta.lore(lore);
   }

   public static String displayName(ItemMeta meta, Function<List<? extends Component>, List<String>> miniMessageHandler) {
      return miniMessageHandler.apply(Collections.singletonList(meta.displayName())).get(0);
   }

   public static List<String> lore(ItemMeta meta, Function<List<? extends Component>, List<String>> miniMessageHandler) {
      return miniMessageHandler.apply(meta.lore());
   }
}
