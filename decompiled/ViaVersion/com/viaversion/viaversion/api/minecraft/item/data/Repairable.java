package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.util.Rewritable;
import java.util.function.Function;

public record Repairable(HolderSet items) implements Rewritable {
   public static final Type<Repairable> TYPE = new TransformingType<HolderSet, Repairable>(
      Types.HOLDER_SET, Repairable.class, Repairable::new, Repairable::items
   ) {
      public void write(Ops ops, Repairable value) {
         ops.writeMap(map -> map.write("items", new HolderSetType(MappingData.MappingType.ITEM), value.items));
      }
   };

   public Repairable rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      HolderSet items = this.items.rewrite(Rewritable.itemRewriteFunction(protocol, clientbound));
      return this.items == items ? this : new Repairable(items);
   }
}
