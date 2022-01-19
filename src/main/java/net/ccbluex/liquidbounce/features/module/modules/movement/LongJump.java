/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.JumpEvent;
import net.ccbluex.liquidbounce.event.MotionEvent;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "LongJump", spacedName = "Long Jump", description = "Allows you to jump further.", category = ModuleCategory.MOVEMENT)
public class LongJump extends Module {

    private final ListValue modeValue = new ListValue("Mode", new String[] {"NCP"/*, "HypixelDamage", "HypixelDamage2"*/, "Damage", "AACv1", "AACv2", "AACv3", "AACv4", "Mineplex", "Mineplex2", "Mineplex3", "RedeskyMaki", "Redesky", "InfiniteRedesky", "Verus", "Pearl"}, "NCP");
    private final BoolValue autoJumpValue = new BoolValue("AutoJump", false);

    // NCP
    private final FloatValue ncpBoostValue = new FloatValue("NCPBoost", 4.25F, 1F, 10F, () -> { return modeValue.get().equalsIgnoreCase("ncp"); });

    /*private final ListValue hypixelDmgMode = new ListValue("HypixelDamage-Mode", new String[] {"Spartan", "Test", "Mini"}, "Spartan", () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage") || modeValue.get().equalsIgnoreCase("hypixeldamage2"); });
    private final IntegerValue hypixelDmgDelay = new IntegerValue("HypixelDamage-DmgDelay", 0, 0, 2000, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage") || modeValue.get().equalsIgnoreCase("hypixeldamage2"); });
    private final BoolValue hypixelDmgBlinkComp = new BoolValue("HypixelDamage-BlinkCompatible", false, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage") || modeValue.get().equalsIgnoreCase("hypixeldamage2"); });
    private final BoolValue hypixelStrafe = new BoolValue("HypixelDamage-StrafeAfterTick", false, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage"); });
    private final IntegerValue hypixelDmgTick = new IntegerValue("HypixelDamage-Tick", 10, 1, 20, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage"); });
    private final FloatValue hypixelDmgMotionY = new FloatValue("HypixelDamage-MotionY", 1F, 0F, 2F, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage"); });
    private final FloatValue hypixelDmgXZBoost = new FloatValue("HypixelDamage-XZBoost", 1F, 0F, 2F, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage"); });

    private final BoolValue hypixelDmgRider = new BoolValue("HypixelDamage2-FakeRideJump", false, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage2"); });
    private final FloatValue hpxDamage2Value = new FloatValue("HypixelDamage2-Boost", 4.25F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage2"); });
    private final FloatValue hpxDamage2HeightValue = new FloatValue("HypixelDamage2-Height", 0.42F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage2"); });
    private final FloatValue hpxDamage2TimerValue = new FloatValue("HypixelDamage2-Timer", 1F, 0.05F, 10F, () -> { return modeValue.get().equalsIgnoreCase("hypixeldamage2"); });*/

    // Verus
    private final BoolValue redeskyTimerBoostValue = new BoolValue("Redesky-TimerBoost", false, () -> { return modeValue.get().equalsIgnoreCase("redesky"); });
    private final BoolValue redeskyGlideAfterTicksValue = new BoolValue("Redesky-GlideAfterTicks", false, () -> { return modeValue.get().equalsIgnoreCase("redesky"); });
    private final IntegerValue redeskyTickValue = new IntegerValue("Redesky-Ticks", 21, 1, 25, () -> { return modeValue.get().equalsIgnoreCase("redesky"); });
    private final FloatValue redeskyYMultiplier = new FloatValue("Redesky-YMultiplier", 0.77F, 0.1F, 1F, () -> { return modeValue.get().equalsIgnoreCase("redesky"); });
    private final FloatValue redeskyXZMultiplier = new FloatValue("Redesky-XZMultiplier", 0.9F, 0.1F, 1F, () -> { return modeValue.get().equalsIgnoreCase("redesky"); });
    private final FloatValue redeskyTimerBoostStartValue = new FloatValue("Redesky-TimerBoostStart", 1.85F, 0.05F, 10F, () -> { return modeValue.get().equalsIgnoreCase("redesky") && redeskyTimerBoostValue.get(); });
    private final FloatValue redeskyTimerBoostEndValue = new FloatValue("Redesky-TimerBoostEnd", 1.0F, 0.05F, 10F, () -> { return modeValue.get().equalsIgnoreCase("redesky") && redeskyTimerBoostValue.get(); });
    private final IntegerValue redeskyTimerBoostSlowDownSpeedValue = new IntegerValue("Redesky-TimerBoost-SlowDownSpeed", 2, 1, 10, () -> { return modeValue.get().equalsIgnoreCase("redesky") && redeskyTimerBoostValue.get(); });

