package de.theredend2000.advancedhunt.lib.cloud.internal;

import de.theredend2000.advancedhunt.lib.cloud.Command;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.key.CloudKey;
import de.theredend2000.advancedhunt.lib.cloud.key.SimpleMutableCloudKeyContainer;
import de.theredend2000.advancedhunt.lib.cloud.permission.Permission;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
public final class CommandNode<C> {
   public static final CloudKey<Set<Type>> META_KEY_SENDER_TYPES = CloudKey.cloudKey("senderTypes", new TypeToken<Set<Type>>() {});
   public static final CloudKey<Map<Type, Permission>> META_KEY_ACCESS = CloudKey.cloudKey("access", new TypeToken<Map<Type, Permission>>() {});
   private final SimpleMutableCloudKeyContainer nodeMeta = new SimpleMutableCloudKeyContainer(new HashMap<>());
   private final List<CommandNode<C>> children = new LinkedList<>();
   private final CommandComponent<C> component;
   private CommandNode<C> parent;
   private Command<C> command;

   public CommandNode(@Nullable final CommandComponent<C> component) {
      this.component = component;
   }

   @NonNull
   public List<CommandNode<C>> children() {
      return Collections.unmodifiableList(this.children);
   }

   @NonNull
   public CommandNode<C> addChild(@NonNull final CommandComponent<C> component) {
      CommandNode<C> node = new CommandNode<>(component);
      this.children.add(node);
      return node;
   }

   @Nullable
   public CommandNode<C> getChild(@NonNull final CommandComponent<C> component) {
      for (CommandNode<C> child : this.children) {
         if (component.equals(child.component())) {
            return child;
         }
      }

      return null;
   }

   public boolean removeChild(@NonNull final CommandNode<C> child) {
      return this.children.remove(child);
   }

   public boolean isLeaf() {
      return this.children.isEmpty();
   }

   @NonNull
   public SimpleMutableCloudKeyContainer nodeMeta() {
      return this.nodeMeta;
   }

   @MonotonicNonNull
   public CommandComponent<C> component() {
      return this.component;
   }

   @MonotonicNonNull
   public Command<C> command() {
      return this.command;
   }

   public void command(@NonNull final Command<C> command) {
      if (this.command != null) {
         throw new IllegalStateException("Cannot replace owning command");
      } else {
         this.command = command;
      }
   }

   @Nullable
   public CommandNode<C> parent() {
      return this.parent;
   }

   public void parent(@Nullable final CommandNode<C> parent) {
      this.parent = parent;
   }

   public void sortChildren() {
      this.children.sort(Comparator.comparing(CommandNode::component));
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommandNode<?> node = (CommandNode<?>)o;
         return Objects.equals(this.component(), node.component());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.component());
   }

   @Override
   public String toString() {
      return "Node{value=" + this.component + '}';
   }
}
