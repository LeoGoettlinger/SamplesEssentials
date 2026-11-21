package com.fastasyncworldedit.core.internal.simd;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.filter.block.DelegateFilter;
import com.fastasyncworldedit.core.function.mask.InverseMask;
import com.fastasyncworldedit.core.function.mask.SingleBlockStateMask;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.InverseSingleBlockStateMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import javax.annotation.Nullable;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import org.apache.logging.log4j.Logger;

public class SimdSupport {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final boolean VECTOR_API_PRESENT;
   private static final boolean JAVA_25_OR_NEWER = Runtime.version().feature() >= 25;

   public static boolean useVectorApi() {
      return VECTOR_API_PRESENT && JAVA_25_OR_NEWER && Settings.settings().EXPERIMENTAL.USE_VECTOR_API;
   }

   @Nullable
   public static VectorizedMask vectorizedTargetMask(Mask mask) {
      if (!useVectorApi()) {
         return null;
      } else {
         return switch (mask) {
            case SingleBlockStateMask single -> vectorizedTargetMask(single.getBlockState().getOrdinalChar());
            case InverseSingleBlockStateMask inverse -> vectorizedTargetMaskInverse(inverse.getBlockState().getOrdinalChar());
            case ExistingBlockMask ignored -> vectorizedTargetMaskNonAir();
            case InverseMask inversex -> {
               VectorizedMask base = vectorizedTargetMask(inversex.inverse());
               yield base == null ? null : (set, get, species) -> base.compareVector(set, get, species).not();
            }
            default -> null;
         };
      }
   }

   private static VectorizedMask vectorizedTargetMaskNonAir() {
      return (set, get, species) -> get.get(species).compare(VectorOperators.UGT, 3L);
   }

   private static VectorizedMask vectorizedTargetMask(char ordinal) {
      return (set, get, species) -> get.get(species).compare(VectorOperators.EQ, (short)ordinal);
   }

   private static VectorizedMask vectorizedTargetMaskInverse(char ordinal) {
      return (set, get, species) -> get.get(species).compare(VectorOperators.NE, (short)ordinal);
   }

   @Nullable
   public static VectorizedFilter vectorizedPattern(Pattern pattern) {
      if (!useVectorApi()) {
         return null;
      } else {
         return switch (pattern) {
            case BaseBlock block -> block.getNbtReference() == null ? new SimdSupport.VectorizedPattern(block, block.getOrdinalChar()) : null;
            case BlockStateHolder<?> blockStateHolder -> new SimdSupport.VectorizedPattern(blockStateHolder, blockStateHolder.getOrdinalChar());
            default -> null;
         };
      }
   }

   static {
      boolean vectorApiPresent = false;

      try {
         Class.forName("jdk.incubator.vector.Vector");
         vectorApiPresent = true;
      } catch (ClassNotFoundException var2) {
      }

      VECTOR_API_PRESENT = vectorApiPresent;
      if (!VECTOR_API_PRESENT && Settings.settings().EXPERIMENTAL.USE_VECTOR_API) {
         LOGGER.warn("FAWE use-vector-api is enabled but --add-modules=jdk.incubator.vector is not set.\nVector instructions will not be used.");
      } else if (VECTOR_API_PRESENT && !Settings.settings().EXPERIMENTAL.USE_VECTOR_API) {
         LOGGER.warn(
            "The server is running with the --add-modules=jdk.incubator.vector option.\nFAWE can use vector instructions, but it is disabled in the config.\nEnable use-vector-api to benefit from vector instructions with FAWE."
         );
      } else if (VECTOR_API_PRESENT && !JAVA_25_OR_NEWER) {
         LOGGER.warn(
            "The server is running with the --add-modules=jdk.incubator.vector option.\nHowever, Java 25+ is required due to compatibility problems.\nUpdate to Java 25 to make use of this feature."
         );
      }
   }

   private static final class VectorizedPattern<T extends Filter> extends DelegateFilter<T> implements VectorizedFilter {
      private final char ordinal;

      public VectorizedPattern(final T parent, char ordinal) {
         super(parent);
         this.ordinal = ordinal;
      }

      @Override
      public Filter newInstance(final Filter other) {
         return new SimdSupport.VectorizedPattern<>(other, this.ordinal);
      }

      @Override
      public void applyVector(final VectorFacade get, final VectorFacade set, final VectorMask<Short> mask) {
         ShortVector s = set.getOrZero(mask.vectorSpecies());
         s = s.blend(ShortVector.broadcast(ShortVector.SPECIES_PREFERRED, (long)this.ordinal), mask);
         set.setOrIgnore(s);
      }
   }
}
