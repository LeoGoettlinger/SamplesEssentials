package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class FilterableComponent extends Filterable<Tag> implements Copyable, Rewritable {
   public static final Type<FilterableComponent> TYPE = new Filterable.FilterableType<Tag, FilterableComponent>(
      Types.TEXT_COMPONENT_TAG, Types.OPTIONAL_TAG, FilterableComponent.class
   ) {
      protected FilterableComponent create(Tag raw, Tag filtered) {
         return new FilterableComponent(raw, filtered);
      }
   };
   public static final Type<FilterableComponent[]> ARRAY_TYPE = new ArrayType<>(TYPE);

   public FilterableComponent(Tag raw, @Nullable Tag filtered) {
      super(raw, filtered);
   }

   public FilterableComponent rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      if (protocol.getComponentRewriter() == null) {
         return this;
      } else {
         FilterableComponent copy = this.copy();
         protocol.getComponentRewriter().processTag(connection, copy.raw());
         protocol.getComponentRewriter().processTag(connection, copy.filtered());
         return copy;
      }
   }

   public FilterableComponent copy() {
      return new FilterableComponent(this.raw().copy(), this.filtered() != null ? this.filtered().copy() : null);
   }
}
