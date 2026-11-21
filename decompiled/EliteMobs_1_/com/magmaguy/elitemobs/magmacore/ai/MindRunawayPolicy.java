package com.magmaguy.elitemobs.magmacore.ai;

public record MindRunawayPolicy(long maxCpuNanosPerCallback, long maxInstructionsPerCallback) {
   public static final MindRunawayPolicy DEFAULT = new MindRunawayPolicy(50000000L, 50000L);

   public MindRunawayPolicy(long maxCpuNanosPerCallback, long maxInstructionsPerCallback) {
      if (maxCpuNanosPerCallback > 0L && maxInstructionsPerCallback > 0L) {
         this.maxCpuNanosPerCallback = maxCpuNanosPerCallback;
         this.maxInstructionsPerCallback = maxInstructionsPerCallback;
      } else {
         throw new IllegalArgumentException("Mind runaway limits must be positive");
      }
   }
}
