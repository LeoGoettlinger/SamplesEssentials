package org.enginehub.linbus.stream.internal;

import java.io.IOException;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public class SurroundingLinStream implements LinStream {
   @Nullable
   private LinToken prefix;
   @Nullable
   private LinStream stream;
   @Nullable
   private LinToken suffix;

   public SurroundingLinStream(@Nullable LinToken prefix, LinStream stream, @Nullable LinToken suffix) {
      this.prefix = prefix;
      this.stream = stream;
      this.suffix = suffix;
   }

   @Nullable
   @Override
   public LinToken nextOrNull() throws IOException {
      if (this.prefix != null) {
         LinToken token = this.prefix;
         this.prefix = null;
         return token;
      } else {
         LinToken token = this.stream == null ? null : this.stream.nextOrNull();
         if (token == null) {
            this.stream = null;
            token = this.suffix;
            this.suffix = null;
         }

         return token;
      }
   }
}
