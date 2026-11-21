package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class GriefPreventionHook extends RegionsPlugin<GriefPrevention> {
   boolean supportsUUIDs;
   @Nullable
   Method getClaim;
   @Nullable
   Field claimsField;

   public GriefPreventionHook() throws IOException {
   }

   @Override
   protected boolean init() {
      this.supportsUUIDs = Skript.fieldExists(Claim.class, "ownerID");

      try {
         this.getClaim = DataStore.class.getDeclaredMethod("getClaim", long.class);
         this.getClaim.setAccessible(true);
         if (!Claim.class.isAssignableFrom(this.getClaim.getReturnType())) {
            this.getClaim = null;
         }
      } catch (NoSuchMethodException var4) {
      } catch (SecurityException var5) {
      }

      try {
         this.claimsField = DataStore.class.getDeclaredField("claims");
         this.claimsField.setAccessible(true);
         if (!List.class.isAssignableFrom(this.claimsField.getType())) {
            this.claimsField = null;
         }
      } catch (NoSuchFieldException var2) {
      } catch (SecurityException var3) {
      }

      if (this.getClaim == null && this.claimsField == null) {
         Skript.error(
            "Skript "
               + Skript.getVersion()
               + " is not compatible with GriefPrevention "
               + this.plugin.getDescription().getVersion()
               + ". Please report this at https://github.com/SkriptLang/Skript/issues/ if this error occurred after you updated GriefPrevention."
         );
         return false;
      } else {
         return super.init();
      }
   }

   @Nullable
   Claim getClaim(long id) {
      if (this.getClaim != null) {
         try {
            return (Claim)this.getClaim.invoke(this.plugin.dataStore, id);
         } catch (IllegalAccessException var6) {
            assert false : var6;
         } catch (IllegalArgumentException var7) {
            assert false : var7;
         } catch (InvocationTargetException var8) {
            throw new RuntimeException(var8.getCause());
         }
      } else {
         assert this.claimsField != null;

         try {
            for (Object claim : (List)this.claimsField.get(this.plugin.dataStore)) {
               if (claim instanceof Claim && ((Claim)claim).getID() == id) {
                  return (Claim)claim;
               }
            }
         } catch (IllegalArgumentException var9) {
            assert false : var9;
         } catch (IllegalAccessException var10) {
            assert false : var10;
         }
      }

      return null;
   }

   @Override
   public String getName() {
      return "GriefPrevention";
   }

   @Override
   public boolean canBuild_i(Player p, Location l) {
      return this.plugin.allowBuild(p, l) == null;
   }

   @Override
   public Collection<? extends Region> getRegionsAt_i(Location l) {
      Claim c = this.plugin.dataStore.getClaimAt(l, false, null);
      return (Collection<? extends Region>)(c != null ? Arrays.asList(new GriefPreventionHook.GriefPreventionRegion(c)) : Collections.emptySet());
   }

   @Nullable
   @Override
   public Region getRegion_i(World world, String name) {
      try {
         Claim c = this.getClaim(Long.parseLong(name));
         return c != null && world.equals(c.getLesserBoundaryCorner().getWorld()) ? new GriefPreventionHook.GriefPreventionRegion(c) : null;
      } catch (NumberFormatException var4) {
         return null;
      }
   }

   @Override
   public boolean hasMultipleOwners_i() {
      return false;
   }

   @Override
   protected Class<? extends Region> getRegionClass() {
      return GriefPreventionHook.GriefPreventionRegion.class;
   }

   static {
      Variables.yggdrasil.registerSingleClass(GriefPreventionHook.GriefPreventionRegion.class);
   }

   @YggdrasilID("GriefPreventionRegion")
   public final class GriefPreventionRegion extends Region {
      private transient Claim claim;

      private GriefPreventionRegion() {
      }

      public GriefPreventionRegion(final Claim c) {
         this.claim = c;
      }

      @Override
      public boolean contains(Location l) {
         return this.claim.contains(l, false, false);
      }

      @Override
      public boolean isMember(OfflinePlayer p) {
         return this.isOwner(p);
      }

      @Override
      public Collection<OfflinePlayer> getMembers() {
         return this.getOwners();
      }

      @Override
      public boolean isOwner(OfflinePlayer p) {
         String name = p.getName();
         return name != null ? name.equalsIgnoreCase(this.claim.getOwnerName()) : false;
      }

      @Override
      public Collection<OfflinePlayer> getOwners() {
         if (!this.claim.isAdminClaim() && (!GriefPreventionHook.this.supportsUUIDs || this.claim.ownerID != null)) {
            return GriefPreventionHook.this.supportsUUIDs
               ? Arrays.asList(Bukkit.getOfflinePlayer(this.claim.ownerID))
               : Arrays.asList(Bukkit.getOfflinePlayer(this.claim.getOwnerName()));
         } else {
            return Collections.emptyList();
         }
      }

      @Override
      public Iterator<Block> getBlocks() {
         Location lower = this.claim.getLesserBoundaryCorner();
         Location upper = this.claim.getGreaterBoundaryCorner();
         if (lower != null && upper != null && lower.getWorld() != null && upper.getWorld() != null && lower.getWorld() == upper.getWorld()) {
            upper.setY(upper.getWorld().getMaxHeight() - 1);
            upper.setX(upper.getBlockX());
            upper.setZ(upper.getBlockZ());
            return new AABB(lower, upper).iterator();
         } else {
            return EmptyIterator.get();
         }
      }

      @Override
      public String toString() {
         return "Claim #" + this.claim.getID();
      }

      @Override
      public Fields serialize() {
         Fields f = new Fields();
         f.putPrimitive("id", this.claim.getID());
         return f;
      }

      @Override
      public void deserialize(Fields fields) throws StreamCorruptedException {
         long id = fields.getPrimitive("id", long.class);
         Claim c = GriefPreventionHook.this.getClaim(id);
         if (c == null) {
            throw new StreamCorruptedException("Invalid claim " + id);
         } else {
            this.claim = c;
         }
      }

      @Override
      public RegionsPlugin<?> getPlugin() {
         return GriefPreventionHook.this;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (o == this) {
            return true;
         } else if (o == null) {
            return false;
         } else {
            return !(o instanceof GriefPreventionHook.GriefPreventionRegion) ? false : this.claim.equals(((GriefPreventionHook.GriefPreventionRegion)o).claim);
         }
      }

      @Override
      public int hashCode() {
         return this.claim.hashCode();
      }
   }
}
