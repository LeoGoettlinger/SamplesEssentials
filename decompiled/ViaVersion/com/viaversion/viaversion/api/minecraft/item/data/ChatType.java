package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record ChatType(ChatType.ChatTypeDecoration chatDecoration, ChatType.ChatTypeDecoration narrationDecoration) implements Copyable {
   public static final HolderType<ChatType> TYPE = new HolderType<ChatType>() {
      public ChatType readDirect(ByteBuf buffer) {
         ChatType.ChatTypeDecoration chatDecoration = ChatType.ChatTypeDecoration.TYPE.read(buffer);
         ChatType.ChatTypeDecoration narrationDecoration = ChatType.ChatTypeDecoration.TYPE.read(buffer);
         return new ChatType(chatDecoration, narrationDecoration);
      }

      public void writeDirect(ByteBuf buffer, ChatType value) {
         ChatType.ChatTypeDecoration.TYPE.write(buffer, value.chatDecoration());
         ChatType.ChatTypeDecoration.TYPE.write(buffer, value.narrationDecoration());
      }
   };

   public ChatType copy() {
      return new ChatType(this.chatDecoration.copy(), this.narrationDecoration.copy());
   }

   public record ChatTypeDecoration(String translationKey, int[] parameters, Tag style) implements Copyable {
      public static final Type<ChatType.ChatTypeDecoration> TYPE = new Type<ChatType.ChatTypeDecoration>(ChatType.ChatTypeDecoration.class) {
         public ChatType.ChatTypeDecoration read(ByteBuf buffer) {
            String translationKey = Types.STRING.read(buffer);
            int[] parameters = Types.INT_ARRAY_PRIMITIVE.read(buffer);
            Tag style = Types.TRUSTED_TAG.read(buffer);
            return new ChatType.ChatTypeDecoration(translationKey, parameters, style);
         }

         public void write(ByteBuf buffer, ChatType.ChatTypeDecoration value) {
            Types.STRING.write(buffer, value.translationKey());
            Types.INT_ARRAY_PRIMITIVE.write(buffer, value.parameters());
            Types.TRUSTED_TAG.write(buffer, value.style());
         }
      };

      public ChatType.ChatTypeDecoration copy() {
         return new ChatType.ChatTypeDecoration(this.translationKey, (int[])this.parameters.clone(), this.style.copy());
      }
   }
}
