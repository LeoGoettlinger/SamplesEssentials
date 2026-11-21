package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class StringType extends Type<String> {
   private static final int MAX_CHAR_UTF_8_LENGTH = Character.toString('\uffff').getBytes(StandardCharsets.UTF_8).length;
   private final int maxLength;
   private final int maxByteLength;

   public StringType() {
      this(32767);
   }

   public StringType(int maxLength) {
      super(String.class);
      this.maxLength = maxLength;
      this.maxByteLength = maxLength * MAX_CHAR_UTF_8_LENGTH;
   }

   public String read(ByteBuf buffer) {
      int len = Types.VAR_INT.readPrimitive(buffer);
      if (len > this.maxByteLength) {
         throw new IllegalArgumentException(
            "Cannot receive string longer than " + this.maxLength + " * " + MAX_CHAR_UTF_8_LENGTH + " bytes (got " + len + " bytes)"
         );
      } else {
         String string = buffer.toString(buffer.readerIndex(), len, StandardCharsets.UTF_8);
         buffer.skipBytes(len);
         if (string.length() > this.maxLength) {
            throw new IllegalArgumentException("Cannot receive string longer than " + this.maxLength + " characters (got " + string.length() + ")");
         } else {
            return string;
         }
      }
   }

   public void write(ByteBuf buffer, String object) {
      if (object.length() > this.maxLength) {
         throw new IllegalArgumentException("Cannot send string longer than " + this.maxLength + " characters (got " + object.length() + " characters)");
      } else {
         byte[] b = object.getBytes(StandardCharsets.UTF_8);
         Types.VAR_INT.writePrimitive(buffer, b.length);
         buffer.writeBytes(b);
      }
   }

   public void write(Ops ops, String value) {
      ops.writeString(value);
   }

   public static final class OptionalStringType extends OptionalType<String> {
      public OptionalStringType() {
         super(Types.STRING);
      }
   }
}
