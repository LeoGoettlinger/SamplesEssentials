package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public interface ResolvableSerializer {
   void resolve(SerializerProvider var1) throws JsonMappingException;
}
