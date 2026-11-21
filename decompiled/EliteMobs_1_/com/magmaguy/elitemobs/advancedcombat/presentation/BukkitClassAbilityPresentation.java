package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class BukkitClassAbilityPresentation {
   public BukkitClassAbilityPresentation.Session begin(Player caster, ClassLineage lineage, FixedAbilitySpec spec) {
      return new BukkitClassAbilityPresentation.Session(
         Objects.requireNonNull(caster, "caster"), Objects.requireNonNull(spec, "spec"), ClassAbilityPresentationPlanner.plan(lineage, spec)
      );
   }

   private static boolean tracesTransfer(AbilityFamily family) {
      return family == AbilityFamily.SAFE_BLINK || family == AbilityFamily.ALLY_FLIGHT;
   }

   private static boolean sameWorld(Location first, Location second) {
      return first != null && second != null && first.getWorld() != null && first.getWorld().equals(second.getWorld());
   }

   private static void burst(Location location, Particle primary, Particle accent, int count, double horizontalSpread, double verticalSpread) {
      World world = location.getWorld();
      if (world != null && count > 0) {
         int accentCount = Math.max(1, count / 3);
         int primaryCount = count - accentCount;
         if (primaryCount > 0) {
            world.spawnParticle(primary, location, primaryCount, horizontalSpread, verticalSpread, horizontalSpread, 0.025);
         }

         world.spawnParticle(accent, location, accentCount, horizontalSpread * 0.7, verticalSpread * 0.8, horizontalSpread * 0.7, 0.012);
      }
   }

   private static Particle cueParticle(AbilityPresentationCue cue, AbilityPresentationTheme theme, BukkitClassAbilityPresentation.Palette palette) {
      if (cue == AbilityPresentationCue.HEAL) {
         return switch (theme) {
            case NATURE -> Particle.COMPOSTER;
            case SPIRIT, SHADOW -> Particle.SOUL;
            default -> Particle.HAPPY_VILLAGER;
         };
      } else if (cue == AbilityPresentationCue.BUFF) {
         return switch (theme) {
            case SPIRIT, SHADOW -> Particle.SOUL;
            case FROST -> Particle.SNOWFLAKE;
            default -> Particle.END_ROD;
         };
      } else {
         return palette.primary();
      }
   }

   private static Sound cueSound(AbilityPresentationCue cue, BukkitClassAbilityPresentation.Palette palette) {
      return switch (cue) {
         case HEAL -> Sound.BLOCK_AMETHYST_BLOCK_CHIME;
         case BUFF -> Sound.BLOCK_BEACON_POWER_SELECT;
         default -> palette.impactSound();
      };
   }

   private static float cuePitch(AbilityPresentationCue cue) {
      return switch (cue) {
         case HEAL -> 1.45F;
         default -> 1.05F;
         case BUFF -> 1.3F;
         case CONTROL -> 0.85F;
      };
   }

   private static BukkitClassAbilityPresentation.Palette palette(AbilityPresentationTheme theme) {
      return switch (theme) {
         case NATURE -> new BukkitClassAbilityPresentation.Palette(
            Particle.COMPOSTER, Particle.HAPPY_VILLAGER, Sound.BLOCK_GRASS_PLACE, Sound.BLOCK_AMETHYST_BLOCK_CHIME
         );
         case SPIRIT -> new BukkitClassAbilityPresentation.Palette(
            Particle.SOUL, Particle.END_ROD, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.ENTITY_EVOKER_CAST_SPELL
         );
         case SHADOW -> new BukkitClassAbilityPresentation.Palette(
            Particle.SOUL, Particle.SCULK_SOUL, Sound.ENTITY_WITHER_SHOOT, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE
         );
         case FROST -> new BukkitClassAbilityPresentation.Palette(Particle.SNOWFLAKE, Particle.CLOUD, Sound.ENTITY_SNOWBALL_THROW, Sound.BLOCK_GLASS_BREAK);
         case VALOR -> new BukkitClassAbilityPresentation.Palette(Particle.END_ROD, Particle.ENCHANT, Sound.BLOCK_BEACON_POWER_SELECT, Sound.ITEM_SHIELD_BLOCK);
         case FURY -> new BukkitClassAbilityPresentation.Palette(
            Particle.CRIT, Particle.DAMAGE_INDICATOR, Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_IRON_GOLEM_ATTACK
         );
         case HUNT -> new BukkitClassAbilityPresentation.Palette(
            Particle.CRIT, Particle.ELECTRIC_SPARK, Sound.ENTITY_ARROW_SHOOT, Sound.ENTITY_PLAYER_ATTACK_CRIT
         );
         case ENGINEERING -> new BukkitClassAbilityPresentation.Palette(
            Particle.SMOKE, Particle.ELECTRIC_SPARK, Sound.BLOCK_PISTON_EXTEND, Sound.ENTITY_FIREWORK_ROCKET_BLAST
         );
         case STORM -> new BukkitClassAbilityPresentation.Palette(
            Particle.ELECTRIC_SPARK, Particle.CLOUD, Sound.ITEM_TRIDENT_RETURN, Sound.ENTITY_LIGHTNING_BOLT_IMPACT
         );
         case RADIANT -> new BukkitClassAbilityPresentation.Palette(
            Particle.HAPPY_VILLAGER, Particle.END_ROD, Sound.BLOCK_AMETHYST_BLOCK_CHIME, Sound.BLOCK_BEACON_POWER_SELECT
         );
         case ARCANE -> new BukkitClassAbilityPresentation.Palette(
            Particle.WITCH, Particle.ENCHANT, Sound.ENTITY_EVOKER_CAST_SPELL, Sound.BLOCK_ENCHANTMENT_TABLE_USE
         );
         case FLAME -> new BukkitClassAbilityPresentation.Palette(Particle.FLAME, Particle.SOUL_FIRE_FLAME, Sound.ENTITY_BLAZE_SHOOT, Sound.ITEM_FIRECHARGE_USE);
      };
   }

   private record Palette(Particle primary, Particle accent, Sound castSound, Sound impactSound) {
   }

   public static final class Session {
      private final Player caster;
      private final FixedAbilitySpec spec;
      private final AbilityPresentationPlan plan;
      private final AbilityPresentationBudgetLedger budget;
      private final Set<UUID> renderedTargets = new HashSet<>();
      private boolean castRendered;
      private boolean mobilityStarted;
      private boolean mobilityFinished;

      private Session(Player caster, FixedAbilitySpec spec, AbilityPresentationPlan plan) {
         this.caster = caster;
         this.spec = spec;
         this.plan = plan;
         this.budget = plan.newBudgetLedger();
      }

      public void commit() {
         this.ensureCast();
      }

      public void effects(Location origin, List<? extends LivingEntity> enemies, List<? extends Player> allies) {
         this.ensureCast();

         for (LivingEntity enemy : enemies) {
            if (!this.budget.canRender(AbilityPresentationCue.IMPACT) && !this.budget.canRender(AbilityPresentationCue.CONTROL)) {
               break;
            }

            if (enemy != null && this.renderedTargets.add(enemy.getUniqueId())) {
               Location target = center(enemy, origin);
               this.renderTarget(AbilityPresentationCue.IMPACT, target);
               this.renderTarget(AbilityPresentationCue.CONTROL, target);
            }
         }

         for (Player ally : allies) {
            if (!this.budget.canRender(AbilityPresentationCue.HEAL) && !this.budget.canRender(AbilityPresentationCue.BUFF)) {
               break;
            }

            if (ally != null && this.renderedTargets.add(ally.getUniqueId())) {
               Location target = center(ally, origin);
               this.renderTarget(AbilityPresentationCue.HEAL, target);
               this.renderTarget(AbilityPresentationCue.BUFF, target);
            }
         }
      }

      public void mobility(Location departure, Location arrival) {
         this.ensureCast();
         if (!this.mobilityFinished && this.plan.cues().contains(AbilityPresentationCue.MOBILITY)) {
            if (departure != null && departure.getWorld() != null) {
               boolean renderDeparture = !this.mobilityStarted;
               boolean hasArrival = BukkitClassAbilityPresentation.sameWorld(departure, arrival) && departure.distanceSquared(arrival) > 1.0E-6;
               List<Double> samples = hasArrival && BukkitClassAbilityPresentation.tracesTransfer(this.spec.family())
                  ? this.plan.traceProgress(departure.distance(arrival))
                  : List.of();
               int desiredParticles = (renderDeparture ? 12 : 0) + samples.size() * 2 + (hasArrival ? 18 : 0);
               int desiredSounds = this.spec.family() == AbilityFamily.SAFE_BLINK && hasArrival ? 3 : (renderDeparture ? 1 : 0) + (hasArrival ? 1 : 0);
               AbilityPresentationBudgetLedger.Grant grant = this.budget.claim(AbilityPresentationCue.MOBILITY, desiredParticles, desiredSounds);
               if (grant.granted()) {
                  BukkitClassAbilityPresentation.Palette palette = BukkitClassAbilityPresentation.palette(this.plan.theme());
                  int particles = grant.particles();
                  if (renderDeparture) {
                     int departureParticles = Math.min(12, particles);
                     BukkitClassAbilityPresentation.burst(
                        departure.clone().add(0.0, 0.8, 0.0), palette.primary(), palette.accent(), departureParticles, 0.38, 0.55
                     );
                     particles -= departureParticles;
                     this.mobilityStarted = true;
                  }

                  if (!samples.isEmpty() && particles > 0) {
                     int arrivalReserve = Math.min(18, particles);
                     int traceParticles = particles - arrivalReserve;
                     this.renderTrace(departure, arrival, samples, palette, traceParticles);
                     particles = arrivalReserve;
                  }

                  if (hasArrival && particles > 0) {
                     BukkitClassAbilityPresentation.burst(arrival.clone().add(0.0, 0.8, 0.0), palette.accent(), palette.primary(), particles, 0.4, 0.65);
                  }

                  this.playMobilitySounds(departure, arrival, palette, grant.sounds(), renderDeparture, hasArrival);
                  if (hasArrival) {
                     this.mobilityFinished = true;
                  }
               }
            }
         }
      }

      private void ensureCast() {
         if (!this.castRendered) {
            this.castRendered = true;
            Location origin = this.caster.getLocation().clone().add(0.0, this.caster.getHeight() * 0.55, 0.0);
            if (origin.getWorld() != null) {
               AbilityPresentationBudgetLedger.Grant grant = this.budget.claim(AbilityPresentationCue.CAST, 16, 1);
               if (grant.granted()) {
                  BukkitClassAbilityPresentation.Palette palette = BukkitClassAbilityPresentation.palette(this.plan.theme());
                  BukkitClassAbilityPresentation.burst(origin, palette.primary(), palette.accent(), grant.particles(), 0.32, 0.5);
                  if (grant.sounds() > 0) {
                     origin.getWorld().playSound(origin, palette.castSound(), 0.65F, 1.18F);
                  }
               }
            }
         }
      }

      private void renderTarget(AbilityPresentationCue cue, Location target) {
         if (target != null && target.getWorld() != null && this.plan.cues().contains(cue)) {
            int desiredParticles = switch (cue) {
               case HEAL -> 16;
               case IMPACT -> 14;
               case BUFF -> 12;
               case CONTROL -> 10;
               default -> 0;
            };
            AbilityPresentationBudgetLedger.Grant grant = this.budget.claim(cue, desiredParticles, 1);
            if (grant.granted()) {
               BukkitClassAbilityPresentation.Palette palette = BukkitClassAbilityPresentation.palette(this.plan.theme());
               Particle primary = BukkitClassAbilityPresentation.cueParticle(cue, this.plan.theme(), palette);
               Particle accent = cue == AbilityPresentationCue.HEAL ? Particle.END_ROD : palette.accent();
               BukkitClassAbilityPresentation.burst(target, primary, accent, grant.particles(), 0.3, 0.45);
               if (grant.sounds() > 0) {
                  target.getWorld()
                     .playSound(target, BukkitClassAbilityPresentation.cueSound(cue, palette), 0.55F, BukkitClassAbilityPresentation.cuePitch(cue));
               }
            }
         }
      }

      private void renderTrace(Location start, Location end, List<Double> samples, BukkitClassAbilityPresentation.Palette palette, int particles) {
         if (particles > 0 && !samples.isEmpty()) {
            Vector delta = end.toVector().subtract(start.toVector());
            int remaining = particles;

            for (int index = 0; index < samples.size() && remaining > 0; index++) {
               double progress = samples.get(index);
               Location point = start.clone().add(delta.clone().multiply(progress));
               point.add(0.0, 0.8 + Math.sin(progress * Math.PI) * 0.18, 0.0);
               int remainingSamples = samples.size() - index;
               int count = Math.max(1, remaining / remainingSamples);
               count = Math.min(count, remaining);
               Particle primary = index % 2 == 0 ? palette.primary() : palette.accent();
               point.getWorld().spawnParticle(primary, point, count, 0.045, 0.045, 0.045, 0.005);
               remaining -= count;
            }
         }
      }

      private void playMobilitySounds(
         Location departure, Location arrival, BukkitClassAbilityPresentation.Palette palette, int sounds, boolean renderDeparture, boolean hasArrival
      ) {
         if (sounds > 0 && departure.getWorld() != null) {
            if (this.spec.family() == AbilityFamily.SAFE_BLINK && hasArrival) {
               departure.getWorld().playSound(departure, Sound.ENTITY_ENDERMAN_TELEPORT, 0.62F, 1.55F);
               if (--sounds > 0 && arrival.getWorld() != null) {
                  arrival.getWorld().playSound(arrival, Sound.ENTITY_ENDERMAN_TELEPORT, 0.88F, 1.18F);
                  sounds--;
               }

               if (sounds > 0 && arrival.getWorld() != null) {
                  arrival.getWorld().playSound(arrival, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.58F, 1.35F);
               }
            } else {
               if (renderDeparture) {
                  departure.getWorld().playSound(departure, palette.castSound(), 0.55F, 1.35F);
                  sounds--;
               }

               if (sounds > 0 && hasArrival && arrival.getWorld() != null) {
                  arrival.getWorld().playSound(arrival, palette.impactSound(), 0.65F, 1.1F);
               }
            }
         }
      }

      private static Location center(LivingEntity entity, Location fallback) {
         Location location = entity.getLocation().add(0.0, entity.getHeight() * 0.55, 0.0);
         return location.getWorld() == null ? fallback : location;
      }
   }
}
