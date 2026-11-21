package de.theredend2000.advancedhunt.lib.cloud.parser;

public interface ParserContributor {
   <C> void contribute(ParserRegistry<C> registry);
}
