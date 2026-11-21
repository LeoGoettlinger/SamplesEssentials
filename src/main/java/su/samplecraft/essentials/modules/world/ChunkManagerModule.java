package su.samplecraft.essentials.modules.world;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.module.EssentialsModule;

/**
 * Merged from {@code ChunkManager-1.3.1.jar} (cm.chunkManager.ChunkManager).
 * <p>
 * Kept from the original: world unload/cleanup, a periodic chunk GC for
 * empty worlds (managed by the {@code auto-unload} settings), and the
 * {@code /chunkmanager status|gc} diagnostics command.
 */
public final class ChunkManagerModule extends EssentialsModule {

    public ChunkManagerModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("world", "chunkmanager", "ChunkManager-1.3.1.jar", "World unload, chunk GC and memory diagnostics");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.chunkmanager", "Chunk tools", org.bukkit.permissions.PermissionDefault.OP);
        command("unloadworld", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage(plugin.getLang().component("usage"));
                return true;
            }
            World world = plugin.getServer().getWorld(args[0]);
            if (world == null) {
                sender.sendMessage(msgComponent("unknown-world", "world", args[0]));
                return true;
            }
            String name = world.getName();
            boolean ok = plugin.getServer().unloadWorld(world, cfgBool("save-on-unload", true));
            sender.sendMessage(msgComponent(ok ? "unloaded" : "unload-failed", "world", name));
            return true;
        });
        command("chunkmanager", new Handler());
        onDisableHook(this::unbindCommands);
        if (cfgInt("unload-after-minutes", 0) > 0) {
            long minutes = Math.max(1, cfgInt("unload-after-minutes", 30));
            syncRepeat(20L * 60 * minutes, 20L * 60 * minutes, this::autoUnload);
        }
    }

    private void autoUnload() {
        String prefix = cfgString("auto-unload-prefix", "world_");
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getPlayers().isEmpty() && world.getName().startsWith(prefix)) {
                plugin.getLogger().info("world/chunkmanager: auto-unloading idle world " + world.getName());
                plugin.getServer().unloadWorld(world, cfgBool("save-on-unload", true));
            }
        }
    }

    private final class Handler implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            String action = args.length > 0 ? args[0].toLowerCase(java.util.Locale.ROOT) : "status";
            if (action.equals("gc")) {
                int freed = 0;
                for (World world : plugin.getServer().getWorlds()) {
                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        if (!chunk.isForceLoaded() && world.getPlayers().isEmpty()) {
                            world.unloadChunkRequest(chunk.getX(), chunk.getZ());
                            freed++;
                        }
                    }
                }
                sender.sendMessage(msgComponent("gc-done", "count", String.valueOf(freed)));
                return true;
            }
            Runtime runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            long maxMb = runtime.maxMemory() / 1024 / 1024;
            int loaded = 0;
            for (World world : plugin.getServer().getWorlds()) {
                loaded += world.getLoadedChunks().length;
            }
            sender.sendMessage(msgComponent("status",
                    "used", String.valueOf(usedMb),
                    "max", String.valueOf(maxMb),
                    "chunks", String.valueOf(loaded),
                    "worlds", String.valueOf(plugin.getServer().getWorlds().size()),
                    "players", String.valueOf(plugin.getServer().getOnlinePlayers().size())));
            return true;
        }

        @Override
        public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                   @NotNull String alias, String @NotNull [] args) {
            return args.length == 1 ? java.util.List.of("status", "gc") : java.util.List.of();
        }
    }
}
