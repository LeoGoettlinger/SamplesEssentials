package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.parser;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.parser.standard.StringParser;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.MiniMessage;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.ComponentSerializer;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ComponentParser<C> implements ArgumentParser<C, Component> {
   private static final StringParser.StringMode DEFAULT_STRING_MODE = StringParser.StringMode.QUOTED;
   private final Function<String, ? extends Component> componentDecoder;
   private final StringParser<C> stringParser;

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Component> miniMessageParser() {
      return miniMessageParser(DEFAULT_STRING_MODE);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Component> miniMessageParser(@NonNull final StringParser.StringMode stringMode) {
      return componentParser(MiniMessage.miniMessage(), stringMode);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Component> componentParser(@NonNull final ComponentSerializer<?, ? extends Component, String> componentSerializer) {
      return componentParser(componentSerializer::deserialize);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Component> componentParser(@NonNull final Function<String, ? extends Component> componentDecoder) {
      return componentParser(componentDecoder, DEFAULT_STRING_MODE);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Component> componentParser(
      @NonNull final ComponentSerializer<?, ? extends Component, String> componentSerializer, @NonNull final StringParser.StringMode stringMode
   ) {
      return componentParser(componentSerializer::deserialize, stringMode);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Component> componentParser(
      @NonNull final Function<String, ? extends Component> componentDecoder, @NonNull final StringParser.StringMode stringMode
   ) {
      return ParserDescriptor.of(new ComponentParser<>(componentDecoder, stringMode), Component.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Component> componentComponent(
      @NonNull final Function<String, ? extends Component> componentDecoder, @NonNull final StringParser.StringMode stringMode
   ) {
      return CommandComponent.<C, Component>builder().parser(componentParser(componentDecoder, stringMode));
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public ComponentParser(@NonNull final Function<String, ? extends Component> componentDecoder, @NonNull final StringParser.StringMode stringMode) {
      this.componentDecoder = componentDecoder;
      this.stringParser = new StringParser<>(stringMode);
   }

   @NonNull
   @Override
   public ArgumentParseResult<Component> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      ArgumentParseResult<String> result = this.stringParser.parse(commandContext, commandInput);
      if (result.failure().isPresent()) {
         return ArgumentParseResult.failure(result.failure().get());
      } else {
         try {
            return ArgumentParseResult.success(this.componentDecoder.apply(result.parsedValue().get()));
         } catch (Exception var5) {
            return ArgumentParseResult.failure(var5);
         }
      }
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public StringParser.StringMode stringMode() {
      return this.stringParser.stringMode();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public Function<String, ? extends Component> componentDecoder() {
      return this.componentDecoder;
   }
}
