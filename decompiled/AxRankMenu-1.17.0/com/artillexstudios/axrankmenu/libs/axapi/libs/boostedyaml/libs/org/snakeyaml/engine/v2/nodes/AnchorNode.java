package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes;

public class AnchorNode extends Node {
   private final Node realNode;

   public AnchorNode(Node realNode) {
      super(realNode.getTag(), realNode.getStartMark(), realNode.getEndMark());
      this.realNode = realNode;
   }

   @Override
   public NodeType getNodeType() {
      return NodeType.ANCHOR;
   }

   public Node getRealNode() {
      return this.realNode;
   }
}
