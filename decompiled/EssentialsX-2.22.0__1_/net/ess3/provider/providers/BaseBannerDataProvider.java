package net.ess3.provider.providers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.ess3.provider.BannerDataProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@ProviderData(
   description = "1.20.5+ Banner Data Provider",
   weight = 1
)
public class BaseBannerDataProvider implements BannerDataProvider {
   private final BiMap<Material, DyeColor> materialToDyeMap = HashBiMap.create();

   public BaseBannerDataProvider() {
      this.materialToDyeMap.put(Material.WHITE_BANNER, DyeColor.WHITE);
      this.materialToDyeMap.put(Material.LIGHT_GRAY_BANNER, DyeColor.LIGHT_GRAY);
      this.materialToDyeMap.put(Material.GRAY_BANNER, DyeColor.GRAY);
      this.materialToDyeMap.put(Material.BLACK_BANNER, DyeColor.BLACK);
      this.materialToDyeMap.put(Material.RED_BANNER, DyeColor.RED);
      this.materialToDyeMap.put(Material.ORANGE_BANNER, DyeColor.ORANGE);
      this.materialToDyeMap.put(Material.YELLOW_BANNER, DyeColor.YELLOW);
      this.materialToDyeMap.put(Material.LIME_BANNER, DyeColor.LIME);
      this.materialToDyeMap.put(Material.GREEN_BANNER, DyeColor.GREEN);
      this.materialToDyeMap.put(Material.CYAN_BANNER, DyeColor.CYAN);
      this.materialToDyeMap.put(Material.LIGHT_BLUE_BANNER, DyeColor.LIGHT_BLUE);
      this.materialToDyeMap.put(Material.BLUE_BANNER, DyeColor.BLUE);
      this.materialToDyeMap.put(Material.PURPLE_BANNER, DyeColor.PURPLE);
      this.materialToDyeMap.put(Material.MAGENTA_BANNER, DyeColor.MAGENTA);
      this.materialToDyeMap.put(Material.PINK_BANNER, DyeColor.PINK);
      this.materialToDyeMap.put(Material.BROWN_BANNER, DyeColor.BROWN);
   }

   @Override
   public DyeColor getBaseColor(ItemStack stack) {
      return (DyeColor)this.materialToDyeMap.get(stack.getType());
   }

   @Override
   public void setBaseColor(ItemStack stack, DyeColor color) {
      Material material = (Material)this.materialToDyeMap.inverse().get(color);
      if (material != null) {
         stack.setType(material);
      }
   }

   @ProviderTest
   public static boolean test() {
      try {
         Material needAVariable = Material.LIGHT_BLUE_BANNER;
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }
}
