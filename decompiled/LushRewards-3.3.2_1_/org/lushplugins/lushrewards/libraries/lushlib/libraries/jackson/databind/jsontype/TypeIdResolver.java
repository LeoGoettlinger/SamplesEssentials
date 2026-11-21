package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DatabindContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;

public interface TypeIdResolver {
   void init(JavaType var1);

   String idFromValue(Object var1);

   String idFromValueAndType(Object var1, Class<?> var2);

   String idFromBaseType();

   JavaType typeFromId(DatabindContext var1, String var2) throws IOException;

   String getDescForKnownTypeIds();

   JsonTypeInfo.Id getMechanism();
}
