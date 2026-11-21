package com.skyblockexp.ezrtp.libs.updater;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ChainedUpdateResult {
   private final UpdateResult result;
   private final String sourceUsed;
   private final Map<String, UpdateCheckException> failuresBySource;

   ChainedUpdateResult(UpdateResult result, String sourceUsed, Map<String, UpdateCheckException> failuresBySource) {
      this.result = result;
      this.sourceUsed = sourceUsed;
      this.failuresBySource = Collections.unmodifiableMap(new LinkedHashMap<>(failuresBySource));
   }

   public UpdateResult getResult() {
      return this.result;
   }

   public Optional<String> getSourceUsed() {
      return Optional.ofNullable(this.sourceUsed);
   }

   public Map<String, UpdateCheckException> getFailuresBySource() {
      return this.failuresBySource;
   }
}
