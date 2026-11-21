package net.ess3.provider.providers;

import net.ess3.provider.BannerDataProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

@ProviderData(
   description = "Legacy Banner Meta Provider"
)
public class LegacyBannerDataProvider implements BannerDataProvider {
   @Override
   public DyeColor getBaseColor(ItemStack stack) {
      BannerMeta bannerMeta = (BannerMeta)stack.getItemMeta();
      return bannerMeta.getBaseColor();
   }

   @Override
   public void setBaseColor(ItemStack stack, DyeColor color) {
      BannerMeta bannerMeta = (BannerMeta)stack.getItemMeta();
      bannerMeta.setBaseColor(color);
      stack.setItemMeta(bannerMeta);
   }
}
