package com.artillexstudios.axtrade.libs.gui.builder.item;

import com.artillexstudios.axtrade.libs.gui.components.exception.GuiException;
import com.artillexstudios.axtrade.libs.gui.components.util.VersionHelper;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BannerBuilder extends BaseItemBuilder<BannerBuilder> {
   private static final Material DEFAULT_BANNER;
   private static final EnumSet<Material> BANNERS;

   BannerBuilder(@NotNull NameLoreHandler nameLoreHandler) {
      super(new ItemStack(DEFAULT_BANNER), nameLoreHandler);
   }

   BannerBuilder(@NotNull ItemStack itemStack, @NotNull NameLoreHandler nameLoreHandler) {
      super(itemStack, nameLoreHandler);
      if (!BANNERS.contains(itemStack.getType())) {
         throw new GuiException("BannerBuilder requires the material to be a banner!");
      }
   }

   @NotNull
   @Contract("_ -> this")
   public BannerBuilder baseColor(@NotNull DyeColor color) {
      BannerMeta bannerMeta = (BannerMeta)this.getMeta();
      bannerMeta.setBaseColor(color);
      this.setMeta(bannerMeta);
      return this;
   }

   @NotNull
   @Contract("_, _ -> this")
   public BannerBuilder pattern(@NotNull DyeColor color, @NotNull PatternType pattern) {
      BannerMeta bannerMeta = (BannerMeta)this.getMeta();
      bannerMeta.addPattern(new Pattern(color, pattern));
      this.setMeta(bannerMeta);
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public BannerBuilder pattern(@NotNull Pattern... pattern) {
      return this.pattern(Arrays.asList(pattern));
   }

   @NotNull
   @Contract("_ -> this")
   public BannerBuilder pattern(@NotNull List<Pattern> patterns) {
      BannerMeta bannerMeta = (BannerMeta)this.getMeta();

      for (Pattern it : patterns) {
         bannerMeta.addPattern(it);
      }

      this.setMeta(bannerMeta);
      return this;
   }

   @NotNull
   @Contract("_, _, _ -> this")
   public BannerBuilder pattern(int index, @NotNull DyeColor color, @NotNull PatternType pattern) {
      return this.pattern(index, new Pattern(color, pattern));
   }

   @NotNull
   @Contract("_, _ -> this")
   public BannerBuilder pattern(int index, @NotNull Pattern pattern) {
      BannerMeta bannerMeta = (BannerMeta)this.getMeta();
      bannerMeta.setPattern(index, pattern);
      this.setMeta(bannerMeta);
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public BannerBuilder setPatterns(@NotNull List<Pattern> patterns) {
      BannerMeta bannerMeta = (BannerMeta)this.getMeta();
      bannerMeta.setPatterns(patterns);
      this.setMeta(bannerMeta);
      return this;
   }

   static {
      if (VersionHelper.IS_ITEM_LEGACY) {
         DEFAULT_BANNER = Material.valueOf("BANNER");
         BANNERS = EnumSet.of(Material.valueOf("BANNER"));
      } else {
         DEFAULT_BANNER = Material.WHITE_BANNER;
         BANNERS = EnumSet.copyOf(Tag.BANNERS.getValues());
      }
   }
}
