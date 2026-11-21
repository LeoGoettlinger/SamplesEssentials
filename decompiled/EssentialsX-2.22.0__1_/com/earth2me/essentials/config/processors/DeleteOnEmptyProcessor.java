package com.earth2me.essentials.config.processors;

import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import java.util.Collection;
import java.util.Map;

public class DeleteOnEmptyProcessor implements Processor<Object> {
   @Override
   public void process(final Object value, final ConfigurationNode destination) {
      if (value != null) {
         try {
            if (value instanceof Map && ((Map)value).isEmpty()) {
               destination.set(null);
            } else if (value instanceof Collection && ((Collection)value).isEmpty()) {
               destination.set(null);
            } else if (value instanceof CharSequence && ((CharSequence)value).length() == 0) {
               destination.set(null);
            }
         } catch (SerializationException var4) {
            var4.printStackTrace();
         }
      }
   }
}
