package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code OpenSit-1.0.6.jar} (io.github.illa4257.opensit.OpenSit).
 * <p>
 * Sitting implemented with an invisible {@link ArmorStand} seat (which is how
 * the original did it): right-click stairs/slabs while sneaking, or use
 * {@code /sit}. The seat is removed when the player leaves, stands up, takes
 * damage or the block below disappears.
 */
public final class OpenSitModule extends EssentialsModule implements Listener {

    private final Map<UUID, ArmorStand> seats = new HashMap<>();

    public OpenSitModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "opensit", "OpenSit-1.0.6.jar", "Sit on stairs and slabs with an invisible seat entity");
    }

    @Override
    protected void onEnable() {
        command("sit", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            sit(player, player.getLocation());
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            for (ArmorStand stand : seats.values()) {
                if (stand.isValid()) {
                    stand.remove();
                }
            }
            seats.clear();
        });
        listen(this);
    }

    private void sit(Player player, Location where) {
        standUp(player);
        Location seat = where.clone();
        seat.setYaw(player.getLocation().getYaw());
        ArmorStand stand = where.getWorld().spawn(seat.subtract(0, 1.05, 0), ArmorStand.class, entity -> {
            entity.setInvisible(true);
            entity.setMarker(true);
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setSmall(true);
            entity.setPersistent(false);
            entity.setCanMove(false);
        });
        stand.addPassenger(player);
        seats.put(player.getUniqueId(), stand);
        send(player, "sat");
    }

    private void standUp(Player player) {
        ArmorStand stand = seats.remove(player.getUniqueId());
        if (stand == null) {
            return;
        }
        if (stand.isValid()) {
            stand.eject();
            stand.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!cfgBool("require-sneak", true) || !player.isSneaking()) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        Material type = event.getClickedBlock().getType();
        boolean stairs = Tag.STAIRS.isTagged(type);
        boolean slabs = Tag.SLABS.isTagged(type);
        if (!stairs && !slabs) {
            return;
        }
        if (cfgBool("only-stairs", false) && !stairs) {
            return;
        }
        event.setCancelled(true);
        Location target = event.getClickedBlock().getLocation().add(0.5, 0.0, 0.5);
        sit(player, target.add(0, 1, 0));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        standUp(event.getPlayer());
    }

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            standUp(player);
        }
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        ArmorStand stand = seats.get(event.getPlayer().getUniqueId());
        if (stand == null) {
            return;
        }
        // passengers are moved by the seat; if the player leaves the seat (sneak),
        // the vehicle link is lost and we clean up
        if (!stand.getPassengers().contains(event.getPlayer()) && stand.isValid()) {
            stand.remove();
            seats.remove(event.getPlayer().getUniqueId());
        }
        Vector velocity = event.getPlayer().getVelocity();
        if (velocity.lengthSquared() > 1.0) {
            standUp(event.getPlayer());
        }
    }
}
