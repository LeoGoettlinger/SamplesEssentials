package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.util.CustomModelDataUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

@Deprecated(
   forRemoval = true,
   since = "4.9.10.10"
)
public class RecipeManager {
   private final Graves plugin;
   private final List<NamespacedKey> namespacedKeyList;

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public RecipeManager(Graves plugin) {
      this.plugin = plugin;
      this.namespacedKeyList = new ArrayList<>();
      this.reload();
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public void reload() {
      this.unload();
      this.load();
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public void load() {
      ConfigurationSection configurationSection = this.plugin.getConfig().getConfigurationSection("settings.token");
      if (configurationSection != null) {
         for (String key : configurationSection.getKeys(false)) {
            if (this.plugin.getConfig().getBoolean("settings.token." + key + ".craft")) {
               this.addTokenRecipe(key, this.getToken(key));
               this.plugin.debugMessage("Added recipe " + key, 1);
            }
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public void unload() {
      try {
         Iterator<Recipe> iterator = Bukkit.recipeIterator();

         while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe != null) {
               ItemStack itemStack = recipe.getResult();
               if (itemStack.hasItemMeta() && this.isToken(itemStack)) {
                  iterator.remove();
               }
            }
         }
      } catch (Exception var4) {
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public ItemStack getToken(String token) {
      if (this.plugin.getConfig().isConfigurationSection("settings.token." + token)) {
         Material material = Material.matchMaterial(this.plugin.getConfig().getString("settings.token." + token + ".material", "SUNFLOWER"));
         ItemStack itemStack = new ItemStack(material != null ? material : Material.CHEST);
         this.setRecipeData(token, itemStack);
         if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
               String name;
               if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                  String newName = StringUtil.parseString("&f" + this.plugin.getConfig().getString("settings.token." + token + ".name"), this.plugin);
                  name = MiniMessage.parseString(newName);
               } else {
                  name = ChatColor.WHITE + StringUtil.parseString(this.plugin.getConfig().getString("settings.token." + token + ".name"), this.plugin);
               }

               List<String> loreList = new ArrayList<>();
               int customModelData = this.plugin.getConfig().getInt("settings.token." + token + ".model-data", -1);

               for (String string : this.plugin.getConfig().getStringList("settings.token." + token + ".lore")) {
                  if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                     String newLine = StringUtil.parseString("&7" + string, this.plugin);
                     loreList.add(MiniMessage.parseString(newLine));
                  } else {
                     loreList.add(ChatColor.GRAY + StringUtil.parseString(string, this.plugin));
                  }
               }

               if (this.plugin.getConfig().getBoolean("settings.token." + token + ".glow")) {
                  itemMeta.addEnchant(this.plugin.getVersionManager().getEnchantmentForVersion("DURABILITY"), 1, true);
                  itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               }

               CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
               itemMeta.setLore(loreList);
               itemMeta.setDisplayName(name);
               itemStack.setItemMeta(itemMeta);
            }
         }

         return itemStack;
      } else {
         return null;
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public List<String> getTokenList() {
      List<String> stringList = new ArrayList<>();
      ConfigurationSection configurationSection = this.plugin.getConfig().getConfigurationSection("settings.token");
      if (configurationSection != null) {
         stringList.addAll(configurationSection.getKeys(false));
      }

      return stringList;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public void addTokenRecipe(String token, ItemStack itemStack) {
      NamespacedKey namespacedKey = new NamespacedKey(this.plugin, token + "GraveToken");
      if (!this.namespacedKeyList.contains(namespacedKey)) {
         try {
            List<String> lineList = this.plugin.getConfig().getStringList("settings.token." + token + ".recipe");
            if (lineList.size() < 3) {
               this.plugin.getLogger().warning("Invalid recipe format for token " + token + ", expected 3 lines.");
               return;
            }

            Map<Character, Material> ingredients = new HashMap<>();
            StringBuilder[] shapeLines = new StringBuilder[]{new StringBuilder(), new StringBuilder(), new StringBuilder()};
            int recipeKey = 1;

            for (int row = 0; row < 3; row++) {
               String[] parts = lineList.get(row).split(" ");

               for (String part : parts) {
                  char ingredientChar = this.getChar(recipeKey);
                  Material mat = Material.matchMaterial(part);
                  if (mat != null && mat != Material.AIR) {
                     ingredients.put(ingredientChar, mat);
                     shapeLines[row].append(ingredientChar);
                  } else {
                     shapeLines[row].append(' ');
                  }

                  recipeKey++;
               }
            }

            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, itemStack);
            shapedRecipe.shape(new String[]{shapeLines[0].toString(), shapeLines[1].toString(), shapeLines[2].toString()});

            for (Entry<Character, Material> entry : ingredients.entrySet()) {
               shapedRecipe.setIngredient(entry.getKey(), entry.getValue());
            }

            if (this.plugin.getServer().getRecipe(namespacedKey) == null) {
               this.plugin.getServer().addRecipe(shapedRecipe);
               this.namespacedKeyList.add(namespacedKey);
            } else {
               this.plugin.debugMessage("Unable to add recipe " + namespacedKey.getKey(), 1);
            }
         } catch (Exception var16) {
            this.plugin.getLogger().severe("Unable to register token recipe for " + token + ". Check your recipe in token.yml");
            this.plugin.getLogger().severe("This is likely an invalid Material or a Spigot-related bug.");
            this.plugin.logStackTrace(var16);
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public ItemStack getGraveTokenFromPlayer(String token, List<ItemStack> itemStackList) {
      for (ItemStack itemStack : itemStackList) {
         if (itemStack != null && this.isToken(token, itemStack)) {
            return itemStack;
         }
      }

      return null;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public void setRecipeData(String token, ItemStack itemStack) {
      if (this.plugin.getVersionManager().hasPersistentData()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "token"), PersistentDataType.STRING, token);
            itemStack.setItemMeta(itemMeta);
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public boolean isToken(String token, ItemStack itemStack) {
      if (this.plugin.getVersionManager().hasPersistentData()) {
         if (itemStack.getItemMeta() != null
            && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "token"), PersistentDataType.STRING)) {
            String string = (String)itemStack.getItemMeta()
               .getPersistentDataContainer()
               .get(new NamespacedKey(this.plugin, "token"), PersistentDataType.STRING);
            return string != null && string.equals(token);
         }
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
               for (String loreEntry : lore) {
                  if (loreEntry.equals(token)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public String getTokenName(ItemStack itemStack) {
      return this.plugin.getVersionManager().hasPersistentData()
            && itemStack.getItemMeta() != null
            && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "token"), PersistentDataType.STRING)
         ? (String)itemStack.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(this.plugin, "token"), PersistentDataType.STRING)
         : null;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   public boolean isToken(ItemStack itemStack) {
      return this.plugin.getVersionManager().hasPersistentData()
         && itemStack.getItemMeta() != null
         && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "token"), PersistentDataType.STRING);
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   private char getChar(int count) {
      return switch (count) {
         case 1 -> 'A';
         case 2 -> 'B';
         case 3 -> 'C';
         case 4 -> 'D';
         case 5 -> 'E';
         case 6 -> 'F';
         case 7 -> 'G';
         case 8 -> 'H';
         case 9 -> 'I';
         default -> '*';
      };
   }
}
