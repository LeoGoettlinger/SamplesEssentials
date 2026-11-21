package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;

public record ItemModel(Key key) implements Rewritable {
   public static final Type<ItemModel> TYPE = TransformingType.of(Types.IDENTIFIER, ItemModel.class, ItemModel::new, ItemModel::key);

   public ItemModel rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      String type = this.key().toString();
      String mappedType = Rewritable.rewriteItem(protocol, clientbound, type);
      return mappedType != null && !type.equals(mappedType) ? new ItemModel(Key.of(mappedType)) : this;
   }
}
