package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.engine;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.LoadSettings;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.StandardConstructor;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.serialization.YamlSerializer;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ExtendedConstructor extends StandardConstructor {
   private final YamlSerializer serializer;
   private final Map<Node, Object> constructed = new HashMap<>();

   public ExtendedConstructor(@NotNull LoadSettings settings, @NotNull YamlSerializer serializer) {
      super(settings);
      this.serializer = serializer;
      this.tagConstructors.put(Tag.MAP, new ExtendedConstructor.ConstructMap((StandardConstructor.ConstructYamlMap)this.tagConstructors.get(Tag.MAP)));
   }

   @Override
   protected Object construct(Node node) {
      Object o = super.construct(node);
      this.constructed.put(node, o);
      return o;
   }

   @Override
   protected Object constructObjectNoCheck(Node node) {
      Object o = super.constructObjectNoCheck(node);
      this.constructed.put(node, o);
      return o;
   }

   @NotNull
   public Object getConstructed(@NotNull Node node) {
      return this.constructed.get(node);
   }

   public void clear() {
      this.constructed.clear();
   }

   private class ConstructMap extends StandardConstructor.ConstructYamlMap {
      private final StandardConstructor.ConstructYamlMap previous;

      private ConstructMap(@NotNull StandardConstructor.ConstructYamlMap previous) {
         this.previous = previous;
      }

      @Override
      public Object construct(Node node) {
         Map<Object, Object> map = (Map<Object, Object>)this.previous.construct(node);
         Object deserialized = ExtendedConstructor.this.serializer.deserialize(map);
         return deserialized == null ? map : deserialized;
      }
   }
}
