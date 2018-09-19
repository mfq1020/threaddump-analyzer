package site.hearen.threaddump.util;

import site.hearen.threaddump.enums.LineTypeEnum;
import site.hearen.threaddump.util.version.LineTypeAbstract;

public final class LineTypeChecker {

    private LineTypeChecker() {
    }
    public static LineTypeEnum getLineType(String line, LineTypeAbstract lineTypeAbstract) {
        LineTypeEnum typeEnum = LineTypeEnum.UNKNOWN;
        for (LineTypeEnum lineTypeEnum : LineTypeEnum.values()) {
            if (line.contains(lineTypeAbstract.getKey(lineTypeEnum))) {
                typeEnum = lineTypeEnum;
            }
        }
        if (typeEnum == LineTypeEnum.WAITING_LOCK || typeEnum == LineTypeEnum.LOCKING) {
            boolean isValidLockLine = line.contains("<") && line.contains(">");
            if (!isValidLockLine) {
                typeEnum = LineTypeEnum.UNKNOWN;
            }
        }
        return typeEnum;
    }
}
