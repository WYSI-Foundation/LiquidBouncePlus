/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class CustomSpeed extends SpeedMode {

    private int groundTick = 0;
    public CustomSpeed() {
        super("Custom");
    }

    @Override
    public void onMotion() {
        final Speed speed = (Speed) LiquidBounce.moduleManager.getModule(Speed.class);

        if(speed == null)
            return;

        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.motionY> 0){
                mc.timer.timerSpeed = speed.customUpTimerValue.get();
            } else {
                mc.timer.timerSpeed = speed.customDownTimerValue.get();
            }
        }

        if (mc.thePlayer.onGround) {
            if (groundTick >= speed.groundStay.get()) {
                if (speed.doLaunchSpeedValue.get()) {
                    MovementUtils.strafe(speed.customLaunchValue.get());
                }
                if (speed.customYValue.get() != 0f) {
                    mc.thePlayer.motionY = (double) speed.customYValue.get();
                }
            } else if (speed.groundResetXZValue.get()) {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionZ = 0.0;
            }

            groundTick++;
        } else {
            groundTick = 0;
            switch (speed.customStrafeValue.get().toLowerCase()) {
                case "strafe":
                    MovementUtils.strafe(speed.customSpeedValue.get());
                break;
                case "boost":
                    MovementUtils.strafe();
                break;
                case "plus":
                    MovementUtils.move(speed.customSpeedValue.get() * 0.1f);
                break;
                case "plusonlyup":
                    if (mc.thePlayer.motionY > 0) {
                        MovementUtils.move(speed.customSpeedValue.get() * 0.1f);
                    }
                default:
                    MovementUtils.strafe();
            }
            mc.thePlayer.motionY += speed.customAddYValue.get() * 0.03;
        }

    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onMove(MoveEvent event) {

    }

    @Override
    public void onEnable() {
        final Speed speed = (Speed) LiquidBounce.moduleManager.getModule(Speed.class);

        if(speed == null)
            return;

        if(speed.resetXZValue.get()) mc.thePlayer.motionX = mc.thePlayer.motionZ = 0D;
        if(speed.resetYValue.get()) mc.thePlayer.motionY = 0D;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
        super.onDisable();
    }

}
