package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.power.EliteLuaPowerActivity;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

final class EliteLuaPowerBinding implements AutoCloseable {
   private final Plugin owner;
   private final List<EliteLuaPowerBinding.AttachedPower> attachedPowers;
   private final ElitePowerPauseState pauseState = new ElitePowerPauseState();
   private boolean closed;

   EliteLuaPowerBinding(Plugin owner, List<EliteLuaPowerBinding.AttachedPower> attachedPowers) {
      this.owner = Objects.requireNonNull(owner, "owner");
      this.attachedPowers = List.copyOf(attachedPowers);
   }

   boolean isOwnedBy(Plugin plugin) {
      return this.owner == plugin;
   }

   List<EliteLuaPowerProgram> descriptors() {
      return this.attachedPowers.stream().map(attachedPower -> attachedPower.entry().descriptor()).toList();
   }

   List<EliteLuaPowerActivity> activity() {
      ArrayList<EliteLuaPowerActivity> result = new ArrayList<>(this.attachedPowers.size());

      for (EliteLuaPowerBinding.AttachedPower attachedPower : this.attachedPowers) {
         result.add(attachedPower.power().activitySnapshot(attachedPower.entry().descriptor()));
      }

      return List.copyOf(result);
   }

   List<ElitePower> powers() {
      return this.attachedPowers.stream().map(EliteLuaPowerBinding.AttachedPower::power).map(power -> (ElitePower)power).toList();
   }

   List<NamespacedKey> powerKeys() {
      return this.attachedPowers.stream().map(attachedPower -> attachedPower.entry().key()).toList();
   }

   boolean containsPower(NamespacedKey key) {
      for (EliteLuaPowerBinding.AttachedPower attachedPower : this.attachedPowers) {
         if (attachedPower.entry().key().equals(key)) {
            return true;
         }
      }

      return false;
   }

   boolean matchesAttachmentOrder(List<ElitePower> currentPowers) {
      if (currentPowers.size() != this.attachedPowers.size()) {
         return false;
      } else {
         for (int index = 0; index < currentPowers.size(); index++) {
            if (currentPowers.get(index) != this.attachedPowers.get(index).power()) {
               return false;
            }
         }

         return true;
      }
   }

   ElitePowerActionResult dispatchMindAction(ElitePowerActionRequest request) {
      if (!this.closed && !this.pauseState.isPaused()) {
         boolean deferred = false;

         for (EliteLuaPowerBinding.AttachedPower attachedPower : this.attachedPowers) {
            ElitePowerActionResult result = attachedPower.power().handleMindAction(request);
            if (result == ElitePowerActionResult.ACCEPTED) {
               return result;
            }

            deferred |= result == ElitePowerActionResult.DEFERRED;
         }

         return deferred ? ElitePowerActionResult.DEFERRED : ElitePowerActionResult.REJECTED;
      } else {
         return ElitePowerActionResult.REJECTED;
      }
   }

   void stopRuntimes() {
      RuntimeException failure = null;

      for (int index = this.attachedPowers.size() - 1; index >= 0; index--) {
         try {
            this.attachedPowers.get(index).power().closeRuntime();
         } catch (RuntimeException var4) {
            if (failure == null) {
               failure = var4;
            } else {
               failure.addSuppressed(var4);
            }
         }
      }

      if (failure != null) {
         throw failure;
      }
   }

   void restartRuntimes() {
      if (this.closed) {
         throw new IllegalStateException("Lua power binding is closed");
      } else {
         this.stopRuntimes();

         try {
            for (EliteLuaPowerBinding.AttachedPower attachedPower : this.attachedPowers) {
               attachedPower.power().startRuntimeOrThrow();
            }
         } catch (RuntimeException var4) {
            try {
               this.stopRuntimes();
            } catch (RuntimeException var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }
      }
   }

   void setPaused(boolean paused) {
      this.setPauseReason(ElitePowerPauseReason.MIND_SERVICE, paused);
   }

   void setPauseReason(ElitePowerPauseReason reason, boolean paused) {
      if (this.closed) {
         throw new IllegalStateException("Lua power binding is closed");
      } else {
         this.pauseState.set(reason, paused);

         for (EliteLuaPowerBinding.AttachedPower attachedPower : this.attachedPowers) {
            attachedPower.power().setRuntimePauseReason(reason, paused);
         }
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.stopRuntimes();
      }
   }

   record AttachedPower(EliteLuaPowerCatalog.Entry entry, LuaElitePower power) {
      AttachedPower(EliteLuaPowerCatalog.Entry entry, LuaElitePower power) {
         Objects.requireNonNull(entry, "entry");
         Objects.requireNonNull(power, "power");
         this.entry = entry;
         this.power = power;
      }
   }
}
