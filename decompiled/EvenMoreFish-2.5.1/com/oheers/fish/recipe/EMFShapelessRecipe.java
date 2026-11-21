package com.oheers.fish.recipe;

import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jspecify.annotations.NonNull;

public class EMFShapelessRecipe extends EMFRecipe<ShapelessRecipe> {
   private final List<String> ingredients;
   private final NamespacedKey key;
   private final ItemStack result;

   public EMFShapelessRecipe(@NonNull NamespacedKey key, @NonNull ItemStack result, @NonNull List<String> ingredients) {
      if (ingredients.isEmpty()) {
         throw new RuntimeException("Shapeless recipe is missing ingredients.");
      } else {
         this.ingredients = ingredients;
         this.key = key;
         this.result = result;
      }
   }

   @NonNull
   protected ShapelessRecipe prepareRecipe() {
      ShapelessRecipe recipe = new ShapelessRecipe(this.key, this.result);
      this.ingredients.forEach(ingredient -> {
         RecipeChoice choice = RecipeUtil.getRecipeChoice(ingredient);
         if (choice != null) {
            recipe.addIngredient(choice);
         }
      });
      return recipe;
   }
}
