package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

final class ScriptWorldActionExecutor {
   private final ScriptAction action;

   ScriptWorldActionExecutor(ScriptAction action) {
      this.action = action;
   }

   void runStrikeLightning(ScriptActionData scriptActionData) {
      this.action.getLocationTargets(scriptActionData).forEach(LightningSpawnBypass::strikeLightningIgnoreProtections);
   }

   void runSpawnParticle(ScriptActionData scriptActionData) {
      boolean needsCentering = switch (scriptActionData.getTargetType()) {
         case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER, LOCATION, LOCATIONS, LANDING_LOCATION -> true;
         default -> false;
      };
      this.action.getLocationTargets(scriptActionData).forEach(location -> {
         Location targetLocation = needsCentering ? location.clone().add(0.5, 0.0, 0.5) : location;
         this.action.scriptParticles().visualize(scriptActionData, targetLocation, this.action.runtimeOwner());
      });
   }

   void runSetMobAI(ScriptActionData scriptActionData) {
      boolean aiEnabled = this.action.getBlueprint().getBValue();
      int duration = this.action.getBlueprint().getDuration().getValue();
      this.action.getTargets(scriptActionData).forEach(target -> TimedScriptStateManager.apply(target.getUniqueId(), "mob_ai", aiEnabled, duration, () -> {
         EliteEntity elite = this.resolveStateOwner(scriptActionData, target);
         return elite == null ? target.hasAI() : elite.isAIEnabled();
      }, value -> {
         EliteEntity elite = this.resolveStateOwner(scriptActionData, target);
         if (elite == null) {
            target.setAI(value);
         } else {
            elite.setAIEnabled(value);
         }
      }));
   }

   void runSetMobAware(ScriptActionData scriptActionData) {
      boolean aware = this.action.getBlueprint().getBValue();
      int duration = this.action.getBlueprint().getDuration().getValue();
      this.action
         .getTargets(scriptActionData)
         .forEach(
            target -> {
               if (target instanceof Mob mob) {
                  TimedScriptStateManager.apply(mob.getUniqueId(), "mob_aware", aware, duration, () -> {
                     EliteEntity elite = this.resolveStateOwner(scriptActionData, mob);
                     return elite == null ? mob.isAware() : elite.isAware();
                  }, value -> {
                     EliteEntity elite = this.resolveStateOwner(scriptActionData, mob);
                     if (elite == null) {
                        mob.setAware(value);
                     } else {
                        elite.setAware(value);
                     }
                  });
               } else {
                  Logger.warn(
                     "SET_MOB_AWARE action must target mobs! Problematic script: '"
                        + this.action.getBlueprint().getScriptName()
                        + "' in file '"
                        + this.action.getBlueprint().getScriptFilename()
                        + "'"
                  );
               }
            }
         );
   }

   private EliteEntity resolveStateOwner(ScriptActionData data, LivingEntity body) {
      if (body.isDead()) {
         return null;
      } else {
         EliteEntity owner = data.getEliteEntity();
         if (this.ownsBody(owner, body)) {
            return owner;
         } else {
            owner = EntityTracker.getEliteMobEntity(body);
            return this.ownsBody(owner, body) ? owner : null;
         }
      }
   }

   private boolean ownsBody(EliteEntity owner, LivingEntity body) {
      return owner != null && (owner.getLivingEntity() == body || owner.getUnsyncedLivingEntity() == body);
   }

   void runPlaySound(ScriptActionData scriptActionData) {
      String sound = this.action.getBlueprint().getSValue();
      float volume = this.action.getBlueprint().getVolume().getValue();
      float pitch = this.action.getBlueprint().getPitch().getValue();
      this.action
         .getLocationTargets(scriptActionData)
         .forEach(
            location -> {
               try {
                  location.getWorld().playSound(location, sound, volume, pitch);
               } catch (Exception var6) {
                  Logger.warn(
                     "Failed to play sound '"
                        + sound
                        + "' at location '"
                        + location
                        + "' in script '"
                        + this.action.getBlueprint().getScriptName()
                        + "': "
                        + var6.getMessage()
                  );
               }
            }
         );
   }

