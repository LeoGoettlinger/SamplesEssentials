package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.BitSet;

public class BitSetType extends Type<BitSet> {
   private final int length;
   private final int bytesLength;

   public BitSetType(int length) {
      super(BitSet.class);
      this.length = length;
      this.bytesLength = (int)Math.ceil(length / 8.0);
   }

   public BitSetType() {
      super(BitSet.class);
      this.length = -1;
      this.bytesLength = -1;
   }

   public BitSet read(ByteBuf buffer) {
      if (this.length != -1) {
         byte[] bytes = new byte[this.bytesLength];
         buffer.readBytes(bytes);
         return BitSet.valueOf(bytes);
      } else {
         return BitSet.valueOf(Types.BYTE_ARRAY_PRIMITIVE.read(buffer));
      }
   }

   public void write(ByteBuf buffer, BitSet object) {
      if (this.length != -1) {
         Preconditions.checkArgument(
            object.length() <= this.length, "BitSet of length %s larger than max length %s", new Object[]{object.length(), this.length}
         );
         byte[] bytes = object.toByteArray();
         buffer.writeBytes(bytes.length == this.bytesLength ? bytes : Arrays.copyOf(bytes, this.bytesLength));
      } else {
         Types.BYTE_ARRAY_PRIMITIVE.write(buffer, object.toByteArray());
      }
   }
}
