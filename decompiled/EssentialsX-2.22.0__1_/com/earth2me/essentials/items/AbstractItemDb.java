package com.earth2me.essentials.items;

import com.earth2me.essentials.Enchantments;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.ess3.api.IEssentials;
import net.ess3.api.IItemDb;
import net.ess3.api.PluginKey;
import net.ess3.provider.BannerDataProvider;
import net.ess3.provider.PotionMetaProvider;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

public abstract class AbstractItemDb implements IConf, IItemDb {
   protected final IEssentials ess;
   private final Map<PluginKey, IItemDb.ItemResolver> resolverMap = new HashMap<>();
   protected boolean ready = false;

   AbstractItemDb(final IEssentials ess) {
      this.ess = ess;
   }

   @Override
   public void registerResolver(final Plugin plugin, final String name, final IItemDb.ItemResolver resolver) throws Exception {
      PluginKey key = PluginKey.fromKey(plugin, name);
      if (this.resolverMap.containsKey(key)) {
         throw new Exception("Tried to add a duplicate resolver with name " + key.toString());
      } else {
         this.resolverMap.put(key, resolver);
      }
   }

   @Override
   public void unregisterResolver(final Plugin plugin, final String name) throws Exception {
      PluginKey key = PluginKey.fromKey(plugin, name);
      if (!this.resolverMap.containsKey(key)) {
         throw new Exception("Tried to remove nonexistent resolver with name " + key.toString());
      } else {
         this.resolverMap.remove(key);
      }
   }

   @Override
   public boolean isResolverPresent(final Plugin plugin, final String name) {
      return this.resolverMap.containsKey(PluginKey.fromKey(plugin, name));
   }

   @Override
   public Map<PluginKey, IItemDb.ItemResolver> getResolvers() {
      return new HashMap<>(this.resolverMap);
   }

   @Override
   public Map<PluginKey, IItemDb.ItemResolver> getResolvers(final Plugin plugin) {
      Map<PluginKey, IItemDb.ItemResolver> matchingResolvers = new HashMap<>();

      for (PluginKey key : this.resolverMap.keySet()) {
         if (key.getPlugin().equals(plugin)) {
            matchingResolvers.put(key, this.resolverMap.get(key));
         }
      }

      return matchingResolvers;
   }

   @Override
   public IItemDb.ItemResolver getResolver(final Plugin plugin, final String name) {
      return this.resolverMap.get(PluginKey.fromKey(plugin, name));
   }

   @Override
   public ItemStack get(final String id) throws Exception {
      return this.get(id, true);
   }

   ItemStack tryResolverDeserialize(final String id) {
      for (PluginKey key : this.resolverMap.keySet()) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info(String.format("Trying to deserialize item '%s' with resolver '%s'...", id, key));
         }

