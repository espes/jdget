package jd.controlling.downloadcontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jd.controlling.downloadcontroller.AccountCache.ACCOUNTTYPE;
import jd.controlling.downloadcontroller.AccountCache.CachedAccount;
import jd.controlling.downloadcontroller.DownloadLinkCandidateResult.RESULT;
import jd.controlling.proxy.AbstractProxySelectorImpl;
import jd.controlling.proxy.ProxyController;
import jd.plugins.Account;
import jd.plugins.DownloadLink;

import org.appwork.exceptions.WTFException;
import org.appwork.storage.config.JsonConfig;
import org.jdownloader.logging.LogController;
import org.jdownloader.settings.GeneralSettings;

public class DownloadLinkCandidateSelector {

    private static class CandidateResultHolder {
        private final DownloadLinkCandidateResult result;

        public DownloadLinkCandidateResult getResult() {
            return result;
        }

        public DownloadLinkCandidate getCandidate() {
            return candidate;
        }

        private final DownloadLinkCandidate candidate;

        private CandidateResultHolder(DownloadLinkCandidate candidate, DownloadLinkCandidateResult result) {
            this.result = result;
            this.candidate = candidate;
        }
    }

    public static enum DownloadLinkCandidatePermission {
        OK,
        OK_FORCED,
        CONCURRENCY_LIMIT,
        CONCURRENCY_FORBIDDEN
    }

    public static enum CachedAccountPermission {
        OK,
        DISABLED,
        TEMP_DISABLED,
        IMPOSSIBLE
    }

    private final Comparator<CandidateResultHolder>                                                        RESULT_SORTER = new Comparator<CandidateResultHolder>() {
                                                                                                                             private final DownloadLinkCandidateResult.RESULT[] FINAL_RESULT_SORT_ORDER = new RESULT[] { DownloadLinkCandidateResult.RESULT.SKIPPED, DownloadLinkCandidateResult.RESULT.ACCOUNT_REQUIRED, DownloadLinkCandidateResult.RESULT.PLUGIN_DEFECT, DownloadLinkCandidateResult.RESULT.FATAL_ERROR, DownloadLinkCandidateResult.RESULT.OFFLINE_UNTRUSTED };

                                                                                                                             private int indexOf(RESULT o1) {
                                                                                                                                 for (int index = 0; index < FINAL_RESULT_SORT_ORDER.length; index++) {
                                                                                                                                     if (FINAL_RESULT_SORT_ORDER[index] == o1) {
                                                                                                                                         return index;
                                                                                                                                     }
                                                                                                                                 }
                                                                                                                                 return -1;
                                                                                                                             }

                                                                                                                             private int compare(long x, long y) {
                                                                                                                                 return (x < y) ? -1 : ((x == y) ? 0 : 1);
                                                                                                                             }

                                                                                                                             @Override
                                                                                                                             public int compare(CandidateResultHolder o1, CandidateResultHolder o2) {
                                                                                                                                 long i1 = indexOf(o1.getResult().getResult());
                                                                                                                                 long i2 = indexOf(o2.getResult().getResult());
                                                                                                                                 if (i1 >= 0 && i2 < 0) {
                                                                                                                                     return -1;
                                                                                                                                 } else if (i2 >= 0 && i1 < 0) {
                                                                                                                                     return 1;
                                                                                                                                 } else if (i1 >= 0 && i2 >= 0) {
                                                                                                                                     return compare(i1, i2);
                                                                                                                                 } else {
                                                                                                                                     i1 = o1.getResult().getRemainingTime();
                                                                                                                                     i2 = o2.getResult().getRemainingTime();
                                                                                                                                     return -compare(i1, i2);
                                                                                                                                 }
                                                                                                                             };
                                                                                                                         };

    private final DownloadSession                                                                          session;

    private LinkedHashMap<DownloadLink, LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult>> roundResults  = new LinkedHashMap<DownloadLink, LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult>>();

    private final boolean                                                                                  loadBalanceFreeDownloads;

    public DownloadSession getSession() {
        return session;
    }

    public DownloadLinkCandidateSelector(DownloadSession session) {
        this.session = session;
        this.loadBalanceFreeDownloads = JsonConfig.create(GeneralSettings.class).isFreeDownloadLoadBalancingEnabled();
    }

    public int getMaxNumberOfDownloadLinkCandidatesResults(DownloadLinkCandidate candidate) {
        return -1;
    }

