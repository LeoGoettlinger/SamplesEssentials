package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record SignText(Tag[] messages, @Nullable Tag[] filteredMessages, int color, boolean hasGlowingText) {
   public static final Type<SignText> TYPE = new Type<SignText>(SignText.class) {
      public SignText read(ByteBuf buffer) {
         Tag[] messages = Types.SIGN_TEXT.read(buffer);
         Tag[] filteredMessages = Types.OPTIONAL_SIGN_TEXT.read(buffer);
         int color = Types.VAR_INT.readPrimitive(buffer);
         boolean hasGlowingText = buffer.readBoolean();
         return new SignText(messages, filteredMessages, color, hasGlowingText);
      }

      public void write(ByteBuf buffer, SignText value) {
         Types.SIGN_TEXT.write(buffer, value.messages);
         Types.OPTIONAL_SIGN_TEXT.write(buffer, value.filteredMessages);
         Types.VAR_INT.writePrimitive(buffer, value.color);
         Types.BOOLEAN.write(buffer, value.hasGlowingText);
      }

      public void write(Ops ops, SignText value) {
         ops.writeMap(
            map -> map.write("messages", Types.TEXT_COMPONENT_TAG_ARRAY, value.messages)
               .writeOptional("filtered_messages", Types.TEXT_COMPONENT_TAG_ARRAY, value.filteredMessages)
               .write("color", EnumTypes.DYE_COLOR, value.color)
               .write("has_glowing_text", Types.BOOLEAN, value.hasGlowingText)
         );
      }
   };
}
