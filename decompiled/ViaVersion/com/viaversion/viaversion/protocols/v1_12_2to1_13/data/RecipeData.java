package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class RecipeData {
   public static Map<String, RecipeData.Recipe> recipes;

   public static void init() {
      InputStream stream = MappingData1_13.class.getClassLoader().getResourceAsStream("assets/viaversion/data/itemrecipes1_12_2to1_13.json");

      JsonObject object;
      try (InputStreamReader reader = new InputStreamReader(stream)) {
         object = GsonUtil.getGson().fromJson(reader, JsonObject.class);
      } catch (IOException var16) {
         throw new RuntimeException(var16);
      }

      recipes = new LinkedHashMap<>();

      for (Entry<String, JsonElement> entry : object.getAsJsonObject("shapeless").entrySet()) {
         JsonObject recipe = entry.getValue().getAsJsonObject();
         DataItem[][] ingredients = ingredients(recipe.getAsJsonArray("ingredients"));
         recipes.put(
            "viaversion:legacy/" + entry.getKey(), new RecipeData.Recipe("crafting_shapeless", group(recipe), 0, 0, 0.0F, 0, null, ingredients, result(recipe))
         );
      }

      for (Entry<String, JsonElement> entry : object.getAsJsonObject("shaped").entrySet()) {
         JsonObject recipe = entry.getValue().getAsJsonObject();
         JsonArray grid = recipe.getAsJsonArray("grid");
         int height = grid.size();
         int width = grid.get(0).getAsJsonArray().size();
         DataItem[][] ingredients = new DataItem[width * height][];
         int slot = 0;

         for (JsonElement row : grid) {
            for (JsonElement element : row.getAsJsonArray()) {
               ingredients[slot++] = ingredient(element);
            }
         }

         recipes.put(
            "viaversion:legacy/" + entry.getKey(),
            new RecipeData.Recipe("crafting_shaped", group(recipe), width, height, 0.0F, 0, null, ingredients, result(recipe))
         );
      }

      for (Entry<String, JsonElement> entry : object.getAsJsonObject("smelting").entrySet()) {
         JsonObject recipe = entry.getValue().getAsJsonObject();
         float experience = recipe.get("experience").getAsFloat();
         recipes.put(
            entry.getKey(), new RecipeData.Recipe("smelting", group(recipe), 0, 0, experience, 200, ingredient(recipe.get("ingredient")), null, result(recipe))
         );
      }

      for (Entry<String, JsonElement> entry : object.getAsJsonObject("special").entrySet()) {
         recipes.put(entry.getKey(), new RecipeData.Recipe("crafting_special_" + entry.getValue().getAsString(), "", 0, 0, 0.0F, 0, null, null, null));
      }
   }

   private static String group(JsonObject recipe) {
      JsonElement group = recipe.get("group");
      return group != null ? group.getAsString() : "";
   }

   private static DataItem result(JsonObject recipe) {
      JsonElement count = recipe.get("count");
      return new DataItem(recipe.get("result").getAsInt(), count != null ? count.getAsByte() : 1, null);
   }

   private static DataItem[] ingredient(JsonElement element) {
      if (element.isJsonNull()) {
         return new DataItem[0];
      } else if (!element.isJsonArray()) {
         return new DataItem[]{new DataItem(element.getAsInt(), (byte)1, null)};
      } else {
         JsonArray choices = element.getAsJsonArray();
         DataItem[] items = new DataItem[choices.size()];

         for (int i = 0; i < items.length; i++) {
            items[i] = new DataItem(choices.get(i).getAsInt(), (byte)1, null);
         }

         return items;
      }
   }

   private static DataItem[][] ingredients(JsonArray slots) {
      DataItem[][] items = new DataItem[slots.size()][];

      for (int i = 0; i < items.length; i++) {
         items[i] = ingredient(slots.get(i));
      }

      return items;
   }

   public record Recipe(
      String type, String group, int width, int height, float experience, int cookingTime, DataItem[] ingredient, DataItem[][] ingredients, DataItem result
   ) {
   }
}
