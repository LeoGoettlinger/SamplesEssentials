package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ClassProgressionModule {
   private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger();
   private final ClassCatalog catalog;
   private final int catalogVersion;
   private final FoundationLevels foundationLevels;
   private final ClassContentAvailability contentAvailability;
   private final ClassProgressionStore store;
   private final ClassProgressionModule.SerialExecutor persistenceExecutor;
   private final ExecutorService ownedExecutor;
   private final Map<UUID, ClassProgressionModule.CachedPlayer> players = new ConcurrentHashMap<>();
   private final Map<UUID, ClassProgressionModule.LockedRun> runSelections = new ConcurrentHashMap<>();
   private final AtomicBoolean closed = new AtomicBoolean();
   private final AtomicReference<Throwable> persistenceFailure = new AtomicReference<>();
   private CompletableFuture<Void> closeFuture;

   public ClassProgressionModule(ClassCatalog catalog, FoundationLevels foundationLevels, ClassProgressionStore store) {
      this(catalog, foundationLevels, store, ClassContentAvailability.ALL_AVAILABLE);
   }

   public ClassProgressionModule(
      ClassCatalog catalog, FoundationLevels foundationLevels, ClassProgressionStore store, ClassContentAvailability contentAvailability
   ) {
      this.catalog = Objects.requireNonNull(catalog, "catalog");
      this.catalogVersion = catalog.persistenceVersion();
      this.foundationLevels = Objects.requireNonNull(foundationLevels, "foundationLevels");
      this.contentAvailability = Objects.requireNonNull(contentAvailability, "contentAvailability");
      this.store = Objects.requireNonNull(store, "store");
      this.ownedExecutor = Executors.newSingleThreadExecutor(task -> {
         Thread thread = new Thread(task, "EliteMobs-ClassProgression-" + THREAD_SEQUENCE.incrementAndGet());
         thread.setDaemon(true);
         return thread;
      });
      this.persistenceExecutor = new ClassProgressionModule.SerialExecutor(this.ownedExecutor);
   }

   public ClassProgressionModule(ClassCatalog catalog, FoundationLevels foundationLevels, ClassProgressionStore store, Executor persistenceExecutor) {
      this(catalog, foundationLevels, store, persistenceExecutor, ClassContentAvailability.ALL_AVAILABLE);
   }

   public ClassProgressionModule(
      ClassCatalog catalog,
      FoundationLevels foundationLevels,
      ClassProgressionStore store,
      Executor persistenceExecutor,
      ClassContentAvailability contentAvailability
   ) {
      this.catalog = Objects.requireNonNull(catalog, "catalog");
      this.catalogVersion = catalog.persistenceVersion();
      this.foundationLevels = Objects.requireNonNull(foundationLevels, "foundationLevels");
      this.contentAvailability = Objects.requireNonNull(contentAvailability, "contentAvailability");
      this.store = Objects.requireNonNull(store, "store");
      this.ownedExecutor = null;
      this.persistenceExecutor = new ClassProgressionModule.SerialExecutor(Objects.requireNonNull(persistenceExecutor, "persistenceExecutor"));
   }

   public static ClassProgressionModule builtIn(FoundationLevels foundationLevels, ClassProgressionStore store) {
      return new ClassProgressionModule(BuiltInClassContent.catalog(), foundationLevels, store);
   }

   public static ClassProgressionModule builtIn(FoundationLevels foundationLevels, ClassProgressionStore store, Executor persistenceExecutor) {
      return new ClassProgressionModule(BuiltInClassContent.catalog(), foundationLevels, store, persistenceExecutor);
   }

   public CompletableFuture<ProfileSnapshot> load(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      if (this.closed.get()) {
         return CompletableFuture.failedFuture(new IllegalStateException("Progression Module is closed"));
      } else {
         ClassProgressionModule.CachedPlayer candidate = new ClassProgressionModule.CachedPlayer();
         ClassProgressionModule.CachedPlayer state = this.players.putIfAbsent(playerId, candidate);
         if (state != null) {
            synchronized (state.monitor) {
               if (state.readiness == ProgressionReadiness.READY) {
                  return CompletableFuture.completedFuture(this.snapshotLocked(playerId, state, this.captureFoundationLevels(playerId)));
               } else {
                  return state.readiness == ProgressionReadiness.FAILED
                     ? CompletableFuture.failedFuture(
                        (Throwable)(state.failure == null ? new IllegalStateException("Player progression is unavailable") : state.failure)
                     )
                     : state.loadFuture;
               }
            }
         } else {
            this.<ProfileSnapshot>submitPersistence(() -> this.hydrate(playerId, candidate)).whenComplete((snapshot, failure) -> {
               if (failure == null) {
                  candidate.loadFuture.complete(snapshot);
               } else {
                  Throwable cause = unwrap(failure);
                  if (cause instanceof SQLException) {
                     this.persistenceFailure.compareAndSet(null, cause);
                  }

                  this.fail(playerId, candidate, cause);
                  candidate.loadFuture.completeExceptionally(cause);
               }
            });
            return candidate.loadFuture;
         }
      }
   }

   public ProgressionReadiness readiness(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      if (this.closed.get()) {
         return ProgressionReadiness.CLOSED;
      } else {
         ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
         return state == null ? ProgressionReadiness.UNLOADED : state.readiness;
      }
   }

   public boolean isReady(UUID playerId) {
      return this.readiness(playerId) == ProgressionReadiness.READY;
   }

   public Optional<Throwable> failure(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
      return state == null ? Optional.empty() : Optional.ofNullable(state.failure);
   }

   public Optional<ClassProgressionModule.ActiveProfileSnapshot> activeSnapshot(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
      if (!this.closed.get() && state != null) {
         synchronized (state.monitor) {
            return state.readiness == ProgressionReadiness.READY && this.players.get(playerId) == state
               ? this.foundationLevels
                  .snapshot(playerId)
                  .map(
                     foundation -> {
                        if (!playerId.equals(foundation.playerId())) {
                           throw new IllegalStateException("Foundation adapter returned another player's levels");
                        } else {
                           ClassProgressionModule.LockedRun run = this.runSelections.get(playerId);
                           String formId = run == null ? state.profile.selectedFormId() : run.selection().formId();
                           FormProgressSnapshot form = formId == null
                              ? null
                              : this.formSnapshot(this.catalog.require(formId), state, foundation.levels(), new HashMap<>());
                           return new ClassProgressionModule.ActiveProfileSnapshot(
                              run == null ? null : run.runId(),
                              run == null ? this.selectedInputProfile(state.profile) : run.selection().inputProfile(),
                              form,
                              this.activeLineage(form)
                           );
                        }
                     }
                  )
               : Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public Optional<UUID> lockedRunId(UUID playerId) {
      ClassProgressionModule.CachedPlayer state = this.players.get(Objects.requireNonNull(playerId, "playerId"));
      if (!this.closed.get() && state != null) {
         synchronized (state.monitor) {
            if (state.readiness == ProgressionReadiness.READY && this.players.get(playerId) == state) {
               ClassProgressionModule.LockedRun run = this.runSelections.get(playerId);
               return run == null ? Optional.empty() : Optional.of(run.runId());
            } else {
               return Optional.empty();
            }
         }
      } else {
         return Optional.empty();
      }
   }

   public Optional<ProfileSnapshot> snapshot(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
      if (!this.closed.get() && state != null) {
         synchronized (state.monitor) {
            if (state.readiness != ProgressionReadiness.READY) {
               return Optional.empty();
            } else {
               return this.players.get(playerId) != state ? Optional.empty() : this.foundationLevels.snapshot(playerId).map(foundation -> {
                  if (!playerId.equals(foundation.playerId())) {
                     throw new IllegalStateException("Foundation adapter returned another player's levels");
                  } else {
                     return this.snapshotLocked(playerId, state, foundation.levels());
                  }
               });
            }
         }
      } else {
         return Optional.empty();
      }
   }

   public Optional<SkillTutorialProgress> tutorialProgress(UUID playerId) {
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      if (state == null) {
         return Optional.empty();
      } else {
         synchronized (state.monitor) {
            return !this.isReady(state) ? Optional.empty() : Optional.of(new SkillTutorialProgress(state.profile.tutorialSkillsUsed()));
         }
      }
   }

   public Optional<SkillTutorialProgress> recordTutorialSkill(UUID playerId, AbilitySlot slot) {
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      if (state == null) {
         return Optional.empty();
      } else {
         synchronized (state.monitor) {
            if (!this.isReady(state)) {
               return Optional.empty();
            } else {
               SkillTutorialProgress current = new SkillTutorialProgress(state.profile.tutorialSkillsUsed());
               if (current.hasUsed(slot)) {
                  return Optional.empty();
               } else {
                  SkillTutorialProgress updated = current.withUsed(slot);
                  state.profile = new StoredClassProfile(
                     playerId, state.profile.selectedFormId(), state.profile.selectedInputId(), this.catalogVersion, updated.usedSkills()
                  );
                  this.enqueueProfileSave(playerId, state);
                  return Optional.of(updated);
               }
            }
         }
      }
   }

   public void unload(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      this.players.computeIfPresent(playerId, (ignored, state) -> {
         synchronized (state.monitor) {
            state.readiness = ProgressionReadiness.UNLOADED;
            if (!state.loadFuture.isDone()) {
               state.loadFuture.completeExceptionally(new CancellationException("Player progression unloaded"));
            }

            return null;
         }
      });
   }

   public SelectionResult selectForm(UUID playerId, String formId) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(formId, "formId");
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      if (state == null) {
         return new SelectionResult(SelectionResult.Status.NOT_READY, null);
      } else {
         synchronized (state.monitor) {
            if (!this.isReady(state)) {
               return new SelectionResult(SelectionResult.Status.NOT_READY, null);
            } else {
               Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
               Optional<ClassFormDefinition> candidate = this.catalog.find(formId);
               if (candidate.isEmpty()) {
                  return new SelectionResult(SelectionResult.Status.UNKNOWN_FORM, this.snapshotLocked(playerId, state, levels));
               } else if (!this.isUnlocked(candidate.get(), state.progressXp, state.challenges, levels, new HashMap<>())) {
                  return new SelectionResult(SelectionResult.Status.LOCKED_FORM, this.snapshotLocked(playerId, state, levels));
               } else if (formId.equals(state.profile.selectedFormId())) {
                  return new SelectionResult(SelectionResult.Status.UNCHANGED, this.snapshotLocked(playerId, state, levels));
               } else {
                  state.profile = new StoredClassProfile(
                     playerId, formId, state.profile.selectedInputId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                  );
                  this.enqueueProfileSave(playerId, state);
                  return new SelectionResult(SelectionResult.Status.APPLIED, this.snapshotLocked(playerId, state, levels));
               }
            }
         }
      }
   }

   public SelectionResult clearSelectedForm(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      if (state == null) {
         return new SelectionResult(SelectionResult.Status.NOT_READY, null);
      } else {
         synchronized (state.monitor) {
            if (!this.isReady(state)) {
               return new SelectionResult(SelectionResult.Status.NOT_READY, null);
            } else {
               Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
               if (state.profile.selectedFormId() == null) {
                  return new SelectionResult(SelectionResult.Status.UNCHANGED, this.snapshotLocked(playerId, state, levels));
               } else {
                  state.profile = new StoredClassProfile(
                     playerId, null, state.profile.selectedInputId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                  );
                  this.enqueueProfileSave(playerId, state);
                  return new SelectionResult(SelectionResult.Status.APPLIED, this.snapshotLocked(playerId, state, levels));
               }
            }
         }
      }
   }

   public SelectionResult selectInputProfile(UUID playerId, InputProfile inputProfile) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(inputProfile, "inputProfile");
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      if (state == null) {
         return new SelectionResult(SelectionResult.Status.NOT_READY, null);
      } else {
         synchronized (state.monitor) {
            if (!this.isReady(state)) {
               return new SelectionResult(SelectionResult.Status.NOT_READY, null);
            } else {
               Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
               if (inputProfile.storedId().equals(state.profile.selectedInputId())) {
                  return new SelectionResult(SelectionResult.Status.UNCHANGED, this.snapshotLocked(playerId, state, levels));
               } else {
                  state.profile = new StoredClassProfile(
                     playerId, state.profile.selectedFormId(), inputProfile.storedId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                  );
                  this.enqueueProfileSave(playerId, state);
                  return new SelectionResult(SelectionResult.Status.APPLIED, this.snapshotLocked(playerId, state, levels));
               }
            }
         }
      }
   }

   public RunLockResult lockRun(UUID playerId, UUID runId) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(runId, "runId");
      if (this.closed.get()) {
         return new RunLockResult(RunLockResult.Status.NOT_READY, runId, null);
      } else {
         ClassProgressionModule.LockedRun existing = this.runSelections.get(playerId);
         if (existing != null) {
            RunLockResult.Status status = existing.runId().equals(runId) ? RunLockResult.Status.ALREADY_LOCKED : RunLockResult.Status.LOCKED_BY_ANOTHER_RUN;
            return new RunLockResult(status, existing.runId(), existing.selection());
         } else {
            ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
            if (state == null) {
               return new RunLockResult(RunLockResult.Status.NOT_READY, runId, null);
            } else {
               synchronized (state.monitor) {
                  if (!this.isReady(state)) {
                     return new RunLockResult(RunLockResult.Status.NOT_READY, runId, null);
                  } else {
                     String formId = state.profile.selectedFormId();
                     if (formId == null) {
                        return new RunLockResult(RunLockResult.Status.NO_SELECTED_FORM, runId, null);
                     } else {
                        ClassFormDefinition form = this.catalog.find(formId).orElse(null);
                        Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
                        if (form != null && this.isUnlocked(form, state.progressXp, state.challenges, levels, new HashMap<>())) {
                           RunSelection selection = new RunSelection(formId, this.selectedInputProfile(state.profile));
                           ClassProgressionModule.LockedRun lockedRun = new ClassProgressionModule.LockedRun(runId, selection);
                           ClassProgressionModule.LockedRun raced = this.runSelections.putIfAbsent(playerId, lockedRun);
                           if (raced != null) {
                              RunLockResult.Status status = raced.runId().equals(runId)
                                 ? RunLockResult.Status.ALREADY_LOCKED
                                 : RunLockResult.Status.LOCKED_BY_ANOTHER_RUN;
                              return new RunLockResult(status, raced.runId(), raced.selection());
                           } else {
                              return new RunLockResult(RunLockResult.Status.LOCKED, runId, selection);
                           }
                        } else {
                           return new RunLockResult(RunLockResult.Status.FORM_LOCKED, runId, null);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public Optional<RunSelection> runSelection(UUID playerId, UUID runId) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(runId, "runId");
      if (this.closed.get()) {
         return Optional.empty();
      } else {
         ClassProgressionModule.LockedRun lockedRun = this.runSelections.get(playerId);
         return lockedRun != null && lockedRun.runId().equals(runId) ? Optional.of(lockedRun.selection()) : Optional.empty();
      }
   }

   public Optional<RunSelection> activeRunSelection(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      if (this.closed.get()) {
         return Optional.empty();
      } else {
         ClassProgressionModule.LockedRun lockedRun = this.runSelections.get(playerId);
         return lockedRun == null ? Optional.empty() : Optional.of(lockedRun.selection());
      }
   }

   public boolean unlockRun(UUID playerId, UUID runId) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(runId, "runId");
      ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
      if (state == null) {
         ClassProgressionModule.LockedRun lockedRun = this.runSelections.get(playerId);
         return lockedRun != null && lockedRun.runId().equals(runId) && this.runSelections.remove(playerId, lockedRun);
      } else {
         synchronized (state.monitor) {
            ClassProgressionModule.LockedRun lockedRun = this.runSelections.get(playerId);
            return lockedRun != null && lockedRun.runId().equals(runId) && this.runSelections.remove(playerId, lockedRun);
         }
      }
   }

   public AwardResult award(UUID playerId, long requestedXp) {
      Objects.requireNonNull(playerId, "playerId");
      if (requestedXp <= 0L) {
         return emptyAward(AwardResult.Status.INVALID_AMOUNT, requestedXp);
      } else {
         ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
         if (state == null) {
            return emptyAward(AwardResult.Status.NOT_READY, requestedXp);
         } else {
            synchronized (state.monitor) {
               if (!this.isReady(state)) {
                  return emptyAward(AwardResult.Status.NOT_READY, requestedXp);
               } else {
                  ClassProgressionModule.LockedRun lockedRun = this.runSelections.get(playerId);
                  String formId = lockedRun == null ? state.profile.selectedFormId() : lockedRun.selection().formId();
                  if (formId == null) {
                     return emptyAward(AwardResult.Status.NO_SELECTED_FORM, requestedXp);
                  } else {
                     ClassFormDefinition form = this.catalog.find(formId).orElse(null);
                     Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
                     if (form != null && this.isUnlocked(form, state.progressXp, state.challenges, levels, new HashMap<>())) {
                        int localCap = form.localProgressionCap(levels::get);
                        int effectiveCap = form.effectiveProgressionCap(levels::get);
                        ProgressionCapReason capReason = capReason(form, levels);
                        long xpAtCap = xpAtLocalCap(form, localCap);
                        long storedXp = state.progressXp.getOrDefault(formId, 0L);
                        long previousXp = Math.min(storedXp, xpAtCap);
                        long headroom = xpAtCap - previousXp;
                        long appliedXp = Math.min(requestedXp, headroom);
                        long currentXp = previousXp + appliedXp;
                        long discardedXp = requestedXp - appliedXp;
                        if (appliedXp > 0L) {
                           state.progressXp.put(formId, currentXp);
                           this.enqueueProgressSave(playerId, state, formId, currentXp);
                        }

                        int previousLocalLevel = localLevelFromXp(form, previousXp);
                        int currentLocalLevel = localLevelFromXp(form, currentXp);
                        AwardResult.Status status;
                        if (appliedXp == 0L) {
                           status = AwardResult.Status.AT_CAP;
                        } else if (discardedXp > 0L) {
                           status = AwardResult.Status.PARTIALLY_APPLIED;
                        } else {
                           status = AwardResult.Status.APPLIED;
                        }

                        return new AwardResult(
                           status,
                           formId,
                           requestedXp,
                           appliedXp,
                           discardedXp,
                           previousXp,
                           currentXp,
                           previousLocalLevel,
                           currentLocalLevel,
                           form.band().toEffectiveLevel(currentLocalLevel),
                           localCap,
                           effectiveCap,
                           capReason,
                           limitingSkills(form, levels, capReason)
                        );
                     } else {
                        return this.lockedAward(formId, requestedXp, form, state.progressXp, levels);
                     }
                  }
               }
            }
         }
      }
   }

   public ClassProgressionSetResult setLineageForAdministration(UUID playerId, String formId, int effectiveLevel) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(formId, "formId");
      ClassFormDefinition requestedForm = this.catalog.find(formId).orElse(null);
      if (requestedForm == null) {
         return progressionSetResult(ClassProgressionSetResult.Status.UNKNOWN_FORM, formId, effectiveLevel, null, 0, List.of(), null);
      } else if (!requestedForm.band().containsEffectiveLevel(effectiveLevel)) {
         return progressionSetResult(
            ClassProgressionSetResult.Status.LEVEL_OUTSIDE_FORM_BAND,
            formId,
            effectiveLevel,
            requestedForm.id(),
            requestedForm.band().effectiveEnd(),
            List.of(),
            this.snapshot(playerId).orElse(null)
         );
      } else if (this.runSelections.containsKey(playerId)) {
         return progressionSetResult(
            ClassProgressionSetResult.Status.RUN_LOCKED, formId, effectiveLevel, null, 0, List.of(), this.snapshot(playerId).orElse(null)
         );
      } else {
         ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
         if (state == null) {
            return progressionSetResult(ClassProgressionSetResult.Status.NOT_READY, formId, effectiveLevel, null, 0, List.of(), null);
         } else {
            synchronized (state.monitor) {
               if (!this.isReady(state)) {
                  return progressionSetResult(ClassProgressionSetResult.Status.NOT_READY, formId, effectiveLevel, null, 0, List.of(), null);
               } else {
                  Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
                  List<ClassFormDefinition> progressionPath = this.catalog.progressionPathOf(formId);

                  for (ClassFormDefinition lineageForm : progressionPath) {
                     int neededLevel = lineageForm.id().equals(formId) ? effectiveLevel : lineageForm.band().effectiveEnd();
                     int effectiveCap = lineageForm.effectiveProgressionCap(levels::get);
                     if (neededLevel > effectiveCap) {
                        return progressionSetResult(
                           ClassProgressionSetResult.Status.FOUNDATION_SKILL_CAP,
                           formId,
                           effectiveLevel,
                           lineageForm.id(),
                           effectiveCap,
                           lineageForm.foundationSkills().limitingSkills(levels::get),
                           this.snapshotLocked(playerId, state, levels)
                        );
                     }
                  }

                  List<StoredClassProgress> persistedRows = new ArrayList<>();

                  for (ClassFormDefinition lineageFormx : progressionPath) {
                     int visibleLevel = lineageFormx.id().equals(formId) ? effectiveLevel : lineageFormx.band().effectiveEnd();
                     int localLevel = lineageFormx.band().toLocalLevel(visibleLevel);
                     long xp = xpAtLocalCap(lineageFormx, localLevel);
                     state.progressXp.put(lineageFormx.id(), xp);
                     state.challenges.add(lineageFormx.id());
                     persistedRows.add(new StoredClassProgress(playerId, lineageFormx.id(), xp, this.catalogVersion, true));
                  }

                  state.profile = new StoredClassProfile(
                     playerId, formId, state.profile.selectedInputId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                  );
                  StoredClassProfile persistedProfile = state.profile;
                  this.trackPersistence(playerId, state, this.submitInternal(() -> {
                     this.store.savePlayerAggregate(persistedProfile, persistedRows);
                     return null;
                  }));
                  return progressionSetResult(
                     ClassProgressionSetResult.Status.APPLIED,
                     formId,
                     effectiveLevel,
                     null,
                     effectiveLevel,
                     List.of(),
                     this.snapshotLocked(playerId, state, levels)
                  );
               }
            }
         }
      }
   }

   public ClassProgressionForgetResult forgetForAdministration(UUID playerId, String formId) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(formId, "formId");
      if (this.catalog.find(formId).isEmpty()) {
         return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.UNKNOWN_FORM, 0, false);
      } else {
         ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
         if (state == null) {
            return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.NOT_READY, 0, false);
         } else {
            synchronized (state.monitor) {
               if (!this.isReady(state)) {
                  return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.NOT_READY, 0, false);
               } else if (this.runSelections.containsKey(playerId)) {
                  return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.RUN_LOCKED, 0, false);
               } else {
                  Set<String> forgotten = new HashSet<>();
                  List<StoredClassProgress> rows = new ArrayList<>();

                  for (ClassFormDefinition form : this.catalog.forms()) {
                     if (!this.catalog.progressionPathOf(form.id()).stream().noneMatch(ancestor -> ancestor.id().equals(formId))) {
                        forgotten.add(form.id());
                        state.progressXp.put(form.id(), 0L);
                        state.challenges.remove(form.id());
                        rows.add(new StoredClassProgress(playerId, form.id(), 0L, this.catalogVersion, false));
                     }
                  }

                  boolean selectionCleared = forgotten.contains(state.profile.selectedFormId());
                  if (selectionCleared) {
                     state.profile = new StoredClassProfile(
                        playerId, null, state.profile.selectedInputId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                     );
                  }

                  StoredClassProfile profile = state.profile;
                  List<StoredClassProgress> persistedRows = List.copyOf(rows);
                  this.trackPersistence(playerId, state, this.submitInternal(() -> {
                     this.store.savePlayerAggregate(profile, persistedRows);
                     return null;
                  }));
                  return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.APPLIED, forgotten.size(), selectionCleared);
               }
            }
         }
      }
   }

   public int setAllLineagesForAdministration(UUID playerId, int effectiveLevel) {
      Objects.requireNonNull(playerId, "playerId");
      if (this.runSelections.containsKey(playerId)) {
         return 0;
      } else {
         ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
         if (state == null) {
            return 0;
         } else {
            synchronized (state.monitor) {
               if (!this.isReady(state)) {
                  return 0;
               } else {
                  Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
                  int formsAtLevel = 0;
                  List<StoredClassProgress> persistedRows = new ArrayList<>();

                  for (ClassFormDefinition form : this.catalog.forms()) {
                     long xp;
                     if (form.band().effectiveStart() > effectiveLevel) {
                        xp = 0L;
                     } else {
                        int visibleLevel = form.band().containsEffectiveLevel(effectiveLevel) ? effectiveLevel : form.band().effectiveEnd();
                        visibleLevel = Math.min(visibleLevel, form.effectiveProgressionCap(levels::get));
                        if (visibleLevel < form.band().effectiveStart()) {
                           xp = 0L;
                        } else {
                           if (visibleLevel == effectiveLevel) {
                              formsAtLevel++;
                           }

                           xp = xpAtLocalCap(form, form.band().toLocalLevel(visibleLevel));
                        }
                     }

                     state.progressXp.put(form.id(), xp);
                     boolean granted = form.requiredFoundationSkillLevel() <= effectiveLevel;
                     if (granted) {
                        state.challenges.add(form.id());
                     } else {
                        state.challenges.remove(form.id());
                     }

                     persistedRows.add(new StoredClassProgress(playerId, form.id(), xp, this.catalogVersion, granted));
                  }

                  String selectedFormId = state.profile.selectedFormId();
                  boolean selectionInvalid = selectedFormId != null
                     && this.catalog.find(selectedFormId).map(selected -> selected.requiredFoundationSkillLevel() > effectiveLevel).orElse(true);
                  if (selectionInvalid) {
                     state.profile = new StoredClassProfile(
                        playerId, null, state.profile.selectedInputId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                     );
                  }

                  StoredClassProfile persistedProfile = state.profile;
                  List<StoredClassProgress> rows = List.copyOf(persistedRows);
                  this.trackPersistence(playerId, state, this.submitInternal(() -> {
                     this.store.savePlayerAggregate(persistedProfile, rows);
                     return null;
                  }));
                  return formsAtLevel;
               }
            }
         }
      }
   }

   public CompletableFuture<Void> flush() {
      return this.closed.get() ? this.closeAsync() : this.<Void>submitPersistence(() -> null).thenCompose(ignored -> this.persistenceOutcome());
   }

   public CompletableFuture<Void> flush(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
      if (state == null) {
         return this.flush();
      } else {
         CompletableFuture<?> tail;
         synchronized (state.monitor) {
            tail = state.readiness == ProgressionReadiness.LOADING ? state.loadFuture : state.persistenceTail;
         }

         return tail.handle((ignored, tailFailure) -> {
            Throwable recordedFailure = state.persistenceFailure != null ? state.persistenceFailure : state.failure;
            if (recordedFailure != null) {
               throw new CompletionException(recordedFailure);
            } else if (tailFailure != null) {
               throw new CompletionException(unwrap(tailFailure));
            } else {
               return null;
            }
         });
      }
   }

   public synchronized CompletableFuture<Void> closeAsync() {
      if (this.closeFuture != null) {
         return this.closeFuture;
      } else {
         this.closed.set(true);

         for (ClassProgressionModule.CachedPlayer state : this.players.values()) {
            synchronized (state.monitor) {
               ;
            }
         }

         this.closeFuture = this.<Void>submitInternal(() -> null).thenCompose(ignored -> this.persistenceOutcome()).whenComplete((ignored, failure) -> {
            this.players.clear();
            this.runSelections.clear();
            if (this.ownedExecutor != null) {
               this.ownedExecutor.shutdown();
            }
         });
         return this.closeFuture;
      }
   }

   private ProfileSnapshot hydrate(UUID playerId, ClassProgressionModule.CachedPlayer state) throws SQLException {
      StoredClassProfile storedProfile = this.store.loadOrCreateProfile(playerId, this.catalogVersion);
      if (!playerId.equals(storedProfile.playerId())) {
         throw new IllegalStateException("Progression store returned another player's profile");
      } else {
         this.requireSupportedCatalogVersion(storedProfile.catalogVersion(), "profile");
         List<StoredClassProgress> storedProgress = this.store.loadAllProgress(playerId);
         Map<SkillType, Integer> levels = this.captureFoundationLevels(playerId);
         Map<String, Long> progressXp = new HashMap<>();
         Set<String> challenges = new HashSet<>();
         List<StoredClassProgress> repairedProgress = new ArrayList<>();

         for (StoredClassProgress progress : storedProgress) {
            if (!playerId.equals(progress.playerId())) {
               throw new IllegalStateException("Progression store returned another player's form progress");
            }

            this.requireSupportedCatalogVersion(progress.catalogVersion(), "form " + progress.formId());
            ClassFormDefinition form = this.catalog.find(progress.formId()).orElse(null);
            if (form == null) {
               String classification = this.catalog.retiredFormIds().contains(progress.formId()) ? "retired" : "unknown";
               throw new IllegalStateException(
                  "Stored class progress references " + classification + " form id " + progress.formId() + " without a catalog migration"
               );
            }

            if (progressXp.putIfAbsent(form.id(), progress.xp()) != null) {
               throw new IllegalStateException("Progression store returned duplicate form " + form.id());
            }

            if (progress.challengeCompleted()) {
               challenges.add(form.id());
            }

            long normalizedXp = normalizePersistedXp(form, progress.xp());
            progressXp.put(form.id(), normalizedXp);
            if (normalizedXp != progress.xp()) {
               repairedProgress.add(new StoredClassProgress(playerId, form.id(), normalizedXp, this.catalogVersion, progress.challengeCompleted()));
            }
         }

         StoredClassProfile repairedProfile = this.repairProfile(playerId, storedProfile, progressXp, challenges, levels);
         if (!repairedProgress.isEmpty() || !repairedProfile.equals(storedProfile)) {
            this.store.savePlayerAggregate(repairedProfile, repairedProgress);
         }

         synchronized (state.monitor) {
            if (this.players.get(playerId) != state) {
               throw new CancellationException("Player progression was unloaded while loading");
            } else {
               state.profile = repairedProfile;
               state.progressXp.clear();
               state.progressXp.putAll(progressXp);
               state.challenges.clear();
               state.challenges.addAll(challenges);
               state.readiness = ProgressionReadiness.READY;
               return this.snapshotLocked(playerId, state, levels);
            }
         }
      }
   }

   private StoredClassProfile repairProfile(
      UUID playerId, StoredClassProfile stored, Map<String, Long> progressXp, Set<String> challenges, Map<SkillType, Integer> levels
   ) {
      String selectedFormId = stored.selectedFormId();
      ClassFormDefinition selectedForm = null;
      if (selectedFormId != null) {
         selectedForm = this.catalog.find(selectedFormId).orElse(null);
         if (selectedForm == null) {
            String classification = this.catalog.retiredFormIds().contains(selectedFormId) ? "retired" : "unknown";
            throw new IllegalStateException("Stored class profile selects " + classification + " form id " + selectedFormId + " without a catalog migration");
         }

         if (!this.isUnlocked(selectedForm, progressXp, challenges, levels, new HashMap<>())) {
            selectedFormId = null;
         }
      }

      InputProfile inputProfile = InputProfile.fromStoredId(stored.selectedInputId()).orElse(InputProfile.DEFAULT);
      return new StoredClassProfile(playerId, selectedFormId, inputProfile.storedId(), this.catalogVersion, stored.tutorialSkillsUsed());
   }

   private void requireSupportedCatalogVersion(int storedVersion, String recordName) {
      if (storedVersion != this.catalogVersion) {
         if (storedVersion > this.catalogVersion) {
            throw new IllegalStateException(
               "Stored class " + recordName + " uses future catalog version " + storedVersion + "; this build supports " + this.catalogVersion
            );
         } else {
            throw new IllegalStateException(
               "Stored class " + recordName + " uses catalog version " + storedVersion + ", but no migration to " + this.catalogVersion + " is registered"
            );
         }
      }
   }

   private ProfileSnapshot snapshotLocked(UUID playerId, ClassProgressionModule.CachedPlayer state, Map<SkillType, Integer> levels) {
      Map<String, FormProgressSnapshot> forms = new LinkedHashMap<>();
      Map<String, List<UnlockBlocker>> unlocks = new HashMap<>();

      for (ClassFormDefinition form : this.catalog.forms()) {
         forms.put(form.id(), this.formSnapshot(form, state, levels, unlocks));
      }

      ClassProgressionModule.LockedRun lockedRun = this.runSelections.get(playerId);
      RunSelection runSelection = lockedRun == null ? null : lockedRun.selection();
      String activeFormId = runSelection == null ? state.profile.selectedFormId() : runSelection.formId();
      ActiveLineageSnapshot activeLineage = this.activeLineage(forms.get(activeFormId));
      return new ProfileSnapshot(
         playerId,
         state.profile.selectedFormId(),
         this.selectedInputProfile(state.profile),
         this.catalogVersion,
         lockedRun == null ? null : lockedRun.runId(),
         runSelection,
         activeLineage,
         forms
      );
   }

   private FormProgressSnapshot formSnapshot(
      ClassFormDefinition form, ClassProgressionModule.CachedPlayer state, Map<SkillType, Integer> levels, Map<String, List<UnlockBlocker>> unlocks
   ) {
      List<UnlockBlocker> unlockBlockers = this.unlockBlockers(form, state.progressXp, state.challenges, levels, unlocks);
      boolean unlocked = unlockBlockers.isEmpty();
      int localCap = unlocked ? form.localProgressionCap(levels::get) : 0;
      int effectiveCap = unlocked ? form.effectiveProgressionCap(levels::get) : 0;
      long xpAtCap = unlocked ? xpAtLocalCap(form, localCap) : 0L;
      long xp = unlocked ? Math.min(state.progressXp.getOrDefault(form.id(), 0L), xpAtCap) : 0L;
      int localLevel = unlocked ? localLevelFromXp(form, xp) : 0;
      int effectiveLevel = unlocked ? form.band().toEffectiveLevel(localLevel) : 0;
      ProgressionCapReason capReason = unlocked ? capReason(form, levels) : ProgressionCapReason.NOT_APPLICABLE;
      return new FormProgressSnapshot(
         form.id(),
         unlocked,
         unlockBlockers,
         xp,
         localLevel,
         effectiveLevel,
         localCap,
         effectiveCap,
         xpAtCap,
         capReason,
         unlocked ? limitingSkills(form, levels, capReason) : List.of()
      );
   }

   private ActiveLineageSnapshot activeLineage(FormProgressSnapshot active) {
      if (active != null && active.unlocked()) {
         String activeFormId = active.formId();
         ClassLineage lineage = this.catalog.lineageOf(activeFormId);
         return new ActiveLineageSnapshot(
            activeFormId, active.localLevel(), active.effectiveLevel(), lineage.formIds(), lineage.passiveContributionLevels(active.effectiveLevel())
         );
      } else {
         return null;
      }
   }

   private boolean isUnlocked(
      ClassFormDefinition form, Map<String, Long> progressXp, Set<String> challenges, Map<SkillType, Integer> levels, Map<String, List<UnlockBlocker>> memo
   ) {
      return this.unlockBlockers(form, progressXp, challenges, levels, memo).isEmpty();
   }

   private List<UnlockBlocker> unlockBlockers(
      ClassFormDefinition form, Map<String, Long> progressXp, Set<String> challenges, Map<SkillType, Integer> levels, Map<String, List<UnlockBlocker>> memo
   ) {
      List<UnlockBlocker> known = memo.get(form.id());
      if (known != null) {
         return known;
      } else {
         List<UnlockBlocker> blockers = new ArrayList<>();
         if (!challenges.contains(form.id())) {
            blockers.add(UnlockBlocker.classChallenge(form.id()));
         }

         if (form.band().isRoot()) {
            this.contentAvailability.unavailableReason(form.id()).map(reason -> UnlockBlocker.contentRequirement(form.id(), reason)).ifPresent(blockers::add);
         }

         int requiredFoundationLevel = form.requiredFoundationSkillLevel();

         for (SkillType skillType : form.foundationSkills().asList()) {
            int currentLevel = levels.get(skillType);
            if (currentLevel < requiredFoundationLevel) {
               blockers.add(UnlockBlocker.foundationSkill(form.id(), skillType, currentLevel, requiredFoundationLevel));
            }
         }

         if (form.parentId() != null) {
            ClassFormDefinition parent = this.catalog.require(form.parentId());
            blockers.addAll(this.unlockBlockers(parent, progressXp, challenges, levels, memo));
            long parentXpAtCap = xpAtLocalCap(parent, parent.localProgressionCap(levels::get));
            int parentLocalLevel = localLevelFromXp(parent, Math.min(progressXp.getOrDefault(parent.id(), 0L), parentXpAtCap));
            int completedParentLevel = parent.band().toLocalLevel(parent.band().effectiveEnd());
            if (parentLocalLevel < completedParentLevel) {
               blockers.add(UnlockBlocker.parentLocalLevel(parent.id(), parentLocalLevel, completedParentLevel));
            }
         }

         List<UnlockBlocker> result = List.copyOf(blockers);
         memo.put(form.id(), result);
         return result;
      }
   }

   private Map<SkillType, Integer> captureFoundationLevels(UUID playerId) {
      FoundationLevelSnapshot snapshot = this.foundationLevels
         .snapshot(playerId)
         .orElseThrow(() -> new IllegalStateException("Foundation levels are not loaded for " + playerId));
      if (!playerId.equals(snapshot.playerId())) {
         throw new IllegalStateException("Foundation adapter returned another player's levels");
      } else {
         return snapshot.levels();
      }
   }

   public boolean canChallenge(UUID playerId, String formId) {
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      ClassFormDefinition form = this.catalog.find(formId).orElse(null);
      if (state != null && form != null) {
         synchronized (state.monitor) {
            return this.isReady(state) && !state.challenges.contains(formId) && !this.runSelections.containsKey(playerId)
               ? this.unlockBlockers(form, state.progressXp, state.challenges, this.captureFoundationLevels(playerId), new HashMap<>())
                  .stream()
                  .allMatch(blocker -> blocker.kind() == UnlockBlocker.Kind.CLASS_CHALLENGE && blocker.formId().equals(formId))
               : false;
         }
      } else {
         return false;
      }
   }

   public boolean completeChallenge(UUID playerId, String formId) {
      ClassProgressionModule.CachedPlayer state = this.readyState(playerId);
      if (state != null && !this.catalog.find(formId).isEmpty()) {
         synchronized (state.monitor) {
            if (!this.isReady(state)) {
               return false;
            } else {
               if (state.challenges.add(formId)) {
                  this.enqueueProgressSave(playerId, state, formId, state.progressXp.getOrDefault(formId, 0L));
               }

               if (!formId.equals(state.profile.selectedFormId())) {
                  state.profile = new StoredClassProfile(
                     playerId, formId, state.profile.selectedInputId(), this.catalogVersion, state.profile.tutorialSkillsUsed()
                  );
                  this.enqueueProfileSave(playerId, state);
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void enqueueProfileSave(UUID playerId, ClassProgressionModule.CachedPlayer state) {
      StoredClassProfile profile = state.profile;
      this.trackPersistence(playerId, state, this.submitInternal(() -> {
         this.store.saveProfile(profile);
         return null;
      }));
   }

   private void enqueueProgressSave(UUID playerId, ClassProgressionModule.CachedPlayer state, String formId, long xp) {
      StoredClassProgress progress = new StoredClassProgress(playerId, formId, xp, this.catalogVersion, state.challenges.contains(formId));
      this.trackPersistence(playerId, state, this.submitInternal(() -> {
         this.store.saveProgress(progress);
         return null;
      }));
   }

   private void trackPersistence(UUID playerId, ClassProgressionModule.CachedPlayer state, CompletableFuture<Void> persistence) {
      state.persistenceTail = persistence;
      persistence.whenComplete((ignored, failure) -> {
         if (failure != null) {
            Throwable cause = unwrap(failure);
            state.persistenceFailure = cause;
            this.persistenceFailure.compareAndSet(null, cause);
            this.fail(playerId, state, cause);
         }
      });
   }

   private CompletableFuture<Void> persistenceOutcome() {
      Throwable failure = this.persistenceFailure.get();
      return failure == null ? CompletableFuture.completedFuture(null) : CompletableFuture.failedFuture(failure);
   }

   private void fail(UUID playerId, ClassProgressionModule.CachedPlayer state, Throwable failure) {
      if (this.players.get(playerId) == state) {
         synchronized (state.monitor) {
            if (this.players.get(playerId) == state) {
               state.failure = failure;
               state.readiness = ProgressionReadiness.FAILED;
            }
         }
      }
   }

   private ClassProgressionModule.CachedPlayer readyState(UUID playerId) {
      if (this.closed.get()) {
         return null;
      } else {
         ClassProgressionModule.CachedPlayer state = this.players.get(playerId);
         return state != null && state.readiness == ProgressionReadiness.READY ? state : null;
      }
   }

   private boolean isReady(ClassProgressionModule.CachedPlayer state) {
      return !this.closed.get() && state.readiness == ProgressionReadiness.READY;
   }

   private InputProfile selectedInputProfile(StoredClassProfile profile) {
      return InputProfile.fromStoredId(profile.selectedInputId()).orElse(InputProfile.DEFAULT);
   }

   private AwardResult lockedAward(String formId, long requestedXp, ClassFormDefinition form, Map<String, Long> progressXp, Map<SkillType, Integer> levels) {
      if (form == null) {
         return emptyAward(AwardResult.Status.FORM_LOCKED, requestedXp, formId);
      } else {
         int localCap = form.localProgressionCap(levels::get);
         ProgressionCapReason capReason = capReason(form, levels);
         long xpAtCap = xpAtLocalCap(form, localCap);
         long xp = Math.min(progressXp.getOrDefault(formId, 0L), xpAtCap);
         int localLevel = localLevelFromXp(form, xp);
         return new AwardResult(
            AwardResult.Status.FORM_LOCKED,
            formId,
            requestedXp,
            0L,
            requestedXp,
            xp,
            xp,
            localLevel,
            localLevel,
            form.band().toEffectiveLevel(localLevel),
            localCap,
            form.effectiveProgressionCap(levels::get),
            capReason,
            limitingSkills(form, levels, capReason)
         );
      }
   }

   private static AwardResult emptyAward(AwardResult.Status status, long requestedXp) {
      return emptyAward(status, requestedXp, null);
   }

   private static AwardResult emptyAward(AwardResult.Status status, long requestedXp, String formId) {
      return new AwardResult(status, formId, requestedXp, 0L, Math.max(requestedXp, 0L), 0L, 0L, 0, 0, 0, 0, 0, ProgressionCapReason.NOT_APPLICABLE, List.of());
   }

   private static ProgressionCapReason capReason(ClassFormDefinition form, Map<SkillType, Integer> levels) {
      return !form.band().isTerminal() && form.effectiveSkillCap(levels::get) >= form.band().effectiveEnd()
         ? ProgressionCapReason.BAND_COMPLETE
         : ProgressionCapReason.FOUNDATION_SKILLS;
   }

   private static List<SkillType> limitingSkills(ClassFormDefinition form, Map<SkillType, Integer> levels, ProgressionCapReason capReason) {
      return capReason == ProgressionCapReason.FOUNDATION_SKILLS ? form.foundationSkills().limitingSkills(levels::get) : List.of();
   }

   private static long xpAtLocalCap(ClassFormDefinition form, int localCap) {
      if (localCap <= 0) {
         return 0L;
      } else {
         int effectiveLevel = form.band().toEffectiveLevel(localCap);
         long baselineXp = SkillXPCalculator.totalXPForLevel(form.band().effectiveStart());
         long effectiveXp = SkillXPCalculator.totalXPForLevel(effectiveLevel);
         if (baselineXp >= 0L && effectiveXp >= baselineXp) {
            return effectiveXp - baselineXp;
         } else {
            throw new ArithmeticException("Class XP cap overflow at effective level " + effectiveLevel);
         }
      }
   }

   private static ClassProgressionSetResult progressionSetResult(
      ClassProgressionSetResult.Status status,
      String formId,
      int requestedEffectiveLevel,
      String blockingFormId,
      int effectiveCap,
      List<SkillType> limitingSkills,
      ProfileSnapshot snapshot
   ) {
      return new ClassProgressionSetResult(status, formId, requestedEffectiveLevel, blockingFormId, effectiveCap, limitingSkills, snapshot);
   }

   private static long normalizePersistedXp(ClassFormDefinition form, long persistedXp) {
      if (persistedXp <= 0L) {
         return 0L;
      } else if (form.band().isTerminal()) {
         return persistedXp;
      } else {
         int maximumLocalLevel = form.band().toLocalLevel(form.band().effectiveEnd());
         return Math.min(persistedXp, xpAtLocalCap(form, maximumLocalLevel));
      }
   }

   private static int localLevelFromXp(ClassFormDefinition form, long branchXp) {
      if (branchXp < 0L) {
         throw new IllegalArgumentException("Class XP must not be negative");
      } else {
         long baselineXp = SkillXPCalculator.totalXPForLevel(form.band().effectiveStart());
         long effectiveTotalXp = Math.addExact(baselineXp, branchXp);
         int effectiveLevel = SkillXPCalculator.levelFromTotalXP(effectiveTotalXp);
         if (!form.band().containsEffectiveLevel(effectiveLevel)) {
            throw new IllegalArgumentException("Class XP resolves outside the " + form.band() + " band at effective level " + effectiveLevel);
         } else {
            return form.band().toLocalLevel(effectiveLevel);
         }
      }
   }

   private <T> CompletableFuture<T> submitPersistence(Callable<T> task) {
      return this.closed.get() ? CompletableFuture.failedFuture(new IllegalStateException("Progression Module is closed")) : this.submitInternal(task);
   }

   private <T> CompletableFuture<T> submitInternal(Callable<T> task) {
      return this.persistenceExecutor.submit(task);
   }

   private static Throwable unwrap(Throwable failure) {
      Throwable cause = failure;

      while ((cause instanceof CompletionException || cause instanceof ExecutionException) && cause.getCause() != null) {
         cause = cause.getCause();
      }

      return cause;
   }

   public record ActiveProfileSnapshot(UUID lockedRunId, InputProfile activeInputProfile, FormProgressSnapshot activeForm, ActiveLineageSnapshot activeLineage) {
      public Optional<ActiveLineageSnapshot> optionalActiveLineage() {
         return Optional.ofNullable(this.activeLineage);
      }
   }

   private static final class CachedPlayer {
      private final Object monitor = new Object();
      private final CompletableFuture<ProfileSnapshot> loadFuture = new CompletableFuture<>();
      private final Map<String, Long> progressXp = new HashMap<>();
      private final Set<String> challenges = new HashSet<>();
      private volatile ProgressionReadiness readiness = ProgressionReadiness.LOADING;
      private volatile Throwable failure;
      private volatile Throwable persistenceFailure;
      private StoredClassProfile profile;
      private CompletableFuture<Void> persistenceTail = CompletableFuture.completedFuture(null);
   }

   private record LockedRun(UUID runId, RunSelection selection) {
      private LockedRun(UUID runId, RunSelection selection) {
         Objects.requireNonNull(runId, "runId");
         Objects.requireNonNull(selection, "selection");
         this.runId = runId;
         this.selection = selection;
      }
   }

   private static final class QueuedTask<T> {
      private final Callable<T> command;
      private final CompletableFuture<T> future = new CompletableFuture<>();

      private QueuedTask(Callable<T> command) {
         this.command = command;
      }

      private void complete() {
         try {
            this.future.complete(this.command.call());
         } catch (Throwable var2) {
            this.future.completeExceptionally(var2);
         }
      }
   }

   private static final class SerialExecutor {
      private final ArrayDeque<ClassProgressionModule.QueuedTask<?>> tasks = new ArrayDeque<>();
      private final Executor executor;
      private ClassProgressionModule.QueuedTask<?> active;

      private SerialExecutor(Executor executor) {
         this.executor = executor;
      }

      private <T> CompletableFuture<T> submit(Callable<T> command) {
         Objects.requireNonNull(command, "command");
         ClassProgressionModule.QueuedTask<T> task = new ClassProgressionModule.QueuedTask<>(command);
         synchronized (this) {
            this.tasks.addLast(task);
         }

         this.dispatchNext();
         return task.future;
      }

      private void dispatchNext() {
         while (true) {
            ClassProgressionModule.QueuedTask<?> next;
            synchronized (this) {
               if (this.active != null) {
                  return;
               }

               next = this.tasks.pollFirst();
               if (next == null) {
                  return;
               }

               this.active = next;
            }

            try {
               this.executor.execute(() -> this.run(next));
               return;
            } catch (Throwable var6) {
               next.future.completeExceptionally(var6);
               synchronized (this) {
                  if (this.active == next) {
                     this.active = null;
                  }
               }
            }
         }
      }

      private void run(ClassProgressionModule.QueuedTask<?> task) {
         try {
            task.complete();
         } finally {
            synchronized (this) {
               if (this.active == task) {
                  this.active = null;
               }
            }

            this.dispatchNext();
         }
      }
   }
}
