package de.oliver.fancynpcs.api.data.property;

import com.google.common.collect.HashMultimap;
import de.oliver.fancynpcs.api.Npc;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

public enum NpcVisibility {
   ALL((player, npc) -> true),
   PERMISSION_REQUIRED((player, npc) -> player.hasPermission("fancynpcs.npc." + npc.getData().getName() + ".see")),
   MANUAL(NpcVisibility.ManualNpcVisibility::canSee);

   private final NpcVisibility.VisibilityPredicate predicate;

   private NpcVisibility(NpcVisibility.VisibilityPredicate predicate) {
      this.predicate = predicate;
   }

   public static Optional<NpcVisibility> byString(String value) {
      return Arrays.stream(values()).filter(visibility -> visibility.toString().equalsIgnoreCase(value)).findFirst();
   }

   public boolean canSee(Player player, Npc npc) {
      return this.predicate.canSee(player, npc);
   }

   public static class ManualNpcVisibility {
      private static final HashMultimap<String, UUID> distantViewers = HashMultimap.create();

      public static boolean canSee(Player player, Npc npc) {
         return npc.isShownFor(player) || distantViewers.containsEntry(npc.getData().getName(), player.getUniqueId());
      }

      public static void addDistantViewer(Npc npc, UUID uuid) {
         addDistantViewer(npc.getData().getName(), uuid);
      }

      public static void addDistantViewer(String npcName, UUID uuid) {
         distantViewers.put(npcName, uuid);
      }

      public static void removeDistantViewer(Npc npc, UUID uuid) {
         removeDistantViewer(npc.getData().getName(), uuid);
      }

      public static void removeDistantViewer(String npcName, UUID uuid) {
         distantViewers.remove(npcName, uuid);
      }

      public static void remove(Npc npc) {
         remove(npc.getData().getName());
      }

      public static void remove(String npcName) {
         distantViewers.removeAll(npcName);
      }

      public static void clear() {
         distantViewers.clear();
      }
   }

   @FunctionalInterface
   public interface VisibilityPredicate {
      boolean canSee(Player var1, Npc var2);
   }
}
