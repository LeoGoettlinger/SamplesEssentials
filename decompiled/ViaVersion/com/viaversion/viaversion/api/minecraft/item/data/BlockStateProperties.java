package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectOpenHashMap;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Map.Entry;

public record BlockStateProperties(Map<String, String> properties) implements Copyable {
   public static final Type<BlockStateProperties> TYPE = new Type<BlockStateProperties>(BlockStateProperties.class) {
      public BlockStateProperties read(ByteBuf buffer) {
         int size = Types.VAR_INT.readPrimitive(buffer);
         Map<String, String> properties = new Object2ObjectOpenHashMap<>();

         for (int i = 0; i < size; i++) {
            properties.put(Types.STRING.read(buffer), Types.STRING.read(buffer));
         }

         return new BlockStateProperties(properties);
      }

      public void write(ByteBuf buffer, BlockStateProperties value) {
         Types.VAR_INT.writePrimitive(buffer, value.properties.size());

         for (Entry<String, String> entry : value.properties.entrySet()) {
            Types.STRING.write(buffer, entry.getKey());
            Types.STRING.write(buffer, entry.getValue());
         }
      }

      public void write(Ops ops, BlockStateProperties value) {
         ops.writeMap(map -> {
            for (Entry<String, String> entry : value.properties.entrySet()) {
               map.write(Types.STRING, entry.getKey(), Types.STRING, entry.getValue());
            }
         });
      }
   };

   public BlockStateProperties copy() {
      return new BlockStateProperties(new Object2ObjectOpenHashMap<>(this.properties));
   }
}
