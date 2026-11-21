package org.enginehub.linbus.stream.impl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public class OptionalInfoCalculator implements LinStream {
   private final LinStream original;
   @Nullable
   private Deque<LinToken> tokenBuffer;

   public OptionalInfoCalculator(LinStream original) {
      this.original = original;
   }

   @Nullable
   @Override
   public LinToken nextOrNull() throws IOException {
      if (this.tokenBuffer != null) {
         LinToken next = this.tokenBuffer.pollFirst();
         if (next != null) {
            return next;
         }

         this.tokenBuffer = null;
      }

      LinToken next = this.original.nextOrNull();
      if (next == null) {
         return null;
      } else {
         OptionalInfoCalculator.TokenAndBuffer tokenAndBuffer = this.fillIfNeeded(next);
         if (tokenAndBuffer.buffer != null && !tokenAndBuffer.buffer.isEmpty()) {
            this.tokenBuffer = tokenAndBuffer.buffer;
         }

         return tokenAndBuffer.token;
      }
   }

   @Override
   public LinStream calculateOptionalInfo() {
      return this;
   }

   private OptionalInfoCalculator.TokenAndBuffer fillIfNeeded(LinToken token) throws IOException {
      Objects.requireNonNull(token);

      return switch (token) {
         case LinToken.ListStart listStart when listStart.size().isEmpty() || listStart.elementId().isEmpty() -> this.getFilled(
            new OptionalInfoCalculator.ListStartFill(listStart)
         );
         case LinToken.ByteArrayStart byteArrayStart -> byteArrayStart.size().isPresent()
            ? new OptionalInfoCalculator.TokenAndBuffer(token, null)
            : this.getFilled(new OptionalInfoCalculator.ByteArrayStartFill());
         case LinToken.IntArrayStart(OptionalInt var18) -> var18.isPresent()
            ? new OptionalInfoCalculator.TokenAndBuffer(token, null)
            : this.getFilled(new OptionalInfoCalculator.IntArrayStartFill());
         case LinToken.LongArrayStart(OptionalInt var26) -> {
            OptionalInt var19 = var26;
            if (!var19.isEmpty()) {
               byte var29 = 4;
            }

            yield this.getFilled(new OptionalInfoCalculator.LongArrayStartFill());
         }
         case LinToken.Name(String var13, Optional var24) -> {
            Optional var20 = var24;
            if (!var20.isEmpty()) {
               byte var3 = 5;
            }

            yield this.getFilled(new OptionalInfoCalculator.NameFill(var13));
         }
         default -> new OptionalInfoCalculator.TokenAndBuffer(token, null);
      };
   }

   private OptionalInfoCalculator.TokenAndBuffer getFilled(OptionalInfoCalculator.OptionalFill fill) throws IOException {
      ArrayDeque<LinToken> buffer = new ArrayDeque<>();
      ArrayDeque<LinToken> consumedTokenStack = new ArrayDeque<>();

      while (true) {
         LinToken next = buffer.pollFirst();
         if (next == null) {
            LinToken originalNext = this.original.nextOrNull();
            if (originalNext == null) {
               throw new NbtParseException("Optional value not filled by the end of token stream");
            }

            OptionalInfoCalculator.TokenAndBuffer tokenAndBuffer = this.fillIfNeeded(originalNext);
            buffer.add(tokenAndBuffer.token);
            consumedTokenStack.add(tokenAndBuffer.token);
            if (tokenAndBuffer.buffer != null) {
               buffer.addAll(tokenAndBuffer.buffer);
               consumedTokenStack.addAll(tokenAndBuffer.buffer);
            }
         } else {
            LinToken filled = fill.tryFill(next);
            if (filled != null) {
               return new OptionalInfoCalculator.TokenAndBuffer(filled, consumedTokenStack);
            }
         }
      }
   }

   private static final class ByteArrayStartFill implements OptionalInfoCalculator.OptionalFill {
      private int size;

      @Nullable
      @Override
      public LinToken tryFill(LinToken token) {
         if (token instanceof LinToken.ByteArrayEnd) {
            return new LinToken.ByteArrayStart(this.size);
         } else if (token instanceof LinToken.ByteArrayContent content) {
            this.size = this.size + content.buffer().remaining();
            return null;
         } else {
            throw new NbtParseException("Unexpected token: " + token);
         }
      }
   }

   private static final class IntArrayStartFill implements OptionalInfoCalculator.OptionalFill {
      private int size;

      @Nullable
      @Override
      public LinToken tryFill(LinToken token) {
         if (token instanceof LinToken.IntArrayEnd) {
            return new LinToken.IntArrayStart(this.size);
         } else if (token instanceof LinToken.IntArrayContent content) {
            this.size = this.size + content.buffer().remaining();
            return null;
         } else {
            throw new NbtParseException("Unexpected token: " + token);
         }
      }
   }

   private static final class ListStartFill implements OptionalInfoCalculator.OptionalFill {
      private final int knownSize;
      @Nullable
      private final ValueCounter counter;
      @Nullable
      private LinTagId elementId;

      public ListStartFill(LinToken.ListStart listStart) {
         if (listStart.size().isPresent()) {
            this.knownSize = listStart.size().getAsInt();
            this.counter = null;
         } else {
            this.knownSize = -1;
            this.counter = new ValueCounter();
         }

         this.elementId = listStart.elementId().orElse(null);
      }

      @Nullable
      @Override
      public LinToken tryFill(LinToken token) {
         if (this.counter == null || !this.counter.isNested()) {
            LinTagId elementId = this.elementId;
            if (elementId == null) {
               if (token instanceof LinToken.ListEnd) {
                  elementId = LinTagId.END;
               } else {
                  elementId = token.tagId().orElseThrow(() -> new NbtParseException("Token doesn't represent a tag directly: " + token));
               }

               this.elementId = elementId;
            }

            if (this.counter == null) {
               return new LinToken.ListStart(this.knownSize, elementId);
            }

            if (token instanceof LinToken.ListEnd) {
               return new LinToken.ListStart(this.counter.count(), elementId);
            }
         }

         this.counter.add(token);
         return null;
      }
   }

   private static final class LongArrayStartFill implements OptionalInfoCalculator.OptionalFill {
      private int size;

      @Nullable
      @Override
      public LinToken tryFill(LinToken token) {
         if (token instanceof LinToken.LongArrayEnd) {
            return new LinToken.LongArrayStart(this.size);
         } else if (token instanceof LinToken.LongArrayContent content) {
            this.size = this.size + content.buffer().remaining();
            return null;
         } else {
            throw new NbtParseException("Unexpected token: " + token);
         }
      }
   }

   private record NameFill(String name) implements OptionalInfoCalculator.OptionalFill {
      @Override
      public LinToken tryFill(LinToken token) {
         return new LinToken.Name(this.name, token.tagId().orElseThrow(() -> new NbtParseException("Token doesn't represent a tag directly: " + token)));
      }
   }

   private interface OptionalFill {
      @Nullable
      LinToken tryFill(LinToken token);
   }

   private record TokenAndBuffer(LinToken token, @Nullable Deque<LinToken> buffer) {
   }
}
