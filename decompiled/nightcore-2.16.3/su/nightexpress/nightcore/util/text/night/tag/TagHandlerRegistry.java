package su.nightexpress.nightcore.util.text.night.tag;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.LowerCase;

public class TagHandlerRegistry {
   private static final Map<String, TagHandlerRegistry.Creator> HANDLER_BY_NAME = new HashMap<>();

   public static void clear() {
      HANDLER_BY_NAME.clear();
   }

   public static void register(@NonNull TagHandlerRegistry.Creator creator, String... aliases) {
      for (String alias : aliases) {
         HANDLER_BY_NAME.put(LowerCase.INTERNAL.apply(alias), creator);
      }
   }

   public static boolean unregister(@NonNull String name) {
      return HANDLER_BY_NAME.remove(LowerCase.INTERNAL.apply(name)) != null;
   }

   public static boolean isValid(@NonNull String name) {
      return HANDLER_BY_NAME.containsKey(LowerCase.INTERNAL.apply(name));
   }

   @Nullable
   public static TagHandlerRegistry.Creator getByName(@NonNull String name) {
      return HANDLER_BY_NAME.get(LowerCase.INTERNAL.apply(name));
   }

   @Nullable
   public static TagHandler create(@NonNull String name) {
      TagHandlerRegistry.Creator creator = getByName(name);
      return creator == null ? null : creator.create();
   }

   public interface Creator {
      @NonNull
      TagHandler create();
   }
}
