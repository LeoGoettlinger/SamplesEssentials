package net.ess3.provider.providers;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import net.ess3.provider.PotionMetaProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

@ProviderData(
   description = "1.9-1.20.4 Potion Meta Provider",
   weight = 1
)
public class LegacyPotionMetaProvider implements PotionMetaProvider {
   private final Map<Integer, PotionType> damageValueToType = ImmutableMap.builder()
      .put(1, PotionType.REGEN)
      .put(2, PotionType.SPEED)
      .put(3, PotionType.FIRE_RESISTANCE)
      .put(4, PotionType.POISON)
      .put(5, PotionType.INSTANT_HEAL)
      .put(6, PotionType.NIGHT_VISION)
      .put(8, PotionType.WEAKNESS)
      .put(9, PotionType.STRENGTH)
      .put(10, PotionType.SLOWNESS)
      .put(11, PotionType.JUMP)
      .put(12, PotionType.INSTANT_DAMAGE)
      .put(13, PotionType.WATER_BREATHING)
      .put(14, PotionType.INVISIBILITY)
      .build();

   private static int getBit(final int n, final int k) {
      return n >> k & 1;
   }

   @Override
   public ItemStack createPotionItem(final Material initial, final int effectId) {
      ItemStack potion = new ItemStack(initial, 1);
      if (effectId == 0) {
         return potion;
      } else {
         int damageValue = getBit(effectId, 0) + 2 * getBit(effectId, 1) + 4 * getBit(effectId, 2) + 8 * getBit(effectId, 3);
         PotionType type = this.damageValueToType.get(damageValue);
         if (type == null) {
            throw new IllegalArgumentException("Unable to process potion effect ID " + effectId + " with damage value " + damageValue);
         } else {
            if (getBit(effectId, 14) == 1 && initial == Material.POTION) {
               potion = new ItemStack(Material.SPLASH_POTION, 1);
            }

            PotionMeta meta = (PotionMeta)potion.getItemMeta();
            PotionData data = new PotionData(type, getBit(effectId, 6) == 1, getBit(effectId, 5) == 1);
            meta.setBasePotionData(data);
            potion.setItemMeta(meta);
            return potion;
         }
      }
   }

   @Override
   public void setSplashPotion(final ItemStack stack, final boolean isSplash) {
      if (stack == null) {
         throw new IllegalArgumentException("ItemStack cannot be null");
      } else {
         if (isSplash && stack.getType() == Material.POTION) {
            stack.setType(Material.SPLASH_POTION);
         } else if (!isSplash && stack.getType() == Material.SPLASH_POTION) {
            stack.setType(Material.POTION);
         }
      }
   }

   @Override
   public boolean isSplashPotion(final ItemStack stack) {
      return stack != null && stack.getType() == Material.SPLASH_POTION;
   }

   @Override
   public Collection<PotionEffect> getCustomEffects(final ItemStack stack) {
      PotionMeta meta = (PotionMeta)stack.getItemMeta();
      return meta.getCustomEffects();
   }

   @Override
   public boolean isExtended(final ItemStack stack) {
      PotionMeta meta = (PotionMeta)stack.getItemMeta();
      PotionData data = meta.getBasePotionData();
      return data.isExtended();
   }

   @Override
   public boolean isUpgraded(final ItemStack stack) {
      PotionMeta meta = (PotionMeta)stack.getItemMeta();
      PotionData data = meta.getBasePotionData();
      return data.isUpgraded();
   }

   @Override
   public PotionType getBasePotionType(final ItemStack stack) {
      PotionMeta meta = (PotionMeta)stack.getItemMeta();
      PotionData data = meta.getBasePotionData();
      return data.getType();
   }

   @Override
   public void setBasePotionType(final ItemStack stack, final PotionType type, final boolean extended, final boolean upgraded) {
      if (stack == null) {
         throw new IllegalArgumentException("ItemStack cannot be null");
      } else if (extended && upgraded) {
         throw new IllegalArgumentException("Potion cannot be both extended and upgraded");
      } else {
         PotionData data = new PotionData(type, extended, upgraded);
         PotionMeta meta = (PotionMeta)stack.getItemMeta();
         meta.setBasePotionData(data);
         stack.setItemMeta(meta);
      }
   }

   @ProviderTest
   public static boolean test() {
      try {
         Class.forName("org.bukkit.potion.PotionData");
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }
}
