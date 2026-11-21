package com.earth2me.essentials.libs.configurate.reactive;

import java.util.Iterator;
import java.util.concurrent.Executor;

class ProcessorImpl<V> extends AbstractProcessor<V, RegistrationImpl<V>> implements Processor.Iso<V> {
   ProcessorImpl(final Executor exec) {
      super(exec);
   }

   @Override
   public void submit(final V value) {
      if (this.subscriberCount.get() >= 0) {
         boolean handled = false;
         Iterator<RegistrationImpl<V>> it = this.registrations.iterator();

         while (it.hasNext()) {
            RegistrationImpl<V> reg = it.next();

            try {
               handled = true;
               reg.submit(value);
            } catch (Exception var6) {
               it.remove();
               this.subscriberCount.getAndDecrement();
               reg.subscriber.onError(var6);
            }
         }

         if (!handled) {
            Subscriber<V> fallback = this.fallbackHandler;
            if (fallback != null) {
               fallback.submit(value);
            }
         }
      }
   }

   protected RegistrationImpl<V> createRegistration(final Subscriber<? super V> sub) {
      return new RegistrationImpl<>(this, sub);
   }
}
