package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.lowlevel;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.DumpSettings;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events.Event;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.serializer.Serializer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Serialize {
   private final DumpSettings settings;

   public Serialize(DumpSettings settings) {
      Objects.requireNonNull(settings, "DumpSettings cannot be null");
      this.settings = settings;
   }

   public List<Event> serializeOne(Node node) {
      Objects.requireNonNull(node, "Node cannot be null");
      return this.serializeAll(Collections.singletonList(node));
   }

   public List<Event> serializeAll(List<Node> nodes) {
      Objects.requireNonNull(nodes, "Nodes cannot be null");
      EmitableEvents emitableEvents = new EmitableEvents();
      Serializer serializer = new Serializer(this.settings, emitableEvents);
      serializer.emitStreamStart();

      for (Node node : nodes) {
         serializer.serializeDocument(node);
      }

      serializer.emitStreamEnd();
      return emitableEvents.getEvents();
   }
}
