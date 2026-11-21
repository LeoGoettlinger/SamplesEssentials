package com.artillexstudios.axrankmenu.libs.gui.builder.item;

import com.artillexstudios.axrankmenu.libs.gui.components.exception.GuiException;
import com.artillexstudios.axrankmenu.libs.gui.components.util.Legacy;
import com.artillexstudios.axrankmenu.libs.gui.components.util.VersionHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class BukkitNameLoreHandler implements NameLoreHandler {
   private static final Field DISPLAY_NAME_FIELD;
   private static final Field LORE_FIELD;
   private static final BukkitNameLoreHandler INSTANCE = new BukkitNameLoreHandler();

   @NotNull
   public static BukkitNameLoreHandler getInstance() {
      return INSTANCE;
   }

   @Override
   public void name(@NotNull ItemMeta itemMeta, @NotNull Component name) {
      if (VersionHelper.IS_COMPONENT_LEGACY) {
         itemMeta.setDisplayName(Legacy.SERIALIZER.serialize(name));
      } else {
         try {
            DISPLAY_NAME_FIELD.set(itemMeta, this.serializeComponent(name));
         } catch (IllegalAccessException var4) {
            throw new GuiException("Could not set display name for ItemBuilder.", var4);
         }
      }
   }

   @Override
   public void lore(@NotNull ItemMeta itemMeta, @NotNull List<Component> lore) {
      if (VersionHelper.IS_COMPONENT_LEGACY) {
         itemMeta.setLore(lore.stream().filter(Objects::nonNull).map(Legacy.SERIALIZER::serialize).collect(Collectors.toList()));
      } else {
         List<Object> jsonLore = lore.stream().filter(Objects::nonNull).map(this::serializeComponent).collect(Collectors.toList());

         try {
            LORE_FIELD.set(itemMeta, jsonLore);
         } catch (IllegalAccessException var5) {
            throw new GuiException("Could not set lore for ItemBuilder.", var5);
         }
      }
   }

   @Override
   public void lore(@NotNull ItemMeta itemMeta, @NotNull Consumer<List<Component>> lore) {
      List<Component> components;
      if (VersionHelper.IS_COMPONENT_LEGACY) {
         List<String> stringLore = itemMeta.getLore();
         components = (List<Component>)(stringLore == null
            ? new ArrayList<>()
            : stringLore.stream().map(Legacy.SERIALIZER::deserialize).collect(Collectors.toList()));
      } else {
         try {
            List<Object> jsonLore = (List<Object>)LORE_FIELD.get(itemMeta);
            components = (List<Component>)(jsonLore == null
               ? new ArrayList<>()
               : jsonLore.stream().map(this::deserializeComponent).collect(Collectors.toList()));
         } catch (IllegalAccessException var5) {
            throw new GuiException("Could not get lore for ItemBuilder.", var5);
         }
      }

      lore.accept(components);
      this.lore(itemMeta, components);
   }

   @NotNull
   private Object serializeComponent(@NotNull Component component) {
      return VersionHelper.IS_ITEM_NAME_COMPONENT
         ? MinecraftComponentSerializer.get().serialize(component)
         : GsonComponentSerializer.gson().serialize(component);
   }

   @NotNull
   private Component deserializeComponent(@NotNull Object obj) {
      return VersionHelper.IS_ITEM_NAME_COMPONENT
         ? MinecraftComponentSerializer.get().deserialize(obj)
         : GsonComponentSerializer.gson().deserialize((String)obj);
   }

   static {
      try {
         Class<?> metaClass = VersionHelper.craftClass("inventory.CraftMetaItem");
         DISPLAY_NAME_FIELD = metaClass.getDeclaredField("displayName");
         DISPLAY_NAME_FIELD.setAccessible(true);
         LORE_FIELD = metaClass.getDeclaredField("lore");
         LORE_FIELD.setAccessible(true);
      } catch (ClassNotFoundException | NoSuchFieldException var1) {
         throw new GuiException("Could not retrieve displayName nor lore field for ItemBuilder.", var1);
      }
   }
}
