package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class EnchantmentInputs implements Listener {
   public static final ScriptHook ATTACK = new ScriptHook("on_attack_entity");
   public static final ScriptHook PROJECTILE_HIT = new ScriptHook("on_projectile_hit");
   public static final ScriptHook TAKE_DAMAGE = new ScriptHook("on_take_damage");
   public static final ScriptHook RIGHT_CLICK = new ScriptHook("on_right_click");
   public static final ScriptHook LEFT_CLICK = new ScriptHook("on_left_click");
   public static final ScriptHook SHIFT_RIGHT_CLICK = new ScriptHook("on_shift_right_click");
   public static final ScriptHook SHIFT_LEFT_CLICK = new ScriptHook("on_shift_left_click");
   public static final ScriptHook BREAK_BLOCK = new ScriptHook("on_break_block");
   public static final ScriptHook CHAT = new ScriptHook("on_chat");
   public static final ScriptHook SNEAK = new ScriptHook("on_sneak");
   public static final ScriptHook PROJECTILE_LAUNCH = new ScriptHook("on_projectile_launch");
   public static final Set<ScriptHook> HOOKS = Set.of(
      ATTACK,
      PROJECTILE_HIT,
      TAKE_DAMAGE,
      RIGHT_CLICK,
      LEFT_CLICK,
      SHIFT_RIGHT_CLICK,
      SHIFT_LEFT_CLICK,
      BREAK_BLOCK,
      CHAT,
      SNEAK,
      PROJECTILE_LAUNCH,
      ScriptHook.ON_TICK,
      ScriptHook.ON_ZONE_ENTER,
      ScriptHook.ON_ZONE_LEAVE
   );
   private static final String SHOT = "nightbreak_enchantment_shot";
   private static final String EXPLICIT_DAMAGE = "nightbreak_enchantment_explicit_damage";
   private static final String OWNED_DAMAGE_EVENTS = "nightbreak_enchantment_owned_damage_events";
   private static final String DAMAGE_OBSERVER = "nightbreak_enchantment_damage_observer";
   private final Plugin plugin;
   private final String namespace;
   private final Set<String> warned = new HashSet<>();

   EnchantmentInputs(Plugin plugin, String namespace) {
      this.plugin = plugin;
      this.namespace = namespace;
   }

   void clearWarnings() {
      this.warned.clear();
   }

   static boolean isInteraction(ScriptHook hook) {
      return hook.equals(RIGHT_CLICK) || hook.equals(LEFT_CLICK) || hook.equals(SHIFT_RIGHT_CLICK) || hook.equals(SHIFT_LEFT_CLICK);
   }

   public static Map<String, Object> copySnapshot(Map<String, Object> snapshot) {
      return EnchantmentValues.copy(snapshot);
   }

   public static void markExplicitDamage(Plugin owner, EntityDamageByEntityEvent event) {
      EnchantmentProviders.requireServerThread();
      Map<Object, Boolean> events = null;

      for (MetadataValue value : event.getDamager().getMetadata("nightbreak_enchantment_owned_damage_events")) {
         if (value.getOwningPlugin() == owner && value.value() instanceof Map<?, ?> existing) {
            events = (Map<Object, Boolean>)existing;
            break;
         }
      }

      if (events == null) {
         events = new WeakHashMap<>();
         event.getDamager().setMetadata("nightbreak_enchantment_owned_damage_events", new FixedMetadataValue(owner, events));
      }

      events.put(event, Boolean.TRUE);
   }

   public static boolean isExplicitDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager()
         .getMetadata("nightbreak_enchantment_owned_damage_events")
         .stream()
         .anyMatch(value -> value.value() instanceof Map<?, ?> events && events.containsKey(event))) {
         return true;
      } else {
         Player actor = damageActor(event);
         return actor != null && actor.hasMetadata("nightbreak_enchantment_explicit_damage");
      }
   }

   public static boolean applyExplicitDamage(Plugin owner, Player actor, LivingEntity target, Runnable damage) {
      EnchantmentProviders.requireServerThread();
      MetadataValue existing = actor.getMetadata("nightbreak_enchantment_damage_observer")
         .stream()
         .filter(value -> value.value() instanceof Deque)
         .findFirst()
         .orElse(null);
      Deque<Consumer<EntityDamageByEntityEvent>> observers = (Deque<Consumer<EntityDamageByEntityEvent>>)(existing == null
         ? new ArrayDeque<>()
         : (Deque)existing.value());
      Plugin registrationOwner = existing == null ? owner : existing.getOwningPlugin();
      boolean[] observed = new boolean[]{false};
      boolean[] accepted = new boolean[]{false};
      Consumer<EntityDamageByEntityEvent> observer = event -> {
         if (!observed[0] && event.getEntity() == target && damageActor(event) == actor) {
            observed[0] = true;
            accepted[0] = !event.isCancelled() && event.getFinalDamage() > 0.0;
         }
      };
      if (existing == null) {
         actor.setMetadata("nightbreak_enchantment_damage_observer", new FixedMetadataValue(owner, observers));
      }

      observers.addLast(observer);

      boolean var10;
      try {
         runExplicitDamage(owner, actor, damage);
         var10 = accepted[0];
      } finally {
         observers.removeLastOccurrence(observer);
         if (observers.isEmpty()) {
            actor.removeMetadata("nightbreak_enchantment_damage_observer", registrationOwner);
         }
      }

      return var10;
   }

   private boolean elected() {
      return EnchantmentProviders.providers()
         .stream()
         .filter(provider -> provider.compatible() && provider.capabilities().contains("enchantment.actions.v1"))
         .findFirst()
         .map(provider -> provider.namespace().equals(this.namespace) && provider.plugin().equals(this.plugin.getName()))
         .orElse(false);
   }

   public static Map<String, Object> capture(
      Player actor, ItemStack item, EnchantmentDefinition.Slot slot, int inventorySlot, ItemStack ammunition, String attackKind, UUID attackId
   ) {
      return capture(actor, item, slot, inventorySlot, ammunition, attackKind, attackId, new EnchantmentInputs.InputFacts(actor));
   }

   private static Map<String, Object> capture(
      Player actor,
      ItemStack item,
      EnchantmentDefinition.Slot slot,
      int inventorySlot,
      ItemStack ammunition,
      String attackKind,
      UUID attackId,
      EnchantmentInputs.InputFacts inputFacts
   ) {
      EnchantmentProviders.requireServerThread();
      if (inputFacts.equipment != null) {
         item = inputFacts.equipment.get(slot.name());
      }

      if (item != null && !item.getType().isAir() && item.getType() != Material.ENCHANTED_BOOK && item.hasItemMeta()) {
         Map<String, Integer> levels = EnchantmentItemData.read(item.getItemMeta());
         if (levels.isEmpty()) {
            return Map.of();
         } else {
            EnchantmentItemProfile profile = EnchantmentItems.classify(item);
            List<Map<String, Object>> effects = new ArrayList<>();

            for (Entry<String, Integer> entry : new TreeMap<>(levels).entrySet()) {
               EnchantmentItems.Resolved resolved = EnchantmentDefinitions.resolve(entry.getKey());
               if (resolved == null || !resolved.available()) {
                  throw new IllegalArgumentException("Unavailable enchantment " + entry.getKey());
               }

               EnchantmentDefinition definition = resolved.definition();
               if (entry.getValue() < 1) {
                  throw new IllegalArgumentException("Invalid level for " + entry.getKey());
               }

               if ((definition.validSlots().isEmpty() || definition.validSlots().contains(slot))
                  && (definition.itemTypes().isEmpty() || definition.itemTypes().contains(profile.type()))
                  && (definition.attackKinds().isEmpty() || definition.attackKinds().contains(attackKind))
                  && resolved.provider().capabilities().contains("enchantment.actions.v1")) {
                  effects.add(
                     Map.of(
                        "id",
                        definition.id(),
                        "level",
                        entry.getValue(),
                        "generation",
                        resolved.provider().generation(),
                        "revision",
                        resolved.provider().revision()
                     )
                  );
               }
            }

            if (effects.isEmpty()) {
               return Map.of();
            } else {
               Map<String, Object> facts = new LinkedHashMap<>();
               facts.put("item", item.clone());
               facts.put("inventory_slot", inventorySlot);
               facts.put("slot", slot.name());
               inputFacts.capture();
               facts.put("equipment", inputFacts.equipment);
               facts.put("attack_kind", attackKind);
               facts.put("providers", inputFacts.providers);
               if (ammunition != null && !ammunition.getType().isAir()) {
                  facts.put("ammunition", ammunition.clone());
               }

               return EnchantmentValues.copy(
                  Map.of("attack", attackId, "actor", actor.getUniqueId(), "world", actor.getWorld().getUID(), "facts", facts, "effects", effects)
               );
            }
         }
      } else {
         return Map.of();
      }
   }

   public static boolean dispatch(Plugin owner, Map<String, Object> captured, ScriptHook hook, LivingEntity target, String stage) {
      EnchantmentProviders.requireServerThread();
      if (captured.isEmpty()) {
         return false;
      } else {
         boolean cancelled = false;
         Map<String, Object> snapshot = EnchantmentValues.copy(captured);

         for (Object raw : (List)snapshot.get("effects")) {
            Map<?, ?> effect = (Map<?, ?>)raw;
            String id = (String)effect.get("id");

            try {
               EnchantmentItems.Resolved resolved = EnchantmentDefinitions.resolve(id);
               if (resolved != null
                  && resolved.available()
                  && resolved.provider().generation().equals(effect.get("generation"))
                  && Long.valueOf(resolved.provider().revision()).equals(effect.get("revision"))) {
                  int level = (Integer)effect.get("level");
                  Object authoredLifetime = resolved.definition().parametersAt(level).get("lifetime_ticks");
                  long lifetime = authoredLifetime == null ? 1L : lifetime(authoredLifetime);
                  EnchantmentActions.Source source = new EnchantmentActions.Source(
                     (UUID)snapshot.get("attack"),
                     (UUID)snapshot.get("actor"),
                     (UUID)snapshot.get("world"),
                     lifetime,
                     EnchantmentValues.copy((Map<?, ?>)snapshot.get("facts"))
                  );
                  EnchantmentActions.Action action = EnchantmentActions.begin(resolved, level, source, hook);
                  if (action != null) {
                     action.dispatchFinal(stage, hook, target == null ? null : target.getUniqueId());
                     cancelled |= action.cancelledInput();
                  }
               }
            } catch (RuntimeException var18) {
               owner.getLogger().warning("Optional enchantment " + id + " failed for attack " + snapshot.get("attack") + ": " + var18.getMessage());
            }
         }

         return cancelled;
      }
   }

   private static long lifetime(Object authored) {
      if (authored instanceof Number number
         && Double.isFinite(number.doubleValue())
         && !(number.doubleValue() < 1.0)
         && !(number.doubleValue() >= 9.223372E18F)
         && number.doubleValue() == Math.rint(number.doubleValue())) {
         return number.longValue();
      } else {
         throw new IllegalArgumentException("lifetime_ticks must be a positive whole number");
      }
   }

   public static void runExplicitDamage(Plugin owner, Player actor, Runnable damage) {
      EnchantmentProviders.requireServerThread();
      MetadataValue previous = actor.getMetadata("nightbreak_enchantment_explicit_damage")
         .stream()
         .filter(value -> value.getOwningPlugin() == owner)
         .findFirst()
         .orElse(null);
      actor.setMetadata("nightbreak_enchantment_explicit_damage", new FixedMetadataValue(owner, true));

      try {
         damage.run();
      } finally {
         actor.removeMetadata("nightbreak_enchantment_explicit_damage", owner);
         if (previous != null) {
            actor.setMetadata("nightbreak_enchantment_explicit_damage", previous);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void shoot(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player player && event.getProjectile() instanceof Projectile projectile && this.elected()) {
         EquipmentSlot hand = event.getHand();

         try {
            Map<String, Object> captured = capture(
               player,
               event.getBow(),
               slot(hand),
               inventorySlot(player, hand),
               event.shouldConsumeItem() ? event.getConsumable() : null,
               "",
               projectile.getUniqueId()
            );
            projectile.setMetadata("nightbreak_enchantment_shot", new FixedMetadataValue(this.plugin, captured));
         } catch (RuntimeException var9) {
            this.warn(player, var9);
         }

         try {
            if (event.shouldConsumeItem() && projectile instanceof AbstractArrow) {
               Map<String, Object> ammunition = capture(
                  player, event.getConsumable(), slot(hand), inventorySlot(player, hand), event.getConsumable(), "", projectile.getUniqueId()
               );
               if (!ammunition.isEmpty()) {
                  LinkedHashMap<String, Object> source = new LinkedHashMap<>(ammunition);
                  LinkedHashMap<String, Object> facts = new LinkedHashMap<>(EnchantmentValues.copy((Map<?, ?>)ammunition.get("facts")));
                  facts.remove("inventory_slot");
                  facts.put("projectile", projectile.getUniqueId());
                  source.put("facts", facts);
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     if (projectile.isValid() && Bukkit.getEntity(projectile.getUniqueId()) == projectile && this.elected()) {
                        dispatch(this.plugin, source, PROJECTILE_LAUNCH, null, "launch");
                     }
                  });
               }
            }
         } catch (RuntimeException var8) {
            this.warn(player, var8);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void sneak(PlayerToggleSneakEvent event) {
      if (event.isSneaking() && this.elected()) {
         Player actor = event.getPlayer();
         UUID attack = UUID.randomUUID();
         Map<String, Map<String, Object>> sources = new TreeMap<>();
         Map<String, Map<?, ?>> effects = new TreeMap<>();
         Map<String, Integer> totals = new TreeMap<>();
         EnchantmentInputs.InputFacts inputFacts = new EnchantmentInputs.InputFacts(actor);
         int heldSlot = actor.getInventory().getHeldItemSlot();

         for (EnchantmentDefinition.Slot slot : EnchantmentDefinition.Slot.values()) {
            int index = switch (slot) {
               case MAINHAND -> heldSlot;
               case OFFHAND -> 40;
               case HEAD -> 39;
               case CHEST -> 38;
               case LEGS -> 37;
               case FEET -> 36;
            };

            try {
               Map<String, Object> captured = capture(actor, actor.getInventory().getItem(index), slot, index, null, "", attack, inputFacts);
               if (!captured.isEmpty()) {
                  for (Object raw : (List)captured.get("effects")) {
                     Map<?, ?> effect = (Map<?, ?>)raw;
                     String id = (String)effect.get("id");
                     sources.putIfAbsent(id, captured);
                     effects.putIfAbsent(id, effect);
                     totals.merge(id, (Integer)effect.get("level"), Math::addExact);
                  }
               }
            } catch (RuntimeException var19) {
               this.warn(actor, var19);
            }
         }

         for (Entry<String, Map<String, Object>> entry : sources.entrySet()) {
            LinkedHashMap<String, Object> source = new LinkedHashMap<>(entry.getValue());
            LinkedHashMap<String, Object> facts = new LinkedHashMap<>(EnchantmentValues.copy((Map<?, ?>)source.get("facts")));
            facts.put("equipped_levels", totals);
            source.put("facts", facts);
            source.put("effects", List.of(effects.get(entry.getKey())));
            dispatch(this.plugin, source, SNEAK, null, "sneak");
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public void attack(EntityDamageByEntityEvent event) {
      if (event.getClass() == EntityDamageByEntityEvent.class) {
         Player actor = damageActor(event);
         if (actor != null && this.elected()) {
            for (MetadataValue value : actor.getMetadata("nightbreak_enchantment_damage_observer")) {
               if (value.value() instanceof Deque<?> observers && observers.peekLast() instanceof Consumer<?> observer) {
                  ((Consumer<EntityDamageByEntityEvent>)observer).accept(event);
               }
            }

            if (!isExplicitDamage(event)) {
               if (!event.isCancelled() && !(event.getFinalDamage() <= 0.0) && event.getEntity() instanceof LivingEntity target) {
                  if (event.getDamager() instanceof Player player) {
                     this.fire(
                        player,
                        player.getInventory().getItemInMainHand(),
                        EnchantmentDefinition.Slot.MAINHAND,
                        player.getInventory().getHeldItemSlot(),
                        ATTACK,
                        target
                     );
                  } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                     for (MetadataValue metadata : projectile.getMetadata("nightbreak_enchantment_shot")) {
                        if (metadata.value() instanceof Map<?, ?> source && !source.isEmpty()) {
                           String claim = "nightbreak_enchantment_shot_" + target.getUniqueId();
                           if (projectile.hasMetadata(claim)) {
                              return;
                           }

                           projectile.setMetadata(claim, new FixedMetadataValue(this.plugin, true));

                           try {
                              dispatch(
                                 this.plugin, EnchantmentValues.copy(source), PROJECTILE_HIT, target, projectile.getUniqueId() + "/" + target.getUniqueId()
                              );
                           } catch (RuntimeException var12) {
                              this.warn(player, var12);
                           }

                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static Player damageActor(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player player) {
         return player;
      } else {
         return event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player ? player : null;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void damaged(EntityDamageEvent event) {
      if (event.getClass() == EntityDamageEvent.class
         || event.getClass() == EntityDamageByEntityEvent.class
         || event.getClass() == EntityDamageByBlockEvent.class) {
         if (!(event.getFinalDamage() <= 0.0) && event.getEntity() instanceof Player player && this.elected()) {
            PlayerInventory inventory = player.getInventory();

            for (EnchantmentDefinition.Slot slot : EnchantmentDefinition.Slot.values()) {
               int index = switch (slot) {
                  case MAINHAND -> inventory.getHeldItemSlot();
                  case OFFHAND -> 40;
                  case HEAD -> 39;
                  case CHEST -> 38;
                  case LEGS -> 37;
                  case FEET -> 36;
               };
               this.fire(player, inventory.getItem(index), slot, index, TAKE_DAMAGE, null);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void interact(PlayerInteractEvent event) {
      if (event.getHand() != null && (event.useItemInHand() != Result.DENY || event.useInteractedBlock() != Result.DENY)) {
         if (event.getAction() != Action.PHYSICAL && this.elected()) {
            boolean right = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            Player player = event.getPlayer();
            ScriptHook hook = right ? (player.isSneaking() ? SHIFT_RIGHT_CLICK : RIGHT_CLICK) : (player.isSneaking() ? SHIFT_LEFT_CLICK : LEFT_CLICK);
            if (this.fire(player, event.getItem(), slot(event.getHand()), inventorySlot(player, event.getHand()), hook, null)) {
               event.setUseItemInHand(Result.DENY);
               event.setUseInteractedBlock(Result.DENY);
            }
         }
      }
   }

   @EventHandler
   public void quit(PlayerQuitEvent event) {
      String prefix = event.getPlayer().getUniqueId() + "|";
      this.warned.removeIf(key -> key.startsWith(prefix));
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void chat(AsyncPlayerChatEvent event) {
      UUID actorId = event.getPlayer().getUniqueId();
      String message = event.getMessage();
      Bukkit.getScheduler().runTask(this.plugin, () -> {
         Player actor = Bukkit.getPlayer(actorId);
         if (actor != null && actor.isOnline() && !actor.isDead() && this.elected()) {
            for (EnchantmentDefinition.Slot slot : EnchantmentDefinition.Slot.values()) {
               int index = switch (slot) {
                  case MAINHAND -> actor.getInventory().getHeldItemSlot();
                  case OFFHAND -> 40;
                  case HEAD -> 39;
                  case CHEST -> 38;
                  case LEGS -> 37;
                  case FEET -> 36;
               };

               try {
                  UUID id = UUID.randomUUID();
                  Map<String, Object> captured = capture(actor, actor.getInventory().getItem(index), slot, index, null, "", id);
                  if (!captured.isEmpty()) {
                     Map<String, Object> source = new LinkedHashMap<>(captured);
                     Map<String, Object> original = (Map<String, Object>)captured.get("facts");
                     Map<String, Object> facts = new LinkedHashMap<>(original);
                     facts.put("message", message);
                     source.put("facts", facts);
                     dispatch(this.plugin, source, CHAT, null, id.toString());
                  }
               } catch (RuntimeException var14) {
                  this.warn(actor, var14);
               }
            }
         }
      });
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void mine(BlockBreakEvent event) {
      if (event.getClass() == BlockBreakEvent.class && this.elected()) {
         Player actor = event.getPlayer();

         try {
            UUID id = UUID.randomUUID();
            Map<String, Object> captured = capture(
               actor, actor.getInventory().getItemInMainHand(), EnchantmentDefinition.Slot.MAINHAND, actor.getInventory().getHeldItemSlot(), null, "", id
            );
            if (captured.isEmpty()) {
               return;
            }

            Map<String, Object> source = new LinkedHashMap<>(captured);
            Map<String, Object> originalFacts = (Map<String, Object>)captured.get("facts");
            Map<String, Object> facts = new LinkedHashMap<>(originalFacts);
            Block block = event.getBlock();
            facts.put("block", Map.of("x", block.getX(), "y", block.getY(), "z", block.getZ(), "material", block.getType().name().toLowerCase(Locale.ROOT)));
            facts.put("drop_items", event.isDropItems());
            facts.put("sneaking", actor.isSneaking());
            source.put("facts", facts);
            dispatch(this.plugin, source, BREAK_BLOCK, null, id.toString());
         } catch (RuntimeException var9) {
            this.warn(actor, var9);
         }
      }
   }

   private boolean fire(Player actor, ItemStack item, EnchantmentDefinition.Slot slot, int index, ScriptHook hook, LivingEntity target) {
      try {
         UUID id = UUID.randomUUID();
         return dispatch(this.plugin, capture(actor, item, slot, index, null, "", id), hook, target, id.toString());
      } catch (RuntimeException var8) {
         this.warn(actor, var8);
         return false;
      }
   }

   private void warn(Player player, RuntimeException failure) {
      if (this.warned.add(player.getUniqueId() + "|" + failure.getMessage())) {
         this.plugin.getLogger().warning("Enchantment input rejected for " + player.getName() + ": " + failure.getMessage());
      }
   }

   private static EnchantmentDefinition.Slot slot(EquipmentSlot hand) {
      return hand == EquipmentSlot.HAND ? EnchantmentDefinition.Slot.MAINHAND : EnchantmentDefinition.Slot.OFFHAND;
   }

   private static int inventorySlot(Player player, EquipmentSlot hand) {
      return hand == EquipmentSlot.HAND ? player.getInventory().getHeldItemSlot() : 40;
   }

   private static final class InputFacts {
      private final Player actor;
      private Map<String, ItemStack> equipment;
      private Map<String, Object> providers;
      private RuntimeException failure;

      private InputFacts(Player actor) {
         this.actor = actor;
      }

      private void capture() {
         if (this.failure != null) {
            throw this.failure;
         } else if (this.providers == null) {
            if (this.equipment == null) {
               this.equipment = EnchantmentActions.captureEquipment(this.actor);
            }

            try {
               this.providers = EnchantmentActions.captureProviderFacts(this.actor.getUniqueId(), this.equipment);
            } catch (RuntimeException var2) {
               this.failure = var2;
               throw var2;
            }
         }
      }
   }
}
