package com.viaversion.viaversion.api.minecraft.item.data.trim;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.EitherHolderType;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ProvidesTrimMaterial1_20_5(EitherHolder<ArmorTrimMaterial1_20_5> material) implements Rewritable {
   public static final Type<ProvidesTrimMaterial1_20_5> TYPE = new Type<ProvidesTrimMaterial1_20_5>(ProvidesTrimMaterial1_20_5.class) {
      public ProvidesTrimMaterial1_20_5 read(ByteBuf buffer) {
         EitherHolder<ArmorTrimMaterial1_20_5> position = EitherHolderType.read(buffer, ArmorTrimMaterial1_20_5.TYPE1_21_5);
         return new ProvidesTrimMaterial1_20_5(position);
      }

      public void write(ByteBuf buffer, ProvidesTrimMaterial1_20_5 value) {
         EitherHolderType.write(buffer, value.material, ArmorTrimMaterial1_20_5.TYPE1_21_5);
      }

      public void write(Ops ops, ProvidesTrimMaterial1_20_5 value) {
         EitherHolderType.write(ops, value.material, ArmorTrimMaterial1_20_5.TYPE1_21_5);
      }
   };

   public ProvidesTrimMaterial1_20_5 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      if (!this.material.hasKey() && !this.material.holder().hasId()) {
         ArmorTrimMaterial1_20_5 trimMaterial = this.material.holder().value();
         return new ProvidesTrimMaterial1_20_5(EitherHolder.of(Holder.of(trimMaterial.rewrite(connection, protocol, clientbound))));
      } else {
         return this;
      }
   }
}
