package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record WritableBook(FilterableString[] pages) implements Copyable {
   public static final Type<WritableBook> TYPE = new Type<WritableBook>(WritableBook.class) {
      public WritableBook read(ByteBuf buffer) {
         return new WritableBook(FilterableString.ARRAY_TYPE.read(buffer));
      }

      public void write(ByteBuf buffer, WritableBook value) {
         FilterableString.ARRAY_TYPE.write(buffer, value.pages);
      }

      public void write(Ops ops, WritableBook writableBook) {
         ops.writeMap(map -> map.writeOptional("pages", FilterableString.ARRAY_TYPE, writableBook.pages, new FilterableString[0]));
      }
   };

   public WritableBook copy() {
      return new WritableBook(Copyable.copy(this.pages));
   }
}
