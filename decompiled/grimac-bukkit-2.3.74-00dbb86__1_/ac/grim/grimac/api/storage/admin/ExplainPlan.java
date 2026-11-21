package ac.grim.grimac.api.storage.admin;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public record ExplainPlan(
   @NotNull String stage,
   long rowsExamined,
   long rowsReturned,
   @NotNull Duration estimatedTime,
   @Nullable String indexUsed,
   @NotNull Map<String, Object> details,
   @NotNull List<ExplainPlan> children,
   @NotNull Map<String, Object> raw
) {
   public ExplainPlan(
      @NotNull String stage,
      long rowsExamined,
      long rowsReturned,
      @NotNull Duration estimatedTime,
      @Nullable String indexUsed,
      @NotNull Map<String, Object> details,
      @NotNull List<ExplainPlan> children,
      @NotNull Map<String, Object> raw
   ) {
      details = Map.copyOf(details);
      children = List.copyOf(children);
      raw = Map.copyOf(raw);
      this.stage = stage;
      this.rowsExamined = rowsExamined;
      this.rowsReturned = rowsReturned;
      this.estimatedTime = estimatedTime;
      this.indexUsed = indexUsed;
      this.details = details;
      this.children = children;
      this.raw = raw;
   }
}
