package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record WrittenBook(FilterableString title, String author, int generation, FilterableComponent[] pages, boolean resolved) implements Copyable {
   public static final Type<WrittenBook> TYPE = new Type<WrittenBook>(WrittenBook.class) {
      public WrittenBook read(ByteBuf buffer) {
         FilterableString title = FilterableString.TYPE.read(buffer);
         String author = Types.STRING.read(buffer);
         int generation = Types.VAR_INT.readPrimitive(buffer);
         FilterableComponent[] pages = FilterableComponent.ARRAY_TYPE.read(buffer);
         boolean resolved = buffer.readBoolean();
         return new WrittenBook(title, author, generation, pages, resolved);
      }

      public void write(ByteBuf buffer, WrittenBook value) {
         FilterableString.TYPE.write(buffer, value.title);
         Types.STRING.write(buffer, value.author);
         Types.VAR_INT.writePrimitive(buffer, value.generation);
         FilterableComponent.ARRAY_TYPE.write(buffer, value.pages);
         buffer.writeBoolean(value.resolved);
      }

      public void write(Ops ops, WrittenBook value) {
         ops.writeMap(
            map -> map.write("title", FilterableString.TYPE, value.title)
               .write("author", Types.STRING, value.author)
               .writeOptional("generation", Types.INT, value.generation, 0)
               .writeOptional("pages", FilterableComponent.ARRAY_TYPE, value.pages, new FilterableComponent[0])
               .writeOptional("resolved", Types.BOOLEAN, value.resolved, false)
         );
      }
   };

   public WrittenBook copy() {
      return new WrittenBook(this.title, this.author, this.generation, Copyable.copy(this.pages), this.resolved);
   }
}
