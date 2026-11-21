package net.luckperms.api.metastacking;

import net.luckperms.api.node.ChatMetaType;
import net.luckperms.api.node.types.ChatMetaNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MetaStackElement {
   boolean shouldAccumulate(@NonNull ChatMetaType var1, @NonNull ChatMetaNode<?, ?> var2, @Nullable ChatMetaNode<?, ?> var3);
}
