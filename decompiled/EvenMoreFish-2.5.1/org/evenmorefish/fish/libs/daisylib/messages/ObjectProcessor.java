package org.evenmorefish.fish.libs.daisylib.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentListMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentSingleMessage;
import org.evenmorefish.fish.libs.daisylib.utils.MessageUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ObjectProcessor {
   private static final List<ObjectProcessor.Processor<?>> PROCESSORS = new ArrayList<>();

   @NonNull
   public static List<Component> process(@Nullable Object object) {
      if (object == null) {
         return List.of();
      } else if (!(object instanceof List<?> list)) {
         for (ObjectProcessor.Processor<?> processor : PROCESSORS) {
            List<Component> components = processor.process(object);
            if (components != null) {
               return components;
            }
         }

         return List.of(MessageUtils.parseString(object.toString()));
      } else {
         List<Component> processed = new ArrayList<>();

         for (Object obj : list) {
            processed.addAll(process(obj));
         }

         return processed;
      }
   }

   public static <T> void registerProcessor(@NonNull Class<T> clazz, @NonNull Function<T, List<Component>> processor) {
      PROCESSORS.add(new ObjectProcessor.Processor<>(clazz, processor));
   }

   static {
      registerProcessor(Component.class, List::of);
      registerProcessor(ComponentSingleMessage.class, singleMessage -> List.of(singleMessage.get()));
      registerProcessor(ComponentListMessage.class, ComponentListMessage::get);
   }

   private record Processor<T>(@NonNull Class<T> clazz, @NonNull Function<T, List<Component>> processor) {
      @Nullable
      public List<Component> process(@NonNull Object object) {
         return this.clazz.isInstance(object) ? this.processor.apply(this.clazz.cast(object)) : null;
      }
   }
}
