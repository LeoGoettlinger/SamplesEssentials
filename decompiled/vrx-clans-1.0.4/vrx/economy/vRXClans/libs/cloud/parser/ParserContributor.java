package vrx.economy.vRXClans.libs.cloud.parser;

public interface ParserContributor {
   <C> void contribute(ParserRegistry<C> registry);
}
