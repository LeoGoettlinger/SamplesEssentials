package su.samplecraft.essentials.modules.social;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import su.samplecraft.essentials.SamplesEssentials;

import java.util.UUID;

/**
 * Shared name/&lt;-&gt;UUID resolution used by the social modules (friends, messages,
 * bounties). Mirrors the original lookup order: online player, then the server
 * player cache, then offline players.
 */
public final class PlayerDirectory {

    private final SamplesEssentials plugin;

    public PlayerDirectory(SamplesEssentials plugin) {
        this.plugin = plugin;
    }

    public record Entry(UUID id, String name) {
    }

    public Entry find(String name) {
        if (name == null) {
            return null;
        }
        Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) {
            return new Entry(online.getUniqueId(), online.getName());
        }
        Player partial = plugin.getServer().getPlayer(name);
        if (partial != null) {
            return new Entry(partial.getUniqueId(), partial.getName());
        }
        UUID cached = plugin.getServer().getPlayerUniqueId(name);
        if (cached != null) {
            String known = plugin.getServer().getOfflinePlayer(cached).getName();
            return new Entry(cached, known == null ? name : known);
        }
        for (OfflinePlayer offline : plugin.getServer().getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(name)) {
                return new Entry(offline.getUniqueId(), offline.getName());
            }
        }
        return null;
    }

    public String nameOf(UUID id) {
        Player online = plugin.getServer().getPlayer(id);
        if (online != null) {
            return online.getName();
        }
        return plugin.getServer().getOfflinePlayer(id).getName();
    }

    public boolean isOnline(UUID id) {
        Player player = plugin.getServer().getPlayer(id);
        return player != null && player.isOnline();
    }
}
