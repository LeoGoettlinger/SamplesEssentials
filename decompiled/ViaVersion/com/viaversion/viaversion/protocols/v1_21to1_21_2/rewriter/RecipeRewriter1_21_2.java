package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

final class RecipeRewriter1_21_2 extends RecipeRewriter1_20_3<ClientboundPacket1_21> implements StorableObject {
   private static final int[] EMPTY_ARRAY = new int[0];
   private final List<RecipeRewriter1_21_2.StoneCutterRecipe> stoneCutterRecipes = new ArrayList<>();
   private final Object2IntMap<String> recipeGroups = new Object2IntOpenHashMap<>();
   private final Map<String, RecipeRewriter1_21_2.Recipe> recipesByKey = new HashMap<>();
   private final Map<String, IntSet> recipeInputs = new Object2ObjectArrayMap<>();
   private final List<RecipeRewriter1_21_2.Recipe> recipes = new ArrayList<>();
   private String currentRecipeIdentifier;

   public void setCurrentRecipeIdentifier(String recipeIdentifier) {
      this.currentRecipeIdentifier = Key.stripMinecraftNamespace(recipeIdentifier);
   }

   RecipeRewriter1_21_2(Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
      super(protocol);
      this.recipeGroups.defaultReturnValue(-1);
      this.recipeHandlers.put("smelting", wrapper -> this.handleSmelting("furnace_input", wrapper));
      this.recipeHandlers.put("blasting", wrapper -> this.handleSmelting("blast_furnace_input", wrapper));
      this.recipeHandlers.put("smoking", wrapper -> this.handleSmelting("smoker_input", wrapper));
      this.recipeHandlers.put("campfire_cooking", wrapper -> this.handleSmelting("campfire_input", wrapper));
   }

   @Override
   public void handleSimpleRecipe(PacketWrapper wrapper) {
      int category = wrapper.read(Types.VAR_INT);
   }

   @Override
   public void handleStonecutting(PacketWrapper wrapper) {
      int group = this.readRecipeGroup(wrapper);
      Item[] ingredient = this.readIngredient(wrapper);
      Item result = this.rewrite(wrapper.user(), wrapper.read(this.itemType()));
      RecipeRewriter1_21_2.StoneCutterRecipe recipe = new RecipeRewriter1_21_2.StoneCutterRecipe(this.currentRecipeIdentifier, group, ingredient, result);
      this.stoneCutterRecipes.add(recipe);
   }

   private void addRecipe(RecipeRewriter1_21_2.Recipe recipe) {
      this.recipes.add(recipe);
      this.recipesByKey.put(this.currentRecipeIdentifier, recipe);
   }

   @Override
   public void handleSmithingTransform(PacketWrapper wrapper) {
      this.readRecipeInputs("smithing_template", wrapper);
      this.readRecipeInputs("smithing_base", wrapper);
      this.readRecipeInputs("smithing_addition", wrapper);
      wrapper.read(this.itemType());
   }

   @Override
   public void handleSmithingTrim(PacketWrapper wrapper) {
      this.readRecipeInputs("smithing_template", wrapper);
      this.readRecipeInputs("smithing_base", wrapper);
      this.readRecipeInputs("smithing_addition", wrapper);
   }

   @Override
   public void handleCraftingShaped(PacketWrapper wrapper) {
      int group = this.readRecipeGroup(wrapper);
      int category = wrapper.read(Types.VAR_INT);
      int width = wrapper.read(Types.VAR_INT);
      int height = wrapper.read(Types.VAR_INT);
      int ingredientsSize = width * height;
      Item[][] ingredients = new Item[ingredientsSize][];

      for (int i = 0; i < ingredientsSize; i++) {
         ingredients[i] = this.readIngredient(wrapper);
      }

      Item result = this.rewrite(wrapper.user(), wrapper.read(this.itemType()));
      boolean showNotification = wrapper.read(Types.BOOLEAN);
      RecipeRewriter1_21_2.ShapedRecipe recipe = new RecipeRewriter1_21_2.ShapedRecipe(
         this.recipesByKey.size(), this.currentRecipeIdentifier, group, category, width, height, ingredients, result, showNotification
      );
      this.addRecipe(recipe);
   }

