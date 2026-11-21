package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.bundledlibraries.imdanix.text.MiniTranslator;
import com.ranull.graves.libraries.kyori.adventure.platform.bukkit.BukkitAudiences;
import com.ranull.graves.libraries.kyori.adventure.text.Component;
import com.ranull.graves.libraries.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import com.ranull.graves.libraries.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import com.ranull.graves.libraries.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.util.CustomModelDataUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniMessage {
   public static com.ranull.graves.libraries.kyori.adventure.text.minimessage.MiniMessage miniMessage;
   private static BukkitAudiences audiences;
   private static LegacyComponentSerializer legacyComponentSerializer;

   public MiniMessage() {
      audiences = BukkitAudiences.create(JavaPlugin.getPlugin(Graves.class));
      legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().extractUrls().build();
      miniMessage = com.ranull.graves.libraries.kyori.adventure.text.minimessage.MiniMessage.builder()
         .strict(false)
         .tags(
            TagResolver.builder()
               .resolver(StandardTags.defaults())
               .resolver(StandardTags.hoverEvent())
               .resolver(StandardTags.clickEvent())
               .resolver(StandardTags.insertion())
               .resolver(StandardTags.rainbow())
               .resolver(StandardTags.gradient())
               .resolver(StandardTags.newline())
               .resolver(StandardTags.score())
               .resolver(StandardTags.nbt())
               .resolver(StandardTags.font())
               .resolver(StandardTags.decorations())
               .resolver(StandardTags.keybind())
               .resolver(StandardTags.selector())
               .resolver(StandardTags.transition())
               .resolver(StandardTags.translatable())
               .resolver(StandardTags.translatableFallback())
               .resolver(StandardTags.reset())
               .resolver(StandardTags.pride())
               .resolver(StandardTags.shadowColor())
               .build()
         )
         .build();
   }

   public static List<String> parseString(List<String> strings) {
      List<String> parseStrings = new ArrayList<>();

      for (String string : strings) {
         parseStrings.add(parseString(string));
      }

      return parseStrings;
   }

   public static String parseString(String string) {
      return miniMessage != null ? legacyComponentSerializer.serialize(miniMessage.deserialize(string)) : string;
   }

   public static List<String> convertLegacyToMiniMessage(List<String> legacyTexts) {
      List<String> convertedTexts = new ArrayList<>();

      for (String legacyText : legacyTexts) {
         convertedTexts.add(convertLegacyToMiniMessage(legacyText));
      }

      return convertedTexts;
   }

   public static String convertLegacyToMiniMessage(String legacyText) {
      String color = MiniTranslator.toMini(legacyText, MiniTranslator.Option.COLOR);
      String format = MiniTranslator.toMini(color, MiniTranslator.Option.FORMAT);
      String gradient = MiniTranslator.toMini(format, MiniTranslator.Option.GRADIENT);
      String close_color = MiniTranslator.toMini(gradient, MiniTranslator.Option.CLOSE_COLORS);
      String hex_colors = MiniTranslator.toMini(close_color, MiniTranslator.Option.HEX_COLOR_STANDALONE);
      String double_to_escape = MiniTranslator.toMini(hex_colors, MiniTranslator.Option.DOUBLE_TO_ESCAPE);
      String verbose_hex_color = MiniTranslator.toMini(double_to_escape, MiniTranslator.Option.VERBOSE_HEX_COLOR);
      return MiniTranslator.toMini(verbose_hex_color, MiniTranslator.Option.FAST_RESET);
   }

   public static Component convertLegacyToComponent(String legacyText) {
      String miniMessageText = MiniTranslator.toMini(
         legacyText,
         MiniTranslator.Option.COLOR,
         MiniTranslator.Option.FORMAT,
         MiniTranslator.Option.GRADIENT,
         MiniTranslator.Option.CLOSE_COLORS,
         MiniTranslator.Option.HEX_COLOR_STANDALONE,
         MiniTranslator.Option.DOUBLE_TO_ESCAPE,
         MiniTranslator.Option.VERBOSE_HEX_COLOR,
         MiniTranslator.Option.FAST_RESET
      );
      return miniMessage().deserialize(miniMessageText);
   }

   public static ItemStack formatBookMeta(
      Graves plugin, Grave grave, ItemStack itemStack, Component title, Component author, List<Component> pages, List<Component> lore
   ) {
      if (itemStack != null && itemStack.getItemMeta() instanceof BookMeta bookMeta) {
         LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
         String titleString = legacySerializer.serialize(title);
         String authorString = legacySerializer.serialize(author);
         bookMeta.setTitle(titleString);
         bookMeta.setAuthor(authorString);
         List serializedPages = pages.stream().map(legacySerializer::serialize).toList();
         bookMeta.setPages(serializedPages);
         List serializedLore = lore.stream().map(legacySerializer::serialize).toList();
         bookMeta.setLore(serializedLore);
         int customModelData = plugin.getConfigManager().getConfigSection("obituary.model-data", grave).getInt("obituary.model-data", -1);
         CustomModelDataUtil.applyCustomModelData(bookMeta, customModelData);
         itemStack.setItemMeta(bookMeta);
         return itemStack;
      } else {
         return itemStack;
      }
   }

   public static void sendMessage(Player player, String message) {
      if (miniMessage != null && audiences != null) {
         String output = convertLegacyToMiniMessage(message);
         audiences.sender(player).sendMessage(miniMessage.deserialize(output));
      } else {
         player.sendMessage(message);
      }
   }

   public static com.ranull.graves.libraries.kyori.adventure.text.minimessage.MiniMessage miniMessage() {
      return com.ranull.graves.libraries.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
   }
}
