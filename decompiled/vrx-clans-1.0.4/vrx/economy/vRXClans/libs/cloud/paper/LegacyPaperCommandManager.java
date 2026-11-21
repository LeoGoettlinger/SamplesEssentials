package vrx.economy.vRXClans.libs.cloud.paper;

import java.util.function.Function;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.SenderMapper;
import vrx.economy.vRXClans.libs.cloud.brigadier.BrigadierManagerHolder;
import vrx.economy.vRXClans.libs.cloud.brigadier.BrigadierSetting;
import vrx.economy.vRXClans.libs.cloud.brigadier.CloudBrigadierManager;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCommandManager;
import vrx.economy.vRXClans.libs.cloud.bukkit.CloudBukkitCapabilities;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;
import vrx.economy.vRXClans.libs.cloud.execution.ExecutionCoordinator;
import vrx.economy.vRXClans.libs.cloud.paper.suggestion.SuggestionListener;
import vrx.economy.vRXClans.libs.cloud.paper.suggestion.SuggestionListenerFactory;
import vrx.economy.vRXClans.libs.cloud.state.RegistrationState;

public class LegacyPaperCommandManager<C> extends BukkitCommandManager<C> {
   @Nullable
   private BrigadierManagerHolder<C, ?> brigadierManagerHolder = null;

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public LegacyPaperCommandManager(
      @NonNull final Plugin owningPlugin,
      @NonNull final ExecutionCoordinator<C> commandExecutionCoordinator,
      @NonNull final SenderMapper<CommandSender, C> senderMapper
   ) throws BukkitCommandManager.InitializationException {
      super(owningPlugin, commandExecutionCoordinator, senderMapper);
      this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(this, this.senderMapper(), Function.identity()));
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static LegacyPaperCommandManager<CommandSender> createNative(
      @NonNull final Plugin owningPlugin, @NonNull final ExecutionCoordinator<CommandSender> commandExecutionCoordinator
   ) throws BukkitCommandManager.InitializationException {
      return new LegacyPaperCommandManager<>(owningPlugin, commandExecutionCoordinator, SenderMapper.identity());
   }

   @Override
   public synchronized void registerBrigadier() throws BukkitCommandManager.BrigadierInitializationException {
      this.registerBrigadier(true);
   }

   @Deprecated
   public synchronized void registerLegacyPaperBrigadier() throws BukkitCommandManager.BrigadierInitializationException {
      this.registerBrigadier(false);
   }

   private void registerBrigadier(final boolean allowModern) {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      this.checkBrigadierCompatibility();
      if (this.brigadierManagerHolder != null) {
         throw new IllegalStateException("Brigadier is already registered! Holder: " + this.brigadierManagerHolder);
      } else {
         if (!this.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            super.registerBrigadier();
         } else if (allowModern && CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.CommandSourceStack")) {
            try {
               ModernPaperBrigadier<C, CommandSender> brig = new ModernPaperBrigadier<>(
                  CommandSender.class, this, this.senderMapper(), () -> this.lockRegistration()
               );
               this.brigadierManagerHolder = brig;
               brig.registerPlugin(this.owningPlugin());
               this.commandRegistrationHandler(brig);
            } catch (Exception var4) {
               throw new BukkitCommandManager.BrigadierInitializationException("Failed to register ModernPaperBrigadier", var4);
            }
         } else {
            try {
               this.brigadierManagerHolder = new LegacyPaperBrigadier<>(this);
               Bukkit.getPluginManager().registerEvents((Listener)this.brigadierManagerHolder, this.owningPlugin());
               this.brigadierManagerHolder.brigadierManager().settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
            } catch (Exception var3) {
               throw new BukkitCommandManager.BrigadierInitializationException("Failed to register LegacyPaperBrigadier", var3);
            }
         }
      }
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Override
   public boolean hasBrigadierManager() {
      return this.brigadierManagerHolder != null || super.hasBrigadierManager();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   @Override
   public CloudBrigadierManager<C, ?> brigadierManager() {
      return this.brigadierManagerHolder != null ? this.brigadierManagerHolder.brigadierManager() : super.brigadierManager();
   }

   public void registerAsynchronousCompletions() throws IllegalStateException {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      if (!this.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
         throw new IllegalStateException("Failed to register asynchronous command completion listener.");
      } else {
         SuggestionListenerFactory<C> suggestionListenerFactory = SuggestionListenerFactory.create(this);
         SuggestionListener<C> suggestionListener = suggestionListenerFactory.createListener();
         Bukkit.getServer().getPluginManager().registerEvents(suggestionListener, this.owningPlugin());
      }
   }
}