   @Override
   public void handleCraftingShapeless(PacketWrapper wrapper) {
      int group = this.readRecipeGroup(wrapper);
      int category = wrapper.read(Types.VAR_INT);
      int ingredientsSize = wrapper.read(Types.VAR_INT);
      Item[][] ingredients = new Item[ingredientsSize][];

      for (int i = 0; i < ingredientsSize; i++) {
         ingredients[i] = this.readIngredient(wrapper);
      }

      Item result = this.rewrite(wrapper.user(), wrapper.read(this.itemType()));
      RecipeRewriter1_21_2.ShapelessRecipe recipe = new RecipeRewriter1_21_2.ShapelessRecipe(
         this.recipesByKey.size(), this.currentRecipeIdentifier, group, category, ingredients, result
      );
      this.addRecipe(recipe);
   }

   public void handleSmelting(String key, PacketWrapper wrapper) {
      int group = this.readRecipeGroup(wrapper);
      int category = wrapper.read(Types.VAR_INT);
      Item[] ingredient = this.readRecipeInputs(key, wrapper);
      Item result = this.rewrite(wrapper.user(), wrapper.read(this.itemType()));
      float experience = wrapper.read(Types.FLOAT);
      int cookingTime = wrapper.read(Types.VAR_INT);
      RecipeRewriter1_21_2.FurnaceRecipe recipe = new RecipeRewriter1_21_2.FurnaceRecipe(
         this.recipesByKey.size(), this.currentRecipeIdentifier, group, category, ingredient, result, cookingTime, experience
      );
      this.addRecipe(recipe);
   }

   private int readRecipeGroup(PacketWrapper wrapper) {
      String recipeGroup = Key.stripMinecraftNamespace(wrapper.read(Types.STRING));
      if (recipeGroup.isEmpty()) {
         return -1;
      } else if (this.recipeGroups.containsKey(recipeGroup)) {
         return this.recipeGroups.getInt(recipeGroup);
      } else {
         int size = this.recipeGroups.size();
         this.recipeGroups.put(recipeGroup, size);
         return size;
      }
   }

   private Item[] readIngredient(PacketWrapper wrapper) {
      Item[] items = wrapper.read(this.itemArrayType());

      for (int i = 0; i < items.length; i++) {
         Item item = items[i];
         items[i] = this.rewrite(wrapper.user(), item);
      }

      return items;
   }

   public HolderSet toHolderSet(Item[] ingredient) {
      int[] ids = new int[ingredient.length];

      for (int i = 0; i < ingredient.length; i++) {
         ids[i] = ingredient[i].identifier();
      }

      return HolderSet.of(ids);
   }

   @Nullable
   public RecipeRewriter1_21_2.Recipe recipe(String key) {
      return this.recipesByKey.get(Key.stripMinecraftNamespace(key));
   }

   @Nullable
   public RecipeRewriter1_21_2.Recipe recipe(int displayId) {
      return displayId >= 0 && displayId < this.recipes.size() ? this.recipes.get(displayId) : null;
   }

   public void finalizeRecipes() {
      FullMappings itemMappings = this.protocol.getMappingData().getFullItemMappings();
      FullMappings blockMappings = this.protocol.getMappingData().getFullBlockMappings();
      this.stoneCutterRecipes.sort(Comparator.comparing(recipe -> {
         String identifier = itemMappings.mappedIdentifier(recipe.result.identifier());
         return blockMappings.mappedId(identifier) == -1 ? "|" + identifier : identifier;
      }));
   }

   public void writeUpdateRecipeInputs(PacketWrapper wrapper) {
      wrapper.write(Types.VAR_INT, this.recipeInputs.size());

      for (Entry<String, IntSet> entry : this.recipeInputs.entrySet()) {
         wrapper.write(Types.STRING, entry.getKey());
         wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, entry.getValue().toArray(EMPTY_ARRAY));
      }

      wrapper.write(Types.VAR_INT, this.stoneCutterRecipes.size());

