package com.viaversion.viaversion.api.data;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.IntConsumer;

public final class IdRanges {
   public static IntSet decode(ByteArrayTag rangesTag) {
      IntSet ids = new IntOpenHashSet();
      forEachId(rangesTag, ids::add);
      return ids;
   }

   public static IntList decodeAsList(ByteArrayTag rangesTag) {
      IntList ids = new IntArrayList();
      forEachId(rangesTag, ids::add);
      return ids;
   }

   public static void forEachId(ByteArrayTag rangesTag, IntConsumer consumer) {
      ByteBuf buf = Unpooled.wrappedBuffer(rangesTag.getValue());
      int prevEnd = 0;

      while (buf.isReadable()) {
         int start = prevEnd + Types.VAR_INT.readPrimitive(buf);
         int end = start + Types.VAR_INT.readPrimitive(buf);

         for (int id = start; id <= end; id++) {
            consumer.accept(id);
         }

         prevEnd = end + 1;
      }
   }
}
