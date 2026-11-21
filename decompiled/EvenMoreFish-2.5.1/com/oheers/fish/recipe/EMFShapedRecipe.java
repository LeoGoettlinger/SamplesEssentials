package com.oheers.fish.recipe;

import com.oheers.fish.FishUtils;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;

public class EMFShapedRecipe extends EMFRecipe<ShapedRecipe> {
   private final List<String> rawShape;
   private final Section ingredientsSection;
   private final NamespacedKey key;
   private final ItemStack result;

   public EMFShapedRecipe(@NonNull NamespacedKey key, @NonNull ItemStack result, @NonNull Section section) {
      this.rawShape = section.getStringList("shape");
      this.ingredientsSection = section.getSection("ingredients");
      if (!this.rawShape.isEmpty() && this.ingredientsSection != null) {
         this.key = key;
         this.result = result;
      } else {
         throw new RuntimeException("Shaped recipe is missing shape or ingredients.");
      }
   }

   @NonNull
   protected ShapedRecipe prepareRecipe() {
      ShapedRecipe recipe = new ShapedRecipe(this.key, this.result);
      String[] shape = this.rawShape.stream().limit(3L).toArray(String[]::new);
      recipe.shape(shape);
      this.ingredientsSection.getRoutesAsStrings(false).forEach(key -> {
         char character = FishUtils.getCharFromString(key, '#');
         String materialStr = this.ingredientsSection.getString(key);
         if (materialStr != null) {
            RecipeChoice choice = RecipeUtil.getRecipeChoice(materialStr);
            if (choice != null) {
               recipe.setIngredient(character, choice);
            }
         }
      });
      return recipe;
   }
}
