package grainalcohol.dtt.api.event;

public class PTSDContext {
    /**
     * PTSD的ID，用于唯一标识
     */
    private final String ptsdId;
    /**
     * PTSD的详细信息，如实体名、映射后的伤害源名
     */
    private final String ptsdInfo;

    public PTSDContext(String ptsdId, String ptsdInfo) {
        this.ptsdId = ptsdId;
        this.ptsdInfo = ptsdInfo;
    }

    public static PTSDContext of(String ptsdId, String ptsdInfo) {
        return new PTSDContext(ptsdId, ptsdInfo);
    }

    public String getPtsdId() {
        return ptsdId;
    }

    public String getPtsdInfo() {
        return ptsdInfo;
    }
}
