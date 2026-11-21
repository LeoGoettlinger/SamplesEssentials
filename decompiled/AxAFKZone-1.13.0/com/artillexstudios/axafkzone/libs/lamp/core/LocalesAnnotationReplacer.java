package com.artillexstudios.axafkzone.libs.lamp.core;

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.annotation.Description;
import com.artillexstudios.axafkzone.libs.lamp.annotation.dynamic.AnnotationReplacer;
import com.artillexstudios.axafkzone.libs.lamp.annotation.dynamic.Annotations;
import com.artillexstudios.axafkzone.libs.lamp.util.Collections;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
