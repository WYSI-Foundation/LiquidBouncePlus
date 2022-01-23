/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Strafe;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.util.MathHelper;

public class VerusDrew extends SpeedMode {

    public VerusDrew() {
        super("VerusDrew");
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
        super.onDisable();
    }

    @Override
    public void onMotion() {
        final Speed speed = (Speed) LiquidBounce.moduleManager.getModule(Speed.class);
        if(speed == null)
            return;
        if(!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0f;
            mc.thePlayer.motionZ = 0.0f;
            return;
        }

        mc.timer.timerSpeed = speed.verusTimer.get();
        
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            if(mc.thePlayer.isSprinting()) {
                MovementUtils.strafe(MovementUtils.getSpeed()+0.2F);
            }
        }else{
            if(MovementUtils.getSpeed() < 0.36) {
                MovementUtils.strafe(0.36F); // why complicated stuffs when you can just
            }
        }
    }

    @Override
    public void onUpdate() {}

    @Override
    public void onMove(MoveEvent event) {}
}
