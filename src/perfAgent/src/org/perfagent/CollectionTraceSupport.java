package org.perfagent;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class CollectionTraceSupport {
    private final static ConcurrentHashMap<String, LongAdder> statistics = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, LongAdder> callerStatistics = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, LongAdder> ctorStatistics = new ConcurrentHashMap<>();

    /* ------------------------------- start Methods injected by the perfAgent ------------------------------------- */
    public final static void traceStaticInvokedWithParam(String cName, String mName, String mDesc, Object[] params) {
        String key = cName + "::" + mName + mDesc;

        if (mName.equals("$init$") || mName.equals("<cinit>")) traceInvokedImpl(ctorStatistics, key);
        else traceInvokedImpl(statistics, key);
    }

    public final static void traceInvokedWithParam(Object colIns, String cName, String mName, String mDesc, Object[] params) {
        String key = colIns.getClass().getName() + "::" + mName + mDesc;
        traceInvokedImpl(statistics, key);
    }

    public final static void traceCtorInvokedWithParam(Object colIns, String cName, String mName, String mDesc, Object[] params) {
        String key = colIns.getClass().getName() + "::" + mName + mDesc;
        traceInvokedImpl(ctorStatistics, key);
    }

    public final static void traceUserInvoked(String invoker, String callerMethod, String colCls, String method, String desc) {
        String key = colCls + "::" + method + desc + " called from: " + invoker + "::" + callerMethod;
        traceInvokedImpl(callerStatistics, key);
    }

    public final static void traceStaticInvoked(String cName, String mName, String mDesc) {
        traceStaticInvokedWithParam(cName, mName, mDesc, null);
    }

    public final static void traceInvoked(Object colIns, String cName, String mName, String mDesc) {
        traceInvokedWithParam(colIns, cName, mName, mDesc, null);
    }

    public final static void traceCtorInvoked(Object colIns, String cName, String mName, String mDesc) {
        traceCtorInvokedWithParam(colIns, cName, mName, mDesc, null);
    }

    /* ------------------------------- end Methods injected by the perfAgent --------------------------------------- */

    // runtime implementation
    private final static void traceInvokedImpl(ConcurrentHashMap<String, LongAdder> data, String key) {
        System.out.println("trace for: " + key);

        LongAdder adder = data.get(key);
        if (adder != null) {
            adder.increment();
        } else {
            adder = new LongAdder();
            adder.increment();
            data.put(key, adder);
        }
    }

    /* ------------------------------- start Methods to get statistics --------------------------------------------- */
    public final static HashMap<String, Long> getStatisticAndReset() { return getStatisticAndResetImpl(statistics); }
    public final static HashMap<String, Long> getCallerStatisticAndReset() { return getStatisticAndResetImpl(callerStatistics); }
    public final static HashMap<String, Long> getCtorStatisticAndReset() { return getStatisticAndResetImpl(ctorStatistics); }

    private final static HashMap<String, Long> getStatisticAndResetImpl(ConcurrentHashMap<String, LongAdder> data) {
        HashMap<String, Long> result = new HashMap<String, Long>();
        data.forEach((k, adder) -> {
            result.put(k, adder.longValue());
            adder.reset();
        });
        return result;
    }
    /* ------------------------------- end Methods to get statistics ----------------------------------------------- */
}