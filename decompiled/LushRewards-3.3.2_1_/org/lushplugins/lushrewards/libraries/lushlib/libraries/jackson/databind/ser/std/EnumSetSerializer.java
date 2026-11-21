package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.util.EnumSet;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

public class EnumSetSerializer extends AsArraySerializerBase<EnumSet<? extends Enum<?>>> {
   public EnumSetSerializer(JavaType elemType) {
      super(EnumSet.class, elemType, true, null, null);
   }

   public EnumSetSerializer(EnumSetSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
      super(src, property, vts, valueSerializer, unwrapSingle);
   }

   public EnumSetSerializer _withValueTypeSerializer(TypeSerializer vts) {
      return this;
   }

   public EnumSetSerializer withResolved(BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
      return new EnumSetSerializer(this, property, vts, elementSerializer, unwrapSingle);
   }

   public boolean isEmpty(SerializerProvider prov, EnumSet<? extends Enum<?>> value) {
      return value.isEmpty();
   }

   public boolean hasSingleElement(EnumSet<? extends Enum<?>> value) {
      return value.size() == 1;
   }

   public final void serialize(EnumSet<? extends Enum<?>> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      int len = value.size();
      if (len != 1
         || (this._unwrapSingle != null || !provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)) && this._unwrapSingle != Boolean.TRUE) {
         gen.writeStartArray(value, len);
         this.serializeContents(value, gen, provider);
         gen.writeEndArray();
      } else {
         this.serializeContents(value, gen, provider);
      }
   }

   public void serializeContents(EnumSet<? extends Enum<?>> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      JsonSerializer<Object> enumSer = this._elementSerializer;

      for (Enum<?> en : value) {
         if (enumSer == null) {
            enumSer = provider.findContentValueSerializer(en.getDeclaringClass(), this._property);
         }

         enumSer.serialize(en, gen, provider);
      }
   }
}
