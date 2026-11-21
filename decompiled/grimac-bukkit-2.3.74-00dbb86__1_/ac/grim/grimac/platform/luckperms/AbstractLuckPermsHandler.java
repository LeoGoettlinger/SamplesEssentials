package ac.grim.grimac.platform.luckperms;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.manager.init.stop.StoppableInitable;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.context.ContextUpdateEvent;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;

public abstract class AbstractLuckPermsHandler implements StoppableInitable {
   private List<EventSubscription<?>> subscriptions;
   private LuckPerms luckPerms;

   protected final void register(LuckPerms luckPerms) {
      this.luckPerms = luckPerms;
      this.subscriptions = new ArrayList<>();
      this.subscriptions.add(this.subscribe(luckPerms, NodeAddEvent.class, this::onNodeAdd));
      this.subscriptions.add(this.subscribe(luckPerms, NodeRemoveEvent.class, this::onNodeRemove));
      this.subscriptions.add(this.subscribe(luckPerms, UserDataRecalculateEvent.class, this::onUserDataRecalculate));
      this.subscriptions.add(this.subscribe(luckPerms, GroupDataRecalculateEvent.class, this::onGroupDataRecalculate));
      this.subscriptions.add(this.subscribe(luckPerms, GroupDeleteEvent.class, this::onGroupDelete));
      this.subscriptions.add(this.subscribe(luckPerms, ContextUpdateEvent.class, this::onContextUpdate));
      LogUtil.info("LuckPerms detected");
   }

   protected final boolean isRegistered() {
      return this.subscriptions != null;
   }

   protected abstract <T extends LuckPermsEvent> EventSubscription<T> subscribe(LuckPerms var1, Class<T> var2, Consumer<? super T> var3);

   protected abstract UUID contextSubjectUuid(ContextUpdateEvent var1);

   @Override
   public void stop() {
      if (this.subscriptions == null) {
         this.luckPerms = null;
      } else {
         for (EventSubscription<?> subscription : this.subscriptions) {
            subscription.close();
         }

         this.subscriptions = null;
         this.luckPerms = null;
      }
   }

   private void onNodeAdd(NodeAddEvent event) {
      this.onNodeMutate(event.getTarget(), event.getNode());
   }

   private void onNodeRemove(NodeRemoveEvent event) {
      this.onNodeMutate(event.getTarget(), event.getNode());
   }

   private void onNodeMutate(PermissionHolder target, Node node) {
      if (target instanceof User user) {
         if (this.shouldRefreshUserForNode(node)) {
            refreshGrimPlayerPermissions(user);
         }
      } else {
         if (target instanceof Group group && this.shouldRefreshGroupForNode(node)) {
            this.refreshPlayersInheritingGroup(group);
         }
      }
   }

   private void onUserDataRecalculate(UserDataRecalculateEvent event) {
      refreshGrimPlayerPermissions(event.getUser());
   }

   private void onGroupDataRecalculate(GroupDataRecalculateEvent event) {
      this.refreshPlayersInheritingGroup(event.getGroup());
   }

   private void onGroupDelete(GroupDeleteEvent event) {
      if (this.shouldRefreshHolderForNodes(event.getExistingData())) {
         refreshTrackedPlayers();
      }
   }

   private void onContextUpdate(ContextUpdateEvent event) {
      refreshGrimPlayerPermissions(this.contextSubjectUuid(event));
   }

   private boolean shouldRefreshUserForNode(Node node) {
      if (node instanceof PermissionNode permissionNode) {
         return isGrimPermission(permissionNode);
      } else {
         return node instanceof InheritanceNode inheritanceNode ? this.inheritedGroupMayAffectGrimPermissions(inheritanceNode) : false;
      }
   }

   private boolean shouldRefreshGroupForNode(Node node) {
      if (node instanceof PermissionNode permissionNode) {
         return isGrimPermission(permissionNode);
      } else {
         return node instanceof InheritanceNode inheritanceNode ? this.inheritedGroupMayAffectGrimPermissions(inheritanceNode) : false;
      }
   }

   private boolean shouldRefreshHolderForNodes(Iterable<? extends Node> nodes) {
      for (Node node : nodes) {
         if (this.shouldRefreshGroupForNode(node)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isGrimPermission(PermissionNode node) {
      String permission = node.getPermission().toLowerCase(Locale.ROOT);
      return permission.equals("*") || permission.equals("grim") || permission.startsWith("grim.");
   }

   private boolean inheritedGroupMayAffectGrimPermissions(InheritanceNode node) {
      if (this.luckPerms == null) {
         return true;
      } else {
         Group group = this.luckPerms.getGroupManager().getGroup(node.getGroupName());
         return group == null || holderHasGrimPermission(group, new LinkedHashSet<>());
      }
   }

   private static boolean holderHasGrimPermission(PermissionHolder holder, Set<String> seenGroups) {
      for (Node node : holder.getNodes()) {
         if (node instanceof PermissionNode permissionNode && isGrimPermission(permissionNode)) {
            return true;
         }
      }

      for (Group group : holder.getInheritedGroups(holder.getQueryOptions())) {
         if (seenGroups.add(normalizeGroupName(group)) && holderHasGrimPermission(group, seenGroups)) {
            return true;
         }
      }

      return false;
   }

   private void refreshPlayersInheritingGroup(Group group) {
      LuckPerms luckPerms = this.luckPerms;
      if (luckPerms != null) {
         String groupName = normalizeGroupName(group);
         Set<UUID> refreshed = new LinkedHashSet<>();

         for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            if (player != null && player.uuid != null) {
               User user = luckPerms.getUserManager().getUser(player.uuid);
               if (user != null && userInheritsGroup(user, groupName) && refreshed.add(player.uuid)) {
                  player.updatePermissions();
               }
            }
         }
      }
   }

   private static void refreshTrackedPlayers() {
      Set<UUID> refreshed = new LinkedHashSet<>();

      for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
         if (player != null && player.uuid != null && refreshed.add(player.uuid)) {
            player.updatePermissions();
         }
      }
   }

   private static boolean userInheritsGroup(User user, String groupName) {
      for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
         if (normalizeGroupName(group).equals(groupName)) {
            return true;
         }
      }

      return false;
   }

   private static String normalizeGroupName(Group group) {
      return group.getName().toLowerCase(Locale.ROOT);
   }

   private static void refreshGrimPlayerPermissions(UUID uuid) {
      if (uuid != null) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(uuid);
         if (player != null) {
            player.updatePermissions();
         }
      }
   }

   private static void refreshGrimPlayerPermissions(User user) {
      if (user != null) {
         refreshGrimPlayerPermissions(user.getUniqueId());
      }
   }
}
