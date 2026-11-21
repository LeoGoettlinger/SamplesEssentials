package org.enginehub.linbus.stream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Optional;
import org.enginehub.linbus.common.internal.AbstractIterator;
import org.enginehub.linbus.stream.impl.OptionalInfoCalculator;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public interface LinStream extends LinStreamable {
   static LinStream of() {
      return () -> null;
   }

   static LinStream of(LinToken token) {
      return new LinStream() {
         @Nullable
         private LinToken nextToken = token;

         @Nullable
         @Override
         public LinToken nextOrNull() {
            LinToken tokenx = this.nextToken;
            this.nextToken = null;
            return tokenx;
         }
      };
   }

   static LinStream of(LinToken... tokens) {
      return new LinStream() {
         private int index = 0;

         @Nullable
         @Override
         public LinToken nextOrNull() {
            if (this.index >= tokens.length) {
               return null;
            } else {
               LinToken token = tokens[this.index];
               this.index++;
               return token;
            }
         }
      };
   }

   @Nullable
   LinToken nextOrNull() throws IOException;

   default Optional<LinToken> next() throws IOException {
      return Optional.ofNullable(this.nextOrNull());
   }

   default LinStream calculateOptionalInfo() {
      return new OptionalInfoCalculator(this);
   }

   @Override
   default LinStream linStream() {
      return this;
   }

   default Iterator<LinToken> asIterator() {
      return new AbstractIterator<LinToken>() {
         @Nullable
         protected LinToken computeNext() {
            try {
               LinToken linToken = LinStream.this.nextOrNull();
               return linToken == null ? this.end() : linToken;
            } catch (IOException var2) {
               throw new UncheckedIOException(var2);
            }
         }
      };
   }
}
