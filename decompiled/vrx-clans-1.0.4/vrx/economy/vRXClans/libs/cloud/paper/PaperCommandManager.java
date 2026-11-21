package vrx.economy.vRXClans.libs.cloud.paper;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apiguardian.api.API;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CloudCapability;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.SenderMapper;
import vrx.economy.vRXClans.libs.cloud.SenderMapperHolder;
import vrx.economy.vRXClans.libs.cloud.brigadier.BrigadierManagerHolder;
import vrx.economy.vRXClans.libs.cloud.brigadier.CloudBrigadierManager;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCommandContextKeys;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitDefaultCaptionsProvider;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitParsers;
import vrx.economy.vRXClans.libs.cloud.bukkit.CloudBukkitCapabilities;
import vrx.economy.vRXClans.libs.cloud.bukkit.PluginHolder;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.BukkitHelper;
import vrx.economy.vRXClans.libs.cloud.execution.ExecutionCoordinator;
import vrx.economy.vRXClans.libs.cloud.internal.CommandRegistrationHandler;
import vrx.economy.vRXClans.libs.cloud.paper.parser.KeyedWorldParser;

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
