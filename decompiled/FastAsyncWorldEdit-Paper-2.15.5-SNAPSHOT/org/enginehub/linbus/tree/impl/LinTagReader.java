package org.enginehub.linbus.tree.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.jspecify.annotations.Nullable;

public class LinTagReader {
   public static LinRootEntry readRoot(LinStream tokens) throws IOException {
      tokens = tokens.calculateOptionalInfo();
      if (tokens.nextOrNull() instanceof LinToken.Name name) {
         if (name.id().orElseThrow() != LinTagId.COMPOUND) {
            throw new NbtParseException("Expected compound tag for root tag");
         } else {
            LinCompoundTag tag = readCompound(tokens);
            return new LinRootEntry(name.name(), tag);
         }
      } else {
         throw new NbtParseException("Expected root name");
      }
   }

   public static LinCompoundTag readCompound(LinStream tokens) throws IOException {
      tokens = tokens.calculateOptionalInfo();
      if (!(tokens.nextOrNull() instanceof LinToken.CompoundStart)) {
         throw new NbtParseException("Expected compound start");
      } else {
         LinCompoundTag.Builder builder = LinCompoundTag.builder();

         while (true) {
            LinToken token = tokens.nextOrNull();
            if (token == null) {
               throw new NbtParseException("Expected compound end");
            }

            if (token instanceof LinToken.CompoundEnd) {
               return builder.build();
            }

            if (!(token instanceof LinToken.Name name)) {
               throw new NbtParseException("Expected name, got " + token);
            }

            LinTag value = readValue(tokens, name.id().map(LinTagType::fromId).orElse(null));
            builder.put(name.name(), value);
         }
      }
   }

   private static LinByteArrayTag readByteArray(LinStream tokens) throws IOException {
      if (tokens.nextOrNull() instanceof LinToken.ByteArrayStart start) {
         ByteBuffer var5 = ByteBuffer.allocate(start.size().orElseThrow());

         while (true) {
            LinToken token = tokens.nextOrNull();
            if (token == null) {
               throw new NbtParseException("Expected byte array end");
            }

            if (token instanceof LinToken.ByteArrayEnd) {
               if (var5.hasRemaining()) {
                  throw new NbtParseException("Not all bytes received");
               }

               return LinByteArrayTag.of(var5.array());
            }

            if (!(token instanceof LinToken.ByteArrayContent content)) {
               throw new NbtParseException("Expected byte array content, got " + token);
            }

            var5.put(content.buffer());
         }
      } else {
         throw new NbtParseException("Expected byte array start");
      }
   }

   private static LinIntArrayTag readIntArray(LinStream tokens) throws IOException {
      if (tokens.nextOrNull() instanceof LinToken.IntArrayStart start) {
         IntBuffer var5 = IntBuffer.allocate(start.size().orElseThrow());

         while (true) {
            LinToken token = tokens.nextOrNull();
            if (token == null) {
               throw new NbtParseException("Expected int array end");
            }

            if (token instanceof LinToken.IntArrayEnd) {
               if (var5.hasRemaining()) {
                  throw new NbtParseException("Not all ints received");
               }

               return LinIntArrayTag.of(var5.array());
            }

            if (!(token instanceof LinToken.IntArrayContent content)) {
               throw new NbtParseException("Expected int array content, got " + token);
            }

            var5.put(content.buffer());
         }
      } else {
         throw new NbtParseException("Expected int array start");
      }
   }

   private static LinLongArrayTag readLongArray(LinStream tokens) throws IOException {
      if (tokens.nextOrNull() instanceof LinToken.LongArrayStart start) {
         LongBuffer var5 = LongBuffer.allocate(start.size().orElseThrow());

         while (true) {
            LinToken token = tokens.nextOrNull();
            if (token == null) {
               throw new NbtParseException("Expected long array end");
            }

            if (token instanceof LinToken.LongArrayEnd) {
               if (var5.hasRemaining()) {
                  throw new NbtParseException("Not all longs received");
               }

               return LinLongArrayTag.of(var5.array());
            }

            if (!(token instanceof LinToken.LongArrayContent content)) {
               throw new NbtParseException("Expected long array content, got " + token);
            }

            var5.put(content.buffer());
         }
      } else {
         throw new NbtParseException("Expected long array start");
      }
   }

   private static <T extends LinTag<?>> LinListTag<T> readList(LinStream tokens) throws IOException {
      if (!(tokens.nextOrNull() instanceof LinToken.ListStart start)) {
         throw new NbtParseException("Expected list start");
      } else {
         LinTagType<T> elementType = LinTagType.fromId(start.elementId().orElseThrow());
         LinListTag.Builder builder = LinListTag.builder(elementType);

         for (int i = 0; i < start.size().orElseThrow(); i++) {
            T tag = readValue(tokens, elementType);
            builder.add(tag);
         }

         if (!(tokens.nextOrNull() instanceof LinToken.ListEnd)) {
            throw new NbtParseException("Expected list end");
         } else {
            return builder.build();
         }
      }
   }

   private static <T extends LinTag<?>> T readValue(LinStream tokens, @Nullable LinTagType<T> id) throws IOException {
      if (id == null) {
         LinToken next = tokens.nextOrNull();
         if (next == null) {
            throw new NbtParseException("Expected value, got end of stream");
         }

         id = LinTagType.fromId(next.tagId().orElseThrow(() -> new NbtParseException("Expected value, got " + next)));
         tokens = new SurroundingLinStream(next, tokens, null);
      }
      return id.cast((LinTag<?>)(switch (id.id()) {
         case BYTE_ARRAY -> readByteArray(tokens);
         case BYTE -> LinByteTag.of(((LinToken.Byte)requireNextToken(tokens)).value());
         case COMPOUND -> readCompound(tokens);
         case DOUBLE -> LinDoubleTag.of(((LinToken.Double)requireNextToken(tokens)).value());
         case END -> throw new NbtParseException("Unexpected END id");
         case FLOAT -> LinFloatTag.of(((LinToken.Float)requireNextToken(tokens)).value());
         case INT_ARRAY -> readIntArray(tokens);
         case INT -> LinIntTag.of(((LinToken.Int)requireNextToken(tokens)).value());
         case LIST -> readList(tokens);
         case LONG_ARRAY -> readLongArray(tokens);
         case LONG -> LinLongTag.of(((LinToken.Long)requireNextToken(tokens)).value());
         case SHORT -> LinShortTag.of(((LinToken.Short)requireNextToken(tokens)).value());
         case STRING -> LinStringTag.of(((LinToken.String)requireNextToken(tokens)).value());
      }));
   }

   private static LinToken requireNextToken(LinStream tokens) throws IOException {
      LinToken linToken = tokens.nextOrNull();
      if (linToken == null) {
         throw new NbtParseException("Unexpected end of stream");
      } else {
         return linToken;
      }
   }

   private LinTagReader() {
   }
}