    public List<AbstractProxySelectorImpl> getProxies(final DownloadLinkCandidate candidate, final boolean ignoreConnectBans, final boolean ignoreAllBans) {
        List<AbstractProxySelectorImpl> ret = ProxyController.getInstance().getProxySelectors(candidate, ignoreConnectBans, ignoreAllBans);
        if (loadBalanceFreeDownloads && ret != null && candidate.getCachedAccount().getAccount() == null) {
            try {
                Collections.sort(ret, new DownloadLinkCandidateLoadBalancer(candidate));
            } catch (final Throwable e) {
                LogController.CL(true).log(e);
            }
        }
        return ret;
    }

    public CachedAccountPermission getCachedAccountPermission(final CachedAccount cachedAccount) {
        if (cachedAccount != null) {
            if (session.isUseAccountsEnabled() == false && (cachedAccount.getType() == ACCOUNTTYPE.MULTI || cachedAccount.getType() == ACCOUNTTYPE.ORIGINAL)) {
                return CachedAccountPermission.DISABLED;
            }
            final Account canidateAccount = cachedAccount.getAccount();
            if (canidateAccount != null) {
                if (!canidateAccount.isEnabled()) {
                    return CachedAccountPermission.DISABLED;
                }
                if (canidateAccount.isTempDisabled()) {
                    return CachedAccountPermission.TEMP_DISABLED;
                }
            }
            return CachedAccountPermission.OK;
        }
        return CachedAccountPermission.IMPOSSIBLE;
    }

    public DownloadLinkCandidatePermission getDownloadLinkCandidatePermission(DownloadLinkCandidate candidate) {
        final DownloadLink candidateLink = candidate.getLink();
        final String candidateLinkHost = candidateLink.getDomainInfo().getTld();
        final CachedAccount cachedAccount = candidate.getCachedAccount();
        final String candidatePluginHost = cachedAccount.getPlugin().getHost();
        final Account candidateAccount = cachedAccount.getAccount();
        int maxPluginConcurrentAccount = cachedAccount.getPlugin().getMaxSimultanDownload(null, candidateAccount);
        int maxPluginConcurrentHost = cachedAccount.getPlugin().getMaxSimultanDownload(candidateLink, candidateAccount);
        int maxConcurrentHost = session.getMaxConcurrentDownloadsPerHost();

        for (final SingleDownloadController singleDownloadController : session.getControllers()) {
            if (singleDownloadController.isActive()) {
                if (singleDownloadController.getDownloadLink().getDomainInfo().getTld().equals(candidateLinkHost)) {
                    /**
                     * use DomainInfo here because we want to count concurrent downloads from same domain and not same plugin
                     */
                    maxConcurrentHost--;
                }
                final Account account = singleDownloadController.getAccount();
                if (account != null) {
                    if (candidateAccount != null) {
                        final boolean sameAccountHost = account.getHoster().equals(candidateAccount.getHoster());
                        if (sameAccountHost && account != candidateAccount && candidateAccount.isConcurrentUsePossible() == false) {
                            return DownloadLinkCandidatePermission.CONCURRENCY_FORBIDDEN;
                        }
                    } else {
                        final boolean sameAccountHost = account.getHoster().equals(candidatePluginHost);
                        if (sameAccountHost && account.isConcurrentUsePossible() == false) {
                            return DownloadLinkCandidatePermission.CONCURRENCY_FORBIDDEN;
                        }
                    }
                } else if (candidateAccount != null) {
                    final boolean sameAccountHost = candidateAccount.getHoster().equals(singleDownloadController.getDownloadLink().getHost());
                    if (sameAccountHost && candidateAccount.isConcurrentUsePossible() == false) {
                        return DownloadLinkCandidatePermission.CONCURRENCY_FORBIDDEN;
                    }
                }
                if (candidatePluginHost.equals(singleDownloadController.getDownloadLinkCandidate().getCachedAccount().getPlugin().getHost())) {
                    /**
                     * same plugin is in use
                     */
                    if (singleDownloadController.getProxySelector() == candidate.getProxySelector()) {
                        if (account == candidateAccount) {
                            maxPluginConcurrentAccount--;
                            if (candidateLink.getHost().equals(singleDownloadController.getDownloadLink().getHost())) {
                                maxPluginConcurrentHost--;
                            }
                        }
                    }
                }
            }
        }

        if (maxPluginConcurrentAccount <= 0 || maxPluginConcurrentHost <= 0) {
            return DownloadLinkCandidatePermission.CONCURRENCY_LIMIT;
        } else if (maxConcurrentHost <= 0) {
            if (candidate.isForced()) {
                return DownloadLinkCandidatePermission.OK_FORCED;
            } else {
                return DownloadLinkCandidatePermission.CONCURRENCY_LIMIT;
            }
        } else {
            return DownloadLinkCandidatePermission.OK;
        }
    }

    public boolean isMirrorManagement() {
        return session.isMirrorManagementEnabled();
    }

