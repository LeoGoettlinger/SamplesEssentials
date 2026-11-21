package com.skyblockexp.ezrtp.teleport.filter;

public record SearchFilterResult(boolean passed, SearchFilterResult.RejectionReason rejectionReason) {
   public static SearchFilterResult pass() {
      return new SearchFilterResult(true, SearchFilterResult.RejectionReason.NONE);
   }

   public static SearchFilterResult rejected(SearchFilterResult.RejectionReason reason) {
      return new SearchFilterResult(false, reason != null ? reason : SearchFilterResult.RejectionReason.NONE);
   }

   public static enum RejectionReason {
      NONE,
      UNSAFE_OR_NULL_CANDIDATE,
      BIOME_FILTERED,
      PROTECTED_CLAIM;
   }
}
