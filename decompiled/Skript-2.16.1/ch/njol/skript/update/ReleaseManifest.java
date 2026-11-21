package ch.njol.skript.update;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import org.jetbrains.annotations.Nullable;

public class ReleaseManifest {
   public final String id;
   public final String date;
   public final String flavor;
   public final Class<? extends UpdateChecker> updateCheckerType;
   public final String updateSource;
   @Nullable
   public final String downloadSource;

   public static ReleaseManifest load(String json) throws JsonParseException {
      return (ReleaseManifest)new GsonBuilder()
         .registerTypeAdapter(Class.class, new ReleaseManifest.ClassSerializer())
         .create()
         .fromJson(json, ReleaseManifest.class);
   }

   public ReleaseManifest(
      String id, String date, String flavor, Class<? extends UpdateChecker> updateCheckerType, String updateSource, @Nullable String downloadSource
   ) {
      this.id = id;
      this.date = date;
      this.flavor = flavor;
      this.updateCheckerType = updateCheckerType;
      this.updateSource = updateSource;
      this.downloadSource = downloadSource;
   }

   public UpdateChecker createUpdateChecker() {
      try {
         return this.updateCheckerType.getConstructor().newInstance();
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var2) {
         throw new IllegalStateException("updater class cannot be created", var2);
      }
   }

   static class ClassSerializer implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
      @Nullable
      public Class<?> deserialize(@Nullable JsonElement json, @Nullable Type typeOfT, @Nullable JsonDeserializationContext context) throws JsonParseException {
         try {
            assert json != null;

            return Class.forName(json.getAsJsonPrimitive().getAsString());
         } catch (ClassNotFoundException var5) {
            throw new JsonParseException("class not found");
         }
      }

      public JsonElement serialize(@Nullable Class<?> src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context) {
         assert src != null;

         return new JsonPrimitive(src.getName());
      }
   }
}