    public boolean isForcedOnly() {
        return forcedOnly;
    }

    public void setForcedOnly(boolean forcedOnly) {
        this.forcedOnly = forcedOnly;
    }

    public boolean isExcluded(DownloadLink link) {
        return roundResults.containsKey(link);
    }

    public void addExcluded(DownloadLink link) {
        if (roundResults.containsKey(link)) {
            return;
        }
        roundResults.put(link, null);
    }

    public boolean validateDownloadLinkCandidate(DownloadLinkCandidate possibleCandidate) {
        DownloadLinkCandidateResult linkResult = null;
        DownloadLinkCandidateResult proxyResult = null;
        DownloadLinkCandidateHistory linkHistory = getSession().getHistory(possibleCandidate.getLink());
        if (linkHistory != null) {
            linkResult = linkHistory.getBlockingHistory(this, possibleCandidate);
        }
        ProxyInfoHistory proxyHistory = getSession().getProxyInfoHistory();
        proxyResult = proxyHistory.getBlockingHistory(possibleCandidate);
        if (linkResult != null && proxyResult == null) {
            addExcluded(possibleCandidate, linkResult);
            return false;
        } else if (proxyResult != null && linkResult == null) {
            addExcluded(possibleCandidate, proxyResult);
            return false;
        } else if (proxyResult != null && linkResult != null) {
            switch (linkResult.getResult()) {
            case PLUGIN_DEFECT:
            case OFFLINE_UNTRUSTED:
            case ACCOUNT_REQUIRED:
            case FATAL_ERROR:
            case SKIPPED:
                addExcluded(possibleCandidate, linkResult);
                break;
            case FILE_UNAVAILABLE:
            case CONNECTION_ISSUES:
                if (proxyResult.getRemainingTime() >= linkResult.getRemainingTime()) {
                    addExcluded(possibleCandidate, proxyResult);
                } else {
                    addExcluded(possibleCandidate, linkResult);
                }
                break;
            default:
                System.out.println("FIXME " + linkResult.getResult());
                break;
            }
            return false;
        }
        return true;
    }

    public void setExcluded(DownloadLink link) {
        roundResults.put(link, null);
    }

    public void addExcluded(DownloadLinkCandidate candidate, DownloadLinkCandidateResult result) {
        if (result == null) {
            throw new IllegalArgumentException("result == null");
        }
        LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult> map = roundResults.get(candidate.getLink());
        if (map == null) {
            map = new LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult>();
            roundResults.put(candidate.getLink(), map);
        }
        map.put(candidate, result);
    }

    public LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult> finalizeDownloadLinkCandidatesResults() {
        LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult> ret = new LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult>();
        Iterator<Entry<DownloadLink, LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult>>> it = roundResults.entrySet().iterator();
        linkLoop: while (it.hasNext()) {
            Entry<DownloadLink, LinkedHashMap<DownloadLinkCandidate, DownloadLinkCandidateResult>> next = it.next();
            Map<DownloadLinkCandidate, DownloadLinkCandidateResult> map = next.getValue();
            if (map == null || map.size() == 0) {
                continue;
            }
            List<CandidateResultHolder> results = new ArrayList<DownloadLinkCandidateSelector.CandidateResultHolder>();
            Iterator<Entry<DownloadLinkCandidate, DownloadLinkCandidateResult>> it2 = map.entrySet().iterator();
            while (it2.hasNext()) {
                Entry<DownloadLinkCandidate, DownloadLinkCandidateResult> next2 = it2.next();
                DownloadLinkCandidateResult candidateResult = next2.getValue();
                switch (candidateResult.getResult()) {
                case CONNECTION_TEMP_UNAVAILABLE:
                    continue linkLoop;
                case PLUGIN_DEFECT:
                case OFFLINE_UNTRUSTED:
                case ACCOUNT_REQUIRED:
                case FATAL_ERROR:
                case SKIPPED:
                case PROXY_UNAVAILABLE:
                case FILE_UNAVAILABLE:
                case CONNECTION_ISSUES:
                case CONDITIONAL_SKIPPED:
                    results.add(new CandidateResultHolder(next2.getKey(), candidateResult));
                    break;
                default:
                    throw new WTFException("This should not happen " + candidateResult.getResult());
                }
                try {
                    Collections.sort(results, RESULT_SORTER);
                } catch (final Throwable e) {
                    LogController.CL(true).log(e);
                }
                CandidateResultHolder mostImportantResult = results.get(0);
                ret.put(mostImportantResult.getCandidate(), mostImportantResult.getResult());
            }
        }
        return ret;
    }

    private boolean forcedOnly = false;

}
