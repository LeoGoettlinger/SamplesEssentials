package org.lushplugins.lushrewards.libraries.lushlib.serializer;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.LushLogger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonLocation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;

public abstract class TextDeserializer<T> extends JsonDeserializer<T> {
   @Override
   public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      String text = jsonParser.getText();
      T deserialized = this.deserialize(text);
      if (deserialized == null) {
         JsonLocation location = jsonParser.currentLocation();
         LushLogger.getLogger()
            .severe("%s cannot deserialize %s at: %s:%s".formatted(this.getClass().getSimpleName(), text, location.getLineNr(), location.getColumnNr()));
      }

      return deserialized;
   }

   public abstract T deserialize(String var1);
}
