package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ScalableItemConstructor {
   private ScalableItemConstructor() {
   }

   public static ItemStack randomizeScalableItem(int itemTier, Player player, EliteEntity eliteEntity) {
      CustomItem customItem = CustomItem.getScalableItems().get(ThreadLocalRandom.current().nextInt(CustomItem.getScalableItems().size()));
      return constructScalableItem(itemTier, customItem, player, eliteEntity);
   }

   public static ItemStack constructScalableItem(int itemTier, CustomItem customItem, Player player, EliteEntity eliteEntity) {
      if (player != null && !customItem.getPermission().isEmpty() && !player.hasPermission(customItem.getPermission())) {
         return null;
      } else {
         HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments());
         newEnchantmentList = EnchantmentGenerator.withProceduralEnchantments(itemTier, customItem.getCustomItemsConfigFields(), newEnchantmentList);
         return ItemConstructor.constructItem(
            itemTier,
            customItem.getCustomItemsConfigFields().getName(),
            customItem.getCustomItemsConfigFields().getMaterial(),
            newEnchantmentList,
            EnchantmentGenerator.withProceduralCustomEnchantments(itemTier, customItem.getCustomItemsConfigFields(), customItem.getCustomEnchantments()),
            customItem.getPotionEffects(),
            customItem.getCustomItemsConfigFields().getLore(),
            eliteEntity,
            player,
            false,
            customItem.getCustomItemsConfigFields().getCustomModelID(),
            customItem.getCustomItemsConfigFields().getEquipmentModelID(),
            customItem.getCustomItemsConfigFields().isSoulbound(),
            customItem.getCustomItemsConfigFields().getFilename(),
            customItem.getCustomItemsConfigFields().getScriptedItem(),
            customItem.getCustomItemsConfigFields().getWeaponType(),
            customItem.getCustomItemsConfigFields().getFmmItemModel()
         );
      }
   }

   private static HashMap<Enchantment, Integer> updateDynamicEnchantments(HashMap<Enchantment, Integer> enchantmentsList) {
      return rollEnchantments(enchantmentsList, 0.5, null, 0);
   }

   public static HashMap<Enchantment, Integer> rollEnchantments(
      Map<Enchantment, Integer> enchantmentsList, double fraction, Enchantment primary, int minimumPrimary
   ) {
      if (Double.isFinite(fraction) && !(fraction < 0.0) && !(fraction > 1.0)) {
         List<Enchantment> types = new ArrayList<>();
         List<Integer> capacities = new ArrayList<>();
         long total = 0L;

         for (Entry<Enchantment, Integer> entry : enchantmentsList.entrySet()) {
            int count = entry.getValue();
            if (count > 0) {
               types.add(entry.getKey());
               capacities.add(count);
               total = Math.addExact(total, (long)count);
            }
         }

         long draws = Math.min(total, (long)Math.ceil(total * fraction));
         int reserved = (int)Math.min(draws, (long)Math.max(0, Math.min(minimumPrimary, primary == null ? 0 : enchantmentsList.getOrDefault(primary, 0))));
         int[] remaining = new int[types.size()];

         for (int index = 0; index < remaining.length; index++) {
            remaining[index] = capacities.get(index) - (types.get(index).equals(primary) ? reserved : 0);
         }

         long available = total - reserved;
         long needed = draws - reserved;
         boolean complement = needed > available / 2L;
         long samples = complement ? available - needed : needed;
         int[] picked = new int[types.size()];

         for (long draw = 0L; draw < samples; draw++) {
            long selected = ThreadLocalRandom.current().nextLong(available--);

            for (int index = 0; index < remaining.length; index++) {
               if (selected < remaining[index]) {
                  remaining[index]--;
                  picked[index]++;
                  break;
               }

               selected -= remaining[index];
            }
         }

         HashMap<Enchantment, Integer> result = new HashMap<>();

         for (int index = 0; index < remaining.length; index++) {
            int count = complement ? remaining[index] : picked[index];
            if (types.get(index).equals(primary)) {
               count += reserved;
            }

            if (count > 0) {
               result.put(types.get(index), count);
            }
         }

         return result;
      } else {
         throw new IllegalArgumentException("Enchantment budget fraction must be between zero and one");
      }
   }

   public static ItemStack randomizeLimitedItem(int itemTier, Player player, EliteEntity eliteEntity) {
      return randomizeLimitedItem(itemTier, player, eliteEntity, eligibleLimitedItems(itemTier, player));
   }

   public static List<CustomItem> eligibleLimitedItems(int itemTier, Player player) {
      List<CustomItem> eligible = new ArrayList<>();

      for (Entry<Integer, ArrayList<CustomItem>> bucket : CustomItem.getLimitedItems().entrySet()) {
         if (bucket.getKey() >= 0 && bucket.getKey() < itemTier) {
            for (CustomItem item : bucket.getValue()) {
               if (player == null || LootItemPolicy.canReceive(item, player)) {
                  eligible.add(item);
               }
            }
         }
      }

      return eligible;
   }

   public static ItemStack randomizeLimitedItem(int itemTier, Player player, EliteEntity eliteEntity, List<CustomItem> eligible) {
      return eligible.isEmpty()
         ? null
         : constructLimitedItem(itemTier, eligible.get(ThreadLocalRandom.current().nextInt(eligible.size())), player, eliteEntity);
   }

   public static ItemStack constructLimitedItem(int itemTier, CustomItem customItem, Player player, EliteEntity eliteEntity) {
      if (player != null && !LootItemPolicy.canReceive(customItem, player)) {
         return null;
      } else {
         int adjustedItemLevel = Math.min(itemTier, customItem.getItemLevel());
         HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(customItem.getEnchantments());
         newEnchantmentList = EnchantmentGenerator.withProceduralEnchantments(adjustedItemLevel, customItem.getCustomItemsConfigFields(), newEnchantmentList);
         return ItemConstructor.constructItem(
            adjustedItemLevel,
            customItem.getCustomItemsConfigFields().getName(),
            customItem.getCustomItemsConfigFields().getMaterial(),
            newEnchantmentList,
            EnchantmentGenerator.withProceduralCustomEnchantments(
               adjustedItemLevel, customItem.getCustomItemsConfigFields(), customItem.getCustomEnchantments()
            ),
            customItem.getPotionEffects(),
            customItem.getCustomItemsConfigFields().getLore(),
            eliteEntity,
            player,
            false,
            customItem.getCustomItemsConfigFields().getCustomModelID(),
            customItem.getCustomItemsConfigFields().getEquipmentModelID(),
            customItem.getCustomItemsConfigFields().isSoulbound(),
            customItem.getCustomItemsConfigFields().getFilename(),
            customItem.getCustomItemsConfigFields().getScriptedItem(),
            customItem.getCustomItemsConfigFields().getWeaponType(),
            customItem.getCustomItemsConfigFields().getFmmItemModel()
         );
      }
   }
}
