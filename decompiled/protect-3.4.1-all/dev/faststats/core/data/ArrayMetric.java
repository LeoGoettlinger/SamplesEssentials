package dev.faststats.core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Optional;
import java.util.concurrent.Callable;

final class ArrayMetric<T> extends SimpleMetric<T[]> {
   public ArrayMetric(@SourceId String id, Callable<T[]> callable) throws IllegalArgumentException {
      super(id, callable);
   }

   @Override
   public Optional<JsonElement> getData() throws Exception {
      return this.compute().map(data -> {
         JsonArray elements = new JsonArray(data.length);

         for (T d : data) {
            if (d instanceof Boolean b) {
               elements.add(b);
            } else if (d instanceof Number n) {
               elements.add(n);
            } else {
               elements.add(d.toString());
            }
         }

         return elements;
      });
   }
}
