package de.oliver.fancynpcs.api.actions.executor;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.types.BlockUntilDoneAction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionExecutionContext {
   private final ActionTrigger trigger;
   private final Npc npc;
   @Nullable
   private final UUID player;
   private final List<NpcAction.NpcActionData> actions;
   private int actionIndex;

   public ActionExecutionContext(ActionTrigger trigger, Npc npc, @Nullable UUID player) {
      this.trigger = trigger;
      this.npc = npc;
      this.player = player;
      this.actions = new ArrayList<>(npc.getData().getActions(trigger));
      this.actionIndex = 0;
   }

   public ActionExecutionContext(ActionTrigger trigger, Npc npc) {
      this(trigger, npc, null);
   }

   public void run(int index) {
      if (index >= 0 && index < this.actions.size()) {
         NpcAction.NpcActionData actionData = this.actions.get(index);
         actionData.action().execute(this, actionData.value());
      }
   }

   public void runNext() {
      if (this.actionIndex >= 0 && this.actionIndex < this.actions.size()) {
         this.run(this.actionIndex++);
      }
   }

   public boolean hasNext() {
      return this.actionIndex >= 0 && this.actionIndex < this.actions.size();
   }

   public void reset() {
      this.actionIndex = 0;
   }

   public void terminate() {
      this.actionIndex = -1;
   }

   public boolean isTerminated() {
      return this.actionIndex == -1;
   }

   public boolean shouldBlockUntilDone() {
      for (NpcAction.NpcActionData action : this.actions) {
         if (action.action() instanceof BlockUntilDoneAction) {
            return true;
         }
      }

      return false;
   }

   public ActionTrigger getTrigger() {
      return this.trigger;
   }

   public Npc getNpc() {
      return this.npc;
   }

   public List<NpcAction.NpcActionData> getActions() {
      return this.actions;
   }

   public UUID getPlayerUUID() {
      return this.player;
   }

   @Nullable
   public Player getPlayer() {
      return this.player == null ? null : Bukkit.getPlayer(this.player);
   }

   public int getActionIndex() {
      return this.actionIndex;
   }
}
