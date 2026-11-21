package de.theredend2000.advancedhunt.lib.cloud.paper;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import de.theredend2000.advancedhunt.lib.cloud.CommandTree;
import de.theredend2000.advancedhunt.lib.cloud.SenderMapper;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.BrigadierManagerHolder;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.CloudBrigadierCommand;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.CloudBrigadierManager;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.node.LiteralBrigadierNodeFactory;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.permission.BrigadierPermissionChecker;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitBrigadierMapper;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitHelper;
import de.theredend2000.advancedhunt.lib.cloud.internal.CommandNode;
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
