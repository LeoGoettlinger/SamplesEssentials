package ch.njol.yggdrasil;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map.Entry;
import javax.annotation.concurrent.NotThreadSafe;
import org.jetbrains.annotations.Nullable;

@NotThreadSafe
public class SimpleClassResolver implements ClassResolver {
   private final BiMap<Class<?>, String> classes = HashBiMap.create();

   public void registerClass(Class<?> type, String id) {
      String oldId = (String)this.classes.put(type, id);
      if (oldId != null && !oldId.equals(id)) {
         throw new YggdrasilException("Changed id of " + type + " from " + oldId + " to " + id);
      }
   }

   @Nullable
   @Override
   public Class<?> getClass(String id) {
      return (Class<?>)this.classes.inverse().get(id);
   }

   @Nullable
   @Override
   public String getID(Class<?> type) {
      if (this.classes.containsKey(type)) {
         return (String)this.classes.get(type);
      } else {
         Class<?> closestClass = null;
         String closestId = null;

         for (Entry<Class<?>, String> entry : this.classes.entrySet()) {
            Class<?> current = entry.getClass();
            if (current.isAssignableFrom(type) && (closestClass == null || closestClass.isAssignableFrom(current))) {
               closestClass = current;
               closestId = entry.getValue();
            }
         }

         return closestId;
      }
   }
}
