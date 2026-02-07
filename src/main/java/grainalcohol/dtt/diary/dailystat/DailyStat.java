package grainalcohol.dtt.diary.dailystat;

import grainalcohol.dtt.init.DTTListener;
import grainalcohol.dtt.mixin.AnimalEntityMixin;
import grainalcohol.dtt.mixin.PlayerEntityMixin;
import grainalcohol.dtt.mixin.RaidMixin;
import net.minecraft.nbt.NbtCompound;

// TODO：可以顺便尝试一下策略模式，用一个Map管理内容
public class DailyStat {
    public static final String DAILY_STAT_NBT_KEY = "DTTDailyStat";
    public static final String TODAY_DAILY_STAT_NBT_KEY = "Today";
    public static final String YESTERDAY_DAILY_STAT_NBT_KEY = "Yesterday";
    public static final String MOVING_AVERAGE_DAILY_STAT_NBT_KEY = "MovingAverage";

    //essential
    /**
     * 今天是否下过雨<br>
     * 触发检查时机是：
     * <ul>
     *     <li>维度开始或停止下雨时</li>
     *     <li>玩家切换维度时</li>
     *     <li>玩家睡醒时</li>
     *     <li>玩家淋到雨时（待定）</li>
     * </ul>
     * @see grainalcohol.dtt.mixin.ServerWorldMixin
     * @see PlayerEntityMixin
     * @see grainalcohol.dtt.mixin.ServerPlayerEntityMixin
     */
    private boolean hasRained;
    /**
     * 宠物繁殖
     * @see AnimalEntityMixin
     */
    private boolean hasPetBred;
    /**
     * 宠物死亡
     * @see DTTListener
     */
    private boolean hasPetDied;

    // stat
    /**
     * 击杀怪物数量
     * @see DTTListener
     */
    private int monsterKilled;
    /**
     * 以下都是通过Mixin注入PlayerEntity的increaseStat方法进行统计的
     * @see PlayerEntityMixin
     */
    private int distanceMoved;// 行走距离
    private int damageTaken; // 受到伤害
    private int tradedCount; // 交易次数
    private int brewedCount; // 酿造次数

    /**
     * 栽培盆栽，但不包括枯死的灌木
     * @see DTTListener
     */
    private boolean hasFlowerPotted;

    /**
     * 吃东西
     * @see PlayerEntityMixin
     */
    private boolean hasAte;
    /**
     * 死亡
     * @see DTTListener
     */
    private boolean hasDead;

    // major impact
    /**
     * 袭击事件获胜
     * @see PlayerEntityMixin
     */
    private boolean hasRaidWon;
    /**
     * 袭击事件失败
     * @see RaidMixin
     */
    private boolean hasRaidFailed;

    // mental
    private boolean hasCured;
    private boolean hasWorsened;

    // method
    public DailyStat() {
        this.monsterKilled = 0;
        this.distanceMoved = 0;
        this.damageTaken = 0;
        this.tradedCount = 0;
        this.brewedCount = 0;
        this.hasRained = false;
        this.hasFlowerPotted = false;
        this.hasRaidWon = false;
        this.hasRaidFailed = false;
        this.hasPetBred = false;
        this.hasAte = false;
        this.hasDead = false;
        this.hasPetDied = false;
        this.hasCured = false;
        this.hasWorsened = false;
    }

    public DailyStat(
            int monsterKilled,
            int distanceMoved,
            int damageTaken,
            int tradedCount,
            int brewedCount,
            boolean hasRained,
            boolean hasFlowerPotted,
            boolean hasRaidWon,
            boolean hasRaidFailed,
            boolean hasPetBred,
            boolean hasAte,
            boolean hasDead,
            boolean hasPetDied,
            boolean hasCured,
            boolean hasWorsened
    ) {
        this.monsterKilled = monsterKilled;
        this.distanceMoved = distanceMoved;
        this.damageTaken = damageTaken;
        this.tradedCount = tradedCount;
        this.brewedCount = brewedCount;
        this.hasRained = hasRained;
        this.hasFlowerPotted = hasFlowerPotted;
        this.hasRaidWon = hasRaidWon;
        this.hasRaidFailed = hasRaidFailed;
        this.hasPetBred = hasPetBred;
        this.hasAte = hasAte;
        this.hasDead = hasDead;
        this.hasPetDied = hasPetDied;
        this.hasCured = hasCured;
        this.hasWorsened = hasWorsened;
    }

