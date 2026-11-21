package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.cloud.parser.ParserParameter;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public final class MinecraftExtrasParserParameters {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static final ParserParameter<Function<String, ? extends Component>> COMPONENT_DECODER = create(
      "component_decoder", new TypeToken<Function<String, ? extends Component>>() {}
   );

   private MinecraftExtrasParserParameters() {
   }

   @NonNull
   private static <T> ParserParameter<T> create(@NonNull final String key, @NonNull final TypeToken<T> expectedType) {
      return new ParserParameter<>(key, expectedType);
   }
}