         Function<String, ItemStack> resolver = this.resolverMap.get(key);
         ItemStack stack = resolver.apply(id);
         if (stack != null) {
            return stack;
         }
      }

      return null;
   }

   String tryResolverSerialize(final ItemStack stack) {
      for (PluginKey key : this.resolverMap.keySet()) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info(String.format("Trying to serialize '%s' with resolver '%s'...", stack.toString(), key));
         }

         IItemDb.ItemResolver resolver = this.resolverMap.get(key);
         String serialized = resolver.serialize(stack);
         if (serialized != null) {
            return serialized;
         }
      }

      return null;
   }

   Collection<String> getResolverNames() {
      List<String> result = new ArrayList<>();

      for (IItemDb.ItemResolver resolver : this.resolverMap.values()) {
         Collection<String> resolverNames = resolver.getNames();
         if (resolverNames != null) {
            result.addAll(resolverNames);
         }
      }

      return result;
   }

   @Override
   public List<ItemStack> getMatching(final User user, final String[] args) throws Exception {
      List<ItemStack> is = new ArrayList<>();
      if (args.length < 1) {
         is.add(user.getItemInHand().clone());
      } else if (args[0].equalsIgnoreCase("hand")) {
         is.add(user.getItemInHand().clone());
      } else if (!args[0].equalsIgnoreCase("inventory") && !args[0].equalsIgnoreCase("invent") && !args[0].equalsIgnoreCase("all")) {
         if (args[0].equalsIgnoreCase("blocks")) {
            for (ItemStack stack : Inventories.getInventory(user.getBase(), true)) {
               if (stack != null && stack.getType() != Material.AIR && stack.getType().isBlock()) {
                  is.add(stack.clone());
               }
            }
         } else {
            is.add(this.get(args[0]));
         }
      } else {
         for (ItemStack stackx : Inventories.getInventory(user.getBase(), true)) {
            if (stackx != null && stackx.getType() != Material.AIR) {
               is.add(stackx.clone());
            }
         }
      }

      if (!is.isEmpty() && is.get(0).getType() != Material.AIR) {
         return is;
      } else {
         throw new Exception(user.playerTl("itemSellAir"));
      }
   }

   @Override
   public String serialize(final ItemStack is) {
      return this.serialize(is, true);
   }

   @Override
   public String serialize(final ItemStack is, final boolean useResolvers) {
      if (useResolvers) {
         String serialized = this.tryResolverSerialize(is);
         if (serialized != null) {
            return serialized;
         }
      }

      String mat = this.name(is);
      if (VersionUtil.getServerBukkitVersion().isLowerThanOrEqualTo(VersionUtil.v1_12_2_R01) && is.getData().getData() != 0) {
         mat = mat + ":" + is.getData().getData();
      }

      int quantity = is.getAmount();
      StringBuilder sb = new StringBuilder();
      sb.append(mat).append(" ").append(quantity).append(" ");
      if (is.hasItemMeta()) {
         ItemMeta meta = is.getItemMeta();
         if (meta.hasDisplayName()) {
            sb.append("name:").append(FormatUtil.unformatString(meta.getDisplayName()).replace(" ", "_")).append(" ");
         }

         if (meta.hasLore()) {
            sb.append("lore:").append(this.serializeLines(meta.getLore())).append(" ");
         }

         if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_14_R01) && meta.hasCustomModelData()) {
            sb.append("custom-model-data:").append(meta.getCustomModelData()).append(" ");
         }

         if (meta.hasEnchants()) {
            for (Enchantment e : meta.getEnchants().keySet()) {
               sb.append(Enchantments.getRealName(e)).append(":").append(meta.getEnchantLevel(e)).append(" ");
            }
         }

         Set<ItemFlag> flags = meta.getItemFlags();
         if (flags != null && !flags.isEmpty()) {
            sb.append("itemflags:");
            boolean first = true;

            for (ItemFlag flag : flags) {
               if (!first) {
                  sb.append(",");
               }

               sb.append(flag.name());
               first = false;
            }
         }
      }

      Material material = is.getType();
      switch (material) {
         case WRITTEN_BOOK:
         case WRITABLE_BOOK:
            BookMeta bookMeta = (BookMeta)is.getItemMeta();
            if (bookMeta.hasTitle()) {
               sb.append("title:").append(FormatUtil.unformatString(bookMeta.getTitle()).replace(' ', '_')).append(" ");
            }

            if (bookMeta.hasAuthor()) {
               sb.append("author:").append(FormatUtil.unformatString(bookMeta.getAuthor()).replace(' ', '_')).append(" ");
            }

            if (bookMeta.hasPages()) {
               List<String> pages = bookMeta.getPages();

               for (int i = 0; i < pages.size(); i++) {
                  sb.append("page").append(i + 1).append(":");
                  sb.append(this.serializeLines(Arrays.asList(pages.get(i).split("\n"))));
                  sb.append(" ");
               }
            }
            break;
         case ENCHANTED_BOOK:
            EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta)is.getItemMeta();

            for (Enchantment e : enchantmentStorageMeta.getStoredEnchants().keySet()) {
               sb.append(Enchantments.getRealName(e)).append(":").append(enchantmentStorageMeta.getStoredEnchantLevel(e)).append(" ");
            }
      }

      if (MaterialUtil.isFirework(material)) {
         FireworkMeta fireworkMeta = (FireworkMeta)is.getItemMeta();
         if (fireworkMeta.hasEffects()) {
            for (FireworkEffect effect : fireworkMeta.getEffects()) {
               this.serializeEffectMeta(sb, effect);
            }

            sb.append("power:").append(fireworkMeta.getPower()).append(" ");
         }
      } else if (MaterialUtil.isFireworkCharge(material)) {
         FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta)is.getItemMeta();
         if (fireworkEffectMeta.hasEffect()) {
            this.serializeEffectMeta(sb, fireworkEffectMeta.getEffect());
         }
      } else if (MaterialUtil.isPotion(material)) {
         PotionMetaProvider provider = this.ess.provider(PotionMetaProvider.class);
         boolean splash = provider.isSplashPotion(is);

         for (PotionEffect e : provider.getCustomEffects(is)) {
            sb.append("splash:")
               .append(splash)
               .append(" ")
               .append("effect:")
               .append(e.getType().getName().toLowerCase())
               .append(" ")
               .append("power:")
               .append(e.getAmplifier())
               .append(" ")
               .append("duration:")
               .append(e.getDuration() / 20)
               .append(" ");
         }
      } else if (MaterialUtil.isPlayerHead(is)) {
         SkullMeta skullMeta = (SkullMeta)is.getItemMeta();
         if (skullMeta != null && skullMeta.hasOwner()) {
            sb.append("player:").append(skullMeta.getOwner()).append(" ");
         }
      } else if (MaterialUtil.isBanner(material)) {
         if (material.toString().contains("SHIELD")) {
            BlockStateMeta shieldMeta = (BlockStateMeta)is.getItemMeta();
            Banner shieldBannerMeta = (Banner)shieldMeta.getBlockState();
            DyeColor baseDyeColor = shieldBannerMeta.getBaseColor();
            if (baseDyeColor != null) {
               int basecolor = baseDyeColor.getColor().asRGB();
               sb.append("basecolor:").append(basecolor).append(" ");
            }

            for (Pattern p : shieldBannerMeta.getPatterns()) {
               String type = p.getPattern().getIdentifier();
               int color = p.getColor().getColor().asRGB();
               sb.append(type).append(",").append(color).append(" ");
            }
         } else {
            BannerMeta bannerMeta = (BannerMeta)is.getItemMeta();
            if (bannerMeta != null) {
               DyeColor baseDyeColor = this.ess.provider(BannerDataProvider.class).getBaseColor(is);
               if (baseDyeColor == null) {
                  baseDyeColor = MaterialUtil.getColorOf(material);
               }

               if (baseDyeColor != null) {
                  int basecolor = baseDyeColor.getColor().asRGB();
                  sb.append("basecolor:").append(basecolor).append(" ");
               }

               for (Pattern p : bannerMeta.getPatterns()) {
                  String type = p.getPattern().getIdentifier();
                  int color = p.getColor().getColor().asRGB();
                  sb.append(type).append(",").append(color).append(" ");
               }
            }
         }
      } else if (MaterialUtil.isArmor(material)) {
         ArmorTrim armorTrim = ((ArmorMeta)is.getItemMeta()).getTrim();
         if (armorTrim != null) {
            String trimPattern = armorTrim.getPattern().getKey().getKey();
            String trimMaterial = armorTrim.getMaterial().getKey().getKey();
            sb.append("trim:").append(trimPattern).append("|").append(trimMaterial).append(" ");
         }

         if (MaterialUtil.isLeatherArmor(material)) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)is.getItemMeta();
            int rgb = leatherArmorMeta.getColor().asRGB();
            sb.append("color:").append(rgb).append(" ");
         }
      }

      return sb.toString().trim().replaceAll("§", "&");
   }

   private void serializeEffectMeta(StringBuilder sb, FireworkEffect effect) {
      if (effect.getColors() != null && !effect.getColors().isEmpty()) {
         sb.append("color:");
         boolean first = true;

         for (Color c : effect.getColors()) {
            if (!first) {
               sb.append(",");
            }

            sb.append("#").append(Integer.toHexString(c.asRGB()));
            first = false;
         }

         sb.append(" ");
      }

      sb.append("shape:").append(effect.getType().name()).append(" ");
      if (effect.getFadeColors() != null && !effect.getFadeColors().isEmpty()) {
         sb.append("fade:");
         boolean first = true;

         for (Color c : effect.getFadeColors()) {
            if (!first) {
               sb.append(",");
            }

            sb.append("#").append(Integer.toHexString(c.asRGB()));
            first = false;
         }

         sb.append(" ");
      }
   }

   private String serializeLines(Iterable<String> lines) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;

      for (String line : lines) {
         if (!first) {
            sb.append("|");
         }

         first = false;
         sb.append(FormatUtil.unformatString(line).replace(" ", "_").replace("|", "\\|"));
      }

      return sb.toString();
   }

   @Override
   public boolean isReady() {
      return this.ready;
   }
}