      for (RecipeRewriter1_21_2.StoneCutterRecipe recipe : this.stoneCutterRecipes) {
         HolderSet ingredient = this.toHolderSet(recipe.ingredient());
         wrapper.write(Types.HOLDER_SET, ingredient);
         writeItemDisplay(wrapper, recipe.result());
      }
   }

   private Item[] readRecipeInputs(String key, PacketWrapper wrapper) {
      Item[] ingredient = this.readIngredient(wrapper);
      IntSet ids = this.recipeInputs.computeIfAbsent(key, $ -> new IntOpenHashSet(ingredient.length));

      for (Item item : ingredient) {
         ids.add(item.identifier());
      }

      return ingredient;
   }

   private static void writeIngredientsDisplay(PacketWrapper wrapper, Item[][] ingredients) {
      wrapper.write(Types.VAR_INT, ingredients.length);

      for (Item[] ingredient : ingredients) {
         writeIngredientDisplay(wrapper, ingredient);
      }
   }

   private static void writeIngredientDisplay(PacketWrapper wrapper, Item[] ingredient) {
      if (ingredient.length == 0) {
         wrapper.write(Types.VAR_INT, 0);
      } else if (ingredient.length == 1) {
         writeItemDisplay(wrapper, ingredient[0]);
      } else {
         wrapper.write(Types.VAR_INT, 7);
         wrapper.write(Types.VAR_INT, ingredient.length);

         for (Item item : ingredient) {
            writeItemDisplay(wrapper, item);
         }
      }
   }

   private static void writeItemDisplay(PacketWrapper wrapper, Item item) {
      wrapper.write(Types.VAR_INT, 3);
      wrapper.write(VersionedTypes.V1_21_2.item, item.copy());
   }

   private static void writeCraftingStationDisplay(PacketWrapper wrapper) {
      wrapper.write(Types.VAR_INT, 0);
   }

   record FurnaceRecipe(int index, String identifier, int group, int category, Item[] ingredient, Item result, int duration, float experience)
      implements RecipeRewriter1_21_2.Recipe {
      @Override
      public int recipeDisplayId() {
         return 2;
      }

      @Override
      public void writeRecipeDisplay(PacketWrapper wrapper) {
         RecipeRewriter1_21_2.writeIngredientDisplay(wrapper, this.ingredient);
         wrapper.write(Types.VAR_INT, 1);
         RecipeRewriter1_21_2.writeItemDisplay(wrapper, this.result);
         RecipeRewriter1_21_2.writeCraftingStationDisplay(wrapper);
         wrapper.write(Types.VAR_INT, this.duration);
         wrapper.write(Types.FLOAT, this.experience);
      }
   }

   interface Recipe {
      int SLOT_DISPLAY_EMPTY = 0;
      int SLOT_DISPLAY_ANY_FUEL = 1;
      int SLOT_DISPLAY_ITEM = 3;
      int SLOT_DISPLAY_COMPOSITE = 7;

      int index();

      String identifier();

      int recipeDisplayId();

      @Nullable
      default Item[][] ingredients() {
         Item[] ingredient = this.ingredient();
         return ingredient == null ? null : new Item[][]{ingredient};
      }

      @Nullable
      default Item[] ingredient() {
         return null;
      }

      default int group() {
         return -1;
      }

      default boolean showNotification() {
         return true;
      }

      int category();

      void writeRecipeDisplay(PacketWrapper var1);
   }

   record ShapedRecipe(
      int index, String identifier, int group, int category, int width, int height, Item[][] ingredients, Item result, boolean showNotification
   ) implements RecipeRewriter1_21_2.Recipe {
      @Override
      public int recipeDisplayId() {
         return 1;
      }

      @Override
      public void writeRecipeDisplay(PacketWrapper wrapper) {
         wrapper.write(Types.VAR_INT, this.width);
         wrapper.write(Types.VAR_INT, this.height);
         RecipeRewriter1_21_2.writeIngredientsDisplay(wrapper, this.ingredients);
         RecipeRewriter1_21_2.writeItemDisplay(wrapper, this.result);
         RecipeRewriter1_21_2.writeCraftingStationDisplay(wrapper);
      }
   }

   record ShapelessRecipe(int index, String identifier, int group, int category, Item[][] ingredients, Item result) implements RecipeRewriter1_21_2.Recipe {
      @Override
      public int recipeDisplayId() {
         return 0;
      }

      @Override
      public void writeRecipeDisplay(PacketWrapper wrapper) {
         RecipeRewriter1_21_2.writeIngredientsDisplay(wrapper, this.ingredients);
         RecipeRewriter1_21_2.writeItemDisplay(wrapper, this.result);
         RecipeRewriter1_21_2.writeCraftingStationDisplay(wrapper);
      }
   }

   record StoneCutterRecipe(String identifier, int group, Item[] ingredient, Item result) {
      public int recipeDisplayId() {
         return 3;
      }

      public void writeRecipeDisplay(PacketWrapper wrapper) {
         RecipeRewriter1_21_2.writeIngredientDisplay(wrapper, this.ingredient);
         RecipeRewriter1_21_2.writeItemDisplay(wrapper, this.result);
         RecipeRewriter1_21_2.writeCraftingStationDisplay(wrapper);
      }
   }
}
