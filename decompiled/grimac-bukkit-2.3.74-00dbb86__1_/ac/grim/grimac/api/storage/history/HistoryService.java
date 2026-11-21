package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApiStatus.Experimental
public interface HistoryService {
   @NotNull
   CompletionStage<Page<SessionSummary>> listSessions(@NotNull UUID var1, @Nullable Cursor var2, int var3);

   @NotNull
   CompletionStage<SessionDetail> getSessionDetail(@NotNull UUID var1, @NotNull UUID var2);

   @NotNull
   CompletionStage<Long> countSessions(@NotNull UUID var1);
}
