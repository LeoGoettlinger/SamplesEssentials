package com.ranull.graves.integration;

import com.jojodmo.itembridge.ItemBridgeListener;
import com.jojodmo.itembridge.ItemBridgeListenerPriority;
import com.ranull.graves.Graves;
import java.util.Locale;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemBridge implements ItemBridgeListener {
   private final Graves plugin;
   private com.jojodmo.itembridge.ItemBridge itemBridge;

   public ItemBridge(Graves plugin) {
      this.plugin = plugin;
      this.unregister();
      this.register();
   }

   private void unregister() {
      if (this.itemBridge != null) {
         this.itemBridge.removeListener(this);
      }
   }

   private void register() {
      this.itemBridge = new com.jojodmo.itembridge.ItemBridge(this.plugin, new String[]{"graves", "grave"});
      this.itemBridge.registerListener(this);
   }

   public ItemBridgeListenerPriority getPriority() {
      return ItemBridgeListenerPriority.MEDIUM;
   }

   public ItemStack fetchItemStack(@NotNull String string) {
      String key = string.toLowerCase(Locale.ROOT);
      if (this.plugin.getVersionManager().hasPersistentData() && key.startsWith("token_")) {
         String token = key.replaceFirst("token_", "");
         return this.plugin.getConfig().isSet("settings.token." + token) ? this.plugin.getRecipeManager().getToken(token) : null;
      } else {
         return null;
      }
   }

   public String getItemName(@NotNull ItemStack itemStack) {
      return this.plugin.getVersionManager().hasPersistentData() && this.plugin.getRecipeManager().isToken(itemStack)
         ? this.plugin.getRecipeManager().getTokenName(itemStack)
         : null;
   }

   public boolean isItem(@NotNull ItemStack itemStack, @NotNull String string) {
      String name = this.getItemName(itemStack);
      return name != null && name.equals(string);
   }
}
