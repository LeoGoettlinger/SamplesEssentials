package su.nightexpress.nightcore.command.experimental;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;

@Deprecated
public interface ImprovedCommands extends NightCorePlugin {
   @Deprecated
   @NonNull
   default ServerCommand getRootCommand() {
      return this.getCommandManager().getRootCommand();
   }

   @Deprecated
   @NonNull
   default ChainedNode getRootNode() {
      return this.getCommandManager().getRootCommand().getNode();
   }
}
