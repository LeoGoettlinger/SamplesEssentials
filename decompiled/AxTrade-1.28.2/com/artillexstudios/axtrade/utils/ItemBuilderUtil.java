package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axtrade.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.ItemBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBuilderUtil {
   @NotNull
   public static ItemBuilder newBuilder(@NotNull Section section, @Nullable Player player) {
      return newBuilder(section, Map.of(), player);
   }

   @NotNull
   public static ItemBuilder newBuilder(@NotNull Section section, Map<String, String> replacements, @Nullable Player player) {
      ItemBuilder builder = ItemBuilder.create(section);
      section.getOptionalString("name").ifPresent(name -> {
         if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            name = PlaceholderAPI.setPlaceholders(player, name);
         }

         builder.setName(name, replacements);
      });
      section.getOptionalStringList("lore").ifPresent(lore -> {
         if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            lore = PlaceholderAPI.setPlaceholders(player, lore);
         }

         builder.setLore((List<String>)lore, replacements);
      });
      section.getOptionalString("texture").ifPresent(texture -> {
         for (Entry<String, String> entry : replacements.entrySet()) {
            texture = texture.replace(entry.getKey(), entry.getValue());
         }

         builder.setTextureValue(texture);
      });
      return builder;
   }

   @NotNull
   @Contract("_ -> new")
   public static ItemBuilder newBuilder(@NotNull ItemStack itemStack) {
      return ItemBuilder.create(itemStack);
   }
}
