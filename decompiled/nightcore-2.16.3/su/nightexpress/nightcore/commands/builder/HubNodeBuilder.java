package su.nightexpress.nightcore.commands.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.tree.ExecutableNode;
import su.nightexpress.nightcore.commands.tree.HubNode;
import su.nightexpress.nightcore.locale.entry.TextLocale;

public class HubNodeBuilder extends ExecutableNodeBuilder<HubNode, HubNodeBuilder> {
   private final List<ExecutableNode> branches = new ArrayList<>();
   private String localizedName;
   private boolean useHelpCommand = true;

   public HubNodeBuilder(@NonNull String name) {
      super(name);
   }

   @NonNull
   protected HubNodeBuilder getThis() {
      return this;
   }

   @NonNull
   public HubNode build() {
      HubNode result = new HubNode(this.name, this.description, this.permission, this.requirements, this.executor, this.localizedName, this.useHelpCommand);
      this.branches.forEach(result::addBranch);
      return result;
   }

   @NonNull
   public HubNodeBuilder branch(ExecutableNodeBuilder<?, ?>... branches) {
      for (ExecutableNodeBuilder<?, ?> builder : branches) {
         this.branches.add(builder.build());
      }

      return this.getThis();
   }

   @NonNull
   public HubNodeBuilder branch(ExecutableNode... branches) {
      this.branches.addAll(Arrays.asList(branches));
      return this.getThis();
   }

   @NonNull
   public HubNodeBuilder localized(@NonNull TextLocale locale) {
      return this.localized(locale.text());
   }

   @NonNull
   public HubNodeBuilder localized(@NonNull String localizedName) {
      this.localizedName = localizedName;
      return this.getThis();
   }

   @NonNull
   public HubNodeBuilder withHelpCommand(boolean useHelpCommand) {
      this.useHelpCommand = useHelpCommand;
      return this.getThis();
   }
}
