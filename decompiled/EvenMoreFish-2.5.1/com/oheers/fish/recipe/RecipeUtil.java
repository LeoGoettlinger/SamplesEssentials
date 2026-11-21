package com.oheers.fish.recipe;

import com.oheers.fish.api.config.serializer.ItemSerializer;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RecipeUtil {
   @Nullable
   public static RecipeChoice getRecipeChoice(String materialStr) {
      ItemStack customItem = ItemSerializer.get().deserialize(materialStr, true);
      return customItem != null && !customItem.isEmpty() ? new ExactChoice(customItem) : null;
   }

   @Nullable
   public static EMFRecipe<?> getRecipe(@NonNull Section section, @NonNull NamespacedKey key, @NonNull ItemStack result) {
      String type = section.getString("type");
      if (type == null) {
         return null;
      } else {
         String var4 = type.toLowerCase();

         return (EMFRecipe<?>)(switch (var4) {
            case "shapeless" -> {
               List<String> ingredients = section.getStringList("ingredients");
               yield new EMFShapelessRecipe(key, result, ingredients);
            }
            case "shaped" -> new EMFShapedRecipe(key, result, section);
            default -> null;
         });
      }
   }
}
