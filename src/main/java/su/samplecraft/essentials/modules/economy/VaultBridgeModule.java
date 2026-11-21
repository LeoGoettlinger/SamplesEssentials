package su.samplecraft.essentials.modules.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.samplecraft.essentials.module.EssentialsModule;

/**
 * Merged from {@code VaultUnlocked-2.20.2.jar} usage plus the old
 * balance-on-login QoL from the economy stack.
 * <p>
 * VaultUnlocked stays installed as the economy service provider (it was
 * deliberately kept out of the merge because it is the bridge other plugins
 * register against). This module is the SamplesEssentials-side consumer:
 * it resolves the economy service and can show the balance on join.
 */
public final class VaultBridgeModule extends EssentialsModule implements Listener {

    private Economy economy;

    public VaultBridgeModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("economy", "vaultbridge", "VaultUnlocked-2.20.2.jar", "Vault economy bridge with optional balance-on-join");
    }

    @Override
    protected void onEnable() {
        RegisteredServiceProvider<Economy> registration =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            plugin.getLogger().info("economy/vaultbridge: no Vault economy registered yet - bridge idle.");
            return;
        }
        economy = registration.getProvider();
        plugin.getLogger().info("economy/vaultbridge: connected to " + economy.getName());
        if (cfgBool("show-balance-on-join", true)) {
            listen(this);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (economy == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (!player.isOnline()) {
                return;
            }
            if (!economy.hasAccount(player)) {
                economy.createPlayerAccount(player);
            }
            player.sendMessage(msgComponent("balance",
                    "balance", economy.format(economy.getBalance(player)),
                    "player", player.getName()));
        }, Math.max(1L, cfgInt("join-delay-ticks", 20)));
    }

    public Economy economy() {
        return economy;
    }
}
