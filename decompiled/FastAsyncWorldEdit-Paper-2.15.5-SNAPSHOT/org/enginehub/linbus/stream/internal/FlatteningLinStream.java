package org.enginehub.linbus.stream.internal;

import java.io.IOException;
import java.util.Iterator;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public class FlatteningLinStream implements LinStream {
   private final Iterator<? extends LinStreamable> streamables;
   @Nullable
   private LinStream current;

   public FlatteningLinStream(Iterator<? extends LinStreamable> streamables) {
      this.streamables = streamables;
   }

   @Nullable
   @Override
   public LinToken nextOrNull() throws IOException {
      while (true) {
         if (this.current == null) {
            if (!this.streamables.hasNext()) {
               return null;
            }

            this.current = this.streamables.next().linStream();
         }

         LinToken token = this.current.nextOrNull();
         if (token != null) {
            return token;
         }

         this.current = null;
      }
   }
}