    public DailyStat(DailyStat dailyStat) {
        this.monsterKilled = dailyStat.getMonsterKilled();
        this.distanceMoved = dailyStat.getDistanceMoved();
        this.damageTaken = dailyStat.getDamageTaken();
        this.tradedCount = dailyStat.getTradedCount();
        this.brewedCount = dailyStat.getBrewedCount();
        this.hasRained = dailyStat.isHasRained();
        this.hasFlowerPotted = dailyStat.isHasFlowerPotted();
        this.hasRaidWon = dailyStat.isHasRaidWon();
        this.hasRaidFailed = dailyStat.isHasRaidFailed();
        this.hasPetBred = dailyStat.isHasPetBred();
        this.hasAte = dailyStat.isHasAte();
        this.hasDead = dailyStat.isHasDead();
        this.hasPetDied = dailyStat.isHasPetDied();
        this.hasCured = dailyStat.isHasCured();
        this.hasWorsened = dailyStat.isHasWorsened();
    }

    public void merge(DailyStat other) {
        setMonsterKilled(getMonsterKilled() + other.getMonsterKilled());
        setDistanceMoved(getDistanceMoved() + other.getDistanceMoved());
        setDamageTaken(getDamageTaken() + other.getDamageTaken());
        setTradedCount(getTradedCount() + other.getTradedCount());
        setBrewedCount(getBrewedCount() + other.getBrewedCount());
        setHasRained(isHasRained() || other.isHasRained());
        setHasFlowerPotted(isHasFlowerPotted() || other.isHasFlowerPotted());
        setHasRaidWon(isHasRaidWon() || other.isHasRaidWon());
        setHasRaidFailed(isHasRaidFailed() || other.isHasRaidFailed());
        setHasPetBred(isHasPetBred() || other.isHasPetBred());
        setHasAte(isHasAte() || other.isHasAte());
        setHasDead(isHasDead() || other.isHasDead());
        setHasPetDied(isHasPetDied() || other.isHasPetDied());
        setHasCured(isHasCured() || other.isHasCured());
        setHasWorsened(isHasWorsened() || other.isHasWorsened());
    }

    public void multiply(double multiplier) {
        setMonsterKilled((int) (getMonsterKilled() * multiplier));
        setDistanceMoved((int) (getDistanceMoved() * multiplier));
        setDamageTaken((int) (getDamageTaken() * multiplier));
        setTradedCount((int) (getTradedCount() * multiplier));
        setBrewedCount((int) (getBrewedCount() * multiplier));
    }

    // increase
    public void increaseMonsterKilled(int amount) {
        setMonsterKilled(getMonsterKilled() + amount);
    }

    public void increaseDistanceMoved(int amount) {
        setDistanceMoved(getDistanceMoved() + amount);
    }

    public void increaseDamageTaken(int amount) {
        setDamageTaken(getDamageTaken() + amount);
    }

    public void increaseTradedCount(int amount) {
        setTradedCount(getTradedCount() + amount);
    }

    public void increaseBrewedCount(int amount) {
        setBrewedCount(getBrewedCount() + amount);
    }

    // setter & getter
    public boolean isHasRained() {
        return hasRained;
    }

    public void setHasRained(boolean hasRained) {
        this.hasRained = hasRained;
    }

    public int getMonsterKilled() {
        return monsterKilled;
    }

    public void setMonsterKilled(int monsterKilled) {
        this.monsterKilled = monsterKilled;
    }

    public int getDistanceMoved() {
        return distanceMoved;
    }

    public void setDistanceMoved(int distanceMoved) {
        this.distanceMoved = distanceMoved;
    }

    public boolean isHasPetBred() {
        return hasPetBred;
    }

