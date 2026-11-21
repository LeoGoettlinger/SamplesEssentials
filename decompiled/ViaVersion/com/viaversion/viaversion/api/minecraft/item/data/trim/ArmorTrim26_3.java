package com.viaversion.viaversion.api.minecraft.item.data.trim;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ArmorTrim26_3(Holder<ArmorTrimMaterial26_3> material, Holder<ArmorTrimPattern> pattern) implements Rewritable {
   public static final Type<ArmorTrim26_3> TYPE = new Type<ArmorTrim26_3>(ArmorTrim26_3.class) {
      public ArmorTrim26_3 read(ByteBuf buffer) {
         Holder<ArmorTrimMaterial26_3> material = ArmorTrimMaterial26_3.TYPE.read(buffer);
         Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_21_5.read(buffer);
         return new ArmorTrim26_3(material, pattern);
      }

      public void write(ByteBuf buffer, ArmorTrim26_3 value) {
         ArmorTrimMaterial26_3.TYPE.write(buffer, value.material);
         ArmorTrimPattern.TYPE1_21_5.write(buffer, value.pattern);
      }

      public void write(Ops ops, ArmorTrim26_3 value) {
         ops.writeMap(map -> map.write("material", ArmorTrimMaterial26_3.TYPE, value.material).write("pattern", ArmorTrimPattern.TYPE1_21_5, value.pattern));
      }
   };

   public ArmorTrim26_3 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      Holder<ArmorTrimPattern> pattern = this.pattern;
      if (pattern.isDirect()) {
         pattern = Holder.of(pattern.value().rewrite(connection, protocol, clientbound));
      }

      return new ArmorTrim26_3(this.material, pattern);
   }
}
