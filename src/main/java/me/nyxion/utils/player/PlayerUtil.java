package me.nyxion.utils.player;

import me.nyxion.utils.client.IMinecraft;
import me.nyxion.utils.other.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class PlayerUtil implements IMinecraft {
    public static final double BASE_SPEED = 0.2873;

    public static double getSpeed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = BASE_SPEED;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        return baseSpeed;
    }

    public static boolean isOverAir() {
        return isOverAir(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public static boolean isOverAir(double x, double y, double z) {
        return BlockUtils.isAir(new BlockPos(x, y - 1, z));
    }

    public static boolean isOnSameTeam(EntityPlayer entity) {
        if (entity.getTeam() != null && mc.thePlayer.getTeam() != null) {
            return entity.getDisplayName().getFormattedText().charAt(1) ==
                    mc.thePlayer.getDisplayName().getFormattedText().charAt(1);
        }
        return false;
    }

    public static boolean isBlockUnder() {
        for (int y = (int) mc.thePlayer.posY; y >= 0; y--) {
            BlockPos pos = new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ);
            if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnLiquid() {
        boolean onLiquid = false;
        AxisAlignedBB playerBB = mc.thePlayer.getEntityBoundingBox();
        int y = (int) playerBB.offset(0.0, -0.01, 0.0).minY;

        for (int x = MathHelper.floor_double(playerBB.minX); x < MathHelper.floor_double(playerBB.maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(playerBB.minZ); z < MathHelper.floor_double(playerBB.maxZ) + 1; z++) {
                Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();

                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) return false;
                    onLiquid = true;
                }
            }
        }

        return onLiquid;
    }

    public static boolean isOverVoid() {
        return isOverVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public static boolean isOverVoid(double x, double y, double z) {
        for (double posY = y; posY > 0.0; posY--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(x, posY, z)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static String getServerIP() {
        String serverIP = "Main Menu";

        if (mc.isIntegratedServerRunning()) {
            serverIP = "SinglePlayer";
        } else if (mc.theWorld != null && mc.theWorld.isRemote) {
            ServerData serverData = mc.getCurrentServerData();
            if (serverData != null) {
                serverIP = serverData.serverIP;
            }
        }

        return serverIP;
    }
}