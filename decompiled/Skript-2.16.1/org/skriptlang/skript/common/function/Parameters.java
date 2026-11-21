package org.skriptlang.skript.common.function;

import java.util.Arrays;
import java.util.Collections;
import java.util.SequencedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class Parameters {
   private final SequencedMap<String, Parameter<?>> named;
   private final Parameter<?>[] indexed;

   public Parameters(SequencedMap<String, Parameter<?>> parameters) {
      this.named = parameters;
      this.indexed = new Parameter[parameters.size()];
      int i = 0;

      for (Parameter<?> parameter : parameters.values()) {
         this.indexed[i] = parameter;
         i++;
      }
   }

   public Parameter<?> get(@NotNull String name) {
      return this.named.get(name);
   }

   public Parameter<?> getFirst() {
      return this.named.firstEntry().getValue();
   }

   public Parameter<?> get(int index) {
      return this.indexed[index];
   }

   public Parameter<?>[] all() {
      return Arrays.copyOf(this.indexed, this.indexed.length);
   }

   public int size() {
      return this.indexed.length;
   }

   @UnmodifiableView
   public SequencedMap<String, Parameter<?>> sequencedMap() {
      return Collections.unmodifiableSequencedMap(this.named);
   }
}
