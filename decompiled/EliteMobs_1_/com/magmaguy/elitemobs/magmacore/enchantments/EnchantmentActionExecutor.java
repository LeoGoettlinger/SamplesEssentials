package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptBlockActions;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptInstance;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptNativeProjectiles;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptProjectiles;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptableEntity;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.magmacore.util.OwnedEntityState;
import com.magmaguy.elitemobs.magmacore.util.TemporaryBlockManager;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.ZeroArgFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

final class EnchantmentActionExecutor implements Listener, AutoCloseable {
   private final Plugin plugin;
   private final Set<ScriptHook> hooks;
   private final Set<String> capabilities;
   private final EnchantmentInputs inputs;
   private final EnchantmentProviders.Handler domainHandler;
   private final Map<String, EnchantmentActionExecutor.Running> active = new HashMap<>();
   private final Map<UUID, Map<String, Long>> globalCooldowns = new HashMap<>();
   private final Map<UUID, Map<String, Map<String, Long>>> localCooldowns = new HashMap<>();
   private EnchantmentCatalog catalog;
   private ScriptProjectiles projectiles;
   private ScriptNativeProjectiles nativeProjectiles;
   private boolean closed;

   EnchantmentActionExecutor(
      Plugin plugin, EnchantmentCatalog catalog, Set<ScriptHook> hooks, Set<String> capabilities, EnchantmentProviders.Handler domainHandler
   ) {
      this.plugin = plugin;
      this.catalog = catalog;
      this.hooks = Set.copyOf(hooks);
      this.capabilities = Set.copyOf(capabilities);
      this.domainHandler = domainHandler;
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.inputs = new EnchantmentInputs(plugin, catalog.namespace());
      Bukkit.getPluginManager().registerEvents(this.inputs, plugin);
   }

   Map<String, Object> evaluate(Map<String, Object> request) {
      if (this.closed) {
         return response(EnchantmentActions.Status.UNAVAILABLE);
      } else {
         String var2 = String.valueOf(request.get("operation"));

         return switch (var2) {
            case "begin" -> this.begin(request);
            case "dispatch" -> this.dispatch(request);
            case "close" -> {
               requireKeys(request, "kind", "operation", "token");
               EnchantmentActionExecutor.Running running = this.active.get(text(request, "token"));
               if (running != null) {
                  running.instance.shutdown();
               }

               yield response(EnchantmentActions.Status.OK);
            }
            default -> throw new IllegalArgumentException("Unknown action operation");
         };
      }
   }

   private Map<String, Object> begin(Map<String, Object> request) {
      requireKeys(request, "kind", "operation", "id", "level", "attack", "actor", "world", "lifetime", "facts", "hook");
      String id = text(request, "id");
      if (request.get("level") instanceof Integer level
         && request.get("lifetime") instanceof Long lifetime
         && lifetime >= 1L
         && request.get("facts") instanceof Map<?, ?> facts) {
         EnchantmentDefinition var15 = this.catalog.definitions().get(id);
         ScriptDefinition script = this.catalog.script(id).orElse(null);
         if (var15 != null && script != null && level >= 1) {
            String initialHook = text(request, "hook");
            if (!initialHook.isEmpty() && !script.supportsHook(new ScriptHook(initialHook))) {
               return response(EnchantmentActions.Status.UNAVAILABLE);
            } else if (!this.capabilities.containsAll(var15.requires())) {
               return response(EnchantmentActions.Status.UNAVAILABLE);
            } else {
               EnchantmentActions.Source source = new EnchantmentActions.Source(
                  UUID.fromString(text(request, "attack")),
                  UUID.fromString(text(request, "actor")),
                  UUID.fromString(text(request, "world")),
                  lifetime,
                  EnchantmentValues.copy(facts)
               );
               EnchantmentActionExecutor.Running running = new EnchantmentActionExecutor.Running(UUID.randomUUID().toString(), var15, level, source);
               if (!running.isScriptOwnerActive()) {
                  return response(EnchantmentActions.Status.UNAVAILABLE);
               } else {
                  running.instance = new ScriptInstance(script, running);
                  this.active.put(running.token, running);

                  try {
                     running.guardTask = running.instance
                        .ownRepeating(
                           1,
                           1,
                           () -> {
                              if (--running.remainingTicks <= 0L
                                 || !running.isScriptOwnerActive()
                                 || running.finalStage && !running.instance.hasOwnedWorkExcept(running.guardTask)) {
                                 running.instance.shutdown();
                              }
                           }
                        );
                  } catch (RuntimeException var12) {
                     running.instance.shutdown();
                     throw var12;
                  }

                  return Map.of("status", EnchantmentActions.Status.OK.name(), "token", running.token);
               }
            }
         } else {
            return response(EnchantmentActions.Status.INVALID);
         }
      } else {
         throw new IllegalArgumentException("Invalid action source");
      }
   }

