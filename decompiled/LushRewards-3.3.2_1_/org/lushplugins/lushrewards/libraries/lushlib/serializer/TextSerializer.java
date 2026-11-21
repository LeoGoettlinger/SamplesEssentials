package org.lushplugins.lushrewards.libraries.lushlib.serializer;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.LushLogger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public abstract class TextSerializer<T> extends JsonSerializer<T> {
   @Override
   public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      String serialized = this.serialize(t);
      if (serialized == null) {
         LushLogger.getLogger().severe("%s cannot serialize %s".formatted(this.getClass().getSimpleName(), t));
      }

      jsonGenerator.writeString(serialized);
   }

   public abstract String serialize(T var1);
}
