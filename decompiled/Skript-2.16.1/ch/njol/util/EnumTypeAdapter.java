package ch.njol.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
   public static final TypeAdapterFactory factory = new TypeAdapterFactory() {
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
         Class<? super T> rawType = typeToken.getRawType();
         if (Enum.class.isAssignableFrom(rawType) && rawType != Enum.class) {
            if (!rawType.isEnum()) {
               rawType = rawType.getSuperclass();
            }

            return new EnumTypeAdapter<>((Class<T>)rawType);
         } else {
            return null;
         }
      }
   };
   private final Map<String, T> nameToConstant = new HashMap<>();
   private final Map<T, String> constantToName = new HashMap<>();

   public EnumTypeAdapter(Class<T> classOfT) {
      for (T constant : (Enum[])classOfT.getEnumConstants()) {
         String name = constant.name();

         try {
            SerializedName annotation = classOfT.getField(name).getAnnotation(SerializedName.class);
            if (annotation != null) {
               name = annotation.value();

               for (String alternate : annotation.alternate()) {
                  this.nameToConstant.put(alternate, constant);
               }
            }
         } catch (NoSuchFieldException var12) {
         }

         this.nameToConstant.put(name, constant);
         this.constantToName.put(constant, name);
      }
   }

   public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
         in.nextNull();
         return null;
      } else {
         return this.nameToConstant.get(in.nextString());
      }
   }

   public void write(JsonWriter out, T value) throws IOException {
      out.value(value == null ? null : this.constantToName.get(value));
   }
}
