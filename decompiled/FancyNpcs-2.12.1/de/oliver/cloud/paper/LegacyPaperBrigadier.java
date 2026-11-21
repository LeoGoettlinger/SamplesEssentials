package de.oliver.cloud.paper;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import de.oliver.cloud.CommandTree;
import de.oliver.cloud.SenderMapper;
import de.oliver.cloud.brigadier.BrigadierManagerHolder;
import de.oliver.cloud.brigadier.CloudBrigadierCommand;
import de.oliver.cloud.brigadier.CloudBrigadierManager;
import de.oliver.cloud.brigadier.node.LiteralBrigadierNodeFactory;
import de.oliver.cloud.brigadier.permission.BrigadierPermissionChecker;
import de.oliver.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import de.oliver.cloud.bukkit.internal.BukkitBrigadierMapper;
import de.oliver.cloud.bukkit.internal.BukkitHelper;
import de.oliver.cloud.internal.CommandNode;
import java.util.regex.Pattern;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

class LegacyPaperBrigadier<C> implements Listener, BrigadierManagerHolder<C, BukkitBrigadierCommandSource> {
   private final CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager;
   private final LegacyPaperCommandManager<C> paperCommandManager;

   LegacyPaperBrigadier(@NonNull final LegacyPaperCommandManager<C> paperCommandManager) {
      this.paperCommandManager = paperCommandManager;
      this.brigadierManager = new CloudBrigadierManager<>(
         this.paperCommandManager,
         SenderMapper.create(
            sender -> this.paperCommandManager.senderMapper().map(sender.getBukkitSender()),
            new BukkitBackwardsBrigadierSenderMapper<>(this.paperCommandManager.senderMapper())
         )
      );
      BukkitBrigadierMapper<C> mapper = new BukkitBrigadierMapper<>(this.paperCommandManager.owningPlugin().getLogger(), this.brigadierManager);
      mapper.registerBuiltInMappings();
      PaperBrigadierMappings.register(mapper);
   }

   @Override
   public final boolean hasBrigadierManager() {
      return true;
   }

   @NonNull
   @Override
   public final CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager() {
      return this.brigadierManager;
   }

   @EventHandler
   public void onCommandRegister(@NonNull final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
      if (event.getCommand() instanceof PluginIdentifiableCommand) {
         if (((PluginIdentifiableCommand)event.getCommand()).getPlugin().equals(this.paperCommandManager.owningPlugin())) {
            CommandTree<C> commandTree = this.paperCommandManager.commandTree();
            String label;
            if (event.getCommandLabel().contains(":")) {
               label = event.getCommandLabel().split(Pattern.quote(":"))[1];
            } else {
               label = event.getCommandLabel();
            }

            CommandNode<C> node = commandTree.getNamedNode(label);
            if (node != null) {
               BrigadierPermissionChecker<C> permissionChecker = (sender, permission) -> commandTree.getNamedNode(label) == null
                  ? false
                  : this.paperCommandManager.testPermission(sender, permission).allowed();
               LiteralBrigadierNodeFactory<C, BukkitBrigadierCommandSource> literalFactory = this.brigadierManager.literalBrigadierNodeFactory();
               event.setLiteral(
                  literalFactory.createNode(
                     event.getLiteral().getLiteral(),
                     node,
                     new CloudBrigadierCommand<>(
                        this.paperCommandManager, this.brigadierManager, command -> BukkitHelper.stripNamespace(this.paperCommandManager, command)
                     ),
                     permissionChecker
                  )
               );
            }
         }
      }
   }
}
