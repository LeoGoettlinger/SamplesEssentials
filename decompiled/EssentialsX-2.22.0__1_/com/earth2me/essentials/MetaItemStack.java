package com.earth2me.essentials;

import com.earth2me.essentials.items.transform.PluginItemTransformer;
import com.earth2me.essentials.textreader.BookInput;
import com.earth2me.essentials.textreader.BookPager;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.ess3.api.TranslatableException;
import net.ess3.provider.BannerDataProvider;
import net.ess3.provider.ItemUnbreakableProvider;
import net.ess3.provider.PotionMetaProvider;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MetaItemStack {
   private static final Map<String, DyeColor> colorMap = new HashMap<>();
   private static final Map<String, Type> fireworkShape = new HashMap<>();
   private static final transient Map<String, PluginItemTransformer> itemTransformers = new HashMap<>();
   private static boolean useNewSkullMethod = true;
   private static final transient Pattern splitPattern;
   private static final transient Pattern hexPattern;
   private ItemStack stack;
   private Builder builder = FireworkEffect.builder();
   private PotionEffectType pEffectType;
   private PotionEffect pEffect;
   private boolean validFirework = false;
   private boolean validFireworkCharge = false;
   private boolean validPotionEffect = false;
   private boolean validPotionDuration = false;
   private boolean validPotionPower = false;
   private boolean isSplashPotion = false;
   private boolean completePotion = false;
   private int power = 1;
   private int duration = 120;

   public MetaItemStack(final ItemStack stack) {
      this.stack = stack.clone();
   }

   public static void registerItemTransformer(final String key, final PluginItemTransformer itemTransformer) {
      if (itemTransformers.containsKey(key)) {
         Essentials.getWrappedLogger()
            .log(
               Level.WARNING,
               String.format(
                  "Plugin transformer registered to \"%s\" attempted to register already existing item transformer \"%s\" belonging to \"%s\"!",
                  itemTransformer.getPlugin().getName(),
                  key,
                  itemTransformers.get(key).getPlugin().getName()
               )
            );
      } else {
         itemTransformers.put(key, itemTransformer);
      }
   }

   public static void unregisterItemTransformer(final String key) {
      itemTransformers.remove(key);
   }

   private static void setSkullOwner(final net.ess3.api.IEssentials ess, final ItemStack stack, final String owner) {
      if (stack.getItemMeta() instanceof SkullMeta) {
         SkullMeta meta = (SkullMeta)stack.getItemMeta();
         if (useNewSkullMethod) {
            try {
               meta.setOwningPlayer(ess.getServer().getOfflinePlayer(owner));
               stack.setItemMeta(meta);
               return;
            } catch (NoSuchMethodError var5) {
               useNewSkullMethod = false;
            }
         }

         meta.setOwner(owner);
         stack.setItemMeta(meta);
      }
   }

   public ItemStack getItemStack() {
      return this.stack;
   }

   public boolean isValidFirework() {
      return this.validFirework;
   }

   public boolean isValidPotion() {
      return this.validPotionEffect && this.validPotionDuration && this.validPotionPower;
   }

   public Builder getFireworkBuilder() {
      return this.builder;
   }

   public PotionEffect getPotionEffect() {
      return this.pEffect;
   }

   public boolean completePotion() {
      return this.completePotion;
   }

   private void resetPotionMeta() {
      this.pEffect = null;
      this.pEffectType = null;
      this.validPotionEffect = false;
      this.validPotionDuration = false;
      this.validPotionPower = false;
      this.isSplashPotion = false;
      this.completePotion = true;
   }

   public boolean canSpawn(final net.ess3.api.IEssentials ess) {
      if (VersionUtil.PRE_FLATTENING) {
         try {
            ess.getServer().getUnsafe().modifyItemStack(this.stack.clone(), "{}");
            return true;
         } catch (NoSuchMethodError var3) {
            return true;
         } catch (Throwable var4) {
            if (ess.getSettings().isDebug()) {
               ess.getLogger().log(Level.INFO, "Itemstack is invalid", var4);
            }

            return false;
         }
      } else {
         return this.stack.getType().isItem();
      }
   }

   public void parseStringMeta(
      final CommandSource sender, final boolean allowUnsafe, final String[] string, final int fromArg, final net.ess3.api.IEssentials ess
   ) throws Exception {
      boolean nbtIsKill = VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_20_6_R01);
      if (string[fromArg].startsWith("{") && this.hasMetaPermission(sender, "vanilla", false, true, ess)) {
         if (nbtIsKill) {
            throw new TranslatableException("noMetaNbtKill");
         }

         try {
            this.stack = ess.getServer().getUnsafe().modifyItemStack(this.stack, Joiner.on(' ').join(Arrays.asList(string).subList(fromArg, string.length)));
         } catch (NullPointerException var11) {
            if (ess.getSettings().isDebug()) {
               ess.getLogger().log(Level.INFO, "Itemstack is invalid", (Throwable)var11);
            }
         } catch (NoSuchMethodError var12) {
            throw new TranslatableException(var12, "noMetaJson");
         } catch (Throwable var13) {
            throw new Exception(var13.getMessage(), var13);
         }
      } else if (string[fromArg].startsWith("[") && this.hasMetaPermission(sender, "vanilla", false, true, ess)) {
         if (!nbtIsKill) {
            throw new TranslatableException("noMetaComponents");
         }

         try {
            String components = Joiner.on(' ').join(Arrays.asList(string).subList(fromArg, string.length));
            this.stack = ess.getServer().getUnsafe().modifyItemStack(this.stack, this.stack.getType().getKey() + components);
         } catch (NullPointerException var9) {
            if (ess.getSettings().isDebug()) {
               ess.getLogger().log(Level.INFO, "Itemstack is invalid", (Throwable)var9);
            }
         } catch (Throwable var10) {
            throw new Exception(var10.getMessage(), var10);
         }
      } else {
         for (int i = fromArg; i < string.length; i++) {
            this.addStringMeta(sender, allowUnsafe, string[i], ess);
         }

         if (this.validFirework) {
            if (!this.hasMetaPermission(sender, "firework", true, true, ess)) {
               throw new TranslatableException("noMetaFirework");
            }

            FireworkEffect effect = this.builder.build();
            FireworkMeta fmeta = (FireworkMeta)this.stack.getItemMeta();
            fmeta.addEffect(effect);
            if (fmeta.getEffects().size() > 1 && !this.hasMetaPermission(sender, "firework-multiple", true, true, ess)) {
               throw new TranslatableException("multipleCharges");
            }

            this.stack.setItemMeta(fmeta);
         }

         if (this.validFireworkCharge) {
            if (!this.hasMetaPermission(sender, "firework", true, true, ess)) {
               throw new TranslatableException("noMetaFirework");
            }

            FireworkEffect effect = this.builder.build();
            FireworkEffectMeta meta = (FireworkEffectMeta)this.stack.getItemMeta();
            meta.setEffect(effect);
            this.stack.setItemMeta(meta);
         }
      }
   }

   public void addStringMeta(final CommandSource sender, final boolean allowUnsafe, final String string, final net.ess3.api.IEssentials ess) throws Exception {
      String[] split = splitPattern.split(string, 2);
      if (split.length >= 1) {
         if (split.length > 1 && split[0].equalsIgnoreCase("name") && this.hasMetaPermission(sender, "name", false, true, ess)) {
            String displayName = FormatUtil.replaceFormat(split[1].replaceAll("(?<!\\\\)_", " ").replace("\\_", "_"));
            ItemMeta meta = this.stack.getItemMeta();
            meta.setDisplayName(displayName);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1
            && (split[0].equalsIgnoreCase("lore") || split[0].equalsIgnoreCase("desc"))
            && this.hasMetaPermission(sender, "lore", false, true, ess)) {
            List<String> lore = new ArrayList<>();

            for (String line : split[1].split("(?<!\\\\)\\|")) {
               lore.add(FormatUtil.replaceFormat(line.replaceAll("(?<!\\\\)_", " ").replace("\\_", "_").replace("\\|", "|")));
            }

            ItemMeta meta = this.stack.getItemMeta();
            meta.setLore(lore);
            this.stack.setItemMeta(meta);
         } else if ((split[0].equalsIgnoreCase("custom-model-data") || split[0].equalsIgnoreCase("cmd"))
            && this.hasMetaPermission(sender, "custom-model-data", false, true, ess)) {
            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_14_R01)) {
               int value = split.length <= 1 ? 0 : Integer.parseInt(split[1]);
               ItemMeta meta = this.stack.getItemMeta();
               meta.setCustomModelData(value);
               this.stack.setItemMeta(meta);
            }
         } else if (split[0].equalsIgnoreCase("unbreakable") && this.hasMetaPermission(sender, "unbreakable", false, true, ess)) {
            boolean value = split.length <= 1 || Boolean.parseBoolean(split[1]);
            this.setUnbreakable(ess, this.stack, value);
         } else if (split.length > 1
            && (split[0].equalsIgnoreCase("player") || split[0].equalsIgnoreCase("owner"))
            && this.hasMetaPermission(sender, "head", false, true, ess)) {
            if (!MaterialUtil.isPlayerHead(this.stack)) {
               throw new TranslatableException("onlyPlayerSkulls");
            }

            String owner = split[1];
            setSkullOwner(ess, this.stack, owner);
         } else if (split.length <= 1
            || !split[0].equalsIgnoreCase("book")
            || !MaterialUtil.isEditableBook(this.stack.getType())
            || !this.hasMetaPermission(sender, "book", true, true, ess)
               && !this.hasMetaPermission(sender, "chapter-" + split[1].toLowerCase(Locale.ENGLISH), true, true, ess)) {
            if (split.length > 1
               && split[0].equalsIgnoreCase("author")
               && this.stack.getType() == Material.WRITTEN_BOOK
               && this.hasMetaPermission(sender, "author", false, true, ess)) {
               String author = FormatUtil.replaceFormat(split[1]);
               BookMeta meta = (BookMeta)this.stack.getItemMeta();
               meta.setAuthor(author);
               this.stack.setItemMeta(meta);
            } else if (split.length > 1
               && split[0].equalsIgnoreCase("title")
               && this.stack.getType() == Material.WRITTEN_BOOK
               && this.hasMetaPermission(sender, "title", false, true, ess)) {
               String title = FormatUtil.replaceFormat(split[1].replaceAll("(?<!\\\\)_", " ").replace("\\_", "_"));
               BookMeta meta = (BookMeta)this.stack.getItemMeta();
               meta.setTitle(title);
               this.stack.setItemMeta(meta);
            } else if (split.length > 1
               && split[0].startsWith("page")
               && split[0].length() > 4
               && MaterialUtil.isEditableBook(this.stack.getType())
               && this.hasMetaPermission(sender, "page", false, true, ess)) {
               int page = NumberUtil.isInt(split[0].substring(4)) ? Integer.parseInt(split[0].substring(4)) - 1 : 0;
               if (page > 100) {
                  throw new TranslatableException("pageLimitExceeded");
               }

               BookMeta meta = (BookMeta)this.stack.getItemMeta();
               List<String> pages = meta.hasPages() ? new ArrayList<>(meta.getPages()) : new ArrayList<>();
               List<String> lines = new ArrayList<>();

               for (String line : split[1].split("(?<!\\\\)\\|")) {
                  lines.add(FormatUtil.replaceFormat(line.replaceAll("(?<!\\\\)_", " ").replace("\\_", "_").replace("\\|", "|")));
               }

               String content = String.join("\n", lines);
               if (page >= pages.size()) {
                  for (int i = 0; i <= page - pages.size(); i++) {
                     pages.add("");
                  }
               }

               pages.set(page, content);
               meta.setPages(pages);
               this.stack.setItemMeta(meta);
            } else if (split.length > 1
               && split[0].equalsIgnoreCase("power")
               && MaterialUtil.isFirework(this.stack.getType())
               && this.hasMetaPermission(sender, "firework-power", false, true, ess)) {
               int power = NumberUtil.isInt(split[1]) ? Integer.parseInt(split[1]) : 0;
               FireworkMeta meta = (FireworkMeta)this.stack.getItemMeta();
               meta.setPower(power > 3 ? 4 : power);
               this.stack.setItemMeta(meta);
            } else if (split.length > 1 && split[0].equalsIgnoreCase("itemflags") && this.hasMetaPermission(sender, "itemflags", false, true, ess)) {
               this.addItemFlags(string);
            } else if (MaterialUtil.isFirework(this.stack.getType())) {
               if (!this.parseEnchantmentStrings(sender, allowUnsafe, split, ess)) {
                  this.addFireworkMeta(sender, false, string, ess);
               }
            } else if (MaterialUtil.isFireworkCharge(this.stack.getType())) {
               if (!this.parseEnchantmentStrings(sender, allowUnsafe, split, ess)) {
                  this.addChargeMeta(sender, false, string, ess);
               }
            } else if (MaterialUtil.isPotion(this.stack.getType())) {
               if (split[0].equalsIgnoreCase("power") || !this.parseEnchantmentStrings(sender, allowUnsafe, split, ess)) {
                  this.addPotionMeta(sender, false, string, ess);
               }
            } else if (MaterialUtil.isBanner(this.stack.getType())) {
               if (!this.parseEnchantmentStrings(sender, allowUnsafe, split, ess)) {
                  this.addBannerMeta(sender, false, string, ess);
               }
            } else if (split.length > 1
               && (split[0].equalsIgnoreCase("color") || split[0].equalsIgnoreCase("colour"))
               && MaterialUtil.isLeatherArmor(this.stack.getType())) {
               String[] color = split[1].split("[|,]");
               if (color.length == 1 && (NumberUtil.isInt(color[0]) || color[0].startsWith("#"))) {
                  LeatherArmorMeta meta = (LeatherArmorMeta)this.stack.getItemMeta();
                  String input = color[0];
                  if (input.startsWith("#")) {
                     meta.setColor(
                        Color.fromRGB(
                           Integer.valueOf(input.substring(1, 3), 16), Integer.valueOf(input.substring(3, 5), 16), Integer.valueOf(input.substring(5, 7), 16)
                        )
                     );
                  } else {
                     meta.setColor(Color.fromRGB(Integer.parseInt(input)));
                  }

                  this.stack.setItemMeta(meta);
               } else {
                  if (color.length != 3) {
                     throw new TranslatableException("leatherSyntax");
                  }

                  int red = NumberUtil.isInt(color[0]) ? Integer.parseInt(color[0]) : 0;
                  int green = NumberUtil.isInt(color[1]) ? Integer.parseInt(color[1]) : 0;
                  int blue = NumberUtil.isInt(color[2]) ? Integer.parseInt(color[2]) : 0;
                  LeatherArmorMeta meta = (LeatherArmorMeta)this.stack.getItemMeta();
                  meta.setColor(Color.fromRGB(red, green, blue));
                  this.stack.setItemMeta(meta);
               }
            } else if (MaterialUtil.isArmor(this.stack.getType()) && split.length > 1 && split[0].equalsIgnoreCase("trim")) {
               ArmorMeta armorMeta = (ArmorMeta)this.stack.getItemMeta();
               String[] trimData = split[1].split("\\|");
               TrimPattern pattern = (TrimPattern)Registry.TRIM_PATTERN.getOrThrow(NamespacedKey.minecraft(trimData[0].toLowerCase()));
               TrimMaterial material = (TrimMaterial)Registry.TRIM_MATERIAL.getOrThrow(NamespacedKey.minecraft(trimData[1].toLowerCase()));
               armorMeta.setTrim(new ArmorTrim(material, pattern));
               this.stack.setItemMeta(armorMeta);
            } else if (split.length > 1 && itemTransformers.containsKey(split[0])) {
               this.transformItem(split[0], split[1]);
            } else {
               this.parseEnchantmentStrings(sender, allowUnsafe, split, ess);
            }
         } else {
            BookMeta meta = (BookMeta)this.stack.getItemMeta();
            IText input = new BookInput("book", true, ess);
            BookPager pager = new BookPager(input);
            if (this.stack.getType() == Material.WRITTEN_BOOK) {
               if (!meta.hasAuthor()) {
                  meta.setAuthor(sender == null ? Console.getInstance().getDisplayName() : sender.getPlayer().getName());
               }

               if (!meta.hasTitle()) {
                  String title = FormatUtil.replaceFormat(split[1].replace('_', ' '));
                  meta.setTitle(title.length() > 32 ? title.substring(0, 32) : title);
               }
            }

            List<String> pages = pager.getPages(split[1]);
            meta.setPages(pages);
            this.stack.setItemMeta(meta);
         }
      }
   }

   private void transformItem(final String key, final String data) {
      PluginItemTransformer transformer = itemTransformers.get(key);
      if (transformer.getPlugin().isEnabled()) {
         try {
            this.stack = transformer.apply(data, this.stack);
         } catch (Throwable var5) {
            Essentials.getWrappedLogger()
               .log(
                  Level.SEVERE,
                  String.format("Error applying data \"%s\" to itemstack! Plugin: %s, Key: %s", data, transformer.getPlugin().getName(), key),
                  var5
               );
         }
      }
   }

   public void addItemFlags(final String string) throws Exception {
      String[] separate = splitPattern.split(string, 2);
      if (separate.length != 2) {
         throw new TranslatableException("invalidItemFlagMeta", string);
      } else {
         String[] split = separate[1].split(",");
         ItemMeta meta = this.stack.getItemMeta();

         for (String s : split) {
            for (ItemFlag flag : ItemFlag.values()) {
               if (s.equalsIgnoreCase(flag.name())) {
                  meta.addItemFlags(new ItemFlag[]{flag});
               }
            }
         }

         if (meta.getItemFlags().isEmpty()) {
            throw new TranslatableException("invalidItemFlagMeta", string);
         } else {
            this.stack.setItemMeta(meta);
         }
      }
   }

   private void addChargeMeta(final CommandSource sender, final boolean allowShortName, final String string, final net.ess3.api.IEssentials ess) throws Exception {
      String[] split = splitPattern.split(string, 2);
      if (split.length >= 2) {
         if (split[0].equalsIgnoreCase("color") || split[0].equalsIgnoreCase("colour") || allowShortName && split[0].equalsIgnoreCase("c")) {
            List<Color> primaryColors = new ArrayList<>();
            String[] colors = split[1].split(",");

            for (String color : colors) {
               if (colorMap.containsKey(color.toUpperCase())) {
                  this.validFireworkCharge = true;
                  primaryColors.add(colorMap.get(color.toUpperCase()).getFireworkColor());
               } else {
                  if (!hexPattern.matcher(color).matches()) {
                     throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
                  }

                  this.validFireworkCharge = true;
                  primaryColors.add(Color.fromRGB(Integer.decode(color)));
               }
            }

            this.builder.withColor(primaryColors);
         } else if (!split[0].equalsIgnoreCase("shape")
            && !split[0].equalsIgnoreCase("type")
            && (!allowShortName || !split[0].equalsIgnoreCase("s") && !split[0].equalsIgnoreCase("t"))) {
            if (!split[0].equalsIgnoreCase("fade") && (!allowShortName || !split[0].equalsIgnoreCase("f"))) {
               if (split[0].equalsIgnoreCase("effect") || allowShortName && split[0].equalsIgnoreCase("e")) {
                  String[] effects = split[1].split(",");

                  for (String effect : effects) {
                     if (effect.equalsIgnoreCase("twinkle")) {
                        this.builder.flicker(true);
                     } else {
                        if (!effect.equalsIgnoreCase("trail")) {
                           throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
                        }

                        this.builder.trail(true);
                     }
                  }
               }
            } else {
               List<Color> fadeColors = new ArrayList<>();
               String[] colors = split[1].split(",");

               for (String colorx : colors) {
                  if (colorMap.containsKey(colorx.toUpperCase())) {
                     fadeColors.add(colorMap.get(colorx.toUpperCase()).getFireworkColor());
                  } else {
                     if (!hexPattern.matcher(colorx).matches()) {
                        throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
                     }

                     fadeColors.add(Color.fromRGB(Integer.decode(colorx)));
                  }
               }

               if (!fadeColors.isEmpty()) {
                  this.builder.withFade(fadeColors);
               }
            }
         } else {
            Type finalEffect = null;
            split[1] = split[1].equalsIgnoreCase("large") ? "BALL_LARGE" : split[1];
            if (!fireworkShape.containsKey(split[1].toUpperCase())) {
               throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
            }

            finalEffect = fireworkShape.get(split[1].toUpperCase());
            if (finalEffect != null) {
               this.builder.with(finalEffect);
            }
         }
      }
   }

   public void addFireworkMeta(final CommandSource sender, final boolean allowShortName, final String string, final net.ess3.api.IEssentials ess) throws Exception {
      if (MaterialUtil.isFirework(this.stack.getType())) {
         String[] split = splitPattern.split(string, 2);
         if (split.length < 2) {
            return;
         }

         if (split[0].equalsIgnoreCase("color") || split[0].equalsIgnoreCase("colour") || allowShortName && split[0].equalsIgnoreCase("c")) {
            if (this.validFirework) {
               if (!this.hasMetaPermission(sender, "firework", true, true, ess)) {
                  throw new TranslatableException("noMetaFirework");
               }

               FireworkEffect effect = this.builder.build();
               FireworkMeta fmeta = (FireworkMeta)this.stack.getItemMeta();
               fmeta.addEffect(effect);
               if (fmeta.getEffects().size() > 1 && !this.hasMetaPermission(sender, "firework-multiple", true, true, ess)) {
                  throw new TranslatableException("multipleCharges");
               }

               this.stack.setItemMeta(fmeta);
               this.builder = FireworkEffect.builder();
            }

            List<Color> primaryColors = new ArrayList<>();
            String[] colors = split[1].split(",");

            for (String color : colors) {
               if (colorMap.containsKey(color.toUpperCase())) {
                  this.validFirework = true;
                  primaryColors.add(colorMap.get(color.toUpperCase()).getFireworkColor());
               } else {
                  if (!hexPattern.matcher(color).matches()) {
                     throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
                  }

                  this.validFirework = true;
                  primaryColors.add(Color.fromRGB(Integer.decode(color)));
               }
            }

            this.builder.withColor(primaryColors);
         } else if (!split[0].equalsIgnoreCase("shape")
            && !split[0].equalsIgnoreCase("type")
            && (!allowShortName || !split[0].equalsIgnoreCase("s") && !split[0].equalsIgnoreCase("t"))) {
            if (!split[0].equalsIgnoreCase("fade") && (!allowShortName || !split[0].equalsIgnoreCase("f"))) {
               if (split[0].equalsIgnoreCase("effect") || allowShortName && split[0].equalsIgnoreCase("e")) {
                  String[] effects = split[1].split(",");

                  for (String effect : effects) {
                     if (effect.equalsIgnoreCase("twinkle")) {
                        this.builder.flicker(true);
                     } else {
                        if (!effect.equalsIgnoreCase("trail")) {
                           throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
                        }

                        this.builder.trail(true);
                     }
                  }
               }
            } else {
               List<Color> fadeColors = new ArrayList<>();
               String[] colors = split[1].split(",");

               for (String colorx : colors) {
                  if (colorMap.containsKey(colorx.toUpperCase())) {
                     fadeColors.add(colorMap.get(colorx.toUpperCase()).getFireworkColor());
                  } else {
                     if (!hexPattern.matcher(colorx).matches()) {
                        throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
                     }

                     fadeColors.add(Color.fromRGB(Integer.decode(colorx)));
                  }
               }

               if (!fadeColors.isEmpty()) {
                  this.builder.withFade(fadeColors);
               }
            }
         } else {
            Type finalEffect = null;
            split[1] = split[1].equalsIgnoreCase("large") ? "BALL_LARGE" : split[1];
            if (!fireworkShape.containsKey(split[1].toUpperCase())) {
               throw new TranslatableException("invalidFireworkFormat", split[1], split[0]);
            }

            finalEffect = fireworkShape.get(split[1].toUpperCase());
            if (finalEffect != null) {
               this.builder.with(finalEffect);
            }
         }
      }
   }

   public void addPotionMeta(final CommandSource sender, final boolean allowShortName, final String string, final net.ess3.api.IEssentials ess) throws Exception {
      if (MaterialUtil.isPotion(this.stack.getType())) {
         String[] split = splitPattern.split(string, 2);
         if (split.length < 2) {
            return;
         }

         if (!split[0].equalsIgnoreCase("effect") && (!allowShortName || !split[0].equalsIgnoreCase("e"))) {
            if (!split[0].equalsIgnoreCase("power") && (!allowShortName || !split[0].equalsIgnoreCase("p"))) {
               if (!split[0].equalsIgnoreCase("amplifier") && (!allowShortName || !split[0].equalsIgnoreCase("a"))) {
                  if (!split[0].equalsIgnoreCase("duration") && (!allowShortName || !split[0].equalsIgnoreCase("d"))) {
                     if (split[0].equalsIgnoreCase("splash") || allowShortName && split[0].equalsIgnoreCase("s")) {
                        this.isSplashPotion = Boolean.parseBoolean(split[1]);
                     }
                  } else {
                     if (!NumberUtil.isInt(split[1])) {
                        throw new TranslatableException("invalidPotionMeta", split[1]);
                     }

                     this.validPotionDuration = true;
                     int parsed = Integer.parseInt(split[1]);
                     this.duration = parsed == -1 ? -1 : parsed * 20;
                  }
               } else {
                  if (!NumberUtil.isInt(split[1])) {
                     throw new TranslatableException("invalidPotionMeta", split[1]);
                  }

                  this.validPotionPower = true;
                  this.power = Integer.parseInt(split[1]);
               }
            } else {
               if (!NumberUtil.isInt(split[1])) {
                  throw new TranslatableException("invalidPotionMeta", split[1]);
               }

               this.validPotionPower = true;
               this.power = Integer.parseInt(split[1]);
               if (this.power > 0 && this.power < 4) {
                  this.power--;
               }
            }
         } else {
            this.pEffectType = Potions.getByName(split[1]);
            if (this.pEffectType == null || this.pEffectType.getName() == null) {
               throw new TranslatableException("invalidPotionMeta", split[1]);
            }

            if (!this.hasMetaPermission(sender, "potions." + this.pEffectType.getName().toLowerCase(Locale.ENGLISH), true, false, ess)) {
               throw new TranslatableException("noPotionEffectPerm", this.pEffectType.getName().toLowerCase(Locale.ENGLISH));
            }

            this.validPotionEffect = true;
         }

         if (this.isValidPotion()) {
            PotionMeta pmeta = (PotionMeta)this.stack.getItemMeta();
            this.pEffect = this.pEffectType.createEffect(this.duration, this.power);
            if (pmeta.getCustomEffects().size() > 1 && !this.hasMetaPermission(sender, "potions.multiple", true, false, ess)) {
               throw new TranslatableException("multiplePotionEffects");
            }

            pmeta.addCustomEffect(this.pEffect, true);
            this.stack.setItemMeta(pmeta);
            ess.provider(PotionMetaProvider.class).setSplashPotion(this.stack, this.isSplashPotion);
            this.resetPotionMeta();
         }
      }
   }

   private boolean parseEnchantmentStrings(final CommandSource sender, final boolean allowUnsafe, final String[] split, final net.ess3.api.IEssentials ess) throws Exception {
      Enchantment enchantment = Enchantments.getByName(split[0]);
      if (enchantment == null) {
         return false;
      } else {
         if (this.hasMetaPermission(sender, "enchantments." + Enchantments.getRealName(enchantment), false, false, ess)) {
            int level = -1;
            if (split.length > 1) {
               try {
                  level = Integer.parseInt(split[1]);
               } catch (NumberFormatException var8) {
                  level = -1;
               }
            }

            if (level < 0 || !allowUnsafe && level > enchantment.getMaxLevel()) {
               level = enchantment.getMaxLevel();
            }

            this.addEnchantment(sender, allowUnsafe, enchantment, level);
         }

         return true;
      }
   }

   public void addEnchantment(final CommandSource sender, final boolean allowUnsafe, final Enchantment enchantment, final int level) throws Exception {
      if (enchantment == null) {
         throw new TranslatableException("enchantmentNotFound");
      } else {
         try {
            if (this.stack.getType().equals(Material.ENCHANTED_BOOK)) {
               EnchantmentStorageMeta meta = (EnchantmentStorageMeta)this.stack.getItemMeta();
               if (level == 0) {
                  meta.removeStoredEnchant(enchantment);
               } else {
                  meta.addStoredEnchant(enchantment, level, allowUnsafe);
               }

               this.stack.setItemMeta(meta);
            } else if (level == 0) {
               this.stack.removeEnchantment(enchantment);
            } else if (allowUnsafe) {
               this.stack.addUnsafeEnchantment(enchantment, level);
            } else {
               this.stack.addEnchantment(enchantment, level);
            }
         } catch (Exception var6) {
            throw new Exception("Enchantment " + Enchantments.getRealName(enchantment) + ": " + var6.getMessage(), var6);
         }
      }
   }

   public Enchantment getEnchantment(final User user, final String name) throws Exception {
      Enchantment enchantment = Enchantments.getByName(name);
      if (enchantment == null) {
         return null;
      } else {
         String enchantmentName = Enchantments.getRealName(enchantment);
         if (!this.hasMetaPermission(user, "enchantments." + enchantmentName, true, false)) {
            throw new TranslatableException("enchantmentPerm", enchantmentName);
         } else {
            return enchantment;
         }
      }
   }

   public void addBannerMeta(final CommandSource sender, final boolean allowShortName, final String string, final net.ess3.api.IEssentials ess) throws Exception {
      if (MaterialUtil.isBanner(this.stack.getType()) && !this.stack.getType().toString().equals("SHIELD") && string != null) {
         String[] split = splitPattern.split(string, 2);
         if (split.length < 2) {
            throw new TranslatableException("invalidBanner", split[1]);
         }

         PatternType patternType = null;

         try {
            patternType = PatternType.getByIdentifier(split[0]);
         } catch (Exception var13) {
         }

         BannerMeta meta = (BannerMeta)this.stack.getItemMeta();
         if (split[0].equalsIgnoreCase("basecolor")) {
            Color color = Color.fromRGB(Integer.parseInt(split[1]));
            ess.provider(BannerDataProvider.class).setBaseColor(this.stack, DyeColor.getByColor(color));
         } else if (patternType != null) {
            PatternType type = PatternType.getByIdentifier(split[0]);
            DyeColor color = DyeColor.getByColor(Color.fromRGB(Integer.parseInt(split[1])));
            org.bukkit.block.banner.Pattern pattern = new org.bukkit.block.banner.Pattern(color, type);
            meta.addPattern(pattern);
         }

         this.stack.setItemMeta(meta);
      } else if (this.stack.getType().toString().equals("SHIELD") && string != null) {
         String[] splitx = splitPattern.split(string, 2);
         if (splitx.length < 2) {
            throw new TranslatableException("invalidBanner", splitx[1]);
         }

         PatternType patternType = null;

         try {
            patternType = PatternType.getByIdentifier(splitx[0]);
         } catch (Exception var12) {
         }

         BlockStateMeta meta = (BlockStateMeta)this.stack.getItemMeta();
         Banner banner = (Banner)meta.getBlockState();
         if (splitx[0].equalsIgnoreCase("basecolor")) {
            Color color = Color.fromRGB(Integer.parseInt(splitx[1]));
            banner.setBaseColor(DyeColor.getByColor(color));
         } else if (patternType != null) {
            PatternType type = PatternType.getByIdentifier(splitx[0]);
            DyeColor color = DyeColor.getByColor(Color.fromRGB(Integer.parseInt(splitx[1])));
            org.bukkit.block.banner.Pattern pattern = new org.bukkit.block.banner.Pattern(color, type);
            banner.addPattern(pattern);
         }

         banner.update();
         meta.setBlockState(banner);
         this.stack.setItemMeta(meta);
      }
   }

   private boolean hasMetaPermission(
      final CommandSource sender, final String metaPerm, final boolean graceful, final boolean includeBase, final net.ess3.api.IEssentials ess
   ) throws Exception {
      User user = sender != null && sender.isPlayer() ? ess.getUser(sender.getPlayer()) : null;
      return this.hasMetaPermission(user, metaPerm, graceful, includeBase);
   }

   private boolean hasMetaPermission(final User user, final String metaPerm, final boolean graceful, final boolean includeBase) throws Exception {
      String permBase = includeBase ? "essentials.itemspawn.meta-" : "essentials.";
      if (user == null || user.isAuthorized(permBase + metaPerm)) {
         return true;
      } else if (graceful) {
         return false;
      } else {
         throw new TranslatableException("noMetaPerm", metaPerm);
      }
   }

   private void setUnbreakable(final net.ess3.api.IEssentials ess, final ItemStack is, final boolean unbreakable) {
      ItemMeta meta = is.getItemMeta();
      ess.provider(ItemUnbreakableProvider.class).setUnbreakable(meta, unbreakable);
      is.setItemMeta(meta);
   }

   static {
      for (DyeColor color : DyeColor.values()) {
         colorMap.put(color.name(), color);
      }

      for (Type type : Type.values()) {
         fireworkShape.put(type.name(), type);
      }

      splitPattern = Pattern.compile("[:+',;.]");
      hexPattern = Pattern.compile("#([0-9a-fA-F]{6})");
   }
}
