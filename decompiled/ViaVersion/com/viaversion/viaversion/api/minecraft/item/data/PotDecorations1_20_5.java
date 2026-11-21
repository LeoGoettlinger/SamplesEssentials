package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import java.util.function.Function;

public record PotDecorations1_20_5(int[] itemIds) implements Copyable, Rewritable {
   private static final Key BRICK = Key.ofPath("brick");
   public static final Type<PotDecorations1_20_5> TYPE = new TransformingType<int[], PotDecorations1_20_5>(
      Types.VAR_INT_ARRAY_PRIMITIVE, PotDecorations1_20_5.class, PotDecorations1_20_5::new, PotDecorations1_20_5::itemIds
   ) {
      public void write(Ops ops, PotDecorations1_20_5 value) {
         ops.writeList(list -> {
            for (int i = 0; i < 4; i++) {
               list.write(Types.IDENTIFIER, i < value.itemIds.length ? ops.context().registryAccess().item(value.itemIds[i]) : PotDecorations1_20_5.BRICK);
            }
         });
      }
   };

   public PotDecorations1_20_5(int backItem, int leftItem, int rightItem, int frontItem) {
      this(new int[]{backItem, leftItem, rightItem, frontItem});
   }

   public int backItem() {
      return this.item(0);
   }

   public int leftItem() {
      return this.item(1);
   }

   public int rightItem() {
      return this.item(2);
   }

   public int frontItem() {
      return this.item(3);
   }

   private int item(int index) {
      return index >= 0 && index < this.itemIds.length ? this.itemIds[index] : -1;
   }

   public PotDecorations1_20_5 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      int[] newItems = new int[this.itemIds.length];

      for (int i = 0; i < this.itemIds.length; i++) {
         newItems[i] = Rewritable.rewriteItem(protocol, clientbound, this.itemIds[i]);
      }

      return new PotDecorations1_20_5(newItems);
   }

   public PotDecorations1_20_5 copy() {
      return new PotDecorations1_20_5(Copyable.copy(this.itemIds));
   }
}
