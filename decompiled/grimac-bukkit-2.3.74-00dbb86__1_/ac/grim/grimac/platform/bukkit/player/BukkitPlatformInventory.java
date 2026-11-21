package ac.grim.grimac.platform.bukkit.player;

import ac.grim.grimac.platform.api.player.PlatformInventory;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.SpigotConversionUtil;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import lombok.Generated;
import org.bukkit.entity.Player;

public class BukkitPlatformInventory implements PlatformInventory {
   @NotNull
   private final Player bukkitPlayer;

   @Override
   public ItemStack getItemInHand() {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getItemInHand());
   }

   @Override
   public ItemStack getItemInOffHand() {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getItemInOffHand());
   }

   @Override
   public ItemStack getStack(int bukkitSlot, int vanillaSlot) {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getItem(bukkitSlot));
   }

   @Override
   public ItemStack getHelmet() {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getHelmet());
   }

   @Override
   public ItemStack getChestplate() {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getChestplate());
   }

   @Override
   public ItemStack getLeggings() {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getLeggings());
   }

   @Override
   public ItemStack getBoots() {
      return SpigotConversionUtil.fromBukkitItemStack(this.bukkitPlayer.getInventory().getBoots());
   }

   @Override
   public ItemStack[] getContents() {
      org.bukkit.inventory.ItemStack[] bukkitItems = this.bukkitPlayer.getInventory().getContents();
      ItemStack[] items = new ItemStack[bukkitItems.length];

      for (int i = 0; i < bukkitItems.length; i++) {
         if (bukkitItems[i] != null) {
            items[i] = SpigotConversionUtil.fromBukkitItemStack(bukkitItems[i]);
         }
      }

      return items;
   }

   @Override
   public String getOpenInventoryKey() {
      return this.bukkitPlayer.getOpenInventory().getType().toString();
   }

   @Generated
   public BukkitPlatformInventory(@NotNull Player bukkitPlayer) {
      if (bukkitPlayer == null) {
         throw new NullPointerException("bukkitPlayer is marked non-null but is null");
      } else {
         this.bukkitPlayer = bukkitPlayer;
      }
   }
}
