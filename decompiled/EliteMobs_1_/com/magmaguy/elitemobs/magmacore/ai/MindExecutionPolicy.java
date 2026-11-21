package com.magmaguy.elitemobs.magmacore.ai;

public record MindExecutionPolicy(
   long maxCallbackNanos,
   long maxEntityNanosPerTick,
   long maxServerNanosPerTick,
   int maxCallbacksPerEntityTick,
   int maxActionRequestsPerEntityTick,
   MindRunawayPolicy runawayPolicy
) {
   public static final int ACTION_REQUESTS_PER_ENTITY_TICK_HARD_LIMIT = 64;
   public static final MindExecutionPolicy DEFAULT = new MindExecutionPolicy(2000000L, 4000000L, 20000000L, 128, 8, MindRunawayPolicy.DEFAULT);

   public MindExecutionPolicy(
      long maxCallbackNanos,
      long maxEntityNanosPerTick,
      long maxServerNanosPerTick,
      int maxCallbacksPerEntityTick,
      long maxLuaInstructionsPerCallback,
      int maxActionRequestsPerEntityTick
   ) {
      this(
         maxCallbackNanos,
         maxEntityNanosPerTick,
         maxServerNanosPerTick,
         maxCallbacksPerEntityTick,
         maxActionRequestsPerEntityTick,
         new MindRunawayPolicy(MindRunawayPolicy.DEFAULT.maxCpuNanosPerCallback(), maxLuaInstructionsPerCallback)
      );
   }

   public MindExecutionPolicy(
      long maxCallbackNanos, long maxEntityNanosPerTick, long maxServerNanosPerTick, int maxCallbacksPerEntityTick, long maxLuaInstructionsPerCallback
   ) {
      this(
         maxCallbackNanos,
         maxEntityNanosPerTick,
         maxServerNanosPerTick,
         maxCallbacksPerEntityTick,
         DEFAULT.maxActionRequestsPerEntityTick,
         new MindRunawayPolicy(MindRunawayPolicy.DEFAULT.maxCpuNanosPerCallback(), maxLuaInstructionsPerCallback)
      );
   }

   public MindExecutionPolicy(
      long maxCallbackNanos,
      long maxEntityNanosPerTick,
      long maxServerNanosPerTick,
      int maxCallbacksPerEntityTick,
      int maxActionRequestsPerEntityTick,
      MindRunawayPolicy runawayPolicy
   ) {
      if (maxCallbackNanos <= 0L
         || maxEntityNanosPerTick <= 0L
         || maxServerNanosPerTick <= 0L
         || maxCallbacksPerEntityTick <= 0
         || maxActionRequestsPerEntityTick <= 0) {
         throw new IllegalArgumentException("All mind scheduling limits must be positive");
      } else if (runawayPolicy == null) {
         throw new NullPointerException("runawayPolicy");
      } else if (maxCallbackNanos > maxEntityNanosPerTick) {
         throw new IllegalArgumentException("Callback budget cannot exceed entity budget");
      } else if (maxEntityNanosPerTick > maxServerNanosPerTick) {
         throw new IllegalArgumentException("Entity budget cannot exceed server budget");
      } else if (maxActionRequestsPerEntityTick > 64) {
         throw new IllegalArgumentException("Mind action-request cap cannot exceed the native hard limit of 64");
      } else {
         this.maxCallbackNanos = maxCallbackNanos;
         this.maxEntityNanosPerTick = maxEntityNanosPerTick;
         this.maxServerNanosPerTick = maxServerNanosPerTick;
         this.maxCallbacksPerEntityTick = maxCallbacksPerEntityTick;
         this.maxActionRequestsPerEntityTick = maxActionRequestsPerEntityTick;
         this.runawayPolicy = runawayPolicy;
      }
   }

   public long maxLuaInstructionsPerCallback() {
      return this.runawayPolicy.maxInstructionsPerCallback();
   }
}
