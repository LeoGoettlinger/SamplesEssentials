package net.luckperms.api.node.metadata.types;

import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.node.metadata.NodeMetadataKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface InheritanceOriginMetadata {
   NodeMetadataKey<InheritanceOriginMetadata> KEY = NodeMetadataKey.of("inheritanceorigin", InheritanceOriginMetadata.class);

   @NonNull
   PermissionHolder.Identifier getOrigin();

   @NonNull
   DataType getDataType();

   default boolean wasInherited(@NonNull PermissionHolder.Identifier holder) {
      return !holder.equals(this.getOrigin());
   }
}
