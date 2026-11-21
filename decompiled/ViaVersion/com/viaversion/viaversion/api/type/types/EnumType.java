package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.MathUtil;

public final class EnumType extends VarIntType {
   private final String[] names;
   private final EnumType.Fallback fallback;

   public EnumType(String... names) {
      this(EnumType.Fallback.ZERO, names);
   }

   public EnumType(EnumType.Fallback fallback, String... names) {
      this.names = names;
      this.fallback = fallback;
   }

   @Override
   public void write(Ops ops, Integer value) {
      Types.STRING.write(ops, this.nameFromId(value));
   }

   public String[] names() {
      return this.names;
   }

   public String nameFromId(int id) {
      if (id >= 0 && id < this.names.length) {
         return this.names[id];
      } else {
         return switch (this.fallback) {
            case ZERO -> this.names[0];
            case WRAP -> this.names[Math.floorMod(id, this.names.length)];
            case CLAMP -> this.names[MathUtil.clamp(id, 0, this.names.length - 1)];
         };
      }
   }

   public int idFromName(String name) {
      for (int i = 0; i < this.names.length; i++) {
         if (this.names[i].equals(name)) {
            return i;
         }
      }
      return switch (this.fallback) {
         case ZERO -> 0;
         case WRAP -> Math.floorMod(name.hashCode(), this.names.length);
         case CLAMP -> this.names.length - 1;
      };
   }

   public final class EnumArrayType extends VarIntArrayType {
      public void write(Ops ops, int[] value) {
         ops.writeList(list -> {
            for (int ordinal : value) {
               list.write(Types.STRING, EnumType.this.nameFromId(ordinal));
            }
         });
      }
   }

   public static enum Fallback {
      ZERO,
      WRAP,
      CLAMP;
   }
}
