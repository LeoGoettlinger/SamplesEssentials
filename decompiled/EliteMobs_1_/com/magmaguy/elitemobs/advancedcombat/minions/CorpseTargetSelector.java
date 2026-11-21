package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CorpseTargetSelector {
   private static final double PRECISE_CORRIDOR = 1.75;
   private static final double FALLBACK_MINIMUM_COSINE = 0.65;

   private CorpseTargetSelector() {
   }

   public static Optional<UUID> select(
      CorpseTargetSelector.Point origin, CorpseTargetSelector.Point lookDirection, double range, List<CorpseTargetSelector.Candidate> candidates
   ) {
      Objects.requireNonNull(origin, "origin");
      Objects.requireNonNull(lookDirection, "lookDirection");
      Objects.requireNonNull(candidates, "candidates");
      if (Double.isFinite(range) && !(range <= 0.0)) {
         CorpseTargetSelector.Point direction = lookDirection.normalized();
         List<CorpseTargetSelector.Scored> forward = candidates.stream()
            .map(candidate -> score(origin, direction, range, candidate))
            .filter(Objects::nonNull)
            .toList();
         return forward.stream()
            .filter(candidate -> candidate.perpendicular() <= 1.75)
            .min(Comparator.comparingDouble(CorpseTargetSelector.Scored::perpendicular).thenComparingDouble(CorpseTargetSelector.Scored::distance))
            .or(
               () -> forward.stream()
                  .filter(candidate -> candidate.cosine() >= 0.65)
                  .min(Comparator.comparingDouble(CorpseTargetSelector.Scored::cosine).reversed().thenComparingDouble(CorpseTargetSelector.Scored::distance))
            )
            .map(candidate -> candidate.candidate().id());
      } else {
         return Optional.empty();
      }
   }

   private static CorpseTargetSelector.Scored score(
      CorpseTargetSelector.Point origin, CorpseTargetSelector.Point direction, double range, CorpseTargetSelector.Candidate candidate
   ) {
      CorpseTargetSelector.Point delta = candidate.position().subtract(origin);
      double distance = delta.length();
      if (!(distance <= 0.0) && !(distance > range)) {
         double projection = delta.dot(direction);
         if (projection <= 0.0) {
            return null;
         } else {
            double perpendicularSquared = Math.max(0.0, delta.dot(delta) - projection * projection);
            return new CorpseTargetSelector.Scored(candidate, distance, Math.sqrt(perpendicularSquared), projection / distance);
         }
      } else {
         return null;
      }
   }

   public record Candidate(UUID id, CorpseTargetSelector.Point position) {
      public Candidate(UUID id, CorpseTargetSelector.Point position) {
         Objects.requireNonNull(id, "id");
         Objects.requireNonNull(position, "position");
         this.id = id;
         this.position = position;
      }
   }

   public record Point(double x, double y, double z) {
      public Point(double x, double y, double z) {
         if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
            this.x = x;
            this.y = y;
            this.z = z;
         } else {
            throw new IllegalArgumentException("Point coordinates must be finite");
         }
      }

      CorpseTargetSelector.Point subtract(CorpseTargetSelector.Point other) {
         return new CorpseTargetSelector.Point(this.x - other.x, this.y - other.y, this.z - other.z);
      }

      double dot(CorpseTargetSelector.Point other) {
         return this.x * other.x + this.y * other.y + this.z * other.z;
      }

      double length() {
         return Math.sqrt(this.dot(this));
      }

      CorpseTargetSelector.Point normalized() {
         double length = this.length();
         if (length <= 1.0E-9) {
            throw new IllegalArgumentException("Look direction cannot be zero");
         } else {
            return new CorpseTargetSelector.Point(this.x / length, this.y / length, this.z / length);
         }
      }
   }

   private record Scored(CorpseTargetSelector.Candidate candidate, double distance, double perpendicular, double cosine) {
   }
}
