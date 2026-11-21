package su.nightexpress.nightcore.bridge.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.LowerCase;

public class PlaceholderRegistry {
   private final Map<Class<?>, PayloadResolver<?>> resolverMap = new HashMap<>();
   private final Map<String, PlaceholderHandler> handlerMap = new HashMap<>();

   public <T> void addResolver(@NonNull Class<T> type, @NonNull PayloadResolver<T> resolver) {
      this.resolverMap.put(type, resolver);
   }

   public void clear() {
      this.resolverMap.clear();
      this.handlerMap.clear();
   }

   @Nullable
   public <T> PayloadResolver<T> resolver(@NonNull Class<T> type) {
      return (PayloadResolver<T>)this.resolverMap.get(type);
   }

   public <T> void registerMapped(@NonNull String key, @NonNull Class<T> payloadType, @NonNull BiFunction<Player, T, String> handler) {
      PayloadResolver<T> resolver = this.resolver(payloadType);
      if (resolver == null) {
         throw new IllegalArgumentException("No payload resolver found for %s.".formatted(payloadType.getName()));
      } else {
         this.registerRaw(key, (player, payload) -> {
            T resolved = resolver.resolve(player, payload);
            return resolved == null ? null : handler.apply(player, resolved);
         });
      }
   }

   public void registerRaw(@NonNull String key, @NonNull PlaceholderHandler handler) {
      this.handlerMap.put(LowerCase.INTERNAL.apply(key), handler);
   }

   public boolean unregister(@NonNull String key) {
      return this.handlerMap.remove(LowerCase.INTERNAL.apply(key)) != null;
   }

   public boolean isEmpty() {
      return this.handlerMap.isEmpty();
   }

   @Nullable
   public String onPlaceholderRequest(@NonNull Player player, @NonNull String params) {
      PlaceholderRegistry.ParsedPlaceholder parsed = this.findHandler(params);
      return parsed == null ? null : parsed.handler().handle(player, parsed.payload());
   }

   @Nullable
   private PlaceholderRegistry.ParsedPlaceholder findHandler(@NonNull String params) {
      String currentKey = params;
      StringBuilder currentPayload = new StringBuilder();

      while (!this.handlerMap.containsKey(currentKey)) {
         int lastUnderscoreIndex = currentKey.lastIndexOf(95);
         if (lastUnderscoreIndex == -1) {
            return null;
         }

         String suffix = currentKey.substring(lastUnderscoreIndex + 1);
         if (currentPayload.isEmpty()) {
            currentPayload = new StringBuilder(suffix);
         } else {
            currentPayload.insert(0, suffix + "_");
         }

         currentKey = currentKey.substring(0, lastUnderscoreIndex);
      }

      return new PlaceholderRegistry.ParsedPlaceholder(currentKey, this.handlerMap.get(currentKey), currentPayload.toString());
   }

   public record ParsedPlaceholder(@NonNull String key, @NonNull PlaceholderHandler handler, @NonNull String payload) {
   }
}
