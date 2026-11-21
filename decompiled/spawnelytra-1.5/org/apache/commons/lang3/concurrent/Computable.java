package org.apache.commons.lang3.concurrent;

@FunctionalInterface
public interface Computable<I, O> {
   O compute(I var1) throws InterruptedException;
}
