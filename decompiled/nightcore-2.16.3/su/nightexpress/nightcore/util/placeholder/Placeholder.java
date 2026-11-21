package su.nightexpress.nightcore.util.placeholder;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;

@Deprecated
public interface Placeholder {
   @NonNull
   PlaceholderMap getPlaceholders();

   @NonNull
   default UnaryOperator<String> replacePlaceholders() {
      return this.getPlaceholders().replacer();
   }
}
