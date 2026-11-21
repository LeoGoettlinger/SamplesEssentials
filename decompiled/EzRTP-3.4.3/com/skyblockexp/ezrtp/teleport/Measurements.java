package com.skyblockexp.ezrtp.teleport;

import java.util.concurrent.atomic.AtomicLong;

public final class Measurements {
   private static final AtomicLong blockAccesses = new AtomicLong(0L);
   private static final AtomicLong chunkLoadRequests = new AtomicLong(0L);
   private static final AtomicLong candidateGenerations = new AtomicLong(0L);

   private Measurements() {
   }

   public static void reset() {
      blockAccesses.set(0L);
      chunkLoadRequests.set(0L);
      candidateGenerations.set(0L);
   }

   public static void incBlockAccess() {
      blockAccesses.incrementAndGet();
   }

   public static void incChunkLoadRequest() {
      chunkLoadRequests.incrementAndGet();
   }

   public static void incCandidateGeneration() {
      candidateGenerations.incrementAndGet();
   }

   public static long getBlockAccesses() {
      return blockAccesses.get();
   }

   public static long getChunkLoadRequests() {
      return chunkLoadRequests.get();
   }

   public static long getCandidateGenerations() {
      return candidateGenerations.get();
   }
}
