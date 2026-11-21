package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;

public abstract class SerializerFactory {
   public abstract SerializerFactory withAdditionalSerializers(Serializers var1);

   public abstract SerializerFactory withAdditionalKeySerializers(Serializers var1);

   public abstract SerializerFactory withSerializerModifier(BeanSerializerModifier var1);

   public abstract JsonSerializer<Object> createSerializer(SerializerProvider var1, JavaType var2) throws JsonMappingException;

   public abstract TypeSerializer createTypeSerializer(SerializationConfig var1, JavaType var2) throws JsonMappingException;

   public JsonSerializer<Object> createKeySerializer(SerializerProvider prov, JavaType type, JsonSerializer<Object> defaultImpl) throws JsonMappingException {
      return this.createKeySerializer(prov.getConfig(), type, defaultImpl);
   }

   @Deprecated
   public abstract JsonSerializer<Object> createKeySerializer(SerializationConfig var1, JavaType var2, JsonSerializer<Object> var3) throws JsonMappingException;
}
