package org.lushplugins.lushrewards.libraries.lushlib.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.LushLogger;

public class RegistryUtils {
   @Nullable
   public static <T extends Keyed> T parseString(String string, Registry<T> registry) {
      NamespacedKey namespacedKey = NamespacedKey.fromString(string.toLowerCase());
      if (namespacedKey == null) {
         LushLogger.getLogger().warning("'" + string + "' contains invalid characters");
         return null;
      } else {
         return (T)registry.get(namespacedKey);
      }
   }

   public static <T extends Keyed> Collection<T> fromString(String string, Registry<T> registry) {
      boolean isTag = string.charAt(0) == '#';
      NamespacedKey namespacedKey = NamespacedKey.fromString(isTag ? string.substring(1) : string.toLowerCase());
      if (namespacedKey == null) {
         LushLogger.getLogger().warning("'" + string + "' contains invalid characters");
         return Collections.emptyList();
      } else {
         if (isTag) {
            TagType<T> tagType = TagType.get(registry);
            if (tagType == null) {
               return Collections.emptyList();
            }

            Tag<T> tag = TagType.getTag(namespacedKey, registry);
            if (tag != null) {
               return tag.getValues();
            }
         } else {
            T t = (T)registry.get(namespacedKey);
            if (t != null) {
               return List.of(t);
            }
         }

         LushLogger.getLogger().warning("Could not find value in registry for '" + string + "'");
         return Collections.emptyList();
      }
   }

   public static <T extends Keyed> List<T> fromStringList(List<String> stringList, Registry<T> registry) {
      return stringList.stream().flatMap(string -> fromString(string, registry).stream()).toList();
   }
}
