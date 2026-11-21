package org.enginehub.linbus.stream;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Optional;
import java.util.OptionalInt;
import org.enginehub.linbus.common.IOFunction;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.exception.NbtWriteException;
import org.enginehub.linbus.stream.impl.LinNbtReader;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public class LinBinaryIO {
   public static LinStream read(DataInput input) {
      return read(input, LinReadOptions.builder().build());
   }

   public static LinStream read(DataInput input, LinReadOptions options) {
      return new LinNbtReader(input, options);
   }

   public static <R> R readUsing(DataInput input, IOFunction<? super LinStream, ? extends R> transform) throws IOException {
      return (R)transform.apply(read(input));
   }

   public static <R> R readUsing(DataInput input, LinReadOptions options, IOFunction<? super LinStream, ? extends R> transform) throws IOException {
      return (R)transform.apply(read(input, options));
   }

   public static void write(DataOutput output, LinStreamable tokens) throws IOException {
      LinStream tokenStream = tokens.linStream().calculateOptionalInfo();
      boolean seenFirstName = false;
      String nextName = null;

      LinToken token;
      while ((token = tokenStream.nextOrNull()) != null) {
         if (!seenFirstName) {
            if (!(token instanceof LinToken.Name)) {
               throw new NbtWriteException("Expected first token to be a name");
            }

            seenFirstName = true;
         }

         switch (token) {
            case LinToken.Name var8:
               LinToken.Name var111 = var8;

               try {
                  var112 = var111.name();
               } catch (Throwable var65) {
                  throw new MatchException(var65.toString(), var65);
               }

               String var66 = var112;
               var111 = var8;

               try {
                  var111.id();
               } catch (Throwable var64) {
                  throw new MatchException(var64.toString(), var64);
               }

               nextName = var66;
               break;
            case LinToken.ByteArrayStart(OptionalInt var67):
               writeIdAndNameIfNeeded(output, LinTagId.BYTE_ARRAY, nextName);
               nextName = null;
               output.writeInt(var67.orElseThrow());
               break;
            case LinToken.ByteArrayContent(ByteBuffer var68):
               byte[] copy = new byte[var68.remaining()];
               var68.get(copy);
               output.write(copy);
               break;
            case LinToken.ByteArrayEnd byteArrayEnd:
               break;
            case LinToken.Byte(byte var70):
               writeIdAndNameIfNeeded(output, LinTagId.BYTE, nextName);
               nextName = null;
               output.writeByte(var70);
               break;
            case LinToken.CompoundStart compoundStart:
               writeIdAndNameIfNeeded(output, LinTagId.COMPOUND, nextName);
               nextName = null;
               break;
            case LinToken.CompoundEnd compoundEnd:
               output.writeByte(LinTagId.END.id());
               break;
            case LinToken.Double(double var71):
               writeIdAndNameIfNeeded(output, LinTagId.DOUBLE, nextName);
               nextName = null;
               output.writeDouble(var71);
               break;
            case LinToken.Float(float var72):
               writeIdAndNameIfNeeded(output, LinTagId.FLOAT, nextName);
               nextName = null;
               output.writeFloat(var72);
               break;
            case LinToken.IntArrayStart(OptionalInt var73):
               writeIdAndNameIfNeeded(output, LinTagId.INT_ARRAY, nextName);
               nextName = null;
               output.writeInt(var73.orElseThrow());
               break;
            case LinToken.IntArrayContent(IntBuffer var74):
               label195: {
                  IntBuffer buffer = var74;

                  while (buffer.hasRemaining()) {
                     output.writeInt(buffer.get());
                  }
                  break label195;
               }
            case LinToken.IntArrayEnd intArrayEnd:
               break;
            case LinToken.Int(int var75):
               writeIdAndNameIfNeeded(output, LinTagId.INT, nextName);
               nextName = null;
               output.writeInt(var75);
               break;
            case LinToken.ListStart(OptionalInt var76, Optional var77):
               writeIdAndNameIfNeeded(output, LinTagId.LIST, nextName);
               nextName = null;
               output.writeByte(((LinTagId)var77.orElseThrow()).id());
               output.writeInt(var76.orElseThrow());
               break;
            case LinToken.ListEnd listEnd:
               break;
            case LinToken.LongArrayStart(OptionalInt var78):
               writeIdAndNameIfNeeded(output, LinTagId.LONG_ARRAY, nextName);
               nextName = null;
               output.writeInt(var78.orElseThrow());
               break;
            case LinToken.LongArrayContent(LongBuffer var79):
               label194: {
                  LongBuffer buffer = var79;

                  while (buffer.hasRemaining()) {
                     output.writeLong(buffer.get());
                  }
                  break label194;
               }
            case LinToken.LongArrayEnd longArrayEnd:
               break;
            case LinToken.Long(long var80):
               writeIdAndNameIfNeeded(output, LinTagId.LONG, nextName);
               nextName = null;
               output.writeLong(var80);
               break;
            case LinToken.Short(short var81):
               writeIdAndNameIfNeeded(output, LinTagId.SHORT, nextName);
               nextName = null;
               output.writeShort(var81);
               break;
            case LinToken.String(String var48):
               writeIdAndNameIfNeeded(output, LinTagId.STRING, nextName);
               nextName = null;
               output.writeUTF(var48);
               break;
            default:
               throw new NbtWriteException("Unknown token: " + token);
         }
      }
   }

   private static void writeIdAndNameIfNeeded(DataOutput output, LinTagId id, @Nullable String name) throws IOException {
      if (name != null) {
         output.writeByte(id.id());
         output.writeUTF(name);
      }
   }

   private LinBinaryIO() {
   }
}
