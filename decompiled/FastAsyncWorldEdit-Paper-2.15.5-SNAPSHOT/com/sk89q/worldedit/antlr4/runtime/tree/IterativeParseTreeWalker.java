package com.sk89q.worldedit.antlr4.runtime.tree;

import com.sk89q.worldedit.antlr4.runtime.misc.IntegerStack;
import java.util.ArrayDeque;
import java.util.Deque;

public class IterativeParseTreeWalker extends ParseTreeWalker {
   @Override
   public void walk(ParseTreeListener listener, ParseTree t) {
      Deque<ParseTree> nodeStack = new ArrayDeque<>();
      IntegerStack indexStack = new IntegerStack();
      ParseTree currentNode = t;
      int currentIndex = 0;

      while (currentNode != null) {
         if (currentNode instanceof ErrorNode) {
            listener.visitErrorNode((ErrorNode)currentNode);
         } else if (currentNode instanceof TerminalNode) {
            listener.visitTerminal((TerminalNode)currentNode);
         } else {
            RuleNode r = (RuleNode)currentNode;
            this.enterRule(listener, r);
         }

         if (currentNode.getChildCount() > 0) {
            nodeStack.push(currentNode);
            indexStack.push(currentIndex);
            currentIndex = 0;
            currentNode = currentNode.getChild(0);
         } else {
            while (true) {
               if (currentNode instanceof RuleNode) {
                  this.exitRule(listener, (RuleNode)currentNode);
               }

               if (nodeStack.isEmpty()) {
                  currentNode = null;
                  currentIndex = 0;
                  break;
               }

               currentNode = nodeStack.peek().getChild(++currentIndex);
               if (currentNode != null) {
                  break;
               }

               currentNode = nodeStack.pop();
               currentIndex = indexStack.pop();
               if (currentNode == null) {
                  break;
               }
            }
         }
      }
   }
}
