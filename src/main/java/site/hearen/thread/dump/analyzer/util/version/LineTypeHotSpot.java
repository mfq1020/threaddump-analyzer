package site.hearen.thread.dump.analyzer.util.version;


import site.hearen.thread.dump.analyzer.enums.LineTypeEnum;

public class LineTypeHotSpot extends LineTypeAbstract {

    public LineTypeHotSpot() {
        initMap();
    }

    @Override
    protected void initMap() {
        map.put(LineTypeEnum.UNKNOWN, "unknown");
        map.put(LineTypeEnum.THREAD_BLOCK_END, "JNI global references:");
        map.put(LineTypeEnum.TITLE, " tid=");
        map.put(LineTypeEnum.STATE, "Thread.State");
        map.put(LineTypeEnum.STACK_TRACE, "at ");
        map.put(LineTypeEnum.WAITING_LOCK, "waiting to lock");
        map.put(LineTypeEnum.LOCKING, "locked");
    }

    @Override
    public String getKey(LineTypeEnum lineTypeEnum) {
        return map.get(lineTypeEnum);
    }
}
