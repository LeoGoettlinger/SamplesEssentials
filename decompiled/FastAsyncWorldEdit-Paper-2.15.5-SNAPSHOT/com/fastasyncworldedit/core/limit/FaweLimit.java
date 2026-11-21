package com.fastasyncworldedit.core.limit;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FaweLimit {
   public int MAX_ACTIONS = 0;
   public AtomicLong MAX_CHANGES = new AtomicLong();
   public AtomicInteger MAX_FAILS = new AtomicInteger();
   public AtomicLong MAX_CHECKS = new AtomicLong();
   public AtomicInteger MAX_ITERATIONS = new AtomicInteger();
   public AtomicInteger MAX_BLOCKSTATES = new AtomicInteger();
   public AtomicInteger MAX_ENTITIES = new AtomicInteger();
   public int MAX_HISTORY = 0;
   public int SCHEM_FILE_SIZE_LIMIT = 0;
   public int SCHEM_FILE_NUM_LIMIT = 0;
   public int MAX_EXPRESSION_MS = 0;
   public int MAX_RADIUS = 0;
   public int MAX_SUPER_PICKAXE_SIZE = 0;
   public int MAX_BRUSH_RADIUS = 0;
   public int MAX_BUTCHER_RADIUS = 0;
   public int INVENTORY_MODE = Integer.MAX_VALUE;
   public int SPEED_REDUCTION = Integer.MAX_VALUE;
   public boolean FAST_PLACEMENT = false;
   public boolean CONFIRM_LARGE = true;
   public boolean RESTRICT_HISTORY_TO_REGIONS = true;
   public boolean ALLOW_LEGACY = true;
   public boolean SKIP_ENTITY_SPAWN_EVENTS = false;
   public Set<String> STRIP_NBT = null;
   public boolean UNIVERSAL_DISALLOWED_BLOCKS = true;
   public Set<String> DISALLOWED_BLOCKS = null;
   public Set<PropertyRemap<?>> REMAP_PROPERTIES = null;
   public static FaweLimit MAX = new FaweLimit() {
      @Override
      public boolean MAX_CHANGES() {
         return true;
      }

      @Override
      public boolean MAX_BLOCKSTATES() {
         return true;
      }

      @Override
      public boolean MAX_CHECKS() {
         return true;
      }

      @Override
      public boolean MAX_ENTITIES() {
         return true;
      }

      @Override
      public boolean MAX_FAILS() {
         return true;
      }

      @Override
      public boolean MAX_ITERATIONS() {
         return true;
      }

      @Override
      public boolean isUnlimited() {
         return true;
      }

      @Override
      public void THROW_MAX_CHANGES() {
      }

      @Override
      public void THROW_MAX_FAILS() {
      }

      @Override
      public void THROW_MAX_CHECKS() {
      }

      @Override
      public void THROW_MAX_ITERATIONS() {
      }

      @Override
      public void THROW_MAX_BLOCKSTATES() {
      }

      @Override
      public void THROW_MAX_ENTITIES() {
      }

      @Override
      public void THROW_MAX_CHANGES(int amt) {
      }

      @Override
      public void THROW_MAX_FAILS(int amt) {
      }

      @Override
      public void THROW_MAX_CHECKS(int amt) {
      }

      @Override
      public void THROW_MAX_ITERATIONS(int amt) {
      }

      @Override
      public void THROW_MAX_BLOCKSTATES(int amt) {
      }

      @Override
      public void THROW_MAX_ENTITIES(int amt) {
      }
   };

   public boolean MAX_CHANGES() {
      return this.MAX_CHANGES.decrementAndGet() < 0L;
   }

   public boolean MAX_FAILS() {
      return this.MAX_FAILS.decrementAndGet() < 0;
   }

   public boolean MAX_CHECKS() {
      return this.MAX_CHECKS.decrementAndGet() < 0L;
   }

   public boolean MAX_ITERATIONS() {
      return this.MAX_ITERATIONS.decrementAndGet() < 0;
   }

   public boolean MAX_BLOCKSTATES() {
      return this.MAX_BLOCKSTATES.decrementAndGet() < 0;
   }

   public boolean MAX_ENTITIES() {
      return this.MAX_ENTITIES.decrementAndGet() < 0;
   }

   public void THROW_MAX_CHANGES() {
      if (this.MAX_CHANGES.decrementAndGet() < 0L) {
         throw FaweCache.MAX_CHANGES;
      }
   }

   public void THROW_MAX_FAILS() {
      if (this.MAX_FAILS.decrementAndGet() < 0) {
         throw FaweCache.MAX_FAILS;
      }
   }

   public void THROW_MAX_CHECKS() {
      if (this.MAX_CHECKS.decrementAndGet() < 0L) {
         throw FaweCache.MAX_CHECKS;
      }
   }

   public void THROW_MAX_ITERATIONS() {
      if (this.MAX_ITERATIONS.decrementAndGet() < 0) {
         throw FaweCache.MAX_ITERATIONS;
      }
   }

   public void THROW_MAX_BLOCKSTATES() {
      if (this.MAX_BLOCKSTATES.decrementAndGet() < 0) {
         throw FaweCache.MAX_TILES;
      }
   }

   public void THROW_MAX_ENTITIES() {
      if (this.MAX_ENTITIES.decrementAndGet() < 0) {
         throw FaweCache.MAX_ENTITIES;
      }
   }

   public void THROW_MAX_CHANGES(int amt) {
      if (amt != 0) {
         if (this.MAX_CHANGES.addAndGet(-amt) < 0L) {
            throw FaweCache.MAX_CHANGES;
         }
      }
   }

   public void THROW_MAX_CHANGES(long amt) {
      if (amt != 0L) {
         if (this.MAX_CHANGES.addAndGet(-amt) < 0L) {
            throw FaweCache.MAX_CHANGES;
         }
      }
   }

   public void THROW_MAX_FAILS(int amt) {
      if (amt != 0) {
         if (this.MAX_FAILS.addAndGet(-amt) < 0) {
            throw FaweCache.MAX_FAILS;
         }
      }
   }

   public void THROW_MAX_CHECKS(int amt) {
      if (amt != 0) {
         if (this.MAX_CHECKS.addAndGet(-amt) < 0L) {
            throw FaweCache.MAX_CHECKS;
         }
      }
   }

   public void THROW_MAX_CHECKS(long amt) {
      if (amt != 0L) {
         if (this.MAX_CHECKS.addAndGet(-amt) < 0L) {
            throw FaweCache.MAX_CHECKS;
         }
      }
   }

   public void THROW_MAX_ITERATIONS(int amt) {
      if (amt != 0) {
         if (this.MAX_ITERATIONS.addAndGet(-amt) < 0) {
            throw FaweCache.MAX_ITERATIONS;
         }
      }
   }

   public void THROW_MAX_BLOCKSTATES(int amt) {
      if (amt != 0) {
         if (this.MAX_BLOCKSTATES.addAndGet(-amt) < 0) {
            throw FaweCache.MAX_TILES;
         }
      }
   }

   public void THROW_MAX_ENTITIES(int amt) {
      if (amt != 0) {
         if (this.MAX_ENTITIES.addAndGet(-amt) < 0) {
            throw FaweCache.MAX_ENTITIES;
         }
      }
   }

   public boolean isUnlimited() {
      return this.MAX_CHANGES.get() == Long.MAX_VALUE
         && this.MAX_FAILS.get() == Integer.MAX_VALUE
         && this.MAX_CHECKS.get() == Long.MAX_VALUE
         && this.MAX_ITERATIONS.get() == Integer.MAX_VALUE
         && this.MAX_BLOCKSTATES.get() == Integer.MAX_VALUE
         && this.MAX_ENTITIES.get() == Integer.MAX_VALUE
         && this.MAX_HISTORY == Integer.MAX_VALUE
         && this.SCHEM_FILE_SIZE_LIMIT == Integer.MAX_VALUE
         && this.SCHEM_FILE_NUM_LIMIT == Integer.MAX_VALUE
         && this.INVENTORY_MODE == 0
         && this.SPEED_REDUCTION == 0
         && this.FAST_PLACEMENT
         && !this.RESTRICT_HISTORY_TO_REGIONS
         && (this.STRIP_NBT == null || this.STRIP_NBT.isEmpty())
         && this.ALLOW_LEGACY
         && (this.DISALLOWED_BLOCKS == null || this.DISALLOWED_BLOCKS.isEmpty())
         && (this.REMAP_PROPERTIES == null || this.REMAP_PROPERTIES.isEmpty())
         && this.MAX_RADIUS == Integer.MAX_VALUE
         && this.MAX_SUPER_PICKAXE_SIZE == Integer.MAX_VALUE
         && this.MAX_BRUSH_RADIUS == Integer.MAX_VALUE
         && this.MAX_BUTCHER_RADIUS == Integer.MAX_VALUE;
   }

   public FaweLimit getLimitUsed(FaweLimit originalLimit) {
      FaweLimit newLimit = new FaweLimit();
      newLimit.MAX_CHANGES = new AtomicLong(originalLimit.MAX_CHANGES.get() - this.MAX_CHANGES.get());
      newLimit.MAX_FAILS = new AtomicInteger(originalLimit.MAX_FAILS.get() - this.MAX_FAILS.get());
      newLimit.MAX_CHECKS = new AtomicLong(originalLimit.MAX_CHECKS.get() - this.MAX_CHECKS.get());
      newLimit.MAX_ITERATIONS = new AtomicInteger(originalLimit.MAX_ITERATIONS.get() - this.MAX_ITERATIONS.get());
      newLimit.MAX_BLOCKSTATES = new AtomicInteger(originalLimit.MAX_BLOCKSTATES.get() - this.MAX_BLOCKSTATES.get());
      newLimit.MAX_ENTITIES = new AtomicInteger(originalLimit.MAX_ENTITIES.get() - this.MAX_ENTITIES.get());
      return newLimit;
   }

   public void set(FaweLimit limit) {
      this.MAX_ACTIONS = limit.MAX_ACTIONS;
      this.MAX_CHANGES.set(limit.MAX_CHANGES.get());
      this.MAX_FAILS.set(limit.MAX_FAILS.get());
      this.MAX_CHECKS.set(limit.MAX_CHECKS.get());
      this.MAX_ITERATIONS.set(limit.MAX_ITERATIONS.get());
      this.MAX_BLOCKSTATES.set(limit.MAX_BLOCKSTATES.get());
      this.MAX_ENTITIES.set(limit.MAX_ENTITIES.get());
      this.MAX_HISTORY = limit.MAX_HISTORY;
      this.SCHEM_FILE_NUM_LIMIT = limit.SCHEM_FILE_NUM_LIMIT;
      this.SCHEM_FILE_SIZE_LIMIT = limit.SCHEM_FILE_SIZE_LIMIT;
      this.INVENTORY_MODE = limit.INVENTORY_MODE;
      this.SPEED_REDUCTION = limit.SPEED_REDUCTION;
      this.FAST_PLACEMENT = limit.FAST_PLACEMENT;
      this.CONFIRM_LARGE = limit.CONFIRM_LARGE;
      this.RESTRICT_HISTORY_TO_REGIONS = limit.RESTRICT_HISTORY_TO_REGIONS;
      this.STRIP_NBT = limit.STRIP_NBT;
      this.UNIVERSAL_DISALLOWED_BLOCKS = limit.UNIVERSAL_DISALLOWED_BLOCKS;
      this.ALLOW_LEGACY = limit.ALLOW_LEGACY;
      this.SKIP_ENTITY_SPAWN_EVENTS = limit.SKIP_ENTITY_SPAWN_EVENTS;
      this.DISALLOWED_BLOCKS = limit.DISALLOWED_BLOCKS;
      this.REMAP_PROPERTIES = limit.REMAP_PROPERTIES;
      this.MAX_RADIUS = limit.MAX_RADIUS;
      this.MAX_SUPER_PICKAXE_SIZE = limit.MAX_SUPER_PICKAXE_SIZE;
      this.MAX_BRUSH_RADIUS = limit.MAX_BRUSH_RADIUS;
      this.MAX_BUTCHER_RADIUS = limit.MAX_BUTCHER_RADIUS;
   }

   public FaweLimit copy() {
      FaweLimit limit = new FaweLimit();
      limit.INVENTORY_MODE = this.INVENTORY_MODE;
      limit.SPEED_REDUCTION = this.SPEED_REDUCTION;
      limit.MAX_ACTIONS = this.MAX_ACTIONS;
      limit.MAX_CHANGES = this.MAX_CHANGES;
      limit.MAX_BLOCKSTATES = this.MAX_BLOCKSTATES;
      limit.MAX_CHECKS = this.MAX_CHECKS;
      limit.MAX_ENTITIES = this.MAX_ENTITIES;
      limit.MAX_FAILS = this.MAX_FAILS;
      limit.MAX_ITERATIONS = this.MAX_ITERATIONS;
      limit.MAX_HISTORY = this.MAX_HISTORY;
      limit.SCHEM_FILE_SIZE_LIMIT = this.SCHEM_FILE_SIZE_LIMIT;
      limit.SCHEM_FILE_NUM_LIMIT = this.SCHEM_FILE_NUM_LIMIT;
      limit.FAST_PLACEMENT = this.FAST_PLACEMENT;
      limit.CONFIRM_LARGE = this.CONFIRM_LARGE;
      limit.RESTRICT_HISTORY_TO_REGIONS = this.RESTRICT_HISTORY_TO_REGIONS;
      limit.STRIP_NBT = this.STRIP_NBT;
      limit.UNIVERSAL_DISALLOWED_BLOCKS = this.UNIVERSAL_DISALLOWED_BLOCKS;
      limit.ALLOW_LEGACY = this.ALLOW_LEGACY;
      limit.SKIP_ENTITY_SPAWN_EVENTS = this.SKIP_ENTITY_SPAWN_EVENTS;
      limit.DISALLOWED_BLOCKS = this.DISALLOWED_BLOCKS;
      limit.REMAP_PROPERTIES = this.REMAP_PROPERTIES;
      limit.MAX_RADIUS = this.MAX_RADIUS;
      limit.MAX_SUPER_PICKAXE_SIZE = this.MAX_SUPER_PICKAXE_SIZE;
      limit.MAX_BRUSH_RADIUS = this.MAX_BRUSH_RADIUS;
      limit.MAX_BUTCHER_RADIUS = this.MAX_BUTCHER_RADIUS;
      return limit;
   }

   @Override
   public String toString() {
      return this.MAX_CHANGES + "";
   }

   static {
      MAX.SPEED_REDUCTION = 0;
      MAX.INVENTORY_MODE = 0;
      MAX.MAX_ACTIONS = 1;
      MAX.MAX_CHANGES = new AtomicLong(Long.MAX_VALUE);
      MAX.MAX_FAILS = new AtomicInteger(Integer.MAX_VALUE);
      MAX.MAX_CHECKS = new AtomicLong(Long.MAX_VALUE);
      MAX.MAX_ITERATIONS = new AtomicInteger(Integer.MAX_VALUE);
      MAX.MAX_BLOCKSTATES = new AtomicInteger(Integer.MAX_VALUE);
      MAX.MAX_ENTITIES = new AtomicInteger(Integer.MAX_VALUE);
      MAX.MAX_HISTORY = Integer.MAX_VALUE;
      MAX.SCHEM_FILE_NUM_LIMIT = Integer.MAX_VALUE;
      MAX.SCHEM_FILE_SIZE_LIMIT = Integer.MAX_VALUE;
      MAX.MAX_EXPRESSION_MS = 50;
      MAX.FAST_PLACEMENT = true;
      MAX.CONFIRM_LARGE = Settings.settings().LIMITS.get("default").CONFIRM_LARGE || Settings.settings().GENERAL.LIMIT_UNLIMITED_CONFIRMS;
      MAX.RESTRICT_HISTORY_TO_REGIONS = false;
      MAX.STRIP_NBT = Collections.emptySet();
      MAX.UNIVERSAL_DISALLOWED_BLOCKS = false;
      MAX.ALLOW_LEGACY = true;
      MAX.SKIP_ENTITY_SPAWN_EVENTS = Settings.settings().GENERAL.LIMIT_UNLIMITED_SKIPS_CREATURE_SPAWN_EVENTS;
      MAX.DISALLOWED_BLOCKS = Collections.emptySet();
      MAX.REMAP_PROPERTIES = Collections.emptySet();
      MAX.MAX_RADIUS = Integer.MAX_VALUE;
      MAX.MAX_SUPER_PICKAXE_SIZE = Integer.MAX_VALUE;
      MAX.MAX_BRUSH_RADIUS = Integer.MAX_VALUE;
      MAX.MAX_BUTCHER_RADIUS = Integer.MAX_VALUE;
   }
}