    // Verus
    private final ListValue verusDmgModeValue = new ListValue("Verus-DamageMode", new String[]{"None", "Instant", "InstantC06", "Jump"}, "None", () -> { return modeValue.get().equalsIgnoreCase("verus"); });
    private final FloatValue verusBoostValue = new FloatValue("Verus-Boost", 4.25F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("verus"); });
    private final FloatValue verusHeightValue = new FloatValue("Verus-Height", 0.42F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("verus"); });
    private final FloatValue verusTimerValue = new FloatValue("Verus-Timer", 1F, 0.05F, 10F, () -> { return modeValue.get().equalsIgnoreCase("verus"); });
    private final BoolValue verusSpoofGround = new BoolValue("Verus-SpoofGround", false, () -> { return modeValue.get().equalsIgnoreCase("verus"); });

    // Pearl
    private final FloatValue pearlBoostValue = new FloatValue("Pearl-Boost", 4.25F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("pearl"); });
    private final FloatValue pearlHeightValue = new FloatValue("Pearl-Height", 0.42F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("pearl"); });
    private final FloatValue pearlTimerValue = new FloatValue("Pearl-Timer", 1F, 0.05F, 10F, () -> { return modeValue.get().equalsIgnoreCase("pearl"); });

    // Damage
    private final FloatValue damageBoostValue = new FloatValue("Damage-Boost", 4.25F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("damage"); });
    private final FloatValue damageHeightValue = new FloatValue("Damage-Height", 0.42F, 0F, 10F, () -> { return modeValue.get().equalsIgnoreCase("damage"); });
    private final FloatValue damageTimerValue = new FloatValue("Damage-Timer", 1F, 0.05F, 10F, () -> { return modeValue.get().equalsIgnoreCase("damage"); });
    private final BoolValue damageNoMoveValue = new BoolValue("Damage-NoMove", false, () -> { return modeValue.get().equalsIgnoreCase("damage"); });
    private final BoolValue damageARValue = new BoolValue("Damage-AutoReset", false, () -> { return modeValue.get().equalsIgnoreCase("damage"); });

    private boolean jumped;
    private boolean canBoost;
    private boolean teleported;
    private boolean canMineplexBoost;
    private int ticks = 0;
    private float currentTimer = 1F;

    private boolean verusDmged, hpxDamage, damaged = false;
    private int pearlState = 0;

    private MSTimer dmgTimer = new MSTimer();

    /*private void sendPosPacket(double x, double y, double z, boolean gr) {
        if (hypixelDmgBlinkComp.get()) mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, gr));
        else PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, gr));
    }*/

    public void onEnable() {
        if (mc.thePlayer == null) return;
        if (modeValue.get().equalsIgnoreCase("redesky") && redeskyTimerBoostValue.get()) {
            currentTimer = redeskyTimerBoostStartValue.get();
        }

        ticks = 0;
        verusDmged = false;
        verusDmgTimes = 0;
        hpxDamage = false;
        damaged = false;
        pearlState = 0;

        dmgTimer.reset();

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

            case "verus":
                if (verusDmgModeValue.get().equalsIgnoreCase("Instant")) {
                    if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, 4, 0).expand(0, 0, 0)).isEmpty()) {
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false));
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false));
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true));
                        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                        if (verusReDamageValue.get()) dmgCooldown = verusReDmgTickValue.get();
                    }
                } else if (verusDmgModeValue.get().equalsIgnoreCase("InstantC06")) {
                    if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, 4, 0).expand(0, 0, 0)).isEmpty()) {
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
                        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                        if (verusReDamageValue.get()) dmgCooldown = verusReDmgTickValue.get();
                    }
                } else if (verusDmgModeValue.get().equalsIgnoreCase("Jump")) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                        verusJumpTimes = 1;
                    }
                } else {
                    // set dmged = true since there's no damage method
                    verusDmged = true;
            }
        }
