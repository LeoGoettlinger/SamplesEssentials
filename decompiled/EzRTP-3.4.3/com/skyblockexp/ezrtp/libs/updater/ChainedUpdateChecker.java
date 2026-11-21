package com.skyblockexp.ezrtp.libs.updater;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class ChainedUpdateChecker {
   private final List<UpdateSource> sources;

   private ChainedUpdateChecker(List<UpdateSource> sources) {
      this.sources = sources;
   }

   public static ChainedUpdateChecker.Builder builder() {
      return new ChainedUpdateChecker.Builder();
   }

   public ChainedUpdateResult checkNow() {
      Map<String, UpdateCheckException> failures = new LinkedHashMap<>();

      for (UpdateSource source : this.sources) {
         UpdateResult result = source.checkNow();
         if (!result.hasError()) {
            return new ChainedUpdateResult(result, source.getSourceName(), failures);
         }

         failures.put(source.getSourceName(), result.getError().orElse(null));
      }

      UpdateCheckException finalError = new UpdateCheckException(
         UpdateCheckException.ErrorType.NETWORK_ERROR, "All update sources failed: " + failures.keySet()
      );
      return new ChainedUpdateResult(UpdateResult.failure("unknown", finalError), null, failures);
   }

   public CompletableFuture<ChainedUpdateResult> checkNowAsync() {
      return CompletableFuture.supplyAsync(this::checkNow);
   }

   public static final class Builder {
      private final List<UpdateSource> sources = new ArrayList<>();

      public ChainedUpdateChecker.Builder primary(UpdateSource source) {
         return this.addSource(source);
      }

      public ChainedUpdateChecker.Builder backup(UpdateSource source) {
         return this.addSource(source);
      }

      public ChainedUpdateChecker.Builder addSource(UpdateSource source) {
         this.sources.add(Objects.requireNonNull(source, "source"));
         return this;
      }

      public ChainedUpdateChecker build() {
         if (this.sources.isEmpty()) {
            throw new IllegalStateException("At least one source is required.");
         } else {
            return new ChainedUpdateChecker(List.copyOf(this.sources));
         }
      }
   }
}
