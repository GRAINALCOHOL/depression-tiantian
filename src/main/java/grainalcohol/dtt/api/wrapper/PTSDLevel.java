package grainalcohol.dtt.api.wrapper;

import grainalcohol.dtt.api.helper.PTSDHelper;
import net.minecraft.server.network.ServerPlayerEntity;

public enum PTSDLevel {
    LATENT("latent", Severity.HEALTHY, 0),
    MILD("mild", Severity.MILD, 1),
    MODERATE("moderate", Severity.MODERATE, 2),
    SEVERE("severe", Severity.SEVERE, 3),
    EXTREME("extreme", Severity.SEVERE, 4),
    CLEAR("clear", Severity.HEALTHY, -1)
    ;

    PTSDLevel(String name, Severity severity, int level) {
        this.name = name;
        this.severity = severity;
        this.level = level;
    }

    private final String name;
    private final int level;
    private final Severity severity;

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public Severity getSeverity() {
        return severity;
    }

    public boolean isLatent() {
        return this == LATENT;
    }

    public boolean hasSymptoms() {
        return this.isSickerThan(LATENT);
    }

    public boolean isExtreme() {
        return this == EXTREME;
    }

    public boolean willPanting() {
        return this.isSickerThan(LATENT);
    }

    public boolean willHeartBeat() {
        return this.isSickerThan(LATENT);
    }

    public boolean willTinnitus() {
        return this.isSickerThan(MILD);
    }

    public boolean willPhonism() {
        return this.isSickerThan(MODERATE);
    }

    public boolean willPhotism() {
        return this.isSickerThan(SEVERE);
    }

    public boolean isHealthierThan(PTSDLevel other) {
        return this.getSeverity().isHealthierThan(other.getSeverity());
    }

    public boolean isSickerThan(PTSDLevel other) {
        return this.getSeverity().isSickerThan(other.getSeverity());
    }


    /**
     * 获取该玩家对某个事物的PTSD等级<br>
     * @param player 需要获取PTSD等级的玩家
     * @param ptsdId 需要获取PTSD等级的事物ID
     * @return 玩家对某个事物的PTSD等级
     */
    public static PTSDLevel from(ServerPlayerEntity player, String ptsdId) {
        return PTSDHelper.getPTSDLevel(player, ptsdId);
    }

    public static PTSDLevel from(int level) {
        for (PTSDLevel ptsdLevel : values()) {
            if (ptsdLevel.getLevel() == level) {
                return ptsdLevel;
            }
        }
        return CLEAR;
    }
}
