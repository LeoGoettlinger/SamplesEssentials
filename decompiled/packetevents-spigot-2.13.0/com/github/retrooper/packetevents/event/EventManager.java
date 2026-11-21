package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.exception.InvalidHandshakeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jetbrains.annotations.Nullable;

public class EventManager {
   private final Map<PacketListenerPriority, Set<PacketListenerCommon>> listenersMap = new ConcurrentHashMap<>();
   private volatile PacketListenerCommon[] listeners = new PacketListenerCommon[0];

   public void callEvent(PacketEvent event) {
      this.callEvent(event, null);
   }

   public void callEvent(PacketEvent event, @Nullable Runnable postCallListenerAction) {
      for (PacketListenerCommon listener : this.listeners) {
         try {
            event.call(listener);
         } catch (Exception var8) {
            if (var8.getClass() != InvalidHandshakeException.class
               && (var8.getCause() == null || var8.getCause().getClass() != InvalidHandshakeException.class)) {
               PacketEvents.getAPI().getLogManager().warn("PacketEvents caught an unhandled exception while calling your listener.", var8);
            }
         }

         if (postCallListenerAction != null) {
            postCallListenerAction.run();
         }
      }

      if (event instanceof ProtocolPacketEvent && !((ProtocolPacketEvent)event).needsReEncode()) {
         ((ProtocolPacketEvent)event).setLastUsedWrapper(null);
      }
   }

   public PacketListenerCommon registerListener(PacketListener listener, PacketListenerPriority priority) {
      PacketListenerCommon packetListenerAbstract = listener.asAbstract(priority);
      return this.registerListener(packetListenerAbstract);
   }

   public PacketListenerCommon registerListener(PacketListenerCommon listener) {
      this.registerListenerNoRecalculation(listener);
      this.recalculateListeners();
      return listener;
   }

   public PacketListenerCommon[] registerListeners(PacketListenerCommon... listeners) {
      for (PacketListenerCommon listener : listeners) {
         this.registerListenerNoRecalculation(listener);
      }

      this.recalculateListeners();
      return listeners;
   }

   public void unregisterListener(PacketListenerCommon listener) {
      if (this.unregisterListenerNoRecalculation(listener)) {
         this.recalculateListeners();
      }
   }

   public void unregisterListeners(PacketListenerCommon... listeners) {
      boolean modified = false;

      for (PacketListenerCommon listener : listeners) {
         modified |= this.unregisterListenerNoRecalculation(listener);
      }

      if (modified) {
         this.recalculateListeners();
      }
   }

   public void unregisterAllListeners() {
      this.listenersMap.clear();
      synchronized (this) {
         this.listeners = new PacketListenerCommon[0];
      }
   }

   private void recalculateListeners() {
      synchronized (this) {
         List<PacketListenerCommon> list = new ArrayList<>();

         for (PacketListenerPriority priority : PacketListenerPriority.values()) {
            Set<PacketListenerCommon> set = this.listenersMap.get(priority);
            if (set != null) {
               list.addAll(set);
            }
         }

         this.listeners = list.toArray(new PacketListenerCommon[0]);
      }
   }

   private void registerListenerNoRecalculation(PacketListenerCommon listener) {
      Set<PacketListenerCommon> listenerSet = this.listenersMap.computeIfAbsent(listener.getPriority(), p -> new CopyOnWriteArraySet<>());
      listenerSet.add(listener);
   }

   private boolean unregisterListenerNoRecalculation(PacketListenerCommon listener) {
      Set<PacketListenerCommon> listenerSet = this.listenersMap.get(listener.getPriority());
      return listenerSet != null && listenerSet.remove(listener);
   }
}
