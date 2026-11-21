package net.luckperms.api.node;

import net.luckperms.api.node.types.DisplayNameNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.RegexPermissionNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.node.types.WeightNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface NodeBuilderRegistry {
   @NonNull
   NodeBuilder<?, ?> forKey(String var1);

   @NonNull
   PermissionNode.Builder forPermission();

   @NonNull
   RegexPermissionNode.Builder forRegexPermission();

   @NonNull
   InheritanceNode.Builder forInheritance();

   @NonNull
   PrefixNode.Builder forPrefix();

   @NonNull
   SuffixNode.Builder forSuffix();

   @NonNull
   MetaNode.Builder forMeta();

   @NonNull
   WeightNode.Builder forWeight();

   @NonNull
   DisplayNameNode.Builder forDisplayName();
}
