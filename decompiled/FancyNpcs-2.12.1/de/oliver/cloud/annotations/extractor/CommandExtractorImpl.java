package de.oliver.cloud.annotations.extractor;

import de.oliver.cloud.annotations.AnnotationParser;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.descriptor.CommandDescriptor;
import de.oliver.cloud.annotations.descriptor.ImmutableCommandDescriptor;
import de.oliver.cloud.util.annotation.AnnotationAccessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.annotations.*"}
)
public final class CommandExtractorImpl implements CommandExtractor {
   private final AnnotationParser<?> annotationParser;

   public CommandExtractorImpl(@NonNull final AnnotationParser<?> annotationParser) {
      this.annotationParser = annotationParser;
   }

   @NonNull
   @Override
   public Collection<CommandDescriptor> extractCommands(@NonNull final Object instance) {
      AnnotationAccessor classAnnotations = AnnotationAccessor.of(instance.getClass());
      Command classCommand = classAnnotations.annotation(Command.class);
      String syntaxPrefix;
      if (classCommand == null) {
         syntaxPrefix = "";
      } else {
         syntaxPrefix = this.annotationParser.processString(classCommand.value()) + " ";
      }

      Method[] methods = instance.getClass().getDeclaredMethods();
      Collection<CommandDescriptor> commandDescriptors = new ArrayList<>();

      for (Method method : methods) {
         Command[] commands = method.getAnnotationsByType(Command.class);
         if (commands.length != 0) {
            if (!method.isAccessible()) {
               method.setAccessible(true);
            }

            if (Modifier.isStatic(method.getModifiers())) {
               throw new IllegalArgumentException(
                  String.format("@Command annotated method '%s' is static! @Command annotated methods should not be static.", method.getName())
               );
            }

            for (Command command : commands) {
               String syntax = syntaxPrefix + this.annotationParser.processString(command.value());
               commandDescriptors.add(
                  ImmutableCommandDescriptor.builder()
                     .method(method)
                     .syntax(this.annotationParser.syntaxParser().parseSyntax(method, syntax))
                     .commandToken(syntax.split(" ")[0].split("\\|")[0])
                     .requiredSender(command.requiredSender())
                     .build()
               );
            }
         }
      }

      return commandDescriptors;
   }
}