   void runPush(final ScriptActionData scriptActionData) {
      Vector velocity = this.action.getBlueprint().getScriptRelativeVectorBlueprint() != null
         ? new ScriptRelativeVector(
               this.action.getBlueprint().getScriptRelativeVectorBlueprint(), this.action.runtimeOwner(), scriptActionData.getEliteEntity().getLocation()
            )
            .getVector(scriptActionData)
         : this.action.getBlueprint().getVValue();
      if (velocity == null || !this.isFiniteVector(velocity)) {
         velocity = new Vector(0, 0, 0);
      }

      final boolean additive = this.action.getBlueprint().getBValue() != null && this.action.getBlueprint().getBValue();
      final Vector localFinalVelocity = velocity;
      (new BukkitRunnable() {
         public void run() {
            Vector finalVelocity = localFinalVelocity;
            ScriptWorldActionExecutor.this.action.getTargets(scriptActionData).forEach(target -> {
               if (additive) {
                  target.setVelocity(target.getVelocity().add(finalVelocity));
               } else {
                  target.setVelocity(finalVelocity);
               }
            });
         }
      }).runTaskLater(MetadataHandler.PLUGIN, 1L);
   }

   private boolean isFiniteVector(Vector vector) {
      return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
   }

   void runSummonReinforcement(ScriptActionData scriptActionData) {
      this.action
         .getLocationTargets(scriptActionData)
         .forEach(
            location -> {
               CustomBossEntity customBossEntity = CustomSummonPower.summonReinforcement(
                  scriptActionData.getEliteEntity(), location, this.action.getBlueprint().getSValue(), this.action.getBlueprint().getDuration().getValue()
               );
               if (customBossEntity != null && customBossEntity.getLivingEntity() != null) {
                  Vector velocity = this.action.getBlueprint().getScriptRelativeVectorBlueprint() != null
                     ? new ScriptRelativeVector(
                           this.action.getBlueprint().getScriptRelativeVectorBlueprint(),
                           this.action.runtimeOwner(),
                           customBossEntity.getLivingEntity().getLocation()
                        )
                        .getVector(scriptActionData)
                     : this.action.getBlueprint().getVValue();
                  if (velocity != null) {
                     customBossEntity.getLivingEntity().setVelocity(velocity);
                  }
               }
            }
         );
   }

   void runSpawnFireworks(ScriptActionData scriptActionData) {
      if (this.action.getBlueprint().getFireworkEffects().isEmpty()) {
         Logger.warn(
            "No colors set for fireworks in script '"
               + this.action.getBlueprint().getScriptName()
               + "' in file '"
               + this.action.getBlueprint().getScriptFilename()
               + "'"
         );
      } else {
         this.action
            .getLocationTargets(scriptActionData)
            .forEach(
               location -> {
                  try {
                     Firework firework = (Firework)location.getWorld().spawn(location, Firework.class);
                     firework.setPersistent(false);
                     FireworkMeta fireworkMeta = firework.getFireworkMeta();
                     List<Type> types = this.action.getBlueprint().getFireworkEffectTypes();
                     List<List<Color>> colorsList = this.action
                        .getBlueprint()
                        .getFireworkEffects()
                        .stream()
                        .map(colorsx -> colorsx.stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList())
                        .toList();
                     if (types == null || types.isEmpty()) {
                        types = List.of(this.action.getBlueprint().getFireworkEffectType());
                     }

                     for (int i = 0; i < types.size(); i++) {
                        Type type = types.get(i);
                        List<Color> colors = i < colorsList.size() ? colorsList.get(i) : colorsList.get(colorsList.size() - 1);
                        FireworkEffect effect = FireworkEffect.builder()
                           .with(type)
                           .withColor(colors)
                           .flicker(this.action.getBlueprint().isFlicker())
                           .trail(this.action.getBlueprint().isWithTrail())
                           .build();
                        fireworkMeta.addEffect(effect);
                     }

                     fireworkMeta.setPower(this.action.getBlueprint().getPower().getValue());
                     if (this.action.getBlueprint().getVValue() != null) {
                        firework.setVelocity(this.action.getBlueprint().getVValue());
                        firework.setShotAtAngle(true);
                     }

                     firework.setFireworkMeta(fireworkMeta);
                  } catch (Exception var10) {
                     Logger.warn(
                        "Failed to spawn fireworks at location '"
                           + location
                           + "' in script '"
                           + this.action.getBlueprint().getScriptName()
                           + "': "
                           + var10.getMessage()
                     );
                  }
               }
            );
      }
   }

   void runMakeInvulnerable(ScriptActionData scriptActionData) {
      boolean invulnerable = this.action.getBlueprint().isInvulnerable();
      int duration = this.action.getBlueprint().getDuration().getValue();
      this.action.getTargets(scriptActionData).forEach(target -> TimedScriptStateManager.applyInvulnerability(this.action, target, invulnerable, duration));
   }

