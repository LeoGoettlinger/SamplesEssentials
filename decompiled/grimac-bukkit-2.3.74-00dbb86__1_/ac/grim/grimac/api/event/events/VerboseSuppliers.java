package ac.grim.grimac.api.event.events;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class VerboseSuppliers {
   private VerboseSuppliers() {
   }

   @NotNull
   public static Supplier<String> constant(String verbose) {
      String value = verbose == null ? "" : verbose;
      return () -> value;
   }

   @NotNull
   public static Supplier<String> memoize(Supplier<String> supplier) {
      final Supplier<String> source = supplier == null ? () -> "" : supplier;
      return new Supplier<String>() {
         private String value;
         private boolean computed;

         public String get() {
            if (!this.computed) {
               this.value = VerboseSuppliers.safeGet(source);
               this.computed = true;
            }

            return this.value;
         }
      };
   }

   @NotNull
   private static String safeGet(@NotNull Supplier<String> supplier) {
      try {
         String value = supplier.get();
         return value == null ? "" : value;
      } catch (Throwable var2) {
         return "";
      }
   }
}
