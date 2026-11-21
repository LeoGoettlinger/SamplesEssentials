package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype;

import java.util.Collection;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;

public interface TypeResolverBuilder<T extends TypeResolverBuilder<T>> {
   Class<?> getDefaultImpl();

   TypeSerializer buildTypeSerializer(SerializationConfig var1, JavaType var2, Collection<NamedType> var3);

   TypeDeserializer buildTypeDeserializer(DeserializationConfig var1, JavaType var2, Collection<NamedType> var3);

   T init(JsonTypeInfo.Id var1, TypeIdResolver var2);

   T inclusion(JsonTypeInfo.As var1);

   T typeProperty(String var1);

   T defaultImpl(Class<?> var1);

   T typeIdVisibility(boolean var1);

   default T withDefaultImpl(Class<?> defaultImpl) {
      return this.defaultImpl(defaultImpl);
   }
}
