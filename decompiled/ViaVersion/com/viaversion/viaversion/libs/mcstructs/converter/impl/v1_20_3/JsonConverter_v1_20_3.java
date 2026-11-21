package com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonNull;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

public class JsonConverter_v1_20_3 implements DataConverter<JsonElement> {
   public static final JsonConverter_v1_20_3 INSTANCE = new JsonConverter_v1_20_3();
   private Consumer<String> stringConsumer;

   @Nullable
   @Override
   public Consumer<String> currentConsumer() {
      return this.stringConsumer;
   }

   @Override
   public void setCurrentConsumer(Consumer<String> consumer) {
      this.stringConsumer = consumer;
   }

   @Override
   public DataConverter<JsonElement> forkIfDefault() {
      return this == INSTANCE ? new JsonConverter_v1_20_3() : this;
   }

   public <N> N convertTo(DataConverter<N> to, @Nullable JsonElement element) {
      if (to == this) {
         return (N)element;
      } else if (element == null || element instanceof JsonNull) {
         return to.empty();
      } else if (element instanceof JsonObject) {
         return this.convertMap(to, element);
      } else if (element instanceof JsonArray) {
         return this.convertList(to, element);
      } else {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (primitive.isString()) {
            return to.createString(primitive.getAsString());
         } else if (primitive.isBoolean()) {
            return to.createBoolean(primitive.getAsBoolean());
         } else {
            BigDecimal number = primitive.getAsBigDecimal();

            try {
               long l = number.longValueExact();
               if ((byte)l == l) {
                  return to.createByte((byte)l);
               } else if ((short)l == l) {
                  return to.createShort((short)l);
               } else {
                  return (int)l == l ? to.createInt((int)l) : to.createLong(l);
               }
            } catch (ArithmeticException var8) {
               double d = number.doubleValue();
               return (float)d == d ? to.createFloat((float)d) : to.createDouble(d);
            }
         }
      }
   }

   public JsonElement empty() {
      return JsonNull.INSTANCE;
   }

   public JsonElement createBoolean(boolean value) {
      return new JsonPrimitive(value);
   }

   public Result<Boolean> asBoolean(JsonElement element) {
      if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (primitive.isBoolean()) {
            return Result.success(primitive.getAsBoolean());
         }

         if (primitive.isNumber()) {
            return Result.success(primitive.getAsNumber().byteValue() != 0);
         }
      }