/*
        if (modeValue.get().equalsIgnoreCase("hypixeldamage2")) {
            switch (hypixelDmgMode.get().toLowerCase()) {
                case "spartan":
                    for(int i = 0; i < 65; ++i) {
                        sendPosPacket(x, y + 0.049D, z, false);
                        sendPosPacket(x, y, z, false);
                    }
                    sendPosPacket(x, y + 0.1D, z, true);
                    break;
                case "test":
                    for(int i = 0; i < 32; ++i) {
                        sendPosPacket(x, y + 0.1075D, z, false);
                        sendPosPacket(x, y, z, false);
                    }
                    sendPosPacket(x, y + 0.0095D, z, true);
                    break;
                case "mini": // only 10 packets??? might actually work LOL
                    for(int i = 0; i < 10; ++i) {
                        sendPosPacket(x, y + 0.3175D, z, false);
                        sendPosPacket(x, y, z, false);
                    }
                    sendPosPacket(x, y + 0.01025D, z, true);
                    break;
            }
        }
*/
    }

    /*@EventTarget
    public void onMotion(final MotionEvent event) {
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        
        if (!hpxDamage && modeValue.get().equalsIgnoreCase("hypixeldamage") && (hypixelDmgDelay.get() <= 0 || dmgTimer.hasTimePassed(hypixelDmgDelay.get()))) {
            switch (hypixelDmgMode.get().toLowerCase()) {
            case "spartan":
                for(int i = 0; i < 65; ++i) {
                    sendPosPacket(x, y + 0.049D, z, false);
                    sendPosPacket(x, y, z, false);
                }
                sendPosPacket(x, y + 0.1D, z, true);
                break;
            case "test":
                for(int i = 0; i < 32; ++i) {
                    sendPosPacket(x, y + 0.1075D, z, false);
                    sendPosPacket(x, y, z, false);
                }
                sendPosPacket(x, y + 0.0095D, z, true);
                break;
            case "mini": // only 10 packets??? might actually work LOL
                for(int i = 0; i < 10; ++i) {
                    sendPosPacket(x, y + 0.3175D, z, false);
                    sendPosPacket(x, y, z, false);
                }
                sendPosPacket(x, y + 0.01025D, z, true);
                break;
            }

            hpxDamage = true;
        }
    }*/

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if (modeValue.get().equalsIgnoreCase("verus")) {
            if (mc.thePlayer.hurtTime > 0 && !verusDmged) {
                verusDmged = true;
                MovementUtils.strafe(verusBoostValue.get());
                mc.thePlayer.motionY = verusHeightValue.get();
            }
            if (verusDmged)
                mc.timer.timerSpeed = verusTimerValue.get();
            else {
                mc.thePlayer.movementInput.moveForward = 0F;
                mc.thePlayer.movementInput.moveStrafe = 0F;
            }

            return;
        }
