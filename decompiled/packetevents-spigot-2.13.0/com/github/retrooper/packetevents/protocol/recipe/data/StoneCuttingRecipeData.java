package com.github.retrooper.packetevents.protocol.recipe.data;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.recipe.Ingredient;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Obsolete
public class StoneCuttingRecipeData implements RecipeData {
   private final String group;
   private final Ingredient ingredient;
   private final ItemStack result;

   public StoneCuttingRecipeData(String group, Ingredient ingredient, ItemStack result) {
      this.group = group;
      this.ingredient = ingredient;
      this.result = result;
   }

   public static StoneCuttingRecipeData read(PacketWrapper<?> wrapper) {
      String group = wrapper.readString();
      Ingredient ingredient = Ingredient.read(wrapper);
      ItemStack result = wrapper.readItemStack();
      return new StoneCuttingRecipeData(group, ingredient, result);
   }

   public static void write(PacketWrapper<?> wrapper, StoneCuttingRecipeData data) {
      wrapper.writeString(data.group);
      Ingredient.write(wrapper, data.ingredient);
      wrapper.writeItemStack(data.result);
   }

   public String getGroup() {
      return this.group;
   }

   public Ingredient getIngredient() {
      return this.ingredient;
   }

   public ItemStack getResult() {
      return this.result;
   }
}
