package com.artillexstudios.axrankmenu.libs.gui.builder.item;

import com.artillexstudios.axrankmenu.libs.gui.components.exception.GuiException;
import com.artillexstudios.axrankmenu.libs.gui.components.util.SkullUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ItemBuilder extends BaseItemBuilder<ItemBuilder> {
   ItemBuilder(@NotNull ItemStack itemStack) {
      super(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static ItemBuilder from(@NotNull ItemStack itemStack) {
      return new ItemBuilder(itemStack);
   }

   @NotNull
   @Contract("_ -> new")
   public static ItemBuilder from(@NotNull Material material) {
      return new ItemBuilder(new ItemStack(material));
   }

   @NotNull
   @Contract(" -> new")
   public static BannerBuilder banner() {
      return new BannerBuilder(BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static BannerBuilder banner(@NotNull ItemStack itemStack) {
      return new BannerBuilder(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static BookBuilder book(@NotNull ItemStack itemStack) {
      return new BookBuilder(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract(" -> new")
   public static FireworkBuilder firework() {
      return new FireworkBuilder(new ItemStack(Material.FIREWORK_ROCKET), BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static FireworkBuilder firework(@NotNull ItemStack itemStack) {
      return new FireworkBuilder(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract(" -> new")
   public static MapBuilder map() {
      return new MapBuilder(BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static MapBuilder map(@NotNull ItemStack itemStack) {
      return new MapBuilder(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract(" -> new")
   public static SkullBuilder skull() {
      return new SkullBuilder(BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static SkullBuilder skull(@NotNull ItemStack itemStack) {
      return new SkullBuilder(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract(" -> new")
   public static FireworkBuilder star() {
      return new FireworkBuilder(new ItemStack(Material.FIREWORK_STAR), BukkitNameLoreHandler.getInstance());
   }

   @NotNull
   @Contract("_ -> new")
   public static FireworkBuilder star(@NotNull ItemStack itemStack) {
      return new FireworkBuilder(itemStack, BukkitNameLoreHandler.getInstance());
   }

   @Deprecated
   public ItemBuilder setSkullTexture(@NotNull String texture) {
      if (!SkullUtil.isPlayerSkull(this.getItemStack())) {
         return this;
      } else {
         SkullMeta skullMeta = (SkullMeta)this.getMeta();
         GameProfile profile = new GameProfile(UUID.randomUUID(), null);
         profile.getProperties().put("textures", new Property("textures", texture));

         try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
         } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException var6) {
            throw new GuiException("Failed to set profile field!", var6);
         }

         this.setMeta(skullMeta);
         return this;
      }
   }

   @Deprecated
   public ItemBuilder setSkullOwner(@NotNull OfflinePlayer player) {
      if (!SkullUtil.isPlayerSkull(this.getItemStack())) {
         return this;
      } else {
         SkullMeta skullMeta = (SkullMeta)this.getMeta();
         skullMeta.setOwningPlayer(player);
         this.setMeta(skullMeta);
         return this;
      }
   }
}
