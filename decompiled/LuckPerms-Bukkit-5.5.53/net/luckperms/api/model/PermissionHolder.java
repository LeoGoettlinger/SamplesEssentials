package net.luckperms.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface PermissionHolder {
   @NonNull
   PermissionHolder.Identifier getIdentifier();

   @NonNull
   String getFriendlyName();

   @NonNull
   QueryOptions getQueryOptions();

   @NonNull
   CachedDataManager getCachedData();

   @NonNull
   NodeMap getData(@NonNull DataType var1);

   @NonNull
   NodeMap data();

   @NonNull
   NodeMap transientData();

   @NonNull
   @Unmodifiable
   default Collection<Node> getNodes() {
      List<Node> nodes = new ArrayList<>();
      nodes.addAll(this.data().toCollection());
      nodes.addAll(this.transientData().toCollection());
      return nodes;
   }

   @NonNull
   @Unmodifiable
   default <T extends Node> Collection<T> getNodes(@NonNull NodeType<T> type) {
      return this.getNodes().stream().filter(type::matches).map(type::cast).collect(Collectors.toList());
   }

   @NonNull
   @Unmodifiable
   SortedSet<Node> getDistinctNodes();

   @NonNull
   @Unmodifiable
   Collection<Node> resolveInheritedNodes(@NonNull QueryOptions var1);

   @NonNull
   @Unmodifiable
   default <T extends Node> Collection<T> resolveInheritedNodes(@NonNull NodeType<T> type, @NonNull QueryOptions queryOptions) {
      return this.resolveInheritedNodes(queryOptions).stream().filter(type::matches).map(type::cast).collect(Collectors.toList());
   }

   @NonNull
   @Unmodifiable
   SortedSet<Node> resolveDistinctInheritedNodes(@NonNull QueryOptions var1);

   @NonNull
   @Unmodifiable
   Collection<Group> getInheritedGroups(@NonNull QueryOptions var1);

   void auditTemporaryNodes();

   public interface Identifier {
      String USER_TYPE = "user";
      String GROUP_TYPE = "group";

      @NonNull
      String getName();

      @NonNull
      String getType();
   }
}
