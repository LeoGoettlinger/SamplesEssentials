package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.parser;

import de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.MinecraftExtrasParserParameters;
import de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.annotation.specifier.Decoder;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserContributor;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserParameters;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserRegistry;
import de.theredend2000.advancedhunt.lib.cloud.parser.StandardParameters;
import de.theredend2000.advancedhunt.lib.cloud.parser.standard.StringParser;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.MiniMessage;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import org.apiguardian.api.API;

@API(
   status = API.Status.INTERNAL
)
public final class ComponentParserContributor implements ParserContributor {
   @Override
   public <C> void contribute(final ParserRegistry<C> registry) {
      try {
         registry.registerAnnotationMapper(
            Decoder.MiniMessage.class,
            (annotation, parsedType) -> ParserParameters.single(MinecraftExtrasParserParameters.COMPONENT_DECODER, MiniMessage.miniMessage()::deserialize)
         );
         registry.registerAnnotationMapper(
            Decoder.Legacy.class,
            (annotation, parsedType) -> {
               char character = annotation.value();
               return ParserParameters.single(
                  MinecraftExtrasParserParameters.COMPONENT_DECODER, string -> LegacyComponentSerializer.legacy(character).deserialize(string)
               );
            }
         );
         registry.registerAnnotationMapper(
            Decoder.Json.class,
            (annotation, parsedType) -> {
               boolean downsampleColors = annotation.downsampleColors();
               Function<String, ? extends Component> decoder = downsampleColors
                  ? GsonComponentSerializer.colorDownsamplingGson()::deserialize
                  : GsonComponentSerializer.gson()::deserialize;
               return ParserParameters.single(MinecraftExtrasParserParameters.COMPONENT_DECODER, decoder);
            }
         );
         registry.registerAnnotationMapper(Decoder.class, (annotation, parsedType) -> {
            Function<String, ? extends Component> decoder;
            try {
               decoder = annotation.value().getConstructor().newInstance().decoder(parsedType);
            } catch (ReflectiveOperationException var4) {
               throw new IllegalArgumentException("Could not create decoder for " + annotation.value(), var4);
            }

            return ParserParameters.single(MinecraftExtrasParserParameters.COMPONENT_DECODER, decoder);
         });
         registry.registerParserSupplier(
            TypeToken.get(Component.class),
            options -> {
               boolean greedy = options.get(StandardParameters.GREEDY, false);
               boolean greedyFlagAware = options.get(StandardParameters.FLAG_YIELDING, false);
               boolean quoted = options.get(StandardParameters.QUOTED, false);
               if (greedyFlagAware && quoted) {
                  throw new IllegalArgumentException("Don't know whether to create GREEDY_FLAG_YIELDING or QUOTED StringArgument.StringParser, both specified.");
               } else if (greedy && quoted) {
                  throw new IllegalArgumentException("Don't know whether to create GREEDY or QUOTED StringArgument.StringParser, both specified.");
               } else {
                  StringParser.StringMode stringMode;
                  if (greedyFlagAware) {
                     stringMode = StringParser.StringMode.GREEDY_FLAG_YIELDING;
                  } else if (greedy) {
                     stringMode = StringParser.StringMode.GREEDY;
                  } else if (quoted) {
                     stringMode = StringParser.StringMode.QUOTED;
                  } else {
                     stringMode = StringParser.StringMode.SINGLE;
                  }

                  Function<String, ? extends Component> decoder = options.get(
                     MinecraftExtrasParserParameters.COMPONENT_DECODER, MiniMessage.miniMessage()::deserialize
                  );
                  return new ComponentParser<>(decoder, stringMode);
               }
            }
         );
      } catch (LinkageError | Exception var3) {
      }
   }
}
