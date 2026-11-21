package com.artillexstudios.axtrade.libs.axapi.config;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.LoaderOptions;
import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.constructor.SafeConstructor;
import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.nodes.MappingNode;
import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.nodes.Node;

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
