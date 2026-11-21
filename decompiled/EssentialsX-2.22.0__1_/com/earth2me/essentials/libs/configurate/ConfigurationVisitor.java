package com.earth2me.essentials.libs.configurate;

public interface ConfigurationVisitor<S, T, E extends Exception> {
   S newState() throws E;

   void beginVisit(ConfigurationNode node, S state) throws E;

   void enterNode(ConfigurationNode node, S state) throws E;

   void enterMappingNode(ConfigurationNode node, S state) throws E;

   void enterListNode(ConfigurationNode node, S state) throws E;

   void enterScalarNode(ConfigurationNode node, S state) throws E;

   void exitMappingNode(ConfigurationNode node, S state) throws E;

   void exitListNode(ConfigurationNode node, S state) throws E;

   T endVisit(S state) throws E;

   public interface Safe<S, T> extends ConfigurationVisitor<S, T, VisitorSafeNoopException> {
      @Override
      S newState();

      @Override
      void beginVisit(ConfigurationNode node, S state);

      @Override
      void enterNode(ConfigurationNode node, S state);

      @Override
      void enterMappingNode(ConfigurationNode node, S state);

      @Override
      void enterListNode(ConfigurationNode node, S state);

      @Override
      void enterScalarNode(ConfigurationNode node, S state);

      @Override
      void exitMappingNode(ConfigurationNode node, S state);

      @Override
      void exitListNode(ConfigurationNode node, S state);

      @Override
      T endVisit(S state);
   }

   @FunctionalInterface
   public interface Stateless<E extends Exception> extends ConfigurationVisitor<Void, Void, E> {
      default Void newState() {
         return null;
      }

      default void beginVisit(ConfigurationNode node, Void state) throws E {
         this.beginVisit(node);
      }

      default void beginVisit(ConfigurationNode node) throws E {
      }

      default void enterNode(ConfigurationNode node, Void state) throws E {
         this.enterNode(node);
      }

      void enterNode(ConfigurationNode node) throws E;

      default void enterMappingNode(ConfigurationNode node, Void state) throws E {
         this.enterMappingNode(node);
      }

      default void enterMappingNode(ConfigurationNode node) throws E {
      }

      default void enterListNode(ConfigurationNode node, Void state) throws E {
         this.enterListNode(node);
      }

      default void enterListNode(ConfigurationNode node) throws E {
      }

      default void enterScalarNode(ConfigurationNode node, Void state) throws E {
         this.enterScalarNode(node);
      }

      default void enterScalarNode(ConfigurationNode node) throws E {
      }

      default void exitMappingNode(ConfigurationNode node, Void state) throws E {
         this.exitMappingNode(node);
      }

      default void exitMappingNode(ConfigurationNode node) throws E {
      }

      default void exitListNode(ConfigurationNode node, Void state) throws E {
         this.exitListNode(node);
      }

      default void exitListNode(ConfigurationNode node) throws E {
      }

      default Void endVisit(Void state) throws E {
         this.endVisit();
         return null;
      }

      default void endVisit() throws E {
      }
   }
}
