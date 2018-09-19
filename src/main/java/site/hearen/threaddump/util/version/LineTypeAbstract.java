package site.hearen.threaddump.util.version;



import java.util.HashMap;
import java.util.Map;

import site.hearen.threaddump.enums.LineTypeEnum;

public abstract class LineTypeAbstract {
    protected static final Map<LineTypeEnum, String> map = new HashMap<>();
    protected abstract void initMap();
    public abstract String getKey(LineTypeEnum lineTypeEnum);
}
