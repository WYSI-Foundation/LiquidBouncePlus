/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.potion.Potion;

public final class MovementUtils extends MinecraftInstance {

    public static float getSpeed() {
        return (float) getSpeed(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }

    public static double getSpeed(double motionX, double motionZ) {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public static boolean isBlockUnder() {
        if (mc.thePlayer == null) return false;

        if (mc.thePlayer.posY < 0.0) {
            return false;
        }
        for (int off = 0; off < (int)mc.thePlayer.posY + 2; off += 2) {
            final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, (double)(-off), 0.0);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void move() {move(getSpeed());}

    public static boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public static boolean hasMotion() {
        return mc.thePlayer.motionX != 0D && mc.thePlayer.motionZ != 0D && mc.thePlayer.motionY != 0D;
    }

    public static void strafe(final float speed) {
        if(!isMoving())
            return;

        final double yaw = getDirection();
        mc.thePlayer.motionX = -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ = Math.cos(yaw) * speed;
    }

    public static void move(final float speed) {
        if(!isMoving())
            return;

        final double yaw = getDirection();
        mc.thePlayer.motionX += -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ += Math.cos(yaw) * speed;
    }

    public static void forward(final double length) {
        final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        mc.thePlayer.setPosition(mc.thePlayer.posX + (-Math.sin(yaw) * length), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(yaw) * length));
    }

    public static double getDirection() {
        return getDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward);
    }

    public static float getRawDirection() {
        return getRawDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward);
    }

    public static double getDirectionRotation(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;

        if(pForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if(pForward < 0F)
            forward = -0.5F;
        else if(pForward > 0F)
            forward = 0.5F;

        if(pStrafe > 0F)
            rotationYaw -= 90F * forward;

        if(pStrafe < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static float getRawDirectionRotation(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;

        if(pForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if(pForward < 0F)
            forward = -0.5F;
        else if(pForward > 0F)
            forward = 0.5F;

        if(pStrafe > 0F)
            rotationYaw -= 90F * forward;

        if(pStrafe < 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }

    public static float getScaffoldRotation(float yaw, float strafe) {
        float rotationYaw = yaw;

        rotationYaw += 180F;

        float forward = -0.5F;

        if(strafe < 0F)
            rotationYaw -= 90F * forward;

        if(strafe > 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0D + 0.2D * (double)(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }

        return baseSpeed;
    }

    public static double getJumpBoostModifier(double baseJumpHeight) {
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            baseJumpHeight += (double)((float)(amplifier + 1) * 0.1F);
        }
        return baseJumpHeight;
    }

    public static void setSpeed(MoveEvent moveEvent, double moveSpeed) {
        setSpeed(moveEvent, moveSpeed, mc.thePlayer.rotationYaw, (double)mc.thePlayer.movementInput.moveStrafe, (double)mc.thePlayer.movementInput.moveForward);
    }

    public static void setSpeed(final MoveEvent moveEvent, final double moveSpeed, final float pseudoYaw, final double pseudoStrafe, final double pseudoForward) {
        double forward = pseudoForward;
        double strafe = pseudoStrafe;
        float yaw = pseudoYaw;

        if (forward == 0.0 && strafe == 0.0) {
            moveEvent.setZ(0);
            moveEvent.setX(0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            if (strafe > 0.0D) {
                strafe = 1.0D;
            } else if (strafe < 0.0D) {
                strafe = -1.0D;
            }
            final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
            final double sin = Math.sin(Math.toRadians(yaw + 90.0f));

            moveEvent.setX((forward * moveSpeed * cos + strafe * moveSpeed * sin));
            moveEvent.setZ((forward * moveSpeed * sin - strafe * moveSpeed * cos));
        }
    }
}
