package de.theredend2000.advancedhunt.lib.cloud.paper;

import de.theredend2000.advancedhunt.lib.cloud.CloudCapability;
import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.SenderMapper;
import de.theredend2000.advancedhunt.lib.cloud.SenderMapperHolder;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.BrigadierManagerHolder;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.CloudBrigadierManager;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitCommandContextKeys;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitDefaultCaptionsProvider;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitParsers;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.CloudBukkitCapabilities;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.PluginHolder;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitHelper;
import de.theredend2000.advancedhunt.lib.cloud.execution.ExecutionCoordinator;
import de.theredend2000.advancedhunt.lib.cloud.internal.CommandRegistrationHandler;
import de.theredend2000.advancedhunt.lib.cloud.paper.parser.KeyedWorldParser;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import java.util.logging.Level;
import org.apiguardian.api.API;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.EXPERIMENTAL
)
public class PaperCommandManager<C>
   extends CommandManager<C>
   implements SenderMapperHolder<CommandSourceStack, C>,
   PluginMetaHolder,
   PluginHolder,
   BrigadierManagerHolder<C, CommandSourceStack> {
   private final PluginMeta pluginMeta;
   private final SenderMapper<CommandSourceStack, C> senderMapper;

   public static <C> PaperCommandManager.Builder<C> builder(final SenderMapper<CommandSourceStack, C> senderMapper) {
      return new PaperCommandManager.Builder<>(senderMapper);
   }

   public static PaperCommandManager.Builder<CommandSourceStack> builder() {
      return new PaperCommandManager.Builder<>(SenderMapper.identity());
   }

   private PaperCommandManager(
      @NonNull final PluginMeta pluginMeta,
      @NonNull final ExecutionCoordinator<C> executionCoordinator,
      @NonNull final SenderMapper<CommandSourceStack, C> senderMapper
   ) {
      super(executionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
      this.pluginMeta = pluginMeta;
      this.senderMapper = senderMapper;
      this.commandRegistrationHandler(new ModernPaperBrigadier<>(CommandSourceStack.class, this, senderMapper, () -> this.lockRegistration()));
      CloudBukkitCapabilities.CAPABLE.forEach(x$0 -> this.registerCapability(x$0));
      this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
      BukkitParsers.register(this);
      this.registerDefaultExceptionHandlers();
      this.captionRegistry().registerProvider(new BukkitDefaultCaptionsProvider<>());
      this.registerCommandPreProcessor(
         ctx -> ctx.commandContext()
            .store(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER, this.senderMapper().reverse((C)ctx.commandContext().sender()).getSender())
      );
      this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(this, this.senderMapper(), commandSourceStack -> {
         Entity executor = commandSourceStack.getExecutor();
         return (CommandSender)(executor != null ? executor : commandSourceStack.getSender());
      }));
      this.parserRegistry().registerParser(KeyedWorldParser.keyedWorldParser());
   }

   @Override
   public final boolean hasPermission(@NonNull final C sender, @NonNull final String permission) {
      return this.senderMapper().reverse(sender).getSender().hasPermission(permission);
   }

   @NonNull
   @Override
   public final SenderMapper<CommandSourceStack, C> senderMapper() {
      return this.senderMapper;
   }

   private void registerDefaultExceptionHandlers() {
      this.registerDefaultExceptionHandlers(
         triplet -> this.senderMapper()
            .reverse(triplet.first().sender())
            .getSender()
            .sendMessage(Component.text(triplet.first().formatCaption(triplet.second(), triplet.third()), NamedTextColor.RED)),
         pair -> this.owningPlugin().getLogger().log(Level.SEVERE, pair.first(), pair.second())
      );
   }

   @Override
   public final PluginMeta owningPluginMeta() {
      return this.pluginMeta;
   }

   @Override
   public final boolean hasBrigadierManager() {
      return true;
   }

   @NonNull
   @Override
   public final CloudBrigadierManager<C, ? extends CommandSourceStack> brigadierManager() {
      return ((BrigadierManagerHolder)this.commandRegistrationHandler()).brigadierManager();
   }

   public static final class Bootstrapped<C> extends PaperCommandManager<C> {
      private Bootstrapped(
         @NonNull final PluginMeta pluginMeta,
         @NonNull final ExecutionCoordinator<C> executionCoordinator,
         @NonNull final SenderMapper<CommandSourceStack, C> senderMapper
      ) {
         super(pluginMeta, executionCoordinator, senderMapper);
      }

      public void onEnable() {
         BukkitHelper.ensurePluginEnabledOrEnabling(this.owningPlugin());
      }
   }

   public static final class Builder<C> {
      private final SenderMapper<CommandSourceStack, C> senderMapper;

      private Builder(final SenderMapper<CommandSourceStack, C> senderMapper) {
         this.senderMapper = senderMapper;
      }

      public PaperCommandManager.CoordinatedBuilder<C> executionCoordinator(final ExecutionCoordinator<C> executionCoordinator) {
         return new PaperCommandManager.CoordinatedBuilder<>(this.senderMapper, executionCoordinator);
      }
   }

   public static final class CoordinatedBuilder<C> {
      private final SenderMapper<CommandSourceStack, C> senderMapper;
      private final ExecutionCoordinator<C> executionCoordinator;

      private CoordinatedBuilder(final SenderMapper<CommandSourceStack, C> senderMapper, final ExecutionCoordinator<C> executionCoordinator) {
         this.senderMapper = senderMapper;
         this.executionCoordinator = executionCoordinator;
      }

      @NonNull
      public PaperCommandManager<C> buildOnEnable(@NonNull final Plugin plugin) {
         PaperCommandManager<C> mgr = new PaperCommandManager<>(plugin.getPluginMeta(), this.executionCoordinator, this.senderMapper);
         ((ModernPaperBrigadier)mgr.commandRegistrationHandler()).registerPlugin(plugin);
         BukkitHelper.ensurePluginEnabledOrEnabling(plugin);
         return mgr;
      }

      @NonNull
      public PaperCommandManager.Bootstrapped<C> buildBootstrapped(@NonNull final BootstrapContext context) {
         PaperCommandManager.Bootstrapped<C> mgr = new PaperCommandManager.Bootstrapped<>(context.getPluginMeta(), this.executionCoordinator, this.senderMapper);
         ((ModernPaperBrigadier)mgr.commandRegistrationHandler()).registerBootstrap(context);
         return mgr;
      }
   }
}
