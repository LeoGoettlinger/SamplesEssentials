package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class WorldGuardHook extends RegionsPlugin<WorldGuardPlugin> {
   public WorldGuardHook() throws IOException {
   }

   @Override
   protected boolean init() {
      if (!Skript.classExists("com.sk89q.worldedit.math.BlockVector3")) {
         Skript.error("WorldEdit you're using is not compatible with Skript. Disabling WorldGuard support!");
         return false;
      } else {
         return super.init();
      }
   }

   @Override
   public String getName() {
      return "WorldGuard";
   }

   @Override
   public boolean canBuild_i(Player p, Location l) {
      if (p.hasPermission("worldguard.region.bypass." + l.getWorld().getName())) {
         return true;
      } else {
         WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
         RegionQuery query = platform.getRegionContainer().createQuery();
         return query.testBuild(BukkitAdapter.adapt(l), this.plugin.wrapPlayer(p), new StateFlag[0]);
      }
   }

   @Override
   public Collection<? extends Region> getRegionsAt_i(@Nullable Location l) {
      ArrayList<Region> r = new ArrayList<>();
      if (l == null) {
         return Collections.emptyList();
      } else if (l.getWorld() == null) {
         return Collections.emptyList();
      } else {
         WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
         RegionManager manager = platform.getRegionContainer().get(BukkitAdapter.adapt(l.getWorld()));
         if (manager == null) {
            return r;
         } else {
            ApplicableRegionSet applicable = manager.getApplicableRegions(BukkitAdapter.asBlockVector(l));
            if (applicable == null) {
               return r;
            } else {
               for (ProtectedRegion region : applicable) {
                  r.add(new WorldGuardHook.WorldGuardRegion(l.getWorld(), region));
               }

               return r;
            }
         }
      }
   }

   @Nullable
   @Override
   public Region getRegion_i(World world, String name) {
      WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
      RegionManager manager = platform.getRegionContainer().get(BukkitAdapter.adapt(world));
      if (manager == null) {
         return null;
      } else {
         ProtectedRegion region = manager.getRegion(name);
         return region != null ? new WorldGuardHook.WorldGuardRegion(world, region) : null;
      }
   }

   @Override
   public boolean hasMultipleOwners_i() {
      return true;
   }

   @Override
   protected Class<? extends Region> getRegionClass() {
      return WorldGuardHook.WorldGuardRegion.class;
   }

   static {
      Variables.yggdrasil.registerSingleClass(WorldGuardHook.WorldGuardRegion.class);
   }

   @YggdrasilID("WorldGuardRegion")
   public final class WorldGuardRegion extends Region {
      final World world;
      private transient ProtectedRegion region;

      private WorldGuardRegion() {
         this.world = null;
      }

      public WorldGuardRegion(final World w, final ProtectedRegion r) {
         this.world = w;
         this.region = r;
      }

      @Override
      public boolean contains(Location l) {
         return l.getWorld().equals(this.world) && this.region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
      }

      @Override
      public boolean isMember(OfflinePlayer p) {
         return this.region.isMember(WorldGuardHook.this.plugin.wrapOfflinePlayer(p));
      }

      @Override
      public Collection<OfflinePlayer> getMembers() {
         Collection<UUID> ids = this.region.getMembers().getUniqueIds();
         Collection<OfflinePlayer> r = new ArrayList<>(ids.size());

         for (UUID id : ids) {
            r.add(Bukkit.getOfflinePlayer(id));
         }

         return r;
      }

      @Override
      public boolean isOwner(OfflinePlayer p) {
         return this.region.isOwner(WorldGuardHook.this.plugin.wrapOfflinePlayer(p));
      }

      @Override
      public Collection<OfflinePlayer> getOwners() {
         Collection<UUID> ids = this.region.getOwners().getUniqueIds();
         Collection<OfflinePlayer> r = new ArrayList<>(ids.size());

         for (UUID id : ids) {
            r.add(Bukkit.getOfflinePlayer(id));
         }

         return r;
      }

      @Override
      public Iterator<Block> getBlocks() {
         BlockVector3 min = this.region.getMinimumPoint();
         BlockVector3 max = this.region.getMaximumPoint();
         return new AABB(
               this.world, new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()), new Vector(max.getBlockX(), max.getBlockY(), max.getBlockZ())
            )
            .iterator();
      }

      @Override
      public Fields serialize() throws NotSerializableException {
         Fields f = new Fields(this);
         f.putObject("region", this.region.getId());
         return f;
      }

      @Override
      public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
         String r = fields.getAndRemoveObject("region", String.class);
         fields.setFields(this);
         WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
         ProtectedRegion region = platform.getRegionContainer().get(BukkitAdapter.adapt(this.world)).getRegion(r);
         if (region == null) {
            throw new StreamCorruptedException("Invalid region " + r + " in world " + this.world);
         } else {
            this.region = region;
         }
      }

      @Override
      public String toString() {
         return this.region.getId() + " in world " + this.world.getName();
      }

      @Override
      public RegionsPlugin<?> getPlugin() {
         return WorldGuardHook.this;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (o == this) {
            return true;
         } else if (o == null) {
            return false;
         } else {
            return !(o instanceof WorldGuardHook.WorldGuardRegion)
               ? false
               : this.world.equals(((WorldGuardHook.WorldGuardRegion)o).world) && this.region.equals(((WorldGuardHook.WorldGuardRegion)o).region);
         }
      }

      @Override
      public int hashCode() {
         return this.world.hashCode() * 31 + this.region.hashCode();
      }
   }
}