   private Map<String, Object> dispatch(Map<String, Object> request) {
      requireKeys(request, "kind", "operation", "token", "stage", "hook", "target", "final");
      if (!(request.get("final") instanceof Boolean finalStage)) {
         throw new IllegalArgumentException("Invalid final-stage flag");
      } else {
         EnchantmentActionExecutor.Running running = this.active.get(text(request, "token"));
         if (running == null) {
            return response(EnchantmentActions.Status.UNAVAILABLE);
         } else if (running.finalStage) {
            return response(EnchantmentActions.Status.UNAVAILABLE);
         } else if (!running.isScriptOwnerActive()) {
            running.instance.shutdown();
            return response(EnchantmentActions.Status.UNAVAILABLE);
         } else {
            ScriptHook hook = new ScriptHook(text(request, "hook"));
            if (!this.hooks.contains(hook)) {
               return response(EnchantmentActions.Status.INVALID);
            } else {
               String stage = text(request, "stage");
               if (stage.isBlank()) {
                  throw new IllegalArgumentException("Missing stage identity");
               } else {
                  String targetId = text(request, "target");
                  Entity entity = targetId.isEmpty() ? null : Bukkit.getEntity(UUID.fromString(targetId));
                  if (targetId.isEmpty() || entity instanceof LivingEntity && entity.isValid() && entity.getWorld().getUID().equals(running.source.world())) {
                     if (!running.delivered.add(stage + "/" + hook.getKey())) {
                        return response(EnchantmentActions.Status.DUPLICATE);
                     } else {
                        running.finalStage = finalStage;
                        running.target = entity instanceof LivingEntity living ? living : null;
                        EnchantmentActionExecutor.InputEvent input = EnchantmentInputs.isInteraction(hook) ? new EnchantmentActionExecutor.InputEvent() : null;
                        running.instance.handleEvent(hook, input, running.target, (LivingEntity)running.getBukkitEntity());
                        if (running.finalStage && !running.instance.hasOwnedWorkExcept(running.guardTask)) {
                           running.instance.shutdown();
                        }

                        return Map.of(
                           "status",
                           (running.failed ? EnchantmentActions.Status.FAILED : EnchantmentActions.Status.OK).name(),
                           "cancelled",
                           input != null && input.isCancelled()
                        );
                     }
                  } else {
                     return response(EnchantmentActions.Status.UNAVAILABLE);
                  }
               }
            }
         }
      }
   }

   void reload(EnchantmentCatalog candidate) {
      this.stopAll();
      this.catalog = candidate;
      this.inputs.clearWarnings();
   }

   private void stopAll() {
      for (EnchantmentActionExecutor.Running running : new ArrayList<>(this.active.values())) {
         running.instance.shutdown();
      }
   }

   @Override
   public void close() {
      this.closed = true;
      HandlerList.unregisterAll(this);
      HandlerList.unregisterAll(this.inputs);
      this.stopAll();
      if (this.projectiles != null) {
         this.projectiles.close();
      }

      if (this.nativeProjectiles != null) {
         this.nativeProjectiles.close();
      }

      this.globalCooldowns.clear();
      this.localCooldowns.clear();
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID actor = event.getPlayer().getUniqueId();
      this.stopActor(actor);
      this.globalCooldowns.remove(actor);
      this.localCooldowns.remove(actor);
   }

