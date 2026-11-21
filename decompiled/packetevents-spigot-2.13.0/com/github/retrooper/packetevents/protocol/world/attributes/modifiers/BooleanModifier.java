package com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum BooleanModifier implements AttributeModifier<Boolean, Boolean> {
   AND {
      public Boolean apply(Boolean value, Boolean arg) {
         return arg && value;
      }
   },
   NAND {
      public Boolean apply(Boolean value, Boolean arg) {
         return !arg || !value;
      }
   },
   OR {
      public Boolean apply(Boolean value, Boolean arg) {
         return arg || value;
      }
   },
   NOR {
      public Boolean apply(Boolean value, Boolean arg) {
         return !arg && !value;
      }
   },
   XOR {
      public Boolean apply(Boolean value, Boolean arg) {
         return arg ^ value;
      }
   },
   XNOR {
      public Boolean apply(Boolean value, Boolean arg) {
         return arg == value;
      }
   };

   private BooleanModifier() {
   }

   @Override
   public NbtCodec<Boolean> argumentCodec(EnvironmentAttribute<Boolean> attribute) {
      return NbtCodecs.BOOLEAN;
   }
}
