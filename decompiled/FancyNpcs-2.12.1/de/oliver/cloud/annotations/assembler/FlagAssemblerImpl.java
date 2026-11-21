package de.oliver.cloud.annotations.assembler;

import de.oliver.cloud.CommandManager;
import de.oliver.cloud.annotations.descriptor.FlagDescriptor;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.description.Description;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.ParserRegistry;
import de.oliver.cloud.parser.flag.CommandFlag;
import de.oliver.cloud.permission.Permission;
import de.oliver.cloud.suggestion.SuggestionProvider;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.annotations.*"}
)
public final class FlagAssemblerImpl implements FlagAssembler {
   private final CommandManager<?> commandManager;

   public FlagAssemblerImpl(@NonNull final CommandManager<?> commandManager) {
      this.commandManager = commandManager;
   }

   @NonNull
   @Override
   public CommandFlag<?> assembleFlag(@NonNull final FlagDescriptor descriptor) {
      Description description;
      if (descriptor.description() == null) {
         description = Description.empty();
      } else {
         description = descriptor.description();
      }

      Permission permission;
      if (descriptor.permission() == null) {
         permission = Permission.empty();
      } else {
         permission = descriptor.permission();
      }

      CommandFlag.Builder<?, Void> builder = this.commandManager
         .flagBuilder(descriptor.name())
         .withDescription(description)
         .withAliases(descriptor.aliases())
         .withPermission(permission);
      if (descriptor.repeatable()) {
         builder = builder.asRepeatable();
      }

      if (descriptor.parameter().getType().equals(boolean.class)) {
         return builder.build();
      } else {
         TypeToken<?> token;
         if (descriptor.repeatable() && Collection.class.isAssignableFrom(descriptor.parameter().getType())) {
            token = TypeToken.get(GenericTypeReflector.getTypeParameter(descriptor.parameter().getParameterizedType(), Collection.class.getTypeParameters()[0]));
         } else {
            token = TypeToken.get(descriptor.parameter().getType());
         }

         if (token.equals(TypeToken.get(boolean.class))) {
            return builder.build();
         } else {
            Collection<Annotation> annotations = Arrays.asList(descriptor.parameter().getAnnotations());
            ParserRegistry<?> registry = this.commandManager.parserRegistry();
            ArgumentParser<?, ?> parser;
            if (descriptor.parserName() == null) {
               parser = registry.createParser(token, registry.parseAnnotations(token, annotations)).orElse(null);
            } else {
               parser = (ArgumentParser<?, ?>)registry.createParser(descriptor.parserName(), registry.parseAnnotations(token, annotations)).orElse(null);
            }

            if (parser == null) {
               throw new IllegalArgumentException(
                  String.format("Cannot find parser for type '%s' for flag '%s'", descriptor.parameter().getType().getCanonicalName(), descriptor.name())
               );
            } else {
               SuggestionProvider<?> suggestionProvider;
               if (descriptor.suggestions() != null) {
                  suggestionProvider = registry.getSuggestionProvider(descriptor.suggestions()).orElse(null);
               } else {
                  suggestionProvider = null;
               }

               CommandComponent.Builder componentBuilder = CommandComponent.builder();
               componentBuilder.commandManager(this.commandManager).name(descriptor.name()).valueType(descriptor.parameter().getType()).parser(parser);
               if (suggestionProvider != null) {
                  componentBuilder.suggestionProvider(suggestionProvider);
               }

               return builder.withComponent(componentBuilder.build()).build();
            }
         }
      }
   }
}