      return Result.unexpected(element, "boolean");
   }

   public JsonElement createNumber(Number number) {
      return new JsonPrimitive(number);
   }

   public Result<Number> asNumber(JsonElement element) {
      if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (primitive.isBoolean()) {
            return Result.success(primitive.getAsBoolean() ? 1 : 0);
         }

         if (primitive.isNumber()) {
            return Result.success(primitive.getAsNumber());
         }
      }

      return Result.unexpected(element, "number");
   }

   public JsonElement createString(String value) {
      return new JsonPrimitive(value);
   }

   public Result<String> asString(JsonElement element) {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
         ? Result.success(element.getAsJsonPrimitive().getAsString())
         : Result.unexpected(element, "string");
   }

   public Result<JsonElement> mergeList(@Nullable JsonElement list, List<JsonElement> values) {
      if (list == null) {
         list = new JsonArray();
      }

      if (!list.isJsonArray()) {
         return Result.unexpected(list, JsonArray.class);
      } else {
         JsonArray jsonArray = list.getAsJsonArray();

         for (JsonElement value : values) {
            jsonArray.add(value);
         }

         return Result.success(jsonArray);
      }
   }

   public Result<List<JsonElement>> asList(JsonElement element) {
      return !element.isJsonArray() ? Result.unexpected(element, JsonArray.class) : Result.success(element.getAsJsonArray().asList());
   }

   public JsonElement createUnsafeMap(Map<JsonElement, JsonElement> values) {
      JsonObject object = new JsonObject();
      values.forEach((key, value) -> object.add(key.getAsString(), value));
      return object;
   }

   public Result<JsonElement> mergeMap(@Nullable JsonElement map, Map<JsonElement, JsonElement> values) {
      if (map == null) {
         map = new JsonObject();
      }

      if (!map.isJsonObject()) {
         return Result.unexpected(map, JsonObject.class);
      } else {
         JsonObject jsonObject = map.getAsJsonObject();

         for (Entry<JsonElement, JsonElement> entry : values.entrySet()) {
            if (!entry.getKey().isJsonPrimitive() || !entry.getKey().getAsJsonPrimitive().isString()) {
               return Result.unexpected(entry.getKey(), "string");
            }

            jsonObject.add(entry.getKey().getAsString(), entry.getValue());
         }

         return Result.success(jsonObject);
      }
   }

   public Result<Map<JsonElement, JsonElement>> asMap(JsonElement element) {
      if (!element.isJsonObject()) {
         return Result.unexpected(element, JsonObject.class);
      } else {
         Map<JsonElement, JsonElement> map = new HashMap<>();
         element.getAsJsonObject().entrySet().forEach(entry -> {
            JsonElement var10000 = map.put(this.createString(entry.getKey()), entry.getValue());
         });
         return Result.success(map);
      }
   }

   public Result<Map<String, JsonElement>> asStringTypeMap(JsonElement element) {
      if (!element.isJsonObject()) {
         return Result.unexpected(element, JsonObject.class);
      } else {
         Map<String, JsonElement> map = new HashMap<>();
         element.getAsJsonObject().entrySet().forEach(entry -> {
            JsonElement var10000 = map.put(entry.getKey(), entry.getValue());
         });
         return Result.success(map);
      }
   }

   public JsonElement createByteArray(byte[] value) {
      JsonArray jsonArray = new JsonArray();

      for (byte b : value) {
         jsonArray.add(b);
      }

      return jsonArray;
   }

   public Result<byte[]> asByteArray(JsonElement element) {
      if (!element.isJsonArray()) {
         return Result.unexpected(element, JsonArray.class);
      } else {
         JsonArray jsonArray = element.getAsJsonArray();
         if (!StreamSupport.stream(jsonArray.spliterator(), false).anyMatch(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())) {
            return Result.unexpected(element, byte[].class);
         } else {
            byte[] bytes = new byte[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
               bytes[i] = jsonArray.get(i).getAsByte();
            }

            return Result.success(bytes);
         }
      }
   }

   public JsonElement createIntArray(int[] value) {
      JsonArray jsonArray = new JsonArray();

      for (int i : value) {
         jsonArray.add(i);
      }

      return jsonArray;
   }

   public Result<int[]> asIntArray(JsonElement element) {
      if (!element.isJsonArray()) {
         return Result.unexpected(element, JsonArray.class);
      } else {
         JsonArray jsonArray = element.getAsJsonArray();
         if (!StreamSupport.stream(jsonArray.spliterator(), false).anyMatch(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())) {
            return Result.unexpected(element, int[].class);
         } else {
            int[] ints = new int[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
               ints[i] = jsonArray.get(i).getAsInt();
            }

            return Result.success(ints);
         }
      }
   }

   public JsonElement createLongArray(long[] value) {
      JsonArray jsonArray = new JsonArray();

      for (long l : value) {
         jsonArray.add(l);
      }

      return jsonArray;
   }

   public Result<long[]> asLongArray(JsonElement element) {
      if (!element.isJsonArray()) {
         return Result.unexpected(element, JsonArray.class);
      } else {
         JsonArray jsonArray = element.getAsJsonArray();
         if (!StreamSupport.stream(jsonArray.spliterator(), false).anyMatch(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())) {
            return Result.unexpected(element, long[].class);
         } else {
            long[] longs = new long[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
               longs[i] = jsonArray.get(i).getAsLong();
            }

            return Result.success(longs);
         }
      }
   }
}
