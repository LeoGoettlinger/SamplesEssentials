package com.skyblockexp.ezrtp.unsafe;

public enum UnsafeLocationCause {
   VOID("Void"),
   LAVA("Lava"),
   LIQUID("Liquid"),
   LIQUID_SURFACE("Liquid Surface"),
   UNSAFE_BLOCK("Unsafe Block"),
   OUT_OF_BOUNDS("Out of Bounds"),
   NULL_CANDIDATE("Null Candidate"),
   OTHER("Other");

   private final String displayName;

   private UnsafeLocationCause(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
