package ac.grim.grimac.utils.data.json;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface JsonSerializable {
   @NotNull
   JsonElement toJson();

   @NotNull
   static JsonArray serializeArray(@Nullable @NotNull JsonSerializable[] serializableArray) {
      JsonArray array = new JsonArray();

      for (JsonSerializable serializable : serializableArray) {
         array.add((JsonElement)(serializable == null ? JsonNull.INSTANCE : serializable.toJson()));
      }

      return array;
   }

   @NotNull
   static <T extends JsonSerializable> T[] deserializeArray(JsonArray jsonArray, IntFunction<T[]> newArray, Function<JsonElement, T> constructor) {
      T[] array = (T[])newArray.apply(jsonArray.size());

      for (int i = 0; i < jsonArray.size(); i++) {
         array[i] = (T)constructor.apply(jsonArray.get(i));
      }

      return array;
   }
}
