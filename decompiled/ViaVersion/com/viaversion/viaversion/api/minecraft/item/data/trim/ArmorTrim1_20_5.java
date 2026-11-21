package com.viaversion.viaversion.api.minecraft.item.data.trim;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ArmorTrim1_20_5(Holder<ArmorTrimMaterial1_20_5> material, Holder<ArmorTrimPattern> pattern, boolean showInTooltip) implements Rewritable {
   public static final Type<ArmorTrim1_20_5> TYPE1_20_5 = new Type<ArmorTrim1_20_5>(ArmorTrim1_20_5.class) {
      public ArmorTrim1_20_5 read(ByteBuf buffer) {
         Holder<ArmorTrimMaterial1_20_5> material = ArmorTrimMaterial1_20_5.TYPE1_20_5.read(buffer);
         Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_20_5.read(buffer);
         boolean showInTooltip = buffer.readBoolean();
         return new ArmorTrim1_20_5(material, pattern, showInTooltip);
      }

      public void write(ByteBuf buffer, ArmorTrim1_20_5 value) {
         ArmorTrimMaterial1_20_5.TYPE1_20_5.write(buffer, value.material);
         ArmorTrimPattern.TYPE1_20_5.write(buffer, value.pattern);
         buffer.writeBoolean(value.showInTooltip);
      }
   };
   public static final Type<ArmorTrim1_20_5> TYPE1_21_2 = new Type<ArmorTrim1_20_5>(ArmorTrim1_20_5.class) {
      public ArmorTrim1_20_5 read(ByteBuf buffer) {
         Holder<ArmorTrimMaterial1_20_5> material = ArmorTrimMaterial1_20_5.TYPE1_21_2.read(buffer);
         Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_20_5.read(buffer);
         boolean showInTooltip = buffer.readBoolean();
         return new ArmorTrim1_20_5(material, pattern, showInTooltip);
      }

      public void write(ByteBuf buffer, ArmorTrim1_20_5 value) {
         ArmorTrimMaterial1_20_5.TYPE1_21_2.write(buffer, value.material);
         ArmorTrimPattern.TYPE1_20_5.write(buffer, value.pattern);
         buffer.writeBoolean(value.showInTooltip);
      }
   };
   public static final Type<ArmorTrim1_20_5> TYPE1_21_4 = new Type<ArmorTrim1_20_5>(ArmorTrim1_20_5.class) {
      public ArmorTrim1_20_5 read(ByteBuf buffer) {
         Holder<ArmorTrimMaterial1_20_5> material = ArmorTrimMaterial1_20_5.TYPE1_21_4.read(buffer);
         Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_20_5.read(buffer);
         boolean showInTooltip = buffer.readBoolean();
         return new ArmorTrim1_20_5(material, pattern, showInTooltip);
      }

      public void write(ByteBuf buffer, ArmorTrim1_20_5 value) {
         ArmorTrimMaterial1_20_5.TYPE1_21_4.write(buffer, value.material);
         ArmorTrimPattern.TYPE1_20_5.write(buffer, value.pattern);
         buffer.writeBoolean(value.showInTooltip);
      }
   };
   public static final Type<ArmorTrim1_20_5> TYPE1_21_5 = new Type<ArmorTrim1_20_5>(ArmorTrim1_20_5.class) {
      public ArmorTrim1_20_5 read(ByteBuf buffer) {
         Holder<ArmorTrimMaterial1_20_5> material = ArmorTrimMaterial1_20_5.TYPE1_21_5.read(buffer);
         Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_21_5.read(buffer);
         return new ArmorTrim1_20_5(material, pattern);
      }

      public void write(ByteBuf buffer, ArmorTrim1_20_5 value) {
         ArmorTrimMaterial1_20_5.TYPE1_21_5.write(buffer, value.material);
         ArmorTrimPattern.TYPE1_21_5.write(buffer, value.pattern);
      }

      public void write(Ops ops, ArmorTrim1_20_5 value) {
         ops.writeMap(
            map -> map.write("material", ArmorTrimMaterial1_20_5.TYPE1_21_5, value.material).write("pattern", ArmorTrimPattern.TYPE1_21_5, value.pattern)
         );
      }
   };

   public ArmorTrim1_20_5(Holder<ArmorTrimMaterial1_20_5> material, Holder<ArmorTrimPattern> pattern) {
      this(material, pattern, true);
   }

   public ArmorTrim1_20_5 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      Holder<ArmorTrimMaterial1_20_5> material = this.material;
      if (material.isDirect()) {
         material = Holder.of(material.value().rewrite(connection, protocol, clientbound));
      }

      Holder<ArmorTrimPattern> pattern = this.pattern;
      if (pattern.isDirect()) {
         pattern = Holder.of(pattern.value().rewrite(connection, protocol, clientbound));
      }

      return new ArmorTrim1_20_5(material, pattern, this.showInTooltip);
   }
}
