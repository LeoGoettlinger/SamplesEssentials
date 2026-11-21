package net.luckperms.api.actionlog;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.luckperms.api.actionlog.filter.ActionFilter;
import net.luckperms.api.util.Page;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ActionLogger {
   @NonNull
   Action.Builder actionBuilder();

   @Deprecated
   @NonNull
   CompletableFuture<ActionLog> getLog();

   @NonNull
   CompletableFuture<List<Action>> queryActions(@NonNull ActionFilter var1);

   @NonNull
   CompletableFuture<Page<Action>> queryActions(@NonNull ActionFilter var1, int var2, int var3);

   @NonNull
   CompletableFuture<Void> submit(@NonNull Action var1);

   @NonNull
   CompletableFuture<Void> submitToStorage(@NonNull Action var1);

   @NonNull
   CompletableFuture<Void> broadcastAction(@NonNull Action var1);
}
