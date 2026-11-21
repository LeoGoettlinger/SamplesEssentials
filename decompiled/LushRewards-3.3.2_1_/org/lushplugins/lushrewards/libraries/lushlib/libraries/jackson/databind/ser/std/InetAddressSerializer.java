package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.net.InetAddress;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.ContextualSerializer;

public class InetAddressSerializer extends StdScalarSerializer<InetAddress> implements ContextualSerializer {
   protected final boolean _asNumeric;

   public InetAddressSerializer() {
      this(false);
   }

   public InetAddressSerializer(boolean asNumeric) {
      super(InetAddress.class);
      this._asNumeric = asNumeric;
   }

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(serializers, property, this.handledType());
      boolean asNumeric = false;
      if (format != null) {
         JsonFormat.Shape shape = format.getShape();
         if (shape.isNumeric() || shape == JsonFormat.Shape.ARRAY) {
            asNumeric = true;
         }
      }

      return asNumeric != this._asNumeric ? new InetAddressSerializer(asNumeric) : this;
   }

   public void serialize(InetAddress value, JsonGenerator g, SerializerProvider provider) throws IOException {
      String str;
      if (this._asNumeric) {
         str = value.getHostAddress();
      } else {
         str = value.toString().trim();
         int ix = str.indexOf(47);
         if (ix >= 0) {
            if (ix == 0) {
               str = str.substring(1);
            } else {
               str = str.substring(0, ix);
            }
         }
      }

      g.writeString(str);
   }

   public void serializeWithType(InetAddress value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, InetAddress.class, JsonToken.VALUE_STRING));
      this.serialize(value, g, provider);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }
}
