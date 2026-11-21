package com.badbones69.crazyauctions.api.support;

import com.badbones69.crazyauctions.CrazyAuctions;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SkullCreator {
   @NotNull
   private static final CrazyAuctions plugin = (CrazyAuctions)JavaPlugin.getPlugin(CrazyAuctions.class);

   public static ItemStack itemFromUuid(UUID id) {
      ItemStack item = getPlayerSkullItem();
      return itemWithUuid(item, id);
   }

   public static ItemStack itemWithUuid(ItemStack item, UUID id) {
      notNull(item, "item");
      notNull(id, "id");
      SkullMeta meta = (SkullMeta)item.getItemMeta();
      meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(id));
      item.setItemMeta(meta);
      return item;
   }

   public static ItemStack itemFromUrl(String url) {
      ItemStack item = getPlayerSkullItem();
      return itemWithUrl(item, url);
   }

   public static ItemStack itemWithUrl(ItemStack item, String url) {
      notNull(item, "item");
      notNull(url, "url");
      return itemWithBase64(item, urlToBase64(url));
   }

   public static ItemStack itemFromBase64(String base64) {
      ItemStack item = getPlayerSkullItem();
      return itemWithBase64(item, base64);
   }

   public static ItemStack itemWithBase64(ItemStack item, String base64) {
      notNull(item, "item");
      notNull(base64, "base64");
      UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
      return plugin.getServer().getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
   }

   public static void blockWithUuid(Block block, UUID id) {
      notNull(block, "block");
      notNull(id, "id");
      setBlockType(block);
      ((Skull)block.getState()).setOwningPlayer(Bukkit.getOfflinePlayer(id));
   }

   public static void blockWithUrl(Block block, String url) {
      notNull(block, "block");
      notNull(url, "url");
      blockWithBase64(block, urlToBase64(url));
   }

   public static void blockWithBase64(Block block, String base64) {
      notNull(block, "block");
      notNull(base64, "base64");
      UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
      String args = String.format(
         "%d %d %d %s", block.getX(), block.getY(), block.getZ(), "{Owner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}"
      );
      plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "data merge block " + args);
   }

   private static ItemStack getPlayerSkullItem() {
      return new ItemStack(Material.PLAYER_HEAD);
   }

   private static void setBlockType(Block block) {
      block.setType(Material.PLAYER_HEAD, false);
   }

   private static void notNull(Object instance, String name) {
      if (instance == null) {
         throw new NullPointerException(name + " should not be null!");
      }
   }

   private static String urlToBase64(String url) {
      URI actualUrl;
      try {
         actualUrl = new URI(url);
      } catch (URISyntaxException var3) {
         throw new RuntimeException(var3);
      }

      String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
      return Base64.getEncoder().encodeToString(toEncode.getBytes());
   }
}
