package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.SoulbindConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructionContext;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinitions;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItemProfile;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EliteItemLore {
   private static final EnchantmentItems ENCHANTMENT_PRESENTATION = new EnchantmentItems(EnchantmentDefinitions::resolve, EnchantmentItemProfile::vanilla);
   private final List<String> vanillaEnchantmentsLore = new ArrayList<>();
   private final HashMap<Enchantment, Integer> eliteVanillaEnchantments = new HashMap<>();
   private final ArrayList<String> eliteVanillaEnchantmentsLore = new ArrayList<>();
   private final List<String> potionListLore = new ArrayList<>();
   private ItemStack itemStack;
   private ItemMeta itemMeta;
   private List<String> lore;
   private String soulbindInfo = null;
   private boolean showItemWorth;
   private String itemWorth = null;
   private String itemSource = null;
   private EliteEntity eliteEntity;
   private Player soulboundPlayer = null;
   private List<String> customLore = new ArrayList<>();
   private int prestigeLevel = 0;
   private List<String> thirdPartyLore = null;
   private boolean hasCustomEnchantments;
   private boolean magicWeapon;
   private int customEnchantmentsPosition = -1;

   public EliteItemLore(ItemStack itemStack, boolean showItemWorth) {
      this(itemStack, showItemWorth, false);
   }

   public EliteItemLore(ItemStack itemStack, boolean showItemWorth, boolean isNewItem) {
      this(itemStack, showItemWorth, isNewItem, null);
   }

   public EliteItemLore(ItemStack itemStack, boolean showItemWorth, boolean isNewItem, ItemConstructionContext construction) {
      this.initialize(itemStack, showItemWorth, isNewItem, construction);
   }

   private void initialize(ItemStack itemStack, boolean showItemWorth, boolean isNewItem, ItemConstructionContext construction) {
      if (EliteItemManager.isEliteMobsItem(itemStack)) {
         this.itemStack = itemStack.clone();
         this.itemMeta = this.itemStack.getItemMeta();
         SkillType skill = construction == null ? WeaponIdentityResolver.progressionSkill(this.itemStack) : construction.skill(this.itemStack);
         this.magicWeapon = skill == SkillType.STAVES || skill == SkillType.WANDS;
         this.lore = new ArrayList<>();
         this.showItemWorth = showItemWorth;
         if (ItemSettingsConfig.isHideAttributes()) {
            this.itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         }

         this.constructVanillaEnchantments();
         this.parseAllEliteEnchantments();
         this.constructEliteEnchantments();
         this.constructSoulbindEntry();
         this.constructSoulboundOwner();
         this.constructItemSource();
         this.constructCustomLore();
         this.constructPrestigeLevel();
         this.constructPotionEffects();
         this.constructItemWorth();
         this.hasCustomEnchantments = !EnchantmentItems.inspectCustom(this.itemMeta).isEmpty();
         this.itemStack.setItemMeta(this.itemMeta);
         UnaryOperator<List<String>> rebuild = hostLore -> {
            if (isNewItem && !hostLore.isEmpty()) {
               this.thirdPartyLore = hostLore;
            }

            this.writeNewLore();
            return this.lore;
         };
         ItemStack rendered = construction == null
            ? ENCHANTMENT_PRESENTATION.refreshPresentation(this.itemStack, rebuild, hostLore -> this.customEnchantmentsPosition)
            : construction.enchantments().refreshPresentation(this.itemStack, rebuild, hostLore -> this.customEnchantmentsPosition);
         if (!itemStack.setItemMeta(rendered.getItemMeta())) {
            throw new IllegalArgumentException("Elite item rejected its rebuilt metadata");
         } else {
            this.itemStack = itemStack;
            this.itemMeta = itemStack.getItemMeta();
         }
      }
   }

   private void constructVanillaEnchantments() {
      if (this.itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
         for (Enchantment enchantment : this.itemMeta.getEnchants().keySet()) {
            if (enchantment.getName().contains("CURSE")) {
               this.vanillaEnchantmentsLore
                  .add(
                     ChatColorConverter.convert(
                        "&c" + EnchantmentsConfig.getEnchantment(enchantment).getName() + " " + this.itemMeta.getEnchants().get(enchantment)
                     )
                  );
            } else {
               this.vanillaEnchantmentsLore
                  .add(
                     ChatColorConverter.convert(
                        "&7" + EnchantmentsConfig.getEnchantment(enchantment).getName() + " " + this.itemMeta.getEnchants().get(enchantment)
                     )
                  );
            }
         }
      }
   }

   private void parseAllEliteEnchantments() {
      this.parseEliteEnchantments(Enchantment.SHARPNESS);
      this.parseEliteEnchantments(Enchantment.POWER);
      this.parseEliteEnchantments(Enchantment.PROTECTION);
      this.parseEliteEnchantments(Enchantment.BANE_OF_ARTHROPODS);
      this.parseEliteEnchantments(Enchantment.SMITE);
      this.parseEliteEnchantments(Enchantment.BLAST_PROTECTION);
      this.parseEliteEnchantments(Enchantment.FIRE_PROTECTION);
      this.parseEliteEnchantments(Enchantment.PROJECTILE_PROTECTION);
      this.parseEliteEnchantments(Enchantment.THORNS);
   }

   private void parseEliteEnchantments(Enchantment enchantment) {
      int enchantmentLevel = ItemTagger.getEnchantment(this.itemMeta, enchantment.getKey());
      if (enchantmentLevel > enchantment.getMaxLevel()) {
         int eliteLevel = enchantmentLevel - enchantment.getMaxLevel();
         this.eliteVanillaEnchantments.put(enchantment, eliteLevel);
      }
   }

   private void constructEliteEnchantments() {
      for (Enchantment enchantment : this.eliteVanillaEnchantments.keySet()) {
         this.eliteVanillaEnchantmentsLore
            .add(
               ChatColorConverter.convert(
                  "&7"
                     + ItemSettingsConfig.getEliteEnchantLoreString()
                     + " "
                     + EnchantmentsConfig.getEnchantment(enchantment).getName()
                     + " "
                     + this.eliteVanillaEnchantments.get(enchantment)
               )
            );
      }
   }

   private void constructSoulbindEntry() {
      Player player = SoulbindEnchantment.getSoulboundPlayer(this.itemMeta);
      if (player == null) {
         this.soulbindInfo = ItemSettingsConfig.getNoSoulbindLore();
      } else {
         this.soulbindInfo = SoulbindConfig.loreStrings.replace("$playerName", player.getName()).replace("$player", player.getDisplayName());
      }
   }

   private void constructPrestigeLevel() {
      this.prestigeLevel = SoulbindEnchantment.getPrestigeLevel(this.itemMeta);
   }

   private void constructItemWorth() {
      this.itemStack.setItemMeta(this.itemMeta);
      ItemTagger.writeItemValue(this.itemStack, this.soulboundPlayer);
      this.itemMeta = this.itemStack.getItemMeta();
      if (this.showItemWorth) {
         this.itemWorth = ItemSettingsConfig.getLoreWorth()
            .replace("$worth", ItemTagger.getItemValue(this.itemStack) + "")
            .replace("$currencyName", EconomySettingsConfig.getCurrencyName());
      } else {
         this.itemWorth = ItemSettingsConfig.getLoreResale()
            .replace("$resale", ItemWorthCalculator.determineResaleWorth(this.itemStack, this.soulboundPlayer) + "")
            .replace("$currencyName", EconomySettingsConfig.getCurrencyName());
      }
   }

   private void constructSoulboundOwner() {
      this.soulboundPlayer = SoulbindEnchantment.getSoulboundPlayer(this.itemMeta);
   }

   private void constructItemSource() {
      this.itemSource = ItemTagger.getItemSource(this.itemMeta);
   }

   private void constructCustomLore() {
      this.customLore = ItemTagger.getCustomLore(this.itemMeta);
   }

   private void constructPotionEffects() {
      for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(this.itemMeta, ItemTagger.continuousPotionEffectKey)) {
         this.potionListLore
            .add(
               PotionEffectsConfig.getPotionEffect(elitePotionEffect.getPotionEffect().getType().getKey().getKey().toLowerCase(Locale.ROOT) + ".yml").getName()
                  + ItemSettingsConfig.getPotionEffectContinuousLore()
                  + " "
                  + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)
            );
      }

      for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(this.itemMeta, ItemTagger.onHitPotionEffectKey)) {
         if (elitePotionEffect.getTarget().equals(ElitePotionEffect.Target.SELF)) {
            this.potionListLore
               .add(
                  PotionEffectsConfig.getPotionEffect(elitePotionEffect.getPotionEffect().getType().getKey().getKey().toLowerCase(Locale.ROOT) + ".yml")
                        .getName()
                     + ItemSettingsConfig.getPotionEffectOnHitSelfLore()
                     + " "
                     + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)
               );
         } else {
            this.potionListLore
               .add(
                  PotionEffectsConfig.getPotionEffect(elitePotionEffect.getPotionEffect().getType().getKey().getKey().toLowerCase(Locale.ROOT) + ".yml")
                        .getName()
                     + ItemSettingsConfig.getPotionEffectOnHitTargetLore()
                     + " "
                     + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)
               );
         }
      }
   }

   private void writeNewLore() {
      this.customEnchantmentsPosition = -1;
      if (this.thirdPartyLore != null) {
         this.lore.addAll(this.thirdPartyLore);
      }

      String materialName = null;
      Double dps = null;
      Double defense = null;
      Integer itemLevel = null;
      Integer maximumDurability = null;
      Iterator var6 = ItemSettingsConfig.getLoreStructure().iterator();

      while (true) {
         String string;
         while (true) {
            if (!var6.hasNext()) {
               return;
            }

            string = (String)var6.next();
            if (string.contains("$weaponOrArmorStats")) {
               if (EliteItemManager.isWeapon(this.itemStack, this.magicWeapon)) {
                  string = ItemSettingsConfig.getWeaponEntry();
               } else if (EliteItemManager.isArmor(this.itemStack)) {
                  string = ItemSettingsConfig.getArmorEntry();
               } else {
                  string = "";
               }
            }

            if (!string.contains("$itemMaxDurability")) {
               break;
            }

            if (maximumDurability == null) {
               maximumDurability = ItemDurability.maximum(this.itemStack, this.magicWeapon);
            }

            if (maximumDurability > 0 && !this.itemMeta.isUnbreakable()) {
               string = this.stringReplacer(string, "$itemMaxDurability", maximumDurability);
               break;
            }
         }

         if (string.contains("$itemMaterial")) {
            if (materialName == null) {
               materialName = materialDisplayName(this.itemStack.getType());
            }

            string = this.stringReplacer(string, "$itemMaterial", materialName);
         }

         if (string.contains("$EDPS")) {
            if (dps == null) {
               dps = Round.twoDecimalPlaces(EliteItemManager.getDPS(this.itemStack));
            }

            string = this.stringReplacer(string, "$EDPS", dps);
         }

         if (string.contains("$EDEF")) {
            if (defense == null) {
               defense = Round.twoDecimalPlaces(EliteItemManager.getEliteDefense(this.itemStack) + EliteItemManager.getBonusEliteDefense(this.itemStack));
            }

            string = this.stringReplacer(string, "$EDEF", defense);
         }

         string = this.stringReplacer(string, "$prestigeLevel", this.prestigeLevel);
         if (string.contains("$itemLevel")) {
            if (itemLevel == null) {
               itemLevel = EliteItemManager.getRoundedItemLevel(this.itemStack);
            }

            string = this.stringReplacer(string, "$itemLevel", itemLevel);
         }

         if (string.contains("$enchantments")) {
            for (String entry : this.vanillaEnchantmentsLore) {
               this.lore.add(ItemSettingsConfig.getVanillaEnchantmentColor() + entry);
            }
         } else if (string.contains("$eliteEnchantments")) {
            for (String entry : this.eliteVanillaEnchantmentsLore) {
               this.lore.add(ItemSettingsConfig.getEliteEnchantmentColor() + ChatColor.stripColor(entry));
            }
         } else if (string.contains("$itemSource")) {
            if (this.itemSource != null) {
               this.lore.add(this.itemSource);
            }
         } else if (string.contains("$potionEffect")) {
            for (String entry : this.potionListLore) {
               this.lore.add(ItemSettingsConfig.getPotionEffectColor() + entry);
            }
         } else if (string.contains("$customEnchantments")) {
            if (this.customEnchantmentsPosition == -1) {
               this.customEnchantmentsPosition = this.lore.size();
            }
         } else if (string.contains("$loreResaleValue")) {
            this.lore.add(this.itemWorth);
         } else if (string.contains("$customLore")) {
            for (String entry : this.customLore) {
               this.lore.add(ChatColorConverter.convert(entry));
            }
         } else if (string.contains("$soulbindInfo")) {
            this.lore.add(this.soulbindInfo);
         } else if (string.contains("$ifPotionEffects")) {
            if (!this.potionListLore.isEmpty()) {
               this.lore.add(string.replace("$ifPotionEffects", ""));
            }
         } else if (string.contains("$ifEnchantments")) {
            if (!this.vanillaEnchantmentsLore.isEmpty() || !this.eliteVanillaEnchantmentsLore.isEmpty()) {
               this.lore.add(string.replace("$ifEnchantments", ""));
            }
         } else if (string.contains("$ifLore")) {
            if (!this.customLore.isEmpty()) {
               this.lore.add(string.replace("$ifLore", ""));
            }
         } else if (string.contains("$ifCustomEnchantments")) {
            if (this.hasCustomEnchantments) {
               this.lore.add(string.replace("$ifCustomEnchantments", ""));
            }
         } else if (!string.isEmpty()) {
            this.lore.add(ChatColorConverter.convert(string));
         }
      }
   }

   static String materialDisplayName(Material material) {
      String[] words = material.name().toLowerCase(Locale.ROOT).split("_");
      StringBuilder displayName = new StringBuilder();

      for (String word : words) {
         if (!word.isEmpty()) {
            if (displayName.length() > 0) {
               displayName.append(' ');
            }

            displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
         }
      }

      return displayName.toString();
   }

   private String stringReplacer(String originalString, String placeholder, int value) {
      return this.stringReplacer(originalString, placeholder, value + "");
   }

   private String stringReplacer(String originalString, String placeholder, double value) {
      return this.stringReplacer(originalString, placeholder, value + "");
   }

   private String stringReplacer(String originalString, String placeholder, String replacement) {
      String processedString = originalString;
      if (originalString.contains(placeholder)) {
         processedString = originalString.replace(placeholder, replacement);
      }

      return processedString;
   }

   @Generated
   public ItemStack getItemStack() {
      return this.itemStack;
   }
}
