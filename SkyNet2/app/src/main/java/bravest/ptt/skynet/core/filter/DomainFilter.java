package bravest.ptt.skynet.core.filter;

/**
 * 针域名的过滤
 */
public interface DomainFilter {

    void prepare();

    boolean needFilter(String domain, int ip);

}