   @EventHandler
   public void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin() == this.plugin) {
         this.close();
      }
   }

   @EventHandler
   public void onWorldChange(PlayerChangedWorldEvent event) {
      this.stopActor(event.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onArrowImpact(ProjectileHitEvent event) {
      if (event.getEntity() instanceof AbstractArrow arrow && event.getHitBlock() != null) {
         if (!this.active.values().stream().noneMatch(running -> running.ownedEntities.containsKey(arrow.getUniqueId()))) {
            LuaEntityTable.recordArrowAttachment(this.plugin, arrow, event.getHitBlock());
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      for (EnchantmentActionExecutor.Running running : new ArrayList<>(this.active.values())) {
         if (running.source.world().equals(event.getWorld().getUID())) {
            running.instance.shutdown();
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onChunkUnload(ChunkUnloadEvent event) {
      for (EnchantmentActionExecutor.Running running : new ArrayList<>(this.active.values())) {
         if (running.ownedEntities
            .values()
            .stream()
            .anyMatch(
               entity -> {
                  Location location = entity.getLocation();
                  return location.getWorld().equals(event.getWorld())
                     && location.getBlockX() >> 4 == event.getChunk().getX()
                     && location.getBlockZ() >> 4 == event.getChunk().getZ();
               }
            )) {
            running.instance.shutdown();
         }
      }
   }

   private void stopActor(UUID actor) {
      for (EnchantmentActionExecutor.Running running : new ArrayList<>(this.active.values())) {
         if (running.source.actor().equals(actor)) {
            running.instance.shutdown();
         }
      }
   }

   private static LuaValue lua(Object value) {
      if (value instanceof UUID id) {
         return LuaValue.valueOf(id.toString());
      } else if (value instanceof ItemStack item) {
         Map<String, Integer> levels = new EnchantmentItems(EnchantmentDefinitions::resolve, EnchantmentItemProfile::vanilla).inspect(item);
         return lua(Map.of("material", item.getType().name(), "amount", item.getAmount(), "enchantments", levels));
      } else if (value instanceof Map<?, ?> map) {
         LuaTable table = new LuaTable();
         map.forEach((key, entry) -> table.set((String)key, lua(entry)));
         return table;
      } else if (!(value instanceof List<?> list)) {
         if (value instanceof Boolean flag) {
            return LuaValue.valueOf(flag);
         } else {
            return (LuaValue)(value instanceof Number number ? LuaValue.valueOf(number.doubleValue()) : LuaValue.valueOf((String)value));
         }
      } else {
         LuaTable table = new LuaTable();

         for (int index = 0; index < list.size(); index++) {
            table.set(index + 1, lua(list.get(index)));
         }

         return table;
      }
   }

   private static String text(Map<String, Object> request, String key) {
      if (request.get(key) instanceof String text) {
         return text;
      } else {
         throw new IllegalArgumentException("Expected " + key);
      }
   }

   private static void requireKeys(Map<String, Object> request, String... keys) {
      if (!request.keySet().equals(Set.of(keys))) {
         throw new IllegalArgumentException("Invalid action fields");
      }
   }

   private static Map<String, Object> response(EnchantmentActions.Status status) {
      return Map.of("status", status.name());
   }

   private static final class InputEvent extends Event implements Cancellable {
      private static final HandlerList HANDLERS = new HandlerList();
      private boolean cancelled;

      public boolean isCancelled() {
         return this.cancelled;
      }

      public void setCancelled(boolean value) {
         this.cancelled = value;
      }

      public HandlerList getHandlers() {
         return HANDLERS;
      }
   }

   private final class Running extends ScriptableEntity {
      final String token;
      final EnchantmentDefinition definition;
      final int level;
      final EnchantmentActions.Source source;
      final Set<String> delivered = new HashSet<>();
      final EnchantmentItemAccess item;
      final boolean stopOnUnequip;
      final Map<UUID, OwnedEntityState> gravity = new HashMap<>();
      final Map<String, OwnedEntityState> potions = new HashMap<>();
      final Map<UUID, Entity> ownedEntities = new HashMap<>();
      ScriptInstance instance;
      LivingEntity target;
      boolean failed;
      long remainingTicks;
      int guardTask;
      boolean finalStage;

      Running(String token, EnchantmentDefinition definition, int level, EnchantmentActions.Source source) {
         this.token = token;
         this.definition = definition;
         this.level = level;
         this.source = source;
         this.remainingTicks = source.lifetimeTicks();
         Map<String, Object> facts = source.facts();
         this.item = facts.get("item") instanceof ItemStack stack && facts.get("inventory_slot") instanceof Integer slot
            ? new EnchantmentItemAccess(source.actor(), slot, stack)
            : null;
         Object unequip = definition.parametersAt(level).get("stop_on_unequip");
         if (unequip != null && !(unequip instanceof Boolean)) {
            throw new IllegalArgumentException("stop_on_unequip must be a boolean");
         } else {
            this.stopOnUnequip = Boolean.TRUE.equals(unequip);
         }
      }

      @Override
      public boolean isScriptOwnerActive() {
         Entity actor = this.getBukkitEntity();
         return !EnchantmentActionExecutor.this.closed
            && EnchantmentActionExecutor.this.plugin.isEnabled()
            && actor instanceof LivingEntity
            && actor.isValid()
            && !actor.isDead()
            && !(actor instanceof Player player && !player.isOnline())
            && actor.getWorld().getUID().equals(this.source.world())
            && (!this.stopOnUnequip || this.item != null && this.item.isEquipped());
      }

      @Override
      public Entity getBukkitEntity() {
         return Bukkit.getEntity(this.source.actor());
      }

      @Override
      public Location getLocation() {
         Entity actor = this.getBukkitEntity();
         return actor == null ? null : actor.getLocation();
      }

      @Override
      public String getContextKey() {
         return "enchantment";
      }

      @Override
      public Set<ScriptHook> getSupportedHooks() {
         return EnchantmentActionExecutor.this.hooks;
      }

      @Override
      public Map<String, Long> getGlobalCooldownStore() {
         return EnchantmentActionExecutor.this.globalCooldowns.computeIfAbsent(this.source.actor(), ignored -> new HashMap<>());
      }

      @Override
      public Map<String, Long> getLocalCooldownStore(ScriptDefinition script) {
         return EnchantmentActionExecutor.this.localCooldowns
            .computeIfAbsent(this.source.actor(), ignored -> new HashMap<>())
            .computeIfAbsent(this.definition.id(), ignored -> new HashMap<>());
      }

      @Override
      public LuaTable buildContextTable(ScriptInstance ignored) {
         return EnchantmentActionExecutor.lua(Map.of("id", this.definition.id(), "level", this.level)).checktable();
      }

      @Override
      public LuaValue resolveExtraContext(String key, final ScriptInstance instance) {
         return (LuaValue)(switch (key) {
            case "parameters" -> EnchantmentActionExecutor.lua(this.definition.parametersAt(this.level));
            case "source" -> EnchantmentActionExecutor.lua(this.source.facts());
            case "item" -> this.item == null ? LuaValue.NIL : this.item.table();
            case "target" -> this.target == null ? LuaValue.NIL : LuaLivingEntityTable.build(this.target);
            case "player" -> this.getBukkitEntity() instanceof Player player ? LuaLivingEntityTable.build(player) : LuaValue.NIL;
            case "action" -> {
               LuaTable table = EnchantmentActionExecutor.lua(
                     Map.of(
                        "id",
                        this.source.attackId().toString(),
                        "instance",
                        this.token,
                        "actor",
                        this.source.actor().toString(),
                        "world",
                        this.source.world().toString()
                     )
                  )
                  .checktable();
               table.set("stop", new ZeroArgFunction() {
                  @Override
                  public LuaValue call() {
                     instance.shutdown();
                     return LuaValue.NIL;
                  }
               });
               table.set(
                  "damage_target",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        if (!this.isScriptOwnerActive()) {
                           return LuaValue.FALSE;
                        } else {
                           double amount = args.checkdouble(2);
                           if (Double.isFinite(amount) && !(amount <= 0.0)) {
                              UUID targetId = UUID.fromString(args.checkjstring(1));
                              Map<String, Object> result = EnchantmentActionExecutor.this.domainHandler
                                 .handle(
                                    EnchantmentProviders.Operation.EVALUATE,
                                    Map.of(
                                       "kind",
                                       "attributed_damage",
                                       "attack",
                                       this.source.attackId(),
                                       "actor",
                                       this.source.actor(),
                                       "world",
                                       this.source.world(),
                                       "target",
                                       targetId,
                                       "amount",
                                       amount,
                                       "facts",
                                       this.source.facts()
                                    )
                                 );
                              return LuaValue.valueOf(Boolean.TRUE.equals(result.get("applied")));
                           } else {
                              throw new IllegalArgumentException("Invalid authored damage");
                           }
                        }
                     }
                  )
               );
               table.set(
                  "launch_projectile",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        if (this.getBukkitEntity() instanceof Player player && this.isScriptOwnerActive()) {
                           Location at = LuaTableSupport.tableToLocation(args.checktable(1), player.getWorld());
                           LuaTable direction = args.checktable(2);
                           if (EnchantmentActionExecutor.this.projectiles == null) {
                              EnchantmentActionExecutor.this.projectiles = new ScriptProjectiles(EnchantmentActionExecutor.this.plugin);
                           }

                           return LuaValue.valueOf(
                              EnchantmentActionExecutor.this.projectiles
                                 .launch(
                                    instance,
                                    player,
                                    this::isScriptOwnerActive,
                                    at,
                                    new Vector(direction.get("x").checkdouble(), direction.get("y").checkdouble(), direction.get("z").checkdouble()),
                                    args.checkdouble(3),
                                    args.checkdouble(4),
                                    args.checkint(5),
                                    args.checkfunction(6),
                                    args.arg(7).isnil() ? null : args.checkfunction(7),
                                    args.optboolean(8, false)
                                 )
                           );
                        } else {
                           return LuaValue.FALSE;
                        }
                     }
                  )
               );
               table.set(
                  "attribute_projectile",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        Entity entity = this.ownedEntities.get(UUID.fromString(args.checkjstring(1)));
                        if (entity instanceof Projectile projectile && this.isScriptOwnerActive()) {
                           if (EnchantmentActionExecutor.this.nativeProjectiles == null) {
                              EnchantmentActionExecutor.this.nativeProjectiles = new ScriptNativeProjectiles(EnchantmentActionExecutor.this.plugin);
                           }

                           return LuaValue.valueOf(
                              EnchantmentActionExecutor.this.nativeProjectiles
                                 .bind(projectile, instance, this::isScriptOwnerActive, args.checkfunction(2), args.optboolean(3, false))
                           );
                        } else {
                           return LuaValue.FALSE;
                        }
                     }
                  )
               );
               table.set("replace_previous", LuaTableSupport.tableMethod(table, args -> {
                  for (EnchantmentActionExecutor.Running previous : new ArrayList<>(EnchantmentActionExecutor.this.active.values())) {
                     if (previous != this && previous.definition.id().equals(this.definition.id()) && previous.source.actor().equals(this.source.actor())) {
                        previous.instance.shutdown();
                     }
                  }

                  return LuaValue.NIL;
               }));
               table.set(
                  "has_previous",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> LuaValue.valueOf(
                        EnchantmentActionExecutor.this.active
                           .values()
                           .stream()
                           .anyMatch(
                              previous -> previous != this
                                 && previous.definition.id().equals(this.definition.id())
                                 && previous.source.actor().equals(this.source.actor())
                           )
                     )
                  )
               );
               table.set("own_entity", LuaTableSupport.tableMethod(table, args -> {
                  Entity entity = Bukkit.getEntity(UUID.fromString(args.checkjstring(1)));
                  if (entity == null || !entity.isValid() || entity instanceof Player || !entity.getWorld().getUID().equals(this.source.world())) {
                     return LuaValue.FALSE;
                  } else if (this.ownedEntities.containsKey(entity.getUniqueId())) {
                     return LuaValue.TRUE;
                  } else {
                     instance.ownCleanup(() -> {
                        this.ownedEntities.remove(entity.getUniqueId());
                        LuaEntityTable.clearArrowAttachment(EnchantmentActionExecutor.this.plugin, entity);
                        entity.remove();
                     });
                     this.ownedEntities.put(entity.getUniqueId(), entity);
                     entity.setPersistent(false);
                     return LuaValue.TRUE;
                  }
               }));
               table.set(
                  "place_block",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        World world = Bukkit.getWorld(this.source.world());
                        int x = args.checkint(1);
                        int y = args.checkint(2);
                        int z = args.checkint(3);
                        if (this.getBukkitEntity() instanceof Player player
                           && world != null
                           && world.isChunkLoaded(x >> 4, z >> 4)
                           && y > world.getMinHeight()
                           && y < world.getMaxHeight()) {
                           Material expected = Material.matchMaterial(args.checkjstring(4));
                           BlockData replacement = Bukkit.createBlockData(args.checkjstring(5));
                           ItemStack stack = this.source.facts().get("item") instanceof ItemStack captured ? captured : null;
                           EquipmentSlot hand = Integer.valueOf(40).equals(this.source.facts().get("inventory_slot"))
                              ? EquipmentSlot.OFF_HAND
                              : EquipmentSlot.HAND;
                           return LuaValue.valueOf(ScriptBlockActions.placeBlock(player, world.getBlockAt(x, y, z), expected, replacement, stack, hand));
                        } else {
                           return LuaValue.FALSE;
                        }
                     }
                  )
               );
               table.set(
                  "break_block",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        World world = Bukkit.getWorld(this.source.world());
                        int x = args.checkint(1);
                        int y = args.checkint(2);
                        int z = args.checkint(3);
                        if (this.getBukkitEntity() instanceof Player player
                           && world != null
                           && world.isChunkLoaded(x >> 4, z >> 4)
                           && y >= world.getMinHeight()
                           && y < world.getMaxHeight()) {
                           Material expected = Material.matchMaterial(args.checkjstring(4));
                           ItemStack drop = null;
                           if (!args.arg(5).isnil()) {
                              Material material = Material.matchMaterial(args.checkjstring(5));
                              int amount = args.optint(6, 1);
                              if (material == null || !material.isItem() || material.isAir() || amount < 1) {
                                 throw new IllegalArgumentException("Invalid authored mining drop");
                              }

                              drop = new ItemStack(material, amount);
                           }

                           return LuaValue.valueOf(ScriptBlockActions.breakBlock(player, world.getBlockAt(x, y, z), expected, drop));
                        } else {
                           return LuaValue.FALSE;
                        }
                     }
                  )
               );
               table.set(
                  "break_naturally",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        World world = Bukkit.getWorld(this.source.world());
                        int x = args.checkint(1);
                        int y = args.checkint(2);
                        int z = args.checkint(3);
                        return this.getBukkitEntity() instanceof Player player
                              && this.isScriptOwnerActive()
                              && world != null
                              && world.isChunkLoaded(x >> 4, z >> 4)
                              && y >= world.getMinHeight()
                              && y < world.getMaxHeight()
                              && this.source.facts().get("item") instanceof ItemStack tool
                           ? LuaValue.valueOf(
                              ScriptBlockActions.breakNaturally(
                                 player,
                                 world.getBlockAt(x, y, z),
                                 Material.matchMaterial(args.checkjstring(4)),
                                 tool,
                                 !Boolean.FALSE.equals(this.source.facts().get("drop_items"))
                              )
                           )
                           : LuaValue.FALSE;
                     }
                  )
               );
               table.set(
                  "temporary_block",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        World world = Bukkit.getWorld(this.source.world());
                        int x = args.checkint(1);
                        int y = args.checkint(2);
                        int z = args.checkint(3);
                        int ticks = args.optint(5, 0);
                        if (ticks < 0) {
                           throw new IllegalArgumentException("Block duration must not be negative");
                        } else if (world != null && world.isChunkLoaded(x >> 4, z >> 4) && y >= world.getMinHeight() && y < world.getMaxHeight()) {
                           TemporaryBlockManager.OwnedBlock lease = TemporaryBlockManager.replaceOwned(
                              world.getBlockAt(x, y, z), Bukkit.createBlockData(args.checkjstring(4)), EnchantmentActionExecutor.this.plugin
                           );
                           if (lease == null) {
                              return LuaValue.FALSE;
                           } else {
                              instance.ownCleanup(lease::close);
                              if (ticks > 0) {
                                 instance.ownLater(ticks, lease::close);
                              }

                              return LuaValue.TRUE;
                           }
                        } else {
                           return LuaValue.FALSE;
                        }
                     }
                  )
               );
               table.set(
                  "can_own_block",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        World world = Bukkit.getWorld(this.source.world());
                        int x = args.checkint(1);
                        int y = args.checkint(2);
                        int z = args.checkint(3);
                        return LuaValue.valueOf(
                           world != null
                              && world.isChunkLoaded(x >> 4, z >> 4)
                              && y >= world.getMinHeight()
                              && y < world.getMaxHeight()
                              && TemporaryBlockManager.canOwn(world.getBlockAt(x, y, z))
                        );
                     }
                  )
               );
               table.set("temporary_gravity", LuaTableSupport.tableMethod(table, args -> {
                  Entity entity = Bukkit.getEntity(UUID.fromString(args.checkjstring(1)));
                  OwnedEntityState lease = OwnedEntityState.gravity(entity, args.checkboolean(2), EnchantmentActionExecutor.this.plugin);
                  if (lease == null) {
                     return LuaValue.FALSE;
                  } else {
                     UUID id = entity.getUniqueId();
                     this.gravity.put(id, lease);
                     instance.ownCleanup(() -> {
                        this.gravity.remove(id, lease);
                        lease.close();
                     });
                     return LuaValue.TRUE;
                  }
               }));
               table.set("restore_gravity", LuaTableSupport.tableMethod(table, args -> {
                  OwnedEntityState lease = this.gravity.remove(UUID.fromString(args.checkjstring(1)));
                  if (lease == null) {
                     return LuaValue.FALSE;
                  } else {
                     lease.close();
                     return LuaValue.TRUE;
                  }
               }));
               table.set("temporary_scale", LuaTableSupport.tableMethod(table, args -> {
                  if (Bukkit.getEntity(UUID.fromString(args.checkjstring(1))) instanceof LivingEntity living) {
                     OwnedEntityState lease = OwnedEntityState.scale(living, args.checkdouble(2), EnchantmentActionExecutor.this.plugin);
                     if (lease == null) {
                        return LuaValue.FALSE;
                     } else {
                        instance.ownCleanup(lease::close);
                        return LuaValue.TRUE;
                     }
                  } else {
                     return LuaValue.FALSE;
                  }
               }));
               table.set("temporary_potion", LuaTableSupport.tableMethod(table, args -> {
                  Entity entity = Bukkit.getEntity(UUID.fromString(args.checkjstring(1)));
                  PotionEffectType type = PotionEffectType.getByName(args.checkjstring(2));
                  int ticks = args.checkint(3);
                  int amplifier = args.checkint(4);
                  int expiry = Math.addExact(ticks, 1);
                  if (entity instanceof LivingEntity living && type != null && ticks >= 1 && amplifier >= 0) {
                     OwnedEntityState lease = OwnedEntityState.potion(living, new PotionEffect(type, ticks, amplifier), EnchantmentActionExecutor.this.plugin);
                     if (lease == null) {
                        return LuaValue.FALSE;
                     } else {
                        String potionKey = entity.getUniqueId() + ":" + type.getKey();
                        this.potions.put(potionKey, lease);
                        Runnable restore = () -> {
                           this.potions.remove(potionKey, lease);
                           lease.close();
                        };
                        instance.ownCleanup(restore);
                        instance.ownLater(expiry, restore);
                        return LuaValue.TRUE;
                     }
                  } else {
                     return LuaValue.FALSE;
                  }
               }));
               table.set("restore_potion", LuaTableSupport.tableMethod(table, args -> {
                  PotionEffectType type = PotionEffectType.getByName(args.checkjstring(2));
                  if (type == null) {
                     return LuaValue.FALSE;
                  } else {
                     OwnedEntityState lease = this.potions.remove(UUID.fromString(args.checkjstring(1)) + ":" + type.getKey());
                     if (lease == null) {
                        return LuaValue.FALSE;
                     } else {
                        lease.close();
                        return LuaValue.TRUE;
                     }
                  }
               }));
               yield table;
            }
            default -> LuaValue.NIL;
         });
      }

      @Override
      public boolean handleScriptError(String context, Exception failure) {
         this.failed = true;
         EnchantmentActionExecutor.this.plugin
            .getLogger()
            .warning("Enchantment " + this.definition.id() + " action " + this.source.attackId() + " failed in " + context + ": " + failure.getMessage());
         return true;
      }

      @Override
      public void onShutdown() {
         EnchantmentActionExecutor.this.active.remove(this.token, this);
      }
   }
}
