package net.xlea99.lifeguard.mixin.betterf3_lost_f3_info;

import me.cominixo.betterf3.config.GeneralOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debugchart.BandwidthDebugChart;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.gui.components.debugchart.PingDebugChart;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.entry.betterf3_lost_f3_info.LostF3Info;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * BetterF3 cancels the F3 drawing lambda at its head (order 2000) to draw its own text; the lambda also posts NeoForge's
 * DebugText event and draws the F3+2 / F3+3 charts. This runs first (order 500: also before BetterF3's font scaling at
 * the same head), does those two jobs, and lets BetterF3 carry on. With BetterF3's "disable mod" on, BetterF3 cancels
 * nothing and vanilla's lambda does it all, so this does nothing.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private boolean renderFpsCharts;
    @Shadow
    private boolean renderNetworkCharts;
    @Shadow
    @Final
    private LocalSampleLogger tickTimeLogger;
    @Shadow
    @Final
    private FpsDebugChart fpsChart;
    @Shadow
    @Final
    private TpsDebugChart tpsChart;
    @Shadow
    @Final
    private PingDebugChart pingChart;
    @Shadow
    @Final
    private BandwidthDebugChart bandwidthChart;

    @Shadow
    protected abstract List<String> getGameInformation();

    @Shadow
    protected abstract List<String> getSystemInformation();

    @Inject(method = "lambda$render$2", at = @At("HEAD"), order = 500)
    private void lifeguard$beforeBetterF3(GuiGraphics guiGraphics, CallbackInfo ci) {
        LostF3Info.frameEnded();
        if (GeneralOptions.disableMod)
            return;
        List<String> left = getGameInformation();
        List<String> right = getSystemInformation();
        List<String> leftBefore = List.copyOf(left);
        List<String> rightBefore = List.copyOf(right);
        NeoForge.EVENT_BUS.post(new CustomizeGuiOverlayEvent.DebugText(minecraft.getWindow(), guiGraphics,
            minecraft.getTimer(), left, right));
        LostF3Info.collected(leftBefore, left, rightBefore, right);

        // Vanilla's chart drawing, verbatim.
        if (renderFpsCharts) {
            int width = guiGraphics.guiWidth();
            int half = width / 2;
            fpsChart.drawChart(guiGraphics, 0, fpsChart.getWidth(half));
            if (tickTimeLogger.size() > 0) {
                int tpsWidth = tpsChart.getWidth(half);
                tpsChart.drawChart(guiGraphics, width - tpsWidth, tpsWidth);
            }
        }
        if (renderNetworkCharts) {
            int width = guiGraphics.guiWidth();
            int half = width / 2;
            if (!minecraft.isLocalServer())
                bandwidthChart.drawChart(guiGraphics, 0, bandwidthChart.getWidth(half));
            int pingWidth = pingChart.getWidth(half);
            pingChart.drawChart(guiGraphics, width - pingWidth, pingWidth);
        }
    }

    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void lifeguard$feedLeft(CallbackInfoReturnable<List<String>> cir) {
        LostF3Info.feedLeft(cir.getReturnValue());
    }

    @Inject(method = "getSystemInformation", at = @At("RETURN"))
    private void lifeguard$feedRight(CallbackInfoReturnable<List<String>> cir) {
        LostF3Info.feedRight(cir.getReturnValue());
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void lifeguard$frameEnded(GuiGraphics guiGraphics, CallbackInfo ci) {
        LostF3Info.frameEnded();
    }
}
