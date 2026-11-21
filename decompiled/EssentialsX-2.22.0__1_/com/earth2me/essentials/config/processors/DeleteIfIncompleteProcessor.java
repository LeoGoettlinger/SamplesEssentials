package com.earth2me.essentials.config.processors;

import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DeleteIfIncompleteProcessor implements Processor<Object> {
   @Override
   public void process(Object value, ConfigurationNode destination) {
      if (value != null) {
         try {
            if (value instanceof DeleteIfIncompleteProcessor.IncompleteEntity && ((DeleteIfIncompleteProcessor.IncompleteEntity)value).isIncomplete()) {
               destination.set(null);
            } else if (value instanceof List) {
               boolean modified = false;
               List<?> newList = new ArrayList((List)value);

               for (Object o : (List)value) {
                  if (o instanceof DeleteIfIncompleteProcessor.IncompleteEntity && ((DeleteIfIncompleteProcessor.IncompleteEntity)o).isIncomplete()) {
                     newList.remove(o);
                     modified = true;
                  }
               }

               if (modified) {
                  destination.set(newList);
               }
            } else if (value instanceof Map) {
               boolean modified = false;
               Map<?, ?> newMap = new HashMap((Map)value);

               for (Entry<?, ?> entry : newMap.entrySet()) {
                  if (entry.getValue() instanceof DeleteIfIncompleteProcessor.IncompleteEntity
                     && ((DeleteIfIncompleteProcessor.IncompleteEntity)entry.getValue()).isIncomplete()) {
                     newMap.remove(entry.getKey());
                     modified = true;
                  }
               }

               if (modified) {
                  destination.set(newMap);
               }
            }
         } catch (SerializationException var7) {
            var7.printStackTrace();
         }
      }
   }

   public interface IncompleteEntity {
      boolean isIncomplete();
   }
}
