package net.milkbowl.vault2.helper.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public class Context {
   private final Map<String, String> contextValues = new ConcurrentHashMap<>();
   public static final Context GLOBAL = new Context();
   private LookupMode lookupMode = LookupMode.GLOBAL_FALLBACK;

   public Context() {
   }

   public Context(final String world) {
      this.contextValues.put("world", world);
   }

   public Context(final String world, final LookupMode lookupMode) {
      this.contextValues.put("world", world);
      this.lookupMode = lookupMode;
   }

   public Context(@Nullable final Map<String, String> contextValues) {
      if (contextValues != null) {
         this.contextValues.putAll(contextValues);
      }
   }

   public Context(@Nullable final Map<String, String> contextValues, final LookupMode lookupMode) {
      if (contextValues != null) {
         this.contextValues.putAll(contextValues);
      }

      this.lookupMode = lookupMode;
   }

   public LookupMode lookupMode() {
      return this.lookupMode;
   }

   public void lookupMode(final LookupMode lookupMode) {
      this.lookupMode = lookupMode;
   }

   public Optional<String> world() {
      return Optional.ofNullable(this.contextValues.get("world"));
   }

   @Nullable
   public String worldOrNull() {
      return this.contextValues.get("world");
   }

   public Optional<String> value(final String key) {
      return Optional.ofNullable(this.contextValues.get(key));
   }

   @Nullable
   public String valueOrNull(final String key) {
      return this.contextValues.get(key);
   }

   public Map<String, String> asMap() {
      return this.contextValues;
   }

   public static Context fromWorld(final String world) {
      return new Context(world, LookupMode.GLOBAL_FALLBACK);
   }

   public static Context fromWorld(final String world, final LookupMode lookupMode) {
      return new Context(world, lookupMode);
   }
}
