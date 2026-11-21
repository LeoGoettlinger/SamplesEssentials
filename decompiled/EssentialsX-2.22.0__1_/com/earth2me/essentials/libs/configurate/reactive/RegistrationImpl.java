package com.earth2me.essentials.libs.configurate.reactive;

class RegistrationImpl<V> implements AbstractProcessor.Registration<V> {
   final ProcessorImpl<V> holder;
   final Subscriber<? super V> subscriber;

   RegistrationImpl(final ProcessorImpl<V> holder, final Subscriber<? super V> subscriber) {
      this.holder = holder;
      this.subscriber = subscriber;
   }

   @Override
   public void dispose() {
      if (this.holder.registrations.remove(this)) {
         this.holder.subscriberCount.getAndDecrement();
      }
   }

   @Override
   public void submit(final V newValue) {
      this.subscriber.submit(newValue);
   }

   @Override
   public void onClose() {
      this.subscriber.onClose();
   }

   @Override
   public void onError(final Throwable e) {
      this.subscriber.onError(e);
   }
}
