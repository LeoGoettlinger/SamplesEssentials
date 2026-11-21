package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl;

import java.io.IOException;
import java.util.Collection;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std.StaticListSerializerBase;

@JacksonStdImpl
public class StringCollectionSerializer extends StaticListSerializerBase<Collection<String>> {
   public static final StringCollectionSerializer instance = new StringCollectionSerializer();

   protected StringCollectionSerializer() {
      super(Collection.class);
   }

   protected StringCollectionSerializer(StringCollectionSerializer src, Boolean unwrapSingle) {
      super(src, unwrapSingle);
   }

   @Override
   public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
      return new StringCollectionSerializer(this, unwrapSingle);
   }

   @Override
   protected JsonNode contentSchema() {
      return this.createSchemaNode("string", true);
   }

   @Override
   protected void acceptContentVisitor(JsonArrayFormatVisitor visitor) throws JsonMappingException {
      visitor.itemsFormat(JsonFormatTypes.STRING);
   }

   public void serialize(Collection<String> value, JsonGenerator g, SerializerProvider provider) throws IOException {
      int len = value.size();
      if (len != 1
         || (this._unwrapSingle != null || !provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)) && this._unwrapSingle != Boolean.TRUE) {
         g.writeStartArray(value, len);
         this.serializeContents(value, g, provider);
         g.writeEndArray();
      } else {
         this.serializeContents(value, g, provider);
      }
   }

   @Override
   public void serializeWithType(Collection<String> value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.START_ARRAY));
      g.setCurrentValue(value);
      this.serializeContents(value, g, provider);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   private final void serializeContents(Collection<String> value, JsonGenerator g, SerializerProvider provider) throws IOException {
      int i = 0;

      try {
         for (String str : value) {
            if (str == null) {
               provider.defaultSerializeNull(g);
            } else {
               g.writeString(str);
            }

            i++;
         }
      } catch (Exception var7) {
         this.wrapAndThrow(provider, var7, value, i);
      }
   }
}
