package grainalcohol.dtt.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class NearbyMentalHealHelper {
    @Nullable
    public static JukeboxBlockEntity findNearestPlayingJukeboxEntity(ServerPlayerEntity player, int radius) {
        JukeboxBlockEntity playingJukebox = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();
        Vec3d playerPos = player.getPos();

        for (BlockPos checkPos : BlockPos.iterateOutwards(center, radius, 2, radius)) {
            if (world.getBlockEntity(checkPos) instanceof JukeboxBlockEntity jukeboxBlockEntity
                    && jukeboxBlockEntity.isPlayingRecord()
                    && !isBlockedByBlocks(world, player.getEyePos(), checkPos, player)
            ) {
                // 这个唱片机至少存在、正在播放且没有被其他方块或液体阻挡
                double distanceSquared = playerPos.squaredDistanceTo(Vec3d.ofCenter(checkPos));
                if (distanceSquared < nearestDistanceSquared) {
                    // 这个唱片机比之前找到的更近
                    playingJukebox = jukeboxBlockEntity;
                    nearestDistanceSquared = distanceSquared;
                }
            }
        }

        return playingJukebox;
    }

    public static boolean isBlockNearby(ServerPlayerEntity player, Predicate<BlockState> predicate, int radius) {
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();

        for (BlockPos checkPos : BlockPos.iterateOutwards(center, radius, 2, radius)) {
            // 曼哈顿距离
            if (predicate.test(world.getBlockState(checkPos)) && !isBlockedByBlocks(world, player.getEyePos(), checkPos, player)) {
                // 有这样一个方块，并且玩家与这个方块之间没有被其他方块或液体阻挡
                return true;
            }
        }

        return false;
    }

    public static boolean isBlockNearby(ServerPlayerEntity player, Block block, int radius) {
        return isBlockNearby(player, state -> state.isOf(block), radius);
    }

    public static boolean isBlockNearby(ServerPlayerEntity player, Class<? extends Block> blockClass, int radius) {
        return isBlockNearby(player, state -> blockClass.isAssignableFrom(state.getBlock().getClass()), radius);
    }

    public static boolean isBlockNearby(ServerPlayerEntity player, TagKey<Block> blockTag, int radius) {
        return isBlockNearby(player, state -> state.isIn(blockTag), radius);
    }

    public static boolean isPetNearby(ServerPlayerEntity player, double radius) {
        ServerWorld world = player.getServerWorld();
        Box searchBox = player.getBoundingBox().expand(radius + 1);

        for (TameableEntity entity : world.getEntitiesByClass(TameableEntity.class, searchBox, TameableEntity::isTamed)) {
            if (entity.getOwnerUuid() != null
                    && entity.getOwnerUuid().equals(player.getUuid())
                    && entity.squaredDistanceTo(player) <= radius * radius
                    && isBlockedByBlocks(world, player.getEyePos(), entity.getEyePos(), player)
            ) {
                // 有这样一个宠物，它的主人是发起检查的玩家
                // 并且玩家与这个宠物的距离不超过指定半径
                // 以及玩家与这个宠物之间没有被其他方块或液体阻挡
                return true;
            }
        }

        return false;
    }

    public static boolean isBlockedByBlocks(ServerWorld world, Vec3d start, BlockPos end, Entity entity) {
        return isBlockedByBlocks(world, start, Vec3d.ofCenter(end), entity);
    }

    public static boolean isBlockedByBlocks(ServerWorld world, Vec3d start, Vec3d end, Entity entity) {
        BlockHitResult hit = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,  // 检测碰撞箱
                RaycastContext.FluidHandling.ANY, // 检测液体
                entity
        ));

        if (hit.getType() == HitResult.Type.MISS) {
            // 没有击中任何东西
            return false;
        }

        // 击中了目标方块本身
        BlockPos hitPos = hit.getBlockPos();
        if (hitPos.toCenterPos().squaredDistanceTo(end) < 0.01) {
            return false;
        }

        // 击中了其他方块
        return true;
    }
}
