package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.libs.kyori.adventure.text.format.NamedTextColor;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.ess3.api.TranslatableException;
import net.ess3.provider.InventoryViewProvider;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.TransmuteRecipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;

public class Commandrecipe extends EssentialsCommand {
   private static final Material FIREWORK_ROCKET = EnumUtil.getMaterial("FIREWORK_ROCKET", "FIREWORK");
   private static final Material FIREWORK_STAR = EnumUtil.getMaterial("FIREWORK_STAR", "FIREWORK_CHARGE");
   private static final Material GUNPOWDER = EnumUtil.getMaterial("GUNPOWDER", "SULPHUR");
   private final boolean unsupported;

   public Commandrecipe() {
      super("recipe");
      boolean unsupported = false;
      if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_12_0_R01)) {
         try {
            Class.forName("com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent");
         } catch (ClassNotFoundException var3) {
            unsupported = true;
         }
      }

      this.unsupported = unsupported;
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (this.unsupported) {
         sender.sendTl("unsupportedFeature");
      } else if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack itemType;
         if (args[0].equalsIgnoreCase("hand")) {
            if (!sender.isPlayer()) {
               throw new TranslatableException("consoleCannotUseCommand");
            }

            itemType = Inventories.getItemInHand(sender.getPlayer());
         } else {
            itemType = this.ess.getItemDb().get(args[0]);
         }

         int recipeNo = 0;
         if (args.length > 1) {
            if (!NumberUtil.isInt(args[1])) {
               throw new TranslatableException("invalidNumber");
            }

            recipeNo = Integer.parseInt(args[1]) - 1;
         }

         List<Recipe> bukkitRecipes = this.ess.getServer().getRecipesFor(itemType);
         if (bukkitRecipes.isEmpty()) {
            throw new TranslatableException("recipeNone", this.getMaterialName(sender, itemType));
         } else {
            List<Recipe> recipes = new ArrayList<>();

            for (Recipe recipe : bukkitRecipes) {
               if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_21_3_R01) && recipe instanceof TransmuteRecipe) {
                  TransmuteRecipe transmuteRecipe = (TransmuteRecipe)recipe;

                  for (ItemStack inputChoice : this.toChoices(transmuteRecipe.getInput())) {
                     for (ItemStack materialChoice : this.toChoices(transmuteRecipe.getMaterial())) {
                        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(this.ess, "transmute"), itemType);
                        shapelessRecipe.addIngredient(inputChoice);
                        shapelessRecipe.addIngredient(materialChoice);
                        recipes.add(shapelessRecipe);
                     }
                  }
               } else {
                  recipes.add(recipe);
               }
            }

            if (recipeNo >= 0 && recipeNo < recipes.size()) {
               Recipe selectedRecipe = recipes.get(recipeNo);
               sender.sendTl("recipe", this.getMaterialName(sender, itemType), recipeNo + 1, recipes.size());
               if (selectedRecipe instanceof FurnaceRecipe) {
                  this.furnaceRecipe(sender, (FurnaceRecipe)selectedRecipe);
               } else if (selectedRecipe instanceof ShapedRecipe) {
                  this.shapedRecipe(sender, (ShapedRecipe)selectedRecipe, sender.isPlayer());
               } else if (selectedRecipe instanceof ShapelessRecipe) {
                  if (recipes.size() == 1 && itemType.getType() == FIREWORK_ROCKET) {
                     ShapelessRecipe shapelessRecipe = new ShapelessRecipe(itemType);
                     shapelessRecipe.addIngredient(GUNPOWDER);
                     shapelessRecipe.addIngredient(Material.PAPER);
                     shapelessRecipe.addIngredient(FIREWORK_STAR);
                     this.shapelessRecipe(sender, shapelessRecipe, sender.isPlayer());
                  } else {
                     this.shapelessRecipe(sender, (ShapelessRecipe)selectedRecipe, sender.isPlayer());
                  }
               }

               if (recipes.size() > 1 && args.length == 1) {
                  sender.sendTl("recipeMore", commandLabel, args[0], this.getMaterialName(sender, itemType));
               }
            } else {
               throw new TranslatableException("recipeBadIndex");
            }
         }
      }
   }

   private List<ItemStack> toChoices(final RecipeChoice choice) {
      if (!(choice instanceof MaterialChoice)) {
         return choice instanceof ExactChoice ? ((ExactChoice)choice).getChoices() : Collections.emptyList();
      } else {
         List<ItemStack> stacks = new ArrayList<>();

         for (Material material : ((MaterialChoice)choice).getChoices()) {
            stacks.add(new ItemStack(material, 1));
         }

         return stacks;
      }
   }

   public void furnaceRecipe(final CommandSource sender, final FurnaceRecipe recipe) {
      sender.sendTl("recipeFurnace", this.getMaterialName(sender, recipe.getInput()));
   }

   public void shapedRecipe(final CommandSource sender, final ShapedRecipe recipe, final boolean showWindow) {
      Map<Character, ItemStack> recipeMap = recipe.getIngredientMap();
      if (showWindow) {
         User user = this.ess.getUser(sender.getPlayer());
         user.getBase().closeInventory();
         user.setRecipeSee(true);
         InventoryView view = this.openWorkbench(user);
         if (view == null) {
            user.setRecipeSee(false);
            return;
         }

         Inventory topInventory = this.ess.provider(InventoryViewProvider.class).getTopInventory(view);
         String[] recipeShape = recipe.getShape();
         Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

         for (int j = 0; j < recipeShape.length; j++) {
            for (int k = 0; k < recipeShape[j].length(); k++) {
               ItemStack item = ingredientMap.get(recipeShape[j].toCharArray()[k]);
               if (item != null) {
                  if (VersionUtil.PRE_FLATTENING && item.getDurability() == 32767) {
                     item.setDurability((short)0);
                  }

                  topInventory.setItem(j * 3 + k + 1, item);
               }
            }
         }
      } else {
         HashMap<Material, String> colorMap = new HashMap<>();
         int i = 1;
         char[] var15 = "abcdefghi".toCharArray();
         int var17 = var15.length;

         for (int var20 = 0; var20 < var17; var20++) {
            Character c = var15[var20];
            ItemStack item = recipeMap.get(c);
            if (!colorMap.containsKey(item == null ? null : item.getType())) {
               colorMap.put(item == null ? null : item.getType(), String.valueOf(i++));
            }
         }

         Material[][] materials = new Material[3][3];

         for (int j = 0; j < recipe.getShape().length; j++) {
            for (int kx = 0; kx < recipe.getShape()[j].length(); kx++) {
               ItemStack item = (ItemStack)recipe.getIngredientMap().get(recipe.getShape()[j].toCharArray()[kx]);
               materials[j][kx] = item == null ? null : item.getType();
            }
         }

         sender.sendTl(
            "recipeGrid", this.colorTag(colorMap, materials, 0, 0), this.colorTag(colorMap, materials, 0, 1), this.colorTag(colorMap, materials, 0, 2)
         );
         sender.sendTl(
            "recipeGrid", this.colorTag(colorMap, materials, 1, 0), this.colorTag(colorMap, materials, 1, 1), this.colorTag(colorMap, materials, 1, 2)
         );
         sender.sendTl(
            "recipeGrid", this.colorTag(colorMap, materials, 2, 0), this.colorTag(colorMap, materials, 2, 1), this.colorTag(colorMap, materials, 2, 2)
         );
         StringBuilder s = new StringBuilder();

         for (Material items : colorMap.keySet().toArray(new Material[0])) {
            s.append(sender.tl("recipeGridItem", colorMap.get(items), this.getMaterialName(sender, items))).append(" ");
         }

         sender.sendTl("recipeWhere", AdventureUtil.parsed(s.toString()));
      }
   }

   private AdventureUtil.ParsedPlaceholder colorTag(final Map<Material, String> colorMap, final Material[][] materials, final int x, final int y) {
      char colorChar = colorMap.get(materials[x][y]).charAt(0);
      NamedTextColor namedTextColor = AdventureUtil.fromChar(colorChar);
      if (namedTextColor == null) {
         throw new IllegalStateException("Illegal amount of materials in recipe");
      } else {
         return AdventureUtil.parsed("<" + namedTextColor + ">" + colorChar);
      }
   }

   public void shapelessRecipe(final CommandSource sender, final ShapelessRecipe recipe, final boolean showWindow) {
      List<ItemStack> ingredients = recipe.getIngredientList();
      if (showWindow) {
         User user = this.ess.getUser(sender.getPlayer());
         user.getBase().closeInventory();
         user.setRecipeSee(true);
         InventoryView view = this.openWorkbench(user);
         if (view == null) {
            user.setRecipeSee(false);
            return;
         }

         for (int i = 0; i < ingredients.size(); i++) {
            ItemStack item = ingredients.get(i);
            if (VersionUtil.PRE_FLATTENING && item.getDurability() == 32767) {
               item.setDurability((short)0);
            }

            this.ess.provider(InventoryViewProvider.class).setItem(view, i + 1, item);
         }
      } else {
         StringBuilder s = new StringBuilder();

         for (int i = 0; i < ingredients.size(); i++) {
            s.append(this.getMaterialName(sender, ingredients.get(i)));
            if (i != ingredients.size() - 1) {
               s.append(",");
            }

            s.append(" ");
         }

         sender.sendTl("recipeShapeless", s.toString());
      }
   }

   private InventoryView openWorkbench(final User user) {
      InventoryView view = user.getBase().openWorkbench(null, true);
      if (view == null) {
         return null;
      } else {
         Inventory inventory = this.ess.provider(InventoryViewProvider.class).getTopInventory(view);
         return inventory.getType() == InventoryType.WORKBENCH ? view : null;
      }
   }

   public String getMaterialName(final CommandSource sender, final ItemStack stack) {
      return stack == null ? sender.tl("recipeNothing") : this.getMaterialName(sender, stack.getType());
   }

   public String getMaterialName(final CommandSource sender, final Material type) {
      return type == null ? sender.tl("recipeNothing") : type.toString().replace("_", " ").toLowerCase(Locale.ENGLISH);
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getItems() : Collections.emptyList();
   }
}
