package revxrsal.commands.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.util.Collections;

final class LocalesAnnotationReplacer implements AnnotationReplacer<Description> {
   private static final Pattern LOCALE_PATTERN = Pattern.compile("#\\{(?<key>.*)}");
   private final CommandHandler handler;

   public LocalesAnnotationReplacer(CommandHandler handler) {
      this.handler = handler;
   }

   @Nullable
   public Collection<Annotation> replaceAnnotations(@NotNull AnnotatedElement element, @NotNull Description annotation) {
      Matcher matcher = LOCALE_PATTERN.matcher(annotation.value());
      if (matcher.matches()) {
         String key = matcher.group("key");
         return Collections.listOf(Annotations.create(Description.class, "value", this.handler.getTranslator().get(key)));
      } else {
         return null;
      }
   }
}
