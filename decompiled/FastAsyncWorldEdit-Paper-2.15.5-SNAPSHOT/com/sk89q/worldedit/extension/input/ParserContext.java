package com.sk89q.worldedit.extension.input;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import javax.annotation.Nullable;
import org.enginehub.piston.inject.InjectedValueAccess;

public class ParserContext {
   @Nullable
   private Extent extent;
   @Nullable
   private LocalSession session;
   @Nullable
   private World world;
   @Nullable
   private Actor actor;
   private boolean restricted = true;
   private boolean tryLegacy = true;
   private boolean preferringWildcard;
   private InjectedValueAccess injected;
   private int minY = Integer.MIN_VALUE;
   private int maxY = Integer.MAX_VALUE;

   public ParserContext() {
   }

   public ParserContext(ParserContext other) {
      this.setExtent(other.getExtent());
      this.setSession(other.getSession());
      this.setWorld(other.getWorld());
      this.setActor(other.getActor());
      this.setRestricted(other.isRestricted());
      this.setPreferringWildcard(other.isPreferringWildcard());
      this.setTryLegacy(other.isTryingLegacy());
   }

   @Nullable
   public Extent getExtent() {
      return this.extent;
   }

   public void setExtent(@Nullable Extent extent) {
      this.extent = extent;
   }

   @Nullable
   public LocalSession getSession() {
      return this.session;
   }

   public void setSession(@Nullable LocalSession session) {
      this.session = session;
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
      if (this.extent == null) {
         this.setExtent(world);
      }
   }

   @Nullable
   public Actor getActor() {
      return this.actor;
   }

   public void setActor(@Nullable Actor actor) {
      this.actor = actor;
   }

   public Extent requireExtent() throws InputParseException {
      Extent extent = this.getExtent();
      if (extent == null) {
         throw new InputParseException(Caption.of("worldedit.error.missing-extent"));
      } else {
         return extent;
      }
   }

   public LocalSession requireSession() throws InputParseException {
      LocalSession session = this.getSession();
      if (session == null) {
         throw new InputParseException(Caption.of("worldedit.error.missing-session"));
      } else {
         return session;
      }
   }

   public World requireWorld() throws InputParseException {
      World world = this.getWorld();
      if (world == null) {
         throw new InputParseException(Caption.of("worldedit.error.missing-world"));
      } else {
         return world;
      }
   }

   public Actor requireActor() throws InputParseException {
      Actor actor = this.getActor();
      if (actor == null) {
         throw new InputParseException(Caption.of("worldedit.error.missing-actor"));
      } else {
         return actor;
      }
   }

   public Player requirePlayer() throws InputParseException {
      if (this.getActor() instanceof Player player) {
         return player;
      } else {
         throw new InputParseException(Caption.of("worldedit.error.missing-player"));
      }
   }

   public boolean isRestricted() {
      return this.restricted;
   }

   public void setRestricted(boolean restricted) {
      this.restricted = restricted;
   }

   public boolean isPreferringWildcard() {
      return this.preferringWildcard;
   }

   public void setPreferringWildcard(boolean preferringWildcard) {
      this.preferringWildcard = preferringWildcard;
   }

   public void setTryLegacy(boolean tryLegacy) {
      this.tryLegacy = tryLegacy;
   }

   public boolean isTryingLegacy() {
      return this.tryLegacy;
   }

   public void setInjected(InjectedValueAccess injected) {
      this.injected = injected;
   }

   public InjectedValueAccess getInjected() {
      return this.injected;
   }

   public int getMinY() {
      if (this.minY != Integer.MIN_VALUE) {
         return this.minY;
      } else {
         Extent extent = null;
         if (this.actor instanceof Locatable) {
            extent = ((Locatable)this.actor).getExtent();
         } else if (this.world != null) {
            extent = this.world;
         } else if (this.extent != null) {
            extent = this.extent;
         }

         if (extent != null) {
            this.minY = extent.getMinY();
            this.maxY = extent.getMaxY();
         } else {
            this.minY = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY();
            this.maxY = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY();
         }

         return this.minY;
      }
   }

   public int getMaxY() {
      if (this.maxY != Integer.MAX_VALUE) {
         return this.maxY;
      } else {
         Extent extent = null;
         if (this.actor instanceof Locatable) {
            extent = ((Locatable)this.actor).getExtent();
         } else if (this.world != null) {
            extent = this.world;
         } else if (this.extent != null) {
            extent = this.extent;
         }

         if (extent != null) {
            this.minY = extent.getMinY();
            this.maxY = extent.getMaxY();
         } else {
            this.minY = 0;
            this.maxY = 255;
         }

         return this.maxY;
      }
   }

   public Region getSelection() {
      if (this.session != null) {
         try {
            return this.session.getSelection();
         } catch (IncompleteRegionException var3) {
         }
      }

      if (this.actor != null) {
         try {
            return this.actor.getSession().getSelection();
         } catch (IncompleteRegionException var2) {
         }
      }

      return null;
   }
}
