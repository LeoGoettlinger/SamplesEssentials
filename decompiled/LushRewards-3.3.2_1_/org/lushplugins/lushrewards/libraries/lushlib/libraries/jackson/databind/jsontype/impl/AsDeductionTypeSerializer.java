package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;

public class AsDeductionTypeSerializer extends TypeSerializerBase {
   private static final AsDeductionTypeSerializer INSTANCE = new AsDeductionTypeSerializer();

   protected AsDeductionTypeSerializer() {
      super(null, null);
   }

   public static AsDeductionTypeSerializer instance() {
      return INSTANCE;
   }

   public AsDeductionTypeSerializer forProperty(BeanProperty prop) {
      return this;
   }

   @Override
   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.EXISTING_PROPERTY;
   }

   @Override
   public WritableTypeId writeTypePrefix(JsonGenerator g, WritableTypeId idMetadata) throws IOException {
      if (idMetadata.valueShape.isStructStart()) {
         if (g.canWriteTypeId()) {
            idMetadata.wrapperWritten = false;
            if (idMetadata.valueShape == JsonToken.START_OBJECT) {
               g.writeStartObject(idMetadata.forValue);
            } else if (idMetadata.valueShape == JsonToken.START_ARRAY) {
               g.writeStartArray(idMetadata.forValue);
            }

            return idMetadata;
         } else {
            return g.writeTypePrefix(idMetadata);
         }
      } else {
         return null;
      }
   }

   @Override
   public WritableTypeId writeTypeSuffix(JsonGenerator g, WritableTypeId idMetadata) throws IOException {
      return idMetadata == null ? null : g.writeTypeSuffix(idMetadata);
   }
}
