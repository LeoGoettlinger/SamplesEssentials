package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.internal;

interface Linked<T extends Linked<T>> {
   T getPrevious();

   void setPrevious(T var1);

   T getNext();

   void setNext(T var1);
}
