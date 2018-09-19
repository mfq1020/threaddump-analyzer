package site.hearen.thread.dump.analyzer.enums;

public enum StateEnum {
    CREATED("NEW"),
    RUNNABLE("RUNNABLE"),
    RUNNING("RUNNING"),
    WAITING("WAITING"),
    TIMED_WAITING("TIMED_WAITING"),
    BLOCKED("BLOCKED");

    private String keyword;

    StateEnum(String theKey) {
        this.keyword = theKey;
    }

    public String toKey() {
        return keyword;
    }
}
