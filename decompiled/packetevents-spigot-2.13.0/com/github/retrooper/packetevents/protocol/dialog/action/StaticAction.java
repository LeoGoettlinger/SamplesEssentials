package com.github.retrooper.packetevents.protocol.dialog.action;

import com.github.retrooper.packetevents.protocol.chat.clickevent.ClickEvent;
import com.github.retrooper.packetevents.protocol.chat.clickevent.ClickEventAction;
import com.github.retrooper.packetevents.protocol.chat.clickevent.ClickEventActions;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StaticAction implements Action {
   private final ActionType<?> actionType;
   private final ClickEvent clickEvent;

   public StaticAction(ClickEvent clickEvent) {
      if (!clickEvent.getAction().isAllowFromServer()) {
         throw new IllegalArgumentException("Can't create action for unreadable click event with action " + clickEvent.getAction());
      } else {
         this.actionType = ActionTypes.getRegistry().getByNameOrThrow(clickEvent.getAction().getName());
         this.clickEvent = clickEvent;
      }
   }

   public static StaticAction decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      String actionName = compound.getStringTagValueOrThrow("type");
      ClickEventAction<?> action = ClickEventActions.getRegistry().getByNameOrThrow(actionName);
      ClickEvent clickEvent = action.decode(compound, wrapper);
      return new StaticAction(clickEvent);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, StaticAction action) {
      ((ClickEventAction<ClickEvent>)action.clickEvent.getAction()).encode(compound, wrapper, action.clickEvent);
   }

   @Override
   public ActionType<?> getType() {
      return this.actionType;
   }

   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }
}
