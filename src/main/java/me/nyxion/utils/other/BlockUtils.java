package me.nyxion.utils.other;

import me.nyxion.utils.client.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class BlockUtils implements IMinecraft {
    public static Block getBlock(BlockPos blockPos) {
        if (mc.theWorld != null && blockPos != null) {
            return mc.theWorld.getBlockState(blockPos).getBlock();
        }
        return null;
    }

    public static Material getMaterial(BlockPos blockPos) {
        Block block = getBlock(blockPos);
        return block != null ? block.getMaterial() : null;
    }

    public static boolean isAir(BlockPos blockPos) {
        Material material = getMaterial(blockPos);
        return material == Material.air;
    }

    public static float getHardness(BlockPos blockPos) {
        return getBlock(blockPos).getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, blockPos);
    }

    public static boolean isReplaceable(BlockPos blockPos) {
        Material material = getMaterial(blockPos);
        return material != null && material.isReplaceable();
    }

    public static boolean canBeClicked(BlockPos blockPos) {
        Block block = getBlock(blockPos);
        return block != null &&
                block.canCollideCheck(mc.theWorld.getBlockState(blockPos), false) &&
                mc.theWorld.getWorldBorder().contains(blockPos);
    }

    public static double getCenterDistance(BlockPos blockPos) {
        return mc.thePlayer.getDistance(
                blockPos.getX() + 0.5,
                blockPos.getY() + 0.5,
                blockPos.getZ() + 0.5
        );
    }

    public static Vec3 floorVec3(Vec3 vec3) {
        return new Vec3(
                Math.floor(vec3.xCoord),
                Math.floor(vec3.yCoord),
                Math.floor(vec3.zCoord)
        );
    }
}