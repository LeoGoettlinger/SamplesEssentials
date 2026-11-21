package dev.faststats.core.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Optional;
import java.util.concurrent.Callable;

final class SingleValueMetric<T> extends SimpleMetric<T> {
   public SingleValueMetric(@SourceId String id, Callable<T> callable) throws IllegalArgumentException {
      super(id, callable);
   }

   @Override
   public Optional<JsonElement> getData() throws Exception {
      return this.compute().map(data -> {
         if (data instanceof Boolean bool) {
            return new JsonPrimitive(bool);
         } else {
            return data instanceof Number number ? new JsonPrimitive(number) : new JsonPrimitive(data.toString());
         }
      });
   }
}
