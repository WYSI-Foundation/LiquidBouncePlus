/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EntityMovementEvent;
import net.ccbluex.liquidbounce.features.module.modules.misc.Patcher;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    @Final
    private NetworkManager netManager;

    @Shadow
    private Minecraft gameController;

    @Shadow
    private WorldClient clientWorldController;

    @Shadow
    public int currentServerMaxPlayers;

    @Shadow
    public abstract NetworkPlayerInfo getPlayerInfo(UUID p_175102_1_);

    @Inject(method = "handleSpawnPlayer", at = @At("HEAD"), cancellable = true)
    private void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn, CallbackInfo callbackInfo) {
        if (Patcher.silentNPESP.get()) {
            try {
                PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
                double d0 = (double)packetIn.getX() / 32.0D;
                double d1 = (double)packetIn.getY() / 32.0D;
                double d2 = (double)packetIn.getZ() / 32.0D;
                float f = (float)(packetIn.getYaw() * 360) / 256.0F;
                float f1 = (float)(packetIn.getPitch() * 360) / 256.0F;
                EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(gameController.theWorld, getPlayerInfo(packetIn.getPlayer()).getGameProfile());
                entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = (double)(entityotherplayermp.serverPosX = packetIn.getX());
                entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = (double)(entityotherplayermp.serverPosY = packetIn.getY());
                entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = (double)(entityotherplayermp.serverPosZ = packetIn.getZ());
                int i = packetIn.getCurrentItemID();

                if (i == 0)
                {
                    entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = null;
                }
                else
                {
                    entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = new ItemStack(Item.getItemById(i), 1, 0);
                }

                entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
                clientWorldController.addEntityToWorld(packetIn.getEntityID(), entityotherplayermp);
                List<DataWatcher.WatchableObject> list = packetIn.func_148944_c();

                if (list != null)
                {
                    entityotherplayermp.getDataWatcher().updateWatchedObjectsFromList(list);
                }
            } catch (Exception e) {
                // ignore
            }
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleDisconnect", at = @At("HEAD")) 
    public void handleDisconnect(S40PacketDisconnect packetIn, CallbackInfo callbackInfo) {
        if (wdl.WDL.downloading) {
			wdl.WDL.stopDownload();

			try {
				Thread.sleep(3000L);
			} catch (Exception var3) {
				;
			}
		}
    }

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void handleResourcePack(final S48PacketResourcePackSend p_handleResourcePack_1_, final CallbackInfo callbackInfo) {
        final String url = p_handleResourcePack_1_.getURL();
        final String hash = p_handleResourcePack_1_.getHash();

        try {
            final String scheme = new URI(url).getScheme();
            final boolean isLevelProtocol = "level".equals(scheme);

            if(!"http".equals(scheme) && !"https".equals(scheme) && !isLevelProtocol)
                throw new URISyntaxException(url, "Wrong protocol");

            if(isLevelProtocol && (url.contains("..")) && (url.contains("liquidbounce"))) {
                ClientUtils.displayChatMessage("§8[§9§lLiquidBounce+§8] §6The current server has triggered an exploit, luckily we patched it.");
                ClientUtils.displayChatMessage("§8[§9§lLiquidBounce+§8] §7Exploit target directory: §r" + url);
                throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
            }
               
        }catch(final URISyntaxException e) {
            ClientUtils.getLogger().error("Failed to handle resource pack", e);
            netManager.sendPacket(new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleJoinGame", at = @At("HEAD"), cancellable = true)
    private void handleJoinGameWithAntiForge(S01PacketJoinGame packetIn, final CallbackInfo callbackInfo) {
        if(!AntiForge.enabled || !AntiForge.blockFML || Minecraft.getMinecraft().isIntegratedServerRunning())
            return;

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
        this.gameController.playerController = new PlayerControllerMP(gameController, (NetHandlerPlayClient) (Object) this);
        this.clientWorldController = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.gameController.mcProfiler);
        this.gameController.gameSettings.difficulty = packetIn.getDifficulty();
        this.gameController.loadWorld(this.clientWorldController);
        this.gameController.thePlayer.dimension = packetIn.getDimension();
        this.gameController.displayGuiScreen(new GuiDownloadTerrain((NetHandlerPlayClient) (Object) this));
        this.gameController.thePlayer.setEntityId(packetIn.getEntityId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.gameController.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        this.gameController.playerController.setGameType(packetIn.getGameType());
        this.gameController.gameSettings.sendSettingsToServer();
        this.netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
        callbackInfo.cancel();
    }

    @Inject(method = "handleEntityMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z"))
    private void handleEntityMovementEvent(S14PacketEntity packetIn, final CallbackInfo callbackInfo) {
        final Entity entity = packetIn.getEntity(this.clientWorldController);

        if(entity != null)
            LiquidBounce.eventManager.callEvent(new EntityMovementEvent(entity));
    }

    @Inject(method = "onDisconnect", at = @At("HEAD")) 
    public void injectDisconnect(IChatComponent reason, CallbackInfo callbackInfo) {
        if (wdl.WDL.downloading) {
			wdl.WDL.stopDownload();

			try {
				Thread.sleep(3000L);
			} catch (Exception var3) {
				;
			}
		}
    }

    @Inject(method = "handleBlockAction", at = @At("RETURN"))
    public void handleBlockAction(S24PacketBlockAction packetIn, CallbackInfo callbackInfo) {
        wdl.WDLHooks.onNHPCHandleBlockAction((NetHandlerPlayClient) (Object) this, packetIn);
    }

    @Inject(method = "handleMaps", at = @At("RETURN"))
    public void handleMaps(S34PacketMaps packetIn, CallbackInfo callbackInfo) {
        wdl.WDLHooks.onNHPCHandleMaps((NetHandlerPlayClient) (Object) this, packetIn);
    }

    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    public void handleCustomPayload(S3FPacketCustomPayload packetIn, CallbackInfo callbackInfo) {
        wdl.WDLHooks.onNHPCHandleCustomPayload((NetHandlerPlayClient) (Object) this, packetIn);
    }

    @Inject(method = "handleChat", at = @At("RETURN"))
    public void handleChat(S02PacketChat packetIn, CallbackInfo callbackInfo) {
        wdl.WDLHooks.onNHPCHandleChat((NetHandlerPlayClient) (Object) this, packetIn);
    }
}