    public void setHasPetBred(boolean hasPetBred) {
        this.hasPetBred = hasPetBred;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public void setDamageTaken(int damageTaken) {
        this.damageTaken = damageTaken;
    }

    public int getTradedCount() {
        return tradedCount;
    }

    public void setTradedCount(int tradedCount) {
        this.tradedCount = tradedCount;
    }

    public int getBrewedCount() {
        return brewedCount;
    }

    public void setBrewedCount(int brewedCount) {
        this.brewedCount = brewedCount;
    }

    public boolean isHasFlowerPotted() {
        return hasFlowerPotted;
    }

    public void setHasFlowerPotted(boolean hasFlowerPotted) {
        this.hasFlowerPotted = hasFlowerPotted;
    }

    public boolean isHasRaidWon() {
        return hasRaidWon;
    }

    public void setHasRaidWon(boolean hasRaidWon) {
        this.hasRaidWon = hasRaidWon;
    }

    public boolean isHasRaidFailed() {
        return hasRaidFailed;
    }

    public void setHasRaidFailed(boolean hasRaidFailed) {
        this.hasRaidFailed = hasRaidFailed;
    }

    public boolean isHasAte() {
        return hasAte;
    }

    public void setHasAte(boolean hasAte) {
        this.hasAte = hasAte;
    }

    public boolean isHasDead() {
        return hasDead;
    }

    public void setHasDead(boolean hasDead) {
        this.hasDead = hasDead;
    }

    public boolean isHasPetDied() {
        return hasPetDied;
    }

    public void setHasPetDied(boolean hasPetDied) {
        this.hasPetDied = hasPetDied;
    }

    public boolean isHasCured() {
        return hasCured;
    }

    public void setHasCured(boolean hasCured) {
        this.hasCured = hasCured;
    }

    public boolean isHasWorsened() {
        return hasWorsened;
    }

    public void setHasWorsened(boolean hasWorsened) {
        this.hasWorsened = hasWorsened;
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putInt("monsterKilled", getMonsterKilled());
        nbt.putInt("distanceMoved", getDistanceMoved());
        nbt.putInt("damageTaken", getDamageTaken());
        nbt.putInt("tradedCount", getTradedCount());
        nbt.putInt("brewedCount", getBrewedCount());
        nbt.putBoolean("isHasRained", isHasRained());
        nbt.putBoolean("isHasFlowerPotted", isHasFlowerPotted());
        nbt.putBoolean("isHasRaidWon", isHasRaidWon());
        nbt.putBoolean("isHasRaidFailed", isHasRaidFailed());
        nbt.putBoolean("isHasPetBred", isHasPetBred());
        nbt.putBoolean("isHasAte", isHasAte());
        nbt.putBoolean("isHasDead", isHasDead());
        nbt.putBoolean("isHasPetDied", isHasPetDied());
        nbt.putBoolean("isHasCured", isHasCured());
        nbt.putBoolean("isHasWorsened", isHasWorsened());
    }

    public void readFromNbt(NbtCompound nbt) {
        setMonsterKilled(nbt.getInt("monsterKilled"));
        setDistanceMoved(nbt.getInt("distanceMoved"));
        setDamageTaken(nbt.getInt("damageTaken"));
        setTradedCount(nbt.getInt("tradedCount"));
        setBrewedCount(nbt.getInt("brewedCount"));
        setHasRained(nbt.getBoolean("isHasRained"));
        setHasFlowerPotted(nbt.getBoolean("isHasFlowerPotted"));
        setHasRaidWon(nbt.getBoolean("isHasRaidWon"));
        setHasRaidFailed(nbt.getBoolean("isHasRaidFailed"));
        setHasPetBred(nbt.getBoolean("isHasPetBred"));
        setHasAte(nbt.getBoolean("isHasAte"));
        setHasDead(nbt.getBoolean("isHasDead"));
        setHasPetDied(nbt.getBoolean("isHasPetDied"));
        setHasCured(nbt.getBoolean("isHasCured"));
        setHasWorsened(nbt.getBoolean("isHasWorsened"));
    }
}
