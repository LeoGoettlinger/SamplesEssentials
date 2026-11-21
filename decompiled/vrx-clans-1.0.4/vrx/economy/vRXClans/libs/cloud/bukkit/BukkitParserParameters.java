package vrx.economy.vRXClans.libs.cloud.bukkit;

import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.parser.ParserParameter;

@API(
   status = API.Status.STABLE,
   since = "1.7.0"
)
public final class BukkitParserParameters {
   @API(
      status = API.Status.STABLE,
      since = "1.8.0"
   )
   public static final ParserParameter<Boolean> ALLOW_EMPTY_SELECTOR_RESULT = create("allow_empty_selector_result", TypeToken.get(Boolean.class));
   public static final ParserParameter<Boolean> REQUIRE_EXPLICIT_NAMESPACE = create("require_explicit_namespace", TypeToken.get(Boolean.class));
   public static final ParserParameter<String> DEFAULT_NAMESPACE = create("default_namespace", TypeToken.get(String.class));

   private BukkitParserParameters() {
   }

   @NonNull
   private static <T> ParserParameter<T> create(@NonNull final String key, @NonNull final TypeToken<T> expectedType) {
      return new ParserParameter<>(key, expectedType);
   }
}
