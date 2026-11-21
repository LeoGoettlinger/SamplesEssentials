package org.enginehub.linbus.stream.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinReadOptions;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public class LinNbtReader implements LinStream {
   private static final byte TWO_BYTE_NULL_START = -64;
   public static final byte TWO_BYTE_NULL_END = -128;
   private static final int TOP_5_BITS = 248;
   private static final int FOUR_BYTE_START = 240;
   private static final byte THREE_BYTE_SURROGATE_START = -19;
   private static final int TOP_3_BITS = 224;
   private static final int THREE_BYTE_SURROGATE_CONTINUATION = 160;
   private final DataInput input;
   private final Deque<LinNbtReader.State> stateStack;
   private LinNbtReader.StringEncoding stringEncoding;
   @Nullable
   private LinNbtReader.NormalUtf8Decoder decoder;

   private static LinNbtReader.StringEncoding getGuaranteedStringEncoding(ByteBuffer bytes) {
      boolean sawTwoByteNullStart = false;
      boolean sawThreeByteSurrogateStart = false;

      for (int i = 0; i < bytes.remaining(); i++) {
         byte b = bytes.get(i);
         if (b == -64) {
            sawTwoByteNullStart = true;
         } else if (sawTwoByteNullStart) {
            if (b == -128) {
               return LinNbtReader.StringEncoding.MODIFIED_UTF_8;
            }

            sawTwoByteNullStart = false;
         }

         if ((b & 248) == 240) {
            return LinNbtReader.StringEncoding.NORMAL_UTF_8;
         }

         if (b == -19) {
            sawThreeByteSurrogateStart = true;
         } else if (sawThreeByteSurrogateStart) {
            if ((b & 224) == 160) {
               return LinNbtReader.StringEncoding.MODIFIED_UTF_8;
            }

            sawThreeByteSurrogateStart = false;
         }
      }

      return LinNbtReader.StringEncoding.UNKNOWN;
   }

   public LinNbtReader(DataInput input, LinReadOptions options) {
      this.input = input;
      this.stateStack = new ArrayDeque<>(List.of(new LinNbtReader.State.Initial()));
      this.stringEncoding = options.allowNormalUtf8Encoding() ? LinNbtReader.StringEncoding.UNKNOWN : LinNbtReader.StringEncoding.MODIFIED_UTF_8;
   }

   @Nullable
   @Override
   public LinToken nextOrNull() throws IOException {
      return (LinToken)(switch ((LinNbtReader.State)this.stateStack.pollLast()) {
         case null -> null;
         case LinNbtReader.State.Initial initial -> {
            if (this.input.readUnsignedByte() != LinTagId.COMPOUND.id()) {
               throw new NbtParseException("NBT stream does not start with a compound tag");
            }

            this.stateStack.addLast(new LinNbtReader.State.CompoundStart());
            yield new LinToken.Name(this.readUtf(), LinTagId.COMPOUND);
         }
         case LinNbtReader.State.CompoundStart compoundStart -> {
            this.stateStack.addLast(new LinNbtReader.State.CompoundEntryName());
            yield new LinToken.CompoundStart();
         }
         case LinNbtReader.State.CompoundEntryName compoundEntryName -> {
            LinTagId id = LinTagId.fromId(this.input.readUnsignedByte());
            if (id == LinTagId.END) {
               yield new LinToken.CompoundEnd();
            } else {
               this.stateStack.addLast(new LinNbtReader.State.CompoundEntryName());
               this.stateStack.addLast(new LinNbtReader.State.ReadValue(id));
               yield new LinToken.Name(this.readUtf(), id);
            }
         }
         case LinNbtReader.State.ReadValue(LinTagId var26) -> this.handleReadValue(var26);
         case LinNbtReader.State.ReadByteArray(int var43) -> {
            int var27 = var43;
            if (var27 == 0) {
               yield new LinToken.ByteArrayEnd();
            } else {
               ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, var27));
               this.input.readFully(buffer.array(), buffer.position(), buffer.remaining());
               this.stateStack.addLast(new LinNbtReader.State.ReadByteArray(var27 - buffer.remaining()));
               yield new LinToken.ByteArrayContent(buffer.asReadOnlyBuffer());
            }
         }
         case LinNbtReader.State.ReadIntArray(int var41) -> {
            int var29 = var41;
            if (var29 == 0) {
               yield new LinToken.IntArrayEnd();
            } else {
               ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, var29 * 4));
               this.input.readFully(buffer.array(), buffer.position(), buffer.remaining());
               this.stateStack.addLast(new LinNbtReader.State.ReadIntArray(var29 - buffer.remaining() / 4));
               yield new LinToken.IntArrayContent(buffer.asIntBuffer().asReadOnlyBuffer());
            }
         }
         case LinNbtReader.State.ReadLongArray(int var39) -> {
            int var31 = var39;
            if (var31 == 0) {
               yield new LinToken.LongArrayEnd();
            } else {
               ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, var31 * 8));
               this.input.readFully(buffer.array(), buffer.position(), buffer.remaining());
               this.stateStack.addLast(new LinNbtReader.State.ReadLongArray(var31 - buffer.remaining() / 8));
               yield new LinToken.LongArrayContent(buffer.asLongBuffer().asReadOnlyBuffer());
            }
         }
         case LinNbtReader.State.ListEntry(int var18, LinTagId var36) -> {
            LinTagId var33 = var36;
            if (var18 == 0) {
               yield new LinToken.ListEnd();
            } else {
               this.stateStack.addLast(new LinNbtReader.State.ListEntry(var18 - 1, var33));
               yield this.handleReadValue(var33);
            }
         }
         default -> throw new MatchException(null, null);
      });
   }

   private LinToken handleReadValue(LinTagId id) throws IOException {
      return (LinToken)(switch (id) {
         case BYTE -> new LinToken.Byte(this.input.readByte());
         case SHORT -> new LinToken.Short(this.input.readShort());
         case INT -> new LinToken.Int(this.input.readInt());
         case LONG -> new LinToken.Long(this.input.readLong());
         case FLOAT -> new LinToken.Float(this.input.readFloat());
         case DOUBLE -> new LinToken.Double(this.input.readDouble());
         case BYTE_ARRAY -> {
            int size = this.input.readInt();
            this.stateStack.addLast(new LinNbtReader.State.ReadByteArray(size));
            yield new LinToken.ByteArrayStart(size);
         }
         case STRING -> new LinToken.String(this.readUtf());
         case LIST -> {
            LinTagId elementId = LinTagId.fromId(this.input.readUnsignedByte());
            int size = this.input.readInt();
            this.stateStack.addLast(new LinNbtReader.State.ListEntry(size, elementId));
            yield new LinToken.ListStart(size, elementId);
         }
         case COMPOUND -> {
            this.stateStack.addLast(new LinNbtReader.State.CompoundEntryName());
            yield new LinToken.CompoundStart();
         }
         case INT_ARRAY -> {
            int size = this.input.readInt();
            this.stateStack.addLast(new LinNbtReader.State.ReadIntArray(size));
            yield new LinToken.IntArrayStart(size);
         }
         case LONG_ARRAY -> {
            int size = this.input.readInt();
            this.stateStack.addLast(new LinNbtReader.State.ReadLongArray(size));
            yield new LinToken.LongArrayStart(size);
         }
         case END -> throw new NbtParseException("Invalid id: " + id);
      });
   }

   private LinNbtReader.NormalUtf8Decoder getNormalUtf8Decoder() {
      LinNbtReader.NormalUtf8Decoder decoder = this.decoder;
      if (decoder == null) {
         decoder = new LinNbtReader.NormalUtf8Decoder();
         this.decoder = decoder;
      }

      return decoder;
   }

   private String readUtf() throws IOException {
      return switch (this.stringEncoding) {
         case MODIFIED_UTF_8 -> this.input.readUTF();
         case NORMAL_UTF_8 -> {
            int length = this.input.readUnsignedShort();
            LinNbtReader.NormalUtf8Decoder decoder = this.getNormalUtf8Decoder();
            decoder.fill(this.input, length);
            yield decoder.decode();
         }
         case UNKNOWN -> {
            int length = this.input.readUnsignedShort();
            LinNbtReader.NormalUtf8Decoder decoder = this.getNormalUtf8Decoder();
            decoder.fill(this.input, length);
            LinNbtReader.StringEncoding knownEncoding = getGuaranteedStringEncoding(decoder.sourceBuffer);
            switch (knownEncoding) {
               case MODIFIED_UTF_8:
                  this.stringEncoding = knownEncoding;
                  byte[] withLength = new byte[length + 2];
                  withLength[0] = (byte)(length >> 8);
                  withLength[1] = (byte)length;
                  System.arraycopy(decoder.sourceBuffer.array(), 0, withLength, 2, length);
                  yield new DataInputStream(new ByteArrayInputStream(withLength)).readUTF();
               case NORMAL_UTF_8:
                  this.stringEncoding = knownEncoding;
                  yield decoder.decode();
               case UNKNOWN:
                  yield decoder.decode();
               default:
                  throw new MatchException(null, null);
            }
         }
      };
   }

   private static final class NormalUtf8Decoder {
      private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
      private ByteBuffer sourceBuffer = ByteBuffer.allocate(128);
      private CharBuffer decodeBuffer = CharBuffer.allocate(128);

      void fill(DataInput input, int length) throws IOException {
         this.ensureSourceBufferCapacity(length);
         input.readFully(this.sourceBuffer.array(), 0, length);
         this.sourceBuffer.limit(length);
      }

      private void ensureSourceBufferCapacity(int requiredCapacity) {
         if (this.sourceBuffer.capacity() < requiredCapacity) {
            this.sourceBuffer = ByteBuffer.allocate(requiredCapacity);
         } else {
            this.sourceBuffer.clear();
         }
      }

      private void ensureCharBufferCapacity(int requiredCapacity) {
         if (this.decodeBuffer.capacity() < requiredCapacity) {
            this.decodeBuffer = CharBuffer.allocate(requiredCapacity);
         } else {
            this.decodeBuffer.clear();
         }
      }

      public String decode() throws CharacterCodingException {
         int n = (int)(this.sourceBuffer.remaining() * this.decoder.averageCharsPerByte());
         this.ensureCharBufferCapacity(n);
         if (n == 0 && this.sourceBuffer.remaining() == 0) {
            return "";
         } else {
            this.decoder.reset();

            while (true) {
               CoderResult cr = this.sourceBuffer.hasRemaining() ? this.decoder.decode(this.sourceBuffer, this.decodeBuffer, true) : CoderResult.UNDERFLOW;
               if (cr.isUnderflow()) {
                  cr = this.decoder.flush(this.decodeBuffer);
               }

               if (cr.isUnderflow()) {
                  this.decodeBuffer.flip();
                  return this.decodeBuffer.toString();
               }

               if (cr.isOverflow()) {
                  n += n / 2 + 1;
                  CharBuffer o = CharBuffer.allocate(n);
                  this.decodeBuffer.flip();
                  o.put(this.decodeBuffer);
                  this.decodeBuffer = o;
               } else {
                  cr.throwException();
               }
            }
         }
      }
   }

   private sealed interface State
      permits LinNbtReader.State.Initial,
      LinNbtReader.State.CompoundStart,
      LinNbtReader.State.CompoundEntryName,
      LinNbtReader.State.ListEntry,
      LinNbtReader.State.ReadValue,
      LinNbtReader.State.ReadByteArray,
      LinNbtReader.State.ReadIntArray,
      LinNbtReader.State.ReadLongArray {
      public record CompoundEntryName() implements LinNbtReader.State {
      }

      public record CompoundStart() implements LinNbtReader.State {
      }

      public record Initial() implements LinNbtReader.State {
      }

      public record ListEntry(int remaining, LinTagId elementId) implements LinNbtReader.State {
      }

      public record ReadByteArray(int remaining) implements LinNbtReader.State {
      }

      public record ReadIntArray(int remaining) implements LinNbtReader.State {
      }

      public record ReadLongArray(int remaining) implements LinNbtReader.State {
      }

      public record ReadValue(LinTagId id) implements LinNbtReader.State {
      }
   }

   private static enum StringEncoding {
      MODIFIED_UTF_8,
      NORMAL_UTF_8,
      UNKNOWN;
   }
}
