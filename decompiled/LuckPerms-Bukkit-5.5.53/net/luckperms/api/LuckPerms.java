package net.luckperms.api;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.luckperms.api.actionlog.ActionLogger;
import net.luckperms.api.actionlog.filter.ActionFilterFactory;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.messaging.MessagingService;
import net.luckperms.api.messenger.MessengerProvider;
import net.luckperms.api.metastacking.MetaStackFactory;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeBuilderRegistry;
import net.luckperms.api.node.matcher.NodeMatcherFactory;
import net.luckperms.api.platform.Health;
import net.luckperms.api.platform.Platform;
import net.luckperms.api.platform.PlayerAdapter;
import net.luckperms.api.platform.PluginMetadata;
import net.luckperms.api.query.QueryOptionsRegistry;
import net.luckperms.api.track.TrackManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface LuckPerms {
   @NonNull
   String getServerName();

   @NonNull
   UserManager getUserManager();

   @NonNull
   GroupManager getGroupManager();

   @NonNull
   TrackManager getTrackManager();

   @NonNull
   <T> PlayerAdapter<T> getPlayerAdapter(@NonNull Class<T> var1);

   @NonNull
   Platform getPlatform();

   @NonNull
   PluginMetadata getPluginMetadata();

   @NonNull
   EventBus getEventBus();

   @NonNull
   Optional<MessagingService> getMessagingService();

   @NonNull
   ActionLogger getActionLogger();

   @NonNull
   ContextManager getContextManager();

   @NonNull
   MetaStackFactory getMetaStackFactory();

   @NonNull
   CompletableFuture<Void> runUpdateTask();

   @NonNull
   Health runHealthCheck();

   void registerMessengerProvider(@NonNull MessengerProvider var1);

   @Internal
   @NonNull
   NodeBuilderRegistry getNodeBuilderRegistry();

   @Internal
   @NonNull
   QueryOptionsRegistry getQueryOptionsRegistry();

   @Internal
   @NonNull
   NodeMatcherFactory getNodeMatcherFactory();

   @Internal
   @NonNull
   ActionFilterFactory getActionFilterFactory();
}
