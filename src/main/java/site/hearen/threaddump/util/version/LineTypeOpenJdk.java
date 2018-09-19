package site.hearen.threaddump.util.version;

import site.hearen.threaddump.enums.LineTypeEnum;

public class LineTypeOpenJdk extends LineTypeAbstract {

    public LineTypeOpenJdk() {
        initMap();
    }

    @Override
    protected void initMap() {
        map.put(LineTypeEnum.UNKNOWN, "unknown");
        map.put(LineTypeEnum.THREAD_BLOCK_END, "JNI global references:");
        map.put(LineTypeEnum.TITLE, " tid=");
        map.put(LineTypeEnum.STATE, "Thread.State");
        map.put(LineTypeEnum.STACK_TRACE, "at ");
        map.put(LineTypeEnum.WAITING_LOCK, "- waiting on ");
        map.put(LineTypeEnum.LOCKING, "- locked ");
    }

    @Override
    public String getKey(LineTypeEnum lineTypeEnum) {
        return map.get(lineTypeEnum);
    }
}
