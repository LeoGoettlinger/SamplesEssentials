package com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder;

import com.mojang.serialization.DataResult;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BannerPatternLayers.Layer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlaceholderLoaderBase {
   private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(List.of(), List.of(), List.of("openinv:custom"), List.of());
   private static final TooltipDisplay HIDE_TOOLTIP = new TooltipDisplay(true, new LinkedHashSet());

   public void load(@Nullable ConfigurationSection section) throws Exception {
      Placeholders.craftingOutput = this.parse(section, "crafting-output", this.defaultCraftingOutput());
      Placeholders.cursor = this.parse(section, "cursor", this.defaultCursor());
      Placeholders.drop = this.parse(section, "drop", this.defaultDrop());
      Placeholders.emptyHelmet = this.parse(section, "empty-helmet", this.getEmptyArmor(Items.LEATHER_HELMET));
      Placeholders.emptyChestplate = this.parse(section, "empty-chestplate", this.getEmptyArmor(Items.LEATHER_CHESTPLATE));
      Placeholders.emptyLeggings = this.parse(section, "empty-leggings", this.getEmptyArmor(Items.LEATHER_LEGGINGS));
      Placeholders.emptyBoots = this.parse(section, "empty-boots", this.getEmptyArmor(Items.LEATHER_BOOTS));
      Placeholders.emptyOffHand = this.parse(section, "empty-off-hand", this.defaultShield());
      Placeholders.notSlot = this.parse(section, "not-a-slot", this.defaultNotSlot());
      Placeholders.blockedOffline = this.parse(section, "blocked.offline", this.defaultBlockedOffline());

      for (GameType type : GameType.values()) {
         ItemStack typeItem = new ItemStack(Items.BARRIER);
         typeItem.set(DataComponents.ITEM_NAME, Component.translatable("options.narrator.notavailable").append(" - ").append(type.getShortDisplayName()));
         Placeholders.BLOCKED_GAME_TYPE.put((Enum)type, typeItem);
      }

      Placeholders.BLOCKED_GAME_TYPE.put(GameType.CREATIVE, this.parse(section, "blocked.creative", Placeholders.BLOCKED_GAME_TYPE.get(GameType.CREATIVE)));
      Placeholders.BLOCKED_GAME_TYPE.put(GameType.SPECTATOR, this.parse(section, "blocked.spectator", Placeholders.BLOCKED_GAME_TYPE.get(GameType.SPECTATOR)));
   }

   @NotNull
   protected ItemStack parse(@Nullable ConfigurationSection section, @NotNull String path, @NotNull ItemStack defaultStack) throws Exception {
      if (section == null) {
         return defaultStack;
      } else {
         String itemText = section.getString(path);
         if (itemText == null) {
            return defaultStack;
         } else {
            CompoundTag compoundTag = TagParser.parseCompoundFully(itemText);
            DataResult<ItemStack> parsed = ItemStack.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), compoundTag);

            ItemStack itemStack;
            try {
               itemStack = (ItemStack)parsed.getOrThrow();
            } catch (Exception var9) {
               itemStack = null;
            }

            return itemStack == null ? defaultStack : itemStack;
         }
      }
   }

   protected void addModelData(@NotNull ItemStack itemStack) {
      itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
   }

   protected void hideTooltip(@NotNull ItemStack itemStack) {
      itemStack.set(DataComponents.TOOLTIP_DISPLAY, HIDE_TOOLTIP);
   }

   @NotNull
   protected ItemStack defaultCraftingOutput() {
      ItemStack itemStack = new ItemStack(Items.CRAFTING_TABLE);
      itemStack.set(DataComponents.ITEM_NAME, Component.translatable("container.crafting"));
      this.addModelData(itemStack);
      return itemStack;
   }

   @NotNull
   protected ItemStack defaultCursor() {
      ItemStack itemStack = new ItemStack(this.getWhiteBanner());
      RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
      Registry<BannerPattern> bannerPatterns = minecraftRegistry.lookupOrThrow(Registries.BANNER_PATTERN);
      BannerPattern halfDiagBottomRight = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.DIAGONAL_RIGHT).value();
      BannerPattern downRight = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.STRIPE_DOWNRIGHT).value();
      BannerPattern border = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.BORDER).value();
      itemStack.set(
         DataComponents.BANNER_PATTERNS,
         new BannerPatternLayers(
            List.of(
               new Layer(bannerPatterns.wrapAsHolder(halfDiagBottomRight), DyeColor.GRAY),
               new Layer(bannerPatterns.wrapAsHolder(downRight), DyeColor.WHITE),
               new Layer(bannerPatterns.wrapAsHolder(border), DyeColor.GRAY)
            )
         )
      );
      this.addModelData(itemStack);
      this.hideTooltip(itemStack);
      return itemStack;
   }

   @NotNull
   protected abstract Item getWhiteBanner();

   @NotNull
   protected ItemStack defaultDrop() {
      ItemStack itemStack = new ItemStack(Items.DROPPER);
      itemStack.set(DataComponents.ITEM_NAME, Component.translatable("key.drop"));
      this.addModelData(itemStack);
      return itemStack;
   }

   @NotNull
   protected ItemStack getEmptyArmor(@NotNull ItemLike item) {
      ItemStack itemStack = new ItemStack(item);
      DyedItemColor color = new DyedItemColor(13158600);
      itemStack.set(DataComponents.DYED_COLOR, color);
      this.hideTooltip(itemStack);
      this.addModelData(itemStack);
      return itemStack;
   }

   @NotNull
   protected ItemStack defaultShield() {
      ItemStack itemStack = new ItemStack(Items.SHIELD);
      itemStack.set(DataComponents.BASE_COLOR, DyeColor.MAGENTA);
      RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
      Registry<BannerPattern> bannerPatterns = minecraftRegistry.lookupOrThrow(Registries.BANNER_PATTERN);
      BannerPattern halfLeft = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.HALF_VERTICAL).value();
      BannerPattern topLeft = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_LEFT).value();
      BannerPattern topRight = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_RIGHT).value();
      BannerPattern bottomLeft = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_LEFT).value();
      BannerPattern bottomRight = (BannerPattern)bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_RIGHT).value();
      itemStack.set(
         DataComponents.BANNER_PATTERNS,
         new BannerPatternLayers(
            List.of(
               new Layer(bannerPatterns.wrapAsHolder(halfLeft), DyeColor.BLACK),
               new Layer(bannerPatterns.wrapAsHolder(topLeft), DyeColor.MAGENTA),
               new Layer(bannerPatterns.wrapAsHolder(bottomLeft), DyeColor.MAGENTA),
               new Layer(bannerPatterns.wrapAsHolder(topRight), DyeColor.BLACK),
               new Layer(bannerPatterns.wrapAsHolder(bottomRight), DyeColor.BLACK)
            )
         )
      );
      this.hideTooltip(itemStack);
      this.addModelData(itemStack);
      return itemStack;
   }

   @NotNull
   protected ItemStack defaultNotSlot() {
      ItemStack itemStack = new ItemStack(this.getWhiteGlassPane());
      this.hideTooltip(itemStack);
      this.addModelData(itemStack);
      return itemStack;
   }

   @NotNull
   protected abstract Item getWhiteGlassPane();

   @NotNull
   protected ItemStack defaultBlockedOffline() {
      ItemStack itemStack = new ItemStack(Items.BARRIER);
      itemStack.set(
         DataComponents.ITEM_NAME,
         Component.translatable("options.narrator.notavailable")
            .append(Component.literal(" - "))
            .append(Component.translatable("gui.socialInteractions.status_offline"))
      );
      return itemStack;
   }
}
