package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public interface JsonFormatVisitorWithSerializerProvider {
   SerializerProvider getProvider();

   void setProvider(SerializerProvider var1);
}
