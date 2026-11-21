package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntLinkedOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record TooltipDisplay(boolean hideTooltip, IntSortedSet hiddenComponents) implements Rewritable {
   public static final Type<TooltipDisplay> TYPE = new Type<TooltipDisplay>(TooltipDisplay.class) {
      public TooltipDisplay read(ByteBuf buffer) {
         boolean hideTooltip = buffer.readBoolean();
         IntSortedSet hiddenComponents = new IntLinkedOpenHashSet();
         int size = Types.VAR_INT.readPrimitive(buffer);

         for (int i = 0; i < size; i++) {
            hiddenComponents.add(Types.VAR_INT.readPrimitive(buffer));
         }

         return new TooltipDisplay(hideTooltip, hiddenComponents);
      }

      public void write(ByteBuf buffer, TooltipDisplay value) {
         buffer.writeBoolean(value.hideTooltip());
         Types.VAR_INT.writePrimitive(buffer, value.hiddenComponents().size());

         for (int hiddenComponent : value.hiddenComponents()) {
            Types.VAR_INT.writePrimitive(buffer, hiddenComponent);
         }
      }

      public void write(Ops ops, TooltipDisplay value) {
         Key[] hiddenComponents = value.hiddenComponents.intStream().mapToObj(id -> ops.context().registryAccess().dataComponentType(id)).toArray(Key[]::new);
         ops.writeMap(
            map -> map.writeOptional("hide_tooltip", Types.BOOLEAN, value.hideTooltip, false)
               .writeOptional("hidden_components", Types.IDENTIFIER_ARRAY, hiddenComponents, new Key[0])
         );
      }
   };

   public TooltipDisplay rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      if (this.hiddenComponents.isEmpty()) {
         return this;
      } else {
         IntSortedSet newHiddenComponents = new IntLinkedOpenHashSet();

         for (int hiddenComponent : this.hiddenComponents) {
            int mappedId = Rewritable.rewriteDataComponentType(protocol, clientbound, hiddenComponent);
            if (mappedId != -1) {
               newHiddenComponents.add(mappedId);
            }
         }

         return new TooltipDisplay(this.hideTooltip, newHiddenComponents);
      }
   }
}
