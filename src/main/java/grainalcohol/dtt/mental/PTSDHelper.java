package grainalcohol.dtt.mental;

import net.minecraft.server.network.ServerPlayerEntity;

public class PTSDHelper {
    public static final double MIN_PTSD_VALUE = 0.0;
    public static final double MAX_PTSD_VALUE = 40.0;

    public static final double BETWEEN_LATENT_AND_MILD = 20.0;
    public static final double BETWEEN_MILD_AND_MODERATE = 26.0;
    public static final double BETWEEN_MODERATE_AND_SEVERE = 32.0;
    public static final double BETWEEN_SEVERE_AND_EXTREME = 36.0;

    /**
     * 判断玩家是否对非玩家事物有PTSD<br>
     * 如生物：僵尸、箭矢等<br>
     * 如事件：摔落、火焰等<br>
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否对非玩家事物有PTSD
     */
    public static boolean hasPTSDForEvent(ServerPlayerEntity serverPlayerEntity) {
        return !MentalStatusHelper.getMentalStatus(serverPlayerEntity).PTSD.isEmpty();
    }

    /**
     * 判断玩家是否对任意玩家有PTSD
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否对其它玩家有PTSD
     */
    public static boolean hasPTSDForPlayer(ServerPlayerEntity serverPlayerEntity) {
        return !MentalStatusHelper.getMentalStatus(serverPlayerEntity).playerPTSDSet.isEmpty();
    }

    /**
     * 判断玩家是否对任何事物有PTSD
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否对任何事物有PTSD
     */
    public static boolean hasPTSDForAnything(ServerPlayerEntity serverPlayerEntity) {
        return hasPTSDForEvent(serverPlayerEntity) || hasPTSDForPlayer(serverPlayerEntity);
    }

    public static PTSDLevel getPTSDLevel(Double amount) {
        if (amount == null) {
            return PTSDLevel.CLEAR;
        }

        if (amount < MIN_PTSD_VALUE) {
            return PTSDLevel.CLEAR;
        } else if (amount <= BETWEEN_LATENT_AND_MILD) {
            return PTSDLevel.LATENT;
        } else if (amount <= BETWEEN_MILD_AND_MODERATE) {
            return PTSDLevel.MILD;
        } else if (amount <= BETWEEN_MODERATE_AND_SEVERE) {
            return PTSDLevel.MODERATE;
        } else if (amount <= BETWEEN_SEVERE_AND_EXTREME) {
            return PTSDLevel.SEVERE;
        } else if (amount <= MAX_PTSD_VALUE) {
            return PTSDLevel.EXTREME;
        } else {
            return PTSDLevel.CLEAR;
        }
    }
}
