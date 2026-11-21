package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class Commandcondense extends EssentialsCommand {
   private final Map<ItemStack, Commandcondense.SimpleRecipe> condenseList = new HashMap<>();

   public Commandcondense() {
      super("condense");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      List<ItemStack> is = new ArrayList<>();
      boolean validateReverse = false;
      if (args.length > 0) {
         is = this.ess.getItemDb().getMatching(user, args);
      } else {
         for (ItemStack stack : Inventories.getInventory(user.getBase(), false)) {
            if (stack != null && stack.getType() != Material.AIR) {
               is.add(stack);
            }
         }

         validateReverse = true;
      }

      boolean didConvert = false;

      for (ItemStack itemStack : is) {
         if (this.condenseStack(user, itemStack, validateReverse)) {
            didConvert = true;
         }
      }

      user.getBase().updateInventory();
      if (didConvert) {
         user.sendTl("itemsConverted");
      } else {
         user.sendTl("itemsNotConverted");
         throw new NoChargeException();
      }
   }

   private boolean condenseStack(final User user, final ItemStack stack, final boolean validateReverse) throws ChargeException, MaxMoneyException {
      Commandcondense.SimpleRecipe condenseType = this.getCondenseType(stack);
      if (condenseType != null) {
         ItemStack input = condenseType.getInput();
         ItemStack result = condenseType.getResult();
         if (validateReverse) {
            boolean pass = false;

            for (Recipe revRecipe : this.ess.getServer().getRecipesFor(input)) {
               if (this.getStackOnRecipeMatch(revRecipe, result) != null) {
                  pass = true;
                  break;
               }
            }

            if (!pass) {
               return false;
            }
         }

         int amount = 0;

         for (ItemStack contents : Inventories.getInventory(user.getBase(), false)) {
            if (contents != null && contents.isSimilar(stack)) {
               amount += contents.getAmount();
            }
         }

         int output = amount / input.getAmount() * result.getAmount();
         amount -= amount % input.getAmount();
         if (amount > 0) {
            input.setAmount(amount);
            result.setAmount(output);
            Trade remove = new Trade(input, this.ess);
            Trade add = new Trade(result, this.ess);
            remove.charge(user);
            add.pay(user, Trade.OverflowType.DROP);
            return true;
         }
      }

      return false;
   }

   private Commandcondense.SimpleRecipe getCondenseType(final ItemStack stack) {
      if (this.condenseList.containsKey(stack)) {
         return this.condenseList.get(stack);
      } else {
         Iterator<Recipe> intr = this.ess.getServer().recipeIterator();
         List<Commandcondense.SimpleRecipe> bestRecipes = new ArrayList<>();

         while (intr.hasNext()) {
            Recipe recipe = intr.next();
            Collection<ItemStack> recipeItems = this.getStackOnRecipeMatch(recipe, stack);
            if (recipeItems != null && (recipeItems.size() == 4 || recipeItems.size() == 9) && recipeItems.size() > recipe.getResult().getAmount()) {
               ItemStack input = stack.clone();
               input.setAmount(recipeItems.size());
               Commandcondense.SimpleRecipe newRecipe = new Commandcondense.SimpleRecipe(recipe.getResult(), input);
               bestRecipes.add(newRecipe);
            }
         }

         if (!bestRecipes.isEmpty()) {
            if (bestRecipes.size() > 1) {
               bestRecipes.sort(Commandcondense.SimpleRecipeComparator.INSTANCE);
            }

            Commandcondense.SimpleRecipe recipe = bestRecipes.get(0);
            this.condenseList.put(stack, recipe);
            return recipe;
         } else {
            this.condenseList.put(stack, null);
            return null;
         }
      }
   }

   private Collection<ItemStack> getStackOnRecipeMatch(final Recipe recipe, final ItemStack stack) {
      Collection<ItemStack> inputList;
      if (recipe instanceof ShapedRecipe) {
         ShapedRecipe sRecipe = (ShapedRecipe)recipe;
         if (sRecipe.getShape().length != sRecipe.getShape()[0].length()) {
            return null;
         }

         inputList = sRecipe.getIngredientMap().values();
      } else {
         if (!(recipe instanceof ShapelessRecipe)) {
            return null;
         }

         ShapelessRecipe slRecipe = (ShapelessRecipe)recipe;
         inputList = slRecipe.getIngredientList();
      }

      boolean match = true;
      Iterator<ItemStack> iter = inputList.iterator();

      while (iter.hasNext()) {
         ItemStack inputSlot = iter.next();
         if (inputSlot == null) {
            iter.remove();
         } else {
            if (VersionUtil.PRE_FLATTENING && inputSlot.getDurability() == 32767) {
               inputSlot.setDurability((short)0);
            }

            if (!inputSlot.isSimilar(stack)) {
               match = false;
            }
         }
      }

      return match ? inputList : null;
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getMatchingItems(args[0]) : Collections.emptyList();
   }

   private static final class SimpleRecipe implements Recipe {
      private final ItemStack result;
      private final ItemStack input;

      private SimpleRecipe(final ItemStack result, final ItemStack input) {
         this.result = result;
         this.input = input;
      }

      public ItemStack getResult() {
         return this.result.clone();
      }

      public ItemStack getInput() {
         return this.input.clone();
      }
   }

   private static class SimpleRecipeComparator implements Comparator<Commandcondense.SimpleRecipe> {
      private static final Commandcondense.SimpleRecipeComparator INSTANCE = new Commandcondense.SimpleRecipeComparator();

      public int compare(final Commandcondense.SimpleRecipe o1, final Commandcondense.SimpleRecipe o2) {
         return Integer.compare(o2.getInput().getAmount(), o1.getInput().getAmount());
      }
   }
}
