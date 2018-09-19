package site.hearen.thread.dump.analyzer.util.version;



import java.util.HashMap;
import java.util.Map;

import site.hearen.thread.dump.analyzer.enums.LineTypeEnum;

public abstract class LineTypeAbstract {
    protected static final Map<LineTypeEnum, String> map = new HashMap<>();
    protected abstract void initMap();
    public abstract String getKey(LineTypeEnum lineTypeEnum);
}
