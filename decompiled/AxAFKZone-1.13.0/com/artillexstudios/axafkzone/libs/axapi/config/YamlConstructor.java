package com.artillexstudios.axafkzone.libs.axapi.config;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.LoaderOptions;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.constructor.SafeConstructor;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.MappingNode;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.nodes.Node;

public class YamlConstructor extends SafeConstructor {
   public YamlConstructor(LoaderOptions loaderOptions) {
      super(loaderOptions);
   }

   public void flatten(MappingNode node) {
      this.flattenMapping(node);
   }

   public Object construct(Node node) {
      return this.constructObject(node);
   }
}
