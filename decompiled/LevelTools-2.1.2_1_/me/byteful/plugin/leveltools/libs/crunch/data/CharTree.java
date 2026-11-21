package me.byteful.plugin.leveltools.libs.crunch.data;

import me.byteful.plugin.leveltools.libs.crunch.ExpressionParser;

public class CharTree<T> {
   private final CharTree.Node root = new CharTree.Node();

   public void set(String str, T value) {
      CharTree.Node node = this.root;

      for (char c : str.toCharArray()) {
         node = node.getOrCreateNode(c);
      }

      node.setValue(value);
   }

   public T get(String str) {
      CharTree.Node node = this.root;

      for (char c : str.toCharArray()) {
         node = node.getNode(c);
         if (node == null) {
            return null;
         }
      }

      return (T)node.getValue();
   }

   public boolean containsFirstChar(char c) {
      return this.root.getNode(c) != null;
   }

   public Pair<T, Integer> getFrom(String str, int index) {
      CharTree.Node node = this.root;
      T val = null;

      for (int i = index; i < str.length(); i++) {
         node = node.getNode(str.charAt(i));
         if (node == null) {
            return new Pair<>(val, i - index);
         }

         if (node.getValue() != null) {
            val = (T)node.getValue();
         }
      }

      return new Pair<>(val, str.length() - index);
   }

   public T getWith(ExpressionParser parser) {
      CharTree.Node node = this.root;
      T val = null;
      int lastParsed = parser.getCursor();
      String input = parser.getInput();

      for (int i = lastParsed; i < input.length(); i++) {
         node = node.getNode(input.charAt(i));
         if (node == null) {
            parser.setCursor(val == null ? parser.getCursor() : lastParsed + 1);
            return val;
         }

         T nodeValue = (T)node.getValue();
         if (nodeValue != null) {
            lastParsed = i;
            val = nodeValue;
         }
      }

      if (val != null) {
         parser.setCursor(lastParsed + 1);
      }

      return val;
   }

   private static class Node {
      private Object value;
      private final CharTree.Node[] children = new CharTree.Node[256];

      private Node() {
      }

      public CharTree.Node getNode(char c) {
         return this.children[c];
      }

      public CharTree.Node getOrCreateNode(char c) {
         if (this.children[c] == null) {
            this.children[c] = new CharTree.Node();
         }

         return this.children[c];
      }

      public Object getValue() {
         return this.value;
      }

      public void setValue(Object value) {
         this.value = value;
      }
   }
}
