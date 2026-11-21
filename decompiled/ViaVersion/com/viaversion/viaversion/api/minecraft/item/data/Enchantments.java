package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public record Enchantments(Int2IntMap enchantments, boolean showInTooltip) implements Copyable {
   public static final Type<Enchantments> TYPE1_20_5 = new Type<Enchantments>(Enchantments.class) {
      public Enchantments read(ByteBuf buffer) {
         Int2IntMap enchantments = new Int2IntOpenHashMap();
         int size = Types.VAR_INT.readPrimitive(buffer);

         for (int i = 0; i < size; i++) {
            int id = Types.VAR_INT.readPrimitive(buffer);
            int level = Types.VAR_INT.readPrimitive(buffer);
            enchantments.put(id, level);
         }

         return new Enchantments(enchantments, buffer.readBoolean());
      }

      public void write(ByteBuf buffer, Enchantments value) {
         Types.VAR_INT.writePrimitive(buffer, value.enchantments.size());

         for (Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
            Types.VAR_INT.writePrimitive(buffer, entry.getIntKey());
            Types.VAR_INT.writePrimitive(buffer, entry.getIntValue());
         }

         buffer.writeBoolean(value.showInTooltip());
      }
   };
   public static final Type<Enchantments> TYPE1_21_5 = new Type<Enchantments>(Enchantments.class) {
      public Enchantments read(ByteBuf buffer) {
         Int2IntMap enchantments = new Int2IntOpenHashMap();
         int size = Types.VAR_INT.readPrimitive(buffer);

         for (int i = 0; i < size; i++) {
            int id = Types.VAR_INT.readPrimitive(buffer);
            int level = Types.VAR_INT.readPrimitive(buffer);
            enchantments.put(id, level);
         }

         return new Enchantments(enchantments);
      }

      public void write(ByteBuf buffer, Enchantments value) {
         Types.VAR_INT.writePrimitive(buffer, value.enchantments.size());

         for (Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
            Types.VAR_INT.writePrimitive(buffer, entry.getIntKey());
            Types.VAR_INT.writePrimitive(buffer, entry.getIntValue());
         }
      }

      public void write(Ops ops, Enchantments value) {
         ops.writeMap(map -> {
            for (Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
               Key key = ops.context().registryAccess().registryKey("enchantment", entry.getIntKey());
               map.write(Types.IDENTIFIER, key, Types.VAR_INT, entry.getIntValue());
            }
         });
      }
   };

   public Enchantments(Int2IntMap enchantments) {
      this(enchantments, true);
   }

   public Enchantments(boolean showInTooltip) {
      this(new Int2IntOpenHashMap(), showInTooltip);
   }

   public int size() {
      return this.enchantments.size();
   }

   public void add(int id, int level) {
      this.enchantments.put(id, level);
   }

   public void remove(int id) {
      this.enchantments.remove(id);
   }

   public void clear() {
      this.enchantments.clear();
   }

   public int getLevel(int id) {
      return this.enchantments.getOrDefault(id, -1);
   }

   public Enchantments copy() {
      return new Enchantments(new Int2IntOpenHashMap(this.enchantments), this.showInTooltip);
   }
}
