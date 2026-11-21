package com.viaversion.viaversion.api.type.types.misc;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.VarIntArrayType;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class HolderSetType extends Type<HolderSet> {
   private final RegistryKey registryKey;

   public HolderSetType() {
      this(null);
   }

   public HolderSetType(RegistryKey registryKey) {
      super(HolderSet.class);
      this.registryKey = registryKey;
   }

   public HolderSet read(ByteBuf buffer) {
      int size = Types.VAR_INT.readPrimitive(buffer) - 1;
      if (size == -1) {
         String tag = Types.STRING.read(buffer);
         return HolderSet.of(tag);
      } else {
         return HolderSet.of(new VarIntArrayType(size).read(buffer));
      }
   }

   public void write(ByteBuf buffer, HolderSet object) {
      if (object.hasTagKey()) {
         Types.VAR_INT.writePrimitive(buffer, 0);
         Types.STRING.write(buffer, object.tagKey());
      } else {
         int[] values = object.ids();
         Types.VAR_INT.writePrimitive(buffer, values.length + 1);

         for (int value : values) {
            Types.VAR_INT.writePrimitive(buffer, value);
         }
      }
   }

   public void write(Ops ops, HolderSet value) {
      if (value.hasTagKey()) {
         ops.write(Types.TAG_KEY, Key.of(value.tagKey()));
      } else {
         Preconditions.checkArgument(this.registryKey != null, "Cannot write HolderSet with direct ids without a mapping type");
         if (value.ids().length == 1) {
            ops.write(Types.IDENTIFIER, this.key(ops, value.ids()[0]));
            return;
         }

         ops.writeList(list -> {
            for (int id : value.ids()) {
               list.write(Types.IDENTIFIER, this.key(ops, id));
            }
         });
      }
   }

   private Key key(Ops ops, int id) {
      if (this.registryKey instanceof MappingData.MappingType mappingType) {
         return ops.context().registryAccess().key(mappingType, id);
      } else {
         return this.registryKey instanceof RegistryValueType registryValueType
            ? registryValueType.byId(id)
            : ops.context().registryAccess().registryKey(this.registryKey.registry().toString(), id);
      }
   }

   public static final class OptionalHolderSetType extends OptionalType<HolderSet> {
      public OptionalHolderSetType() {
         super(Types.HOLDER_SET);
      }
   }
}
