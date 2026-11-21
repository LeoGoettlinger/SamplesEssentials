package com.artillexstudios.axrankmenu.utils;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ItemBuilder;
import java.util.List;
import java.util.Map;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ItemBuilderUtil {
   @NotNull
   public static ItemBuilder newBuilder(@NotNull Section section, Player player) {
      return newBuilder(section, Map.of(), player);
   }

   @NotNull
   public static ItemBuilder newBuilder(@NotNull Section section, Map<String, String> replacements, Player player) {
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
      return builder;
   }

   @NotNull
   @Contract("_ -> new")
   public static ItemBuilder newBuilder(@NotNull ItemStack itemStack) {
      return ItemBuilder.create(itemStack);
   }
}
