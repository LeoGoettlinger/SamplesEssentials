package ac.grim.grimac.manager.init;

import ac.grim.grimac.manager.init.start.StartableInitable;
import ac.grim.grimac.manager.init.stop.StoppableInitable;
import ac.grim.grimac.utils.anticheat.LogUtil;

public abstract class OptionalReflectiveInitable implements StartableInitable, StoppableInitable {
   private final String handlerClassName;
   private final String errorMessage;
   private Initable delegate;

   protected OptionalReflectiveInitable(String handlerClassName, String errorMessage) {
      this.handlerClassName = handlerClassName;
      this.errorMessage = errorMessage;
   }

   protected abstract boolean isAvailable();

   @Override
   public final void start() {
      if (this.isAvailable()) {
         try {
            Class<? extends Initable> handlerClass = Class.forName(this.handlerClassName).asSubclass(Initable.class);
            this.delegate = handlerClass.getDeclaredConstructor().newInstance();
            if (this.delegate instanceof StartableInitable startable) {
               startable.start();
            }
         } catch (LinkageError | ReflectiveOperationException var4) {
            LogUtil.error(this.errorMessage, var4);
         }
      }
   }

   @Override
   public final void stop() {
      if (this.delegate instanceof StoppableInitable stoppable) {
         stoppable.stop();
      }

      this.delegate = null;
   }
}