   void runTag(ScriptActionData scriptActionData) {
      List<String> tags = this.action.getBlueprint().getTags();
      int duration = this.action.getBlueprint().getDuration().getValue();
      this.action
         .getTargets(scriptActionData)
         .forEach(
            target -> {
               EliteEntity bossEntity = EntityTracker.getEliteMobEntity(target);
               if (bossEntity != null) {
                  for (String tag : tags) {
                     TimedScriptStateManager.apply(
                        target.getUniqueId(),
                        "elite_tag:" + tag,
                        true,
                        duration,
                        () -> bossEntity.getTags().contains(tag),
                        present -> setEliteTag(bossEntity, tag, present)
                     );
                  }
               }

               if (target instanceof Player player) {
                  ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                  if (playerInventory != null) {
                     for (String tag : tags) {
                        TimedScriptStateManager.apply(
                           player.getUniqueId(),
                           "player_tag:" + tag,
                           true,
                           duration,
                           () -> playerInventory.getTags().contains(tag),
                           present -> setPlayerTag(playerInventory, tag, present)
                        );
                     }
                  }
               }
            }
         );
   }

   void runUntag(ScriptActionData scriptActionData) {
      List<String> tags = this.action.getBlueprint().getTags();
      int duration = this.action.getBlueprint().getDuration().getValue();
      this.action
         .getTargets(scriptActionData)
         .forEach(
            target -> {
               EliteEntity bossEntity = EntityTracker.getEliteMobEntity(target);
               if (bossEntity != null) {
                  for (String tag : tags) {
                     TimedScriptStateManager.apply(
                        target.getUniqueId(),
                        "elite_tag:" + tag,
                        false,
                        duration,
                        () -> bossEntity.getTags().contains(tag),
                        present -> setEliteTag(bossEntity, tag, present)
                     );
                  }
               }

               if (target instanceof Player player) {
                  ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                  if (playerInventory != null) {
                     for (String tag : tags) {
                        TimedScriptStateManager.apply(
                           player.getUniqueId(),
                           "player_tag:" + tag,
                           false,
                           duration,
                           () -> playerInventory.getTags().contains(tag),
                           present -> setPlayerTag(playerInventory, tag, present)
                        );
                     }
                  }
               }
            }
         );
   }

   void runSetTime(ScriptActionData scriptActionData) {
      long time = this.action.getBlueprint().getTime().getValue().intValue();
      this.action
         .getLocationTargets(scriptActionData)
         .forEach(
            location -> {
               try {
                  location.getWorld().setTime(time);
               } catch (Exception var5) {
                  Logger.warn(
                     "Failed to set time in world '"
                        + location.getWorld().getName()
                        + "' in script '"
                        + this.action.getBlueprint().getScriptName()
                        + "': "
                        + var5.getMessage()
                  );
               }
            }
         );
   }

   void runSetWeather(ScriptActionData scriptActionData) {
      int duration = this.action.getBlueprint().getDuration().getValue();
      this.action
         .getTargets(scriptActionData)
         .forEach(
            target -> {
               final World world = target.getWorld();

               try {
                  switch (this.action.getBlueprint().getWeatherType()) {
                     case CLEAR:
                        world.setStorm(false);
                        world.setThundering(false);
                        world.setWeatherDuration(duration > 0 ? duration : 6000);
                        break;
                     case PRECIPITATION:
                        world.setStorm(true);
                        world.setThundering(false);
                        world.setWeatherDuration(duration > 0 ? duration : 6000);
                        if (duration > 0) {
                           (new BukkitRunnable() {
                              public void run() {
                                 world.setStorm(false);
                              }
                           }).runTaskLater(MetadataHandler.PLUGIN, duration + 1);
                        }
                        break;
                     case THUNDER:
                        world.setStorm(true);
                        world.setThundering(true);
                        world.setThunderDuration(duration > 0 ? duration : 6000);
                        if (duration > 0) {
                           (new BukkitRunnable() {
                              public void run() {
                                 world.setStorm(false);
                                 world.setThundering(false);
                              }
                           }).runTaskLater(MetadataHandler.PLUGIN, duration + 1);
                        }
                  }
               } catch (Exception var5) {
                  Logger.warn(
                     "Failed to set weather in world '"
                        + world.getName()
                        + "' in script '"
                        + this.action.getBlueprint().getScriptName()
                        + "': "
                        + var5.getMessage()
                  );
               }
            }
         );
   }

   private static void setEliteTag(EliteEntity eliteEntity, String tag, boolean present) {
      if (present) {
         eliteEntity.addTags(List.of(tag));
      } else {
         eliteEntity.removeTags(List.of(tag));
      }
   }

   private static void setPlayerTag(ElitePlayerInventory inventory, String tag, boolean present) {
      if (present) {
         inventory.addTags(List.of(tag));
      } else {
         inventory.removeTags(List.of(tag));
      }
   }
}
