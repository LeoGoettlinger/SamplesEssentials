package com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage;

import com.google.common.base.Preconditions;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.Protocol1_21_2To1_21;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RecipeStorage {
   public static final int RECIPE_BOOK_SETTINGS = 8;
   private static final String[] EMPTY_STRINGS = new String[0];
   private final List<RecipeStorage.Recipe> recipes = new ArrayList<>();
   private final List<RecipeStorage.Recipe> tempRecipes = new ArrayList<>();
   private final List<RecipeStorage.StoneCutterRecipe> stoneCutterRecipes = new ArrayList<>();
   private boolean[] recipeBookSettings = new boolean[8];
   private final Protocol1_21_2To1_21 protocol;

   public RecipeStorage(Protocol1_21_2To1_21 protocol) {
      this.protocol = protocol;
   }

   public void sendRecipes(UserConnection connection) {
      if (!this.tempRecipes.isEmpty()) {
         this.recipes.addAll(this.tempRecipes);
         this.tempRecipes.clear();
      }

      int highestIndex = -1;

      for (RecipeStorage.Recipe recipe : this.recipes) {
         highestIndex = Math.max(highestIndex, recipe.index);
      }

      List<RecipeStorage.Recipe> recipes = new ArrayList<>(this.recipes);

      for (RecipeStorage.StoneCutterRecipe recipe : this.stoneCutterRecipes) {
         recipe.index = ++highestIndex;
         recipes.add(recipe);
      }

      recipes.sort(Comparator.comparingInt(a -> a.index));
      PacketWrapper updateRecipesPacket = PacketWrapper.create(ClientboundPackets1_21.UPDATE_RECIPES, connection);
      updateRecipesPacket.write(Types.VAR_INT, recipes.size());

      for (RecipeStorage.Recipe recipe : recipes) {
         updateRecipesPacket.write(Types.STRING, identifier(recipe.index));
         recipe.write(updateRecipesPacket);
      }

      updateRecipesPacket.send(Protocol1_21_2To1_21.class);
      this.sendUnlockedRecipes(connection, recipes);
   }

   private static String identifier(int recipeIndex) {
      return String.format("%06d", recipeIndex);
   }

   public void lockRecipes(PacketWrapper wrapper, int[] ids) {
      for (int id : ids) {
         this.recipes.removeIf(recipe -> recipe.index == id);
      }

      wrapper.write(Types.VAR_INT, 2);

      for (boolean recipeBookSetting : this.recipeBookSettings) {
         wrapper.write(Types.BOOLEAN, recipeBookSetting);
      }

      String[] recipeKeys = new String[ids.length];

      for (int i = 0; i < ids.length; i++) {
         recipeKeys[i] = identifier(ids[i]);
      }

      wrapper.write(Types.STRING_ARRAY, recipeKeys);
   }

   private void sendUnlockedRecipes(UserConnection connection, List<RecipeStorage.Recipe> recipes) {
      PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_21.RECIPE, connection);
      wrapper.write(Types.VAR_INT, 0);

      for (boolean recipeBookSetting : this.recipeBookSettings) {
         wrapper.write(Types.BOOLEAN, recipeBookSetting);
      }

      String[] recipeKeys = new String[recipes.size()];
      List<String> highlightRecipes = new ArrayList<>();

      for (int i = 0; i < recipes.size(); i++) {
         recipeKeys[i] = identifier(recipes.get(i).index);
         if (recipes.get(i).highlight) {
            highlightRecipes.add(recipeKeys[i]);
         }
      }

      wrapper.write(Types.STRING_ARRAY, recipeKeys);
      wrapper.write(Types.STRING_ARRAY, highlightRecipes.toArray(EMPTY_STRINGS));
      wrapper.send(Protocol1_21_2To1_21.class);
   }

   public void readRecipe(PacketWrapper wrapper) {
      int id = (Integer)wrapper.read(Types.VAR_INT);
      int type = (Integer)wrapper.passthrough(Types.VAR_INT);

      RecipeStorage.Recipe recipe = switch (type) {
         case 0 -> this.readShapeless(wrapper);
         case 1 -> this.readShaped(wrapper);
         case 2 -> this.readFurnace(wrapper);
         case 3 -> this.readStoneCutter(wrapper);
         case 4 -> this.readSmithing(wrapper);
         default -> null;
      };
      Integer group = (Integer)wrapper.read(Types.OPTIONAL_VAR_INT);
      int category = (Integer)wrapper.read(Types.VAR_INT);
      if ((Boolean)wrapper.read(Types.BOOLEAN)) {
         int ingredientsSize = (Integer)wrapper.read(Types.VAR_INT);

         for (int j = 0; j < ingredientsSize; j++) {
            wrapper.read(Types.HOLDER_SET);
         }
      }

      byte flags = (Byte)wrapper.read(Types.BYTE);
      if (recipe != null) {
         recipe.index = id;
         recipe.group = group;
         recipe.category = category;
         recipe.highlight = (flags & 2) != 0;
      }
   }

   private RecipeStorage.Recipe readShapeless(PacketWrapper wrapper) {
      Item[][] ingredients = this.readSlotDisplayList(wrapper);
      Item result = this.readSingleSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      return this.add(new RecipeStorage.ShapelessRecipe(ingredients, result));
   }

   private RecipeStorage.Recipe readShaped(PacketWrapper wrapper) {
      int width = (Integer)wrapper.passthrough(Types.VAR_INT);
      int height = (Integer)wrapper.passthrough(Types.VAR_INT);
      Item[][] ingredients = this.readSlotDisplayList(wrapper);
      Item result = this.readSingleSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      return this.add(new RecipeStorage.ShapedRecipe(width, height, ingredients, result));
   }

   private RecipeStorage.Recipe readFurnace(PacketWrapper wrapper) {
      Item[] ingredient = this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      Item result = this.readSingleSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      int duration = (Integer)wrapper.read(Types.VAR_INT);
      float experience = (Float)wrapper.read(Types.FLOAT);
      return this.add(new RecipeStorage.FurnaceRecipe(ingredient, result, duration, experience));
   }

   private RecipeStorage.Recipe readStoneCutter(PacketWrapper wrapper) {
      this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      return null;
   }

   private RecipeStorage.Recipe readSmithing(PacketWrapper wrapper) {
      this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      this.readSlotDisplay(wrapper);
      return null;
   }

   private RecipeStorage.Recipe add(RecipeStorage.Recipe recipe) {
      this.tempRecipes.add(recipe);
      return recipe;
   }

   private Item[][] readSlotDisplayList(PacketWrapper wrapper) {
      int size = (Integer)wrapper.passthrough(Types.VAR_INT);
      Item[][] ingredients = new Item[size][];

      for (int i = 0; i < size; i++) {
         ingredients[i] = this.readSlotDisplay(wrapper);
      }

      return ingredients;
   }

   private Item readSingleSlotDisplay(PacketWrapper wrapper) {
      Item[] items = this.readSlotDisplay(wrapper);
      return (Item)(items.length == 0 ? new StructuredItem(1, 1) : items[0]);
   }

   private Item[] readSlotDisplay(PacketWrapper wrapper) {
      int type = (Integer)wrapper.read(Types.VAR_INT);

      return switch (type) {
         case 2 -> {
            int id = (Integer)wrapper.read(Types.VAR_INT);
            if (id == 0) {
               this.protocol.getLogger().warning("Empty item id in recipe");
               yield new Item[0];
            } else {
               yield new Item[]{new StructuredItem(this.rewriteItemId(id), 1)};
            }
         }
         case 3 -> {
            Item item = this.protocol.getItemRewriter().handleItemToClient(wrapper.user(), (Item)wrapper.read(VersionedTypes.V1_21_2.item()));
            if (item.isEmpty()) {
               this.protocol.getLogger().warning("Empty item in recipe");
               yield new Item[0];
            } else {
               yield new Item[]{item};
            }
         }
         case 4 -> {
            wrapper.read(Types.STRING);
            yield new Item[0];
         }
         case 5 -> {
            this.readSlotDisplay(wrapper);
            this.readSlotDisplay(wrapper);
            this.readSlotDisplay(wrapper);
            yield new Item[0];
         }
         case 6 -> {
            this.readSlotDisplay(wrapper);
            this.readSlotDisplay(wrapper);
            yield new Item[0];
         }
         case 7 -> this.readSlotDisplayList(wrapper)[0];
         default -> new Item[0];
      };
   }

   private int rewriteItemId(int id) {
      return this.protocol.getMappingData().getNewItemId(id);
   }

   public void readStoneCutterRecipes(PacketWrapper wrapper) {
      this.stoneCutterRecipes.clear();
      int stonecutterRecipesSize = (Integer)wrapper.read(Types.VAR_INT);

      for (int i = 0; i < stonecutterRecipesSize; i++) {
         Item[] ingredient = this.readHolderSet(wrapper);
         Item result = this.readSingleSlotDisplay(wrapper);
         this.stoneCutterRecipes.add(new RecipeStorage.StoneCutterRecipe(ingredient, result));
      }
   }

   private Item[] readHolderSet(PacketWrapper wrapper) {
      HolderSet holderSet = (HolderSet)wrapper.read(Types.HOLDER_SET);
      if (holderSet.hasTagKey()) {
         return new Item[]{new StructuredItem(1, 1)};
      } else {
         int[] ids = holderSet.ids();

         for (int i = 0; i < ids.length; i++) {
            ids[i] = this.rewriteItemId(ids[i]);
         }

         Item[] ingredient = new Item[ids.length];

         for (int i = 0; i < ingredient.length; i++) {
            ingredient[i] = new StructuredItem(ids[i], 1);
         }

         return ingredient;
      }
   }

   public void setRecipeBookSettings(boolean[] recipeBookSettings) {
      this.recipeBookSettings = recipeBookSettings;
   }

   public void clearRecipes() {
      this.recipes.clear();
   }

   private static final class FurnaceRecipe extends RecipeStorage.Recipe {
      private final Item[] ingredient;
      private final Item result;
      private final float experience;
      private final int cookingTime;

      private FurnaceRecipe(Item[] ingredient, Item result, int cookingTime, float experience) {
         this.ingredient = ingredient;
         this.result = result;
         this.experience = experience;
         this.cookingTime = cookingTime;
      }

      @Override
      public void write(PacketWrapper wrapper) {
         wrapper.write(Types.VAR_INT, this.serializerId());
         this.writeGroup(wrapper);
         this.writeCategory(wrapper);
         this.writeIngredient(wrapper, this.ingredient);
         this.writeResult(wrapper, this.result);
         wrapper.write(Types.FLOAT, this.experience);
         wrapper.write(Types.VAR_INT, this.cookingTime);
      }

      private int serializerId() {
         return switch (this.category()) {
            case 4, 5, 6 -> 15;
            case 7, 8 -> 16;
            case 9 -> 17;
            default -> 15;
            case 12 -> 18;
         };
      }
   }

   abstract static class Recipe {
      private static final int FOOD_CRAFTING_BOOK_CATEGORY = 0;
      private static final int BLOCKS_CRAFTING_BOOK_CATEGORY = 1;
      private static final int MISC_CRAFTING_BOOK_CATEGORY = 2;
      protected int index;
      private Integer group;
      private int category;
      private boolean highlight;

      abstract void write(PacketWrapper var1);

      void writeGroup(PacketWrapper wrapper) {
         wrapper.write(Types.STRING, this.group != null ? Integer.toString(this.group) : "");
      }

      void writeIngredients(PacketWrapper wrapper, Item[][] ingredients) {
         wrapper.write(Types.VAR_INT, ingredients.length);

         for (Item[] ingredient : ingredients) {
            this.writeIngredient(wrapper, ingredient);
         }
      }

      void writeIngredient(PacketWrapper wrapper, Item[] ingredient) {
         Item[] copy = new Item[ingredient.length];

         for (int i = 0; i < ingredient.length; i++) {
            copy[i] = ingredient[i].copy();
         }

         wrapper.write(VersionedTypes.V1_21_2.itemArray(), copy);
      }

      void writeResult(PacketWrapper wrapper, Item result) {
         wrapper.write(VersionedTypes.V1_21_2.item(), result.copy());
      }

      void writeCategory(PacketWrapper wrapper) {
         int craftingBookCategory = switch (this.category) {
            case 0, 5, 7, 10 -> 1;
            case 1, 2, 3, 6, 8, 11 -> 2;
            case 4, 9, 12 -> 0;
            default -> 2;
         };
         wrapper.write(Types.VAR_INT, craftingBookCategory);
      }

      int category() {
         return this.category;
      }
   }

   private static final class ShapedRecipe extends RecipeStorage.Recipe {
      private static final int SERIALIZER_ID = 0;
      private final int width;
      private final int height;
      private final Item[][] ingredients;
      private final Item result;

      private ShapedRecipe(int width, int height, Item[][] ingredients, Item result) {
         this.width = width;
         this.height = height;
         this.ingredients = ingredients;
         this.result = result;
      }

      @Override
      public void write(PacketWrapper wrapper) {
         wrapper.write(Types.VAR_INT, 0);
         this.writeGroup(wrapper);
         this.writeCategory(wrapper);
         wrapper.write(Types.VAR_INT, this.width);
         wrapper.write(Types.VAR_INT, this.height);
         Preconditions.checkArgument(this.width * this.height == this.ingredients.length, "Invalid shaped recipe");

         for (Item[] ingredient : this.ingredients) {
            this.writeIngredient(wrapper, ingredient);
         }

         this.writeResult(wrapper, this.result);
         wrapper.write(Types.BOOLEAN, false);
      }
   }

   private static final class ShapelessRecipe extends RecipeStorage.Recipe {
      private static final int SERIALIZER_ID = 1;
      private final Item[][] ingredients;
      private final Item result;

      private ShapelessRecipe(Item[][] ingredients, Item result) {
         this.ingredients = ingredients;
         this.result = result;
      }

      @Override
      public void write(PacketWrapper wrapper) {
         wrapper.write(Types.VAR_INT, 1);
         this.writeGroup(wrapper);
         this.writeCategory(wrapper);
         this.writeIngredients(wrapper, this.ingredients);
         this.writeResult(wrapper, this.result);
      }
   }

   private static final class StoneCutterRecipe extends RecipeStorage.Recipe {
      private static final int SERIALIZER_ID = 19;
      private final Item[] ingredient;
      private final Item result;

      private StoneCutterRecipe(Item[] ingredient, Item result) {
         this.ingredient = ingredient;
         this.result = result;
      }

      @Override
      public void write(PacketWrapper wrapper) {
         wrapper.write(Types.VAR_INT, 19);
         this.writeGroup(wrapper);
         this.writeIngredient(wrapper, this.ingredient);
         this.writeResult(wrapper, this.result);
      }
   }
}
