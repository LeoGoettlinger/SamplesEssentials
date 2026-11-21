package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.ai.route.CurvedRoute;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class TransportEditor implements AutoCloseable {
   private final TransportModule module;
   private final Map<UUID, TransportEditor.Draft> drafts = new HashMap<>();
   private final BukkitTask preview;

   TransportEditor(TransportModule module, Plugin plugin) {
      this.module = module;
      this.preview = Bukkit.getScheduler().runTaskTimer(plugin, this::preview, 10L, 10L);
   }

   public void execute(Player player, String action, String value) {
      try {
         if (action.equals("create") || action.equals("edit")) {
            if (this.drafts.containsKey(player.getUniqueId())) {
               throw new IllegalArgumentException("Save or cancel your current draft first.");
            }

            TransportRoute route = this.module.files.get(value);
            if (action.equals("create") && this.module.files.hasDefinition(value)) {
               throw new IllegalArgumentException("Route file already exists. Use edit, or correct its configuration if disabled or invalid.");
            }

            if (action.equals("edit") && route == null) {
               throw new IllegalArgumentException("Unknown route.");
            }

            if (value != null && value.matches("[a-z0-9_-]{1,64}")) {
               World world = route == null ? player.getWorld() : TransportModule.resolveWorld(player, route);
               String authoredWorld = world.getName();
               if (PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon) {
                  authoredWorld = dungeon.getContentPackagesConfigFields().getWorldName();
               }

               TransportEditor.Draft draft = new TransportEditor.Draft(value, world, authoredWorld, route);
               if (route == null) {
                  draft.points.add(player.getLocation().toVector());
               }

               this.drafts.put(player.getUniqueId(), draft);
               TransportModule.message(
                  player, "Editing " + value + ". Fly to each waypoint and use /em transport add. The faint curve previews your next point."
               );
               return;
            }

            throw new IllegalArgumentException("Use a lowercase route id with letters, digits, underscores or hyphens.");
         }

         if (action.equals("list")) {
            TransportModule.message(player, String.join(", ", this.module.files.ids()));
            return;
         }

         if (action.equals("reload")) {
            this.module.files.reload();
            TransportModule.message(player, "Routes reloaded; active flights retain their original route.");
            return;
         }

         if (action.equals("start")) {
            TransportModule.startRoute(player, value);
            return;
         }

         if (action.equals("stop")) {
            this.module.cancel(player);
            return;
         }

         TransportEditor.Draft draft = this.drafts.get(player.getUniqueId());
         if (draft == null) {
            throw new IllegalArgumentException("Use /em transport create <id> or edit <id> first.");
         }

         if (action.equals("cancel")) {
            this.drafts.remove(player.getUniqueId());
            TransportModule.message(player, "Draft discarded.");
            return;
         }

         if (!player.getWorld().equals(draft.world)) {
            throw new IllegalArgumentException("Return to the draft's world to edit it.");
         }

         switch (action) {
            case "add":
            case "remove":
            case "move":
               List<Vector> next = new ArrayList<>(draft.points);
               if (action.equals("add")) {
                  next.add(player.getLocation().toVector());
               } else if (action.equals("remove")) {
                  if (next.isEmpty()) {
                     throw new IllegalArgumentException("No waypoints to remove.");
                  }

                  next.removeLast();
               } else {
                  int index = Integer.parseInt(value) - 1;
                  if (index < 0 || index >= next.size()) {
                     throw new IllegalArgumentException("Waypoint number is out of range.");
                  }

                  next.set(index, player.getLocation().toVector());
               }

               if (next.size() >= 2) {
                  new CurvedRoute(next);
               }

               if (draft.undo.size() == 32) {
                  draft.undo.removeLast();
               }

               draft.undo.push(List.copyOf(draft.points));
               draft.points = next;
               draft.dirty = true;
               draft.ghostPosition = null;
               break;
            case "undo":
               if (draft.undo.isEmpty()) {
                  throw new IllegalArgumentException("Nothing to undo.");
               }

               draft.points = new ArrayList<>(draft.undo.pop());
               draft.dirty = true;
               draft.ghostPosition = null;
               break;
            case "save":
               this.module.files.save(draft.route(player.getLocation().getYaw()));
               this.drafts.remove(player.getUniqueId());
               TransportModule.message(player, "Saved " + draft.id + ". Board at its first waypoint with /em transport start " + draft.id + ".");
               return;
            case "ride":
               this.module.start(player, draft.route(player.getLocation().getYaw()));
               return;
            case "status":
               break;
            default:
               throw new IllegalArgumentException("Unknown editor action.");
         }

         TransportModule.message(player, draft.id + ": " + draft.points.size() + " waypoints. Add, move <number>, remove, undo, save, ride or cancel.");
      } catch (Exception var9) {
         TransportModule.message(player, var9.getMessage());
      }
   }

   private void preview() {
      Iterator<Entry<UUID, TransportEditor.Draft>> iterator = this.drafts.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, TransportEditor.Draft> entry = iterator.next();
         Player player = Bukkit.getPlayer(entry.getKey());
         if (player == null) {
            iterator.remove();
         } else {
            TransportEditor.Draft draft = entry.getValue();
            if (player.getWorld() == draft.world) {
               if (draft.dirty) {
                  draft.curve = draft.points.size() < 2 ? null : new CurvedRoute(draft.points);
                  draft.dirty = false;
               }

               Vector position = player.getLocation().toVector();
               if (draft.ghostPosition == null || draft.ghostPosition.distanceSquared(position) > 0.25) {
                  draft.ghostPosition = position;
                  ArrayList<Vector> candidate = new ArrayList<>(draft.points);
                  candidate.add(position);

                  try {
                     draft.ghost = new CurvedRoute(candidate);
                  } catch (IllegalArgumentException var8) {
                     draft.ghost = null;
                  }
               }

               draw(player, draft.curve, false);
               draw(player, draft.ghost, true);

               for (Vector point : draft.points) {
                  if (point.distanceSquared(position) < 9216.0) {
                     player.spawnParticle(Particle.END_ROD, point.toLocation(draft.world), 1, 0.0, 0.0, 0.0, 0.0);
                  }
               }
            }
         }
      }
   }

   private static void draw(Player player, CurvedRoute curve, boolean ghost) {
      if (curve != null) {
         Vector viewer = player.getLocation().toVector();
         double step = Math.max(0.5, curve.length() / 300.0);

         for (double distance = 0.0; distance <= curve.length(); distance += step) {
            Vector point = curve.at(distance);
            if (!(point.distanceSquared(viewer) > 9216.0)) {
               Color color = ghost ? Color.AQUA : Color.LIME;
               player.spawnParticle(Particle.DUST, point.toLocation(player.getWorld()), 1, new DustOptions(color, ghost ? 0.5F : 1.0F));
            }
         }
      }
   }

   @Override
   public void close() {
      this.preview.cancel();
      this.drafts.clear();
   }

   private static final class Draft {
      final String id;
      final World world;
      final String authoredWorld;
      final TransportRoute original;
      List<Vector> points = new ArrayList<>();
      final Deque<List<Vector>> undo = new ArrayDeque<>();
      boolean dirty = true;
      CurvedRoute curve;
      CurvedRoute ghost;
      Vector ghostPosition;

      Draft(String id, World world, String authoredWorld, TransportRoute original) {
         this.id = id;
         this.world = world;
         this.authoredWorld = authoredWorld;
         this.original = original;
         if (original != null) {
            this.points.addAll(original.points());
         }
      }

      TransportRoute route(float yaw) {
         return new TransportRoute(
            this.id,
            this.original == null ? this.id : this.original.name(),
            this.original == null ? this.authoredWorld : this.original.world(),
            this.original == null ? "transport_cannon_seat.yml" : this.original.transportEntity(),
            this.original == null ? 12.0 : this.original.speed(),
            this.original == null ? 8.0 : this.original.acceleration(),
            this.original == null ? 60 : this.original.countdown(),
            yaw,
            this.points
         );
      }
   }
}
