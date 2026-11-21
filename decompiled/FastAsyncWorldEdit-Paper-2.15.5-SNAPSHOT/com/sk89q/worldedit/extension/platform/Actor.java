package com.sk89q.worldedit.extension.platform;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.entity.MapMetadatable;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.util.task.InterruptableCondition;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.Identifiable;
import com.sk89q.worldedit.util.auth.Subject;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import java.io.File;
import java.util.Locale;

public interface Actor extends Identifiable, SessionOwner, Subject, MapMetadatable {
   String getName();

   default String getDisplayName() {
      return this.getName();
   }

   @Deprecated
   void printRaw(String msg);

   @Deprecated
   void printDebug(String msg);

   @Deprecated
   void print(String msg);

   @Deprecated
   void printError(String msg);

   default void printError(Component component) {
      this.print(component.color(TextColor.RED));
   }

   default void printInfo(Component component) {
      this.print(component.color(TextColor.LIGHT_PURPLE));
   }

   default void printDebug(Component component) {
      this.print(component.color(TextColor.GRAY));
   }

   void print(Component component);

   boolean canDestroyBedrock();

   boolean isPlayer();

   File openFileOpenDialog(String[] extensions);

   File openFileSaveDialog(String[] extensions);

   void dispatchCUIEvent(CUIEvent event);

   Locale getLocale();

   default void sendAnnouncements() {
   }

   boolean runAction(Runnable ifFree, boolean checkFree, boolean async);

   default boolean decline() {
      InterruptableCondition confirm = this.deleteMeta("cmdConfirm");
      if (confirm != null) {
         confirm.interrupt();
         return true;
      } else {
         return false;
      }
   }

   default boolean confirm() {
      InterruptableCondition confirm = this.deleteMeta("cmdConfirm");
      if (confirm == null) {
         return false;
      } else {
         confirm.signal();
         return true;
      }
   }

   default void queueAction(Runnable run) {
      this.runAction(run, false, true);
   }

   default boolean checkAction() {
      long time = this.getMeta("faweActionTick", Long.MIN_VALUE);
      long tick = Fawe.instance().getTimer().getTick();
      this.setMeta("faweActionTick", tick);
      return tick > time;
   }

   default FaweLimit getLimit() {
      return Settings.settings().getLimit(this);
   }

   default boolean runAsyncIfFree(Runnable r) {
      return this.runAction(r, true, true);
   }

   default boolean runIfFree(Runnable r) {
      return this.runAction(r, true, false);
   }

   default int cancel(boolean close) {
      int cancelled = this.decline() ? 1 : 0;

      for (Request request : Request.getAll()) {
         EditSession editSession = request.getEditSession();
         if (editSession != null) {
            Actor actor = editSession.getActor();
            if (this.equals(actor)) {
               editSession.cancel();
               cancelled++;
            }
         }
      }

      return cancelled;
   }
}