/*
        if (modeValue.get().equalsIgnoreCase("hypixeldamage2")) {
            if (mc.thePlayer.hurtTime > 0 && !hpxDamage) {
                if (hypixelDmgRider.get()) {
                    PacketUtils.sendPacketNoEvent(new C0CPacketInput());
                    PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.RIDING_JUMP, 100));
                }
                hpxDamage = true;
                MovementUtils.strafe(hpxDamage2Value.get());
                mc.thePlayer.motionY = hpxDamage2HeightValue.get();
            }
            if (hpxDamage)
                mc.timer.timerSpeed = hpxDamage2TimerValue.get();
            else {
                mc.thePlayer.movementInput.moveForward = 0F;
                mc.thePlayer.movementInput.moveStrafe = 0F;
            }

            return;
        }
*/
        if (modeValue.get().equalsIgnoreCase("damage")) {
            if (mc.thePlayer.hurtTime > 0 && !damaged) {
                damaged = true;
                MovementUtils.strafe(damageBoostValue.get());
                mc.thePlayer.motionY = damageHeightValue.get();
            }
            if (damaged) {
                mc.timer.timerSpeed = damageTimerValue.get();
                if (damageARValue.get() && mc.thePlayer.hurtTime <= 0) damaged = false;
            }

            return;
        }

        if (modeValue.get().equalsIgnoreCase("pearl")) {
            int enderPearlSlot = getPearlSlot();
            if (pearlState == 0) {
                if (enderPearlSlot == -1) {
                    LiquidBounce.hud.addNotification(new Notification("You don't have any ender pearl!", Notification.Type.ERROR));
                    pearlState = -1;
                    this.setState(false);
                    return;                    
                }
                if (mc.thePlayer.inventory.currentItem != enderPearlSlot) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(enderPearlSlot));
                }
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, 90, mc.thePlayer.onGround));
                mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventoryContainer.getSlot(enderPearlSlot + 36).getStack(), 0, 0, 0));
                if (enderPearlSlot != mc.thePlayer.inventory.currentItem) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));                    
                }
                pearlState = 1;                    
            }

            if (pearlState == 1 && mc.thePlayer.hurtTime > 0) {
                pearlState = 2;
                MovementUtils.strafe(pearlBoostValue.get());
                mc.thePlayer.motionY = pearlHeightValue.get();
            }

            if (pearlState == 2) 
                mc.timer.timerSpeed = pearlTimerValue.get();

            return;
        }

        if(jumped) {
            final String mode = modeValue.get();

            if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
                jumped = false;
                canMineplexBoost = false;

                if (mode.equalsIgnoreCase("NCP")) {
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionZ = 0;
                }
                return;
            }

            switch (mode.toLowerCase()) {
                case "ncp":
                    MovementUtils.strafe(MovementUtils.getSpeed() * (canBoost ? ncpBoostValue.get() : 1F));
                    canBoost = false;
                    break;
                case "aacv1":
                    mc.thePlayer.motionY += 0.05999D;
                    MovementUtils.strafe(MovementUtils.getSpeed() * 1.08F);
                    break;
                case "aacv2":
                case "mineplex3":
                    mc.thePlayer.jumpMovementFactor = 0.09F;
                    mc.thePlayer.motionY += 0.0132099999999999999999999999999;
                    mc.thePlayer.jumpMovementFactor = 0.08F;
                    MovementUtils.strafe();
                    break;
                case "aacv3":
                    final EntityPlayerSP player = mc.thePlayer;

                    if (player.fallDistance > 0.5F && !teleported) {
                        double value = 3;
                        EnumFacing horizontalFacing = player.getHorizontalFacing();
                        double x = 0;
                        double z = 0;
                        switch (horizontalFacing) {
                            case NORTH:
                                z = -value;
                                break;
                            case EAST:
                                x = +value;
                                break;
                            case SOUTH:
                                z = +value;
                                break;
                            case WEST:
                                x = -value;
                                break;
                        }

                        player.setPosition(player.posX + x, player.posY, player.posZ + z);
                        teleported = true;
                    }
                    break;
                case "mineplex":
                    mc.thePlayer.motionY += 0.0132099999999999999999999999999;
                    mc.thePlayer.jumpMovementFactor = 0.08F;
                    MovementUtils.strafe();
                    break;
                case "mineplex2":
                    if (!canMineplexBoost)
                        break;

                    mc.thePlayer.jumpMovementFactor = 0.1F;

                    if (mc.thePlayer.fallDistance > 1.5F) {
                        mc.thePlayer.jumpMovementFactor = 0F;
                        mc.thePlayer.motionY = -10F;
                    }
                    MovementUtils.strafe();
                    break;
                // add timer to use longjump longer forward without boost
                case "aacv4":
                    mc.thePlayer.jumpMovementFactor = 0.05837456f;
                    mc.timer.timerSpeed = 0.5F;
                    break;
                //simple lmfao
                case "redeskymaki":
                    mc.thePlayer.jumpMovementFactor = 0.15f;
                    mc.thePlayer.motionY += 0.05F;
                    break;
                case "redesky":
                    if (redeskyTimerBoostValue.get()) {
                        mc.timer.timerSpeed = currentTimer;
                    }
                    if (ticks < redeskyTickValue.get()) {
                        mc.thePlayer.motionY *= redeskyYMultiplier.get();
                        mc.thePlayer.motionX *= redeskyXZMultiplier.get();
                        mc.thePlayer.motionZ *= redeskyXZMultiplier.get();

                        mc.thePlayer.jump();
                    } else {
                        if (redeskyGlideAfterTicksValue.get()) {
                            mc.thePlayer.motionY += 0.03F;
                        }
                        if (redeskyTimerBoostValue.get() && currentTimer > redeskyTimerBoostEndValue.get()) {
                            currentTimer -= 0.05F * redeskyTimerBoostSlowDownSpeedValue.get();
                        }
                    }
                    ticks++;
                    break;
                // copied????
                /*case "hypixeldamage":
                    if (ticks < hypixelDmgTick.get()) {
                        mc.thePlayer.jump();
                        mc.thePlayer.motionY = hypixelDmgMotionY.get();
                        MovementUtils.strafe(MovementUtils.getSpeed() * hypixelDmgXZBoost.get());
                    } else {
                        if (!mc.thePlayer.onGround && hypixelStrafe.get()) MovementUtils.strafe(hypixelDmgXZBoost.get());
                    }
                    ticks++;
                    break;*/
                case "infiniteredesky":
                    if(mc.thePlayer.fallDistance > 0.6F) 
                        mc.thePlayer.motionY += 0.02F;
                
                    MovementUtils.strafe((float) Math.min(0.85, Math.max(0.25, MovementUtils.getSpeed() * 1.05878)));
                    break;
            }
        }

        if(autoJumpValue.get() && mc.thePlayer.onGround && MovementUtils.isMoving()) {
                jumped = true;
                mc.thePlayer.jump();
        }
    }

    @EventTarget
    public void onMove(final MoveEvent event) {
        final String mode = modeValue.get();

        if (mode.equalsIgnoreCase("mineplex3")) {
            if(mc.thePlayer.fallDistance != 0)
                mc.thePlayer.motionY += 0.037;
        } else if (mode.equalsIgnoreCase("ncp") && !MovementUtils.isMoving() && jumped) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            event.zeroXZ();
        }

        if (mode.equalsIgnoreCase("damage") && damageNoMoveValue.get() && !damaged) 
            event.zeroXZ();

        if ((mode.equalsIgnoreCase("verus") && !verusDmged)/* || (mode.equalsIgnoreCase("hypixeldamage2") && !hpxDamage)*/ || (mode.equalsIgnoreCase("pearl") && pearlState != 2))
            event.cancelEvent();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final String mode = modeValue.get();
        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer c03 = (C03PacketPlayer) event.getPacket();
            if ((mode.equalsIgnoreCase("verus") && !verusDmged)/* || (mode.equalsIgnoreCase("hypixeldamage2") && !hpxDamage)*/ || (mode.equalsIgnoreCase("pearl") && pearlState != 2)) c03.setMoving(false);
        }
    }

    @EventTarget(ignoreCondition = true)
    public void onJump(final JumpEvent event) {
        jumped = true;
        canBoost = true;
        teleported = false;

        if(getState()) {
            switch(modeValue.get().toLowerCase()) {
                case "mineplex":
                    event.setMotion(event.getMotion() * 4.08f);
                    break;
                case "mineplex2":
                    if(mc.thePlayer.isCollidedHorizontally) {
                        event.setMotion(2.31f);
                        canMineplexBoost = true;
                        mc.thePlayer.onGround = false;
                    }
                    break;
                case "aacv4":
                    event.setMotion(event.getMotion() * 1.0799F);
               break;
            }
        }

    }

    private int getPearlSlot() {
        for(int i = 36; i < 45; ++i) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemEnderPearl) {
                return i - 36;
            }
        }
        return -1;
    }

    public void onDisable(){
        mc.timer.timerSpeed = 1.0F;
        mc.thePlayer.speedInAir = 0.02F;
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }
}
