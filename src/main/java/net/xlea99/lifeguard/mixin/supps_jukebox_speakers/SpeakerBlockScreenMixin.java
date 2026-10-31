package net.xlea99.lifeguard.mixin.supps_jukebox_speakers;

import net.mehvahdjukaar.supplementaries.client.screens.SpeakerBlockScreen;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.xlea99.lifeguard.entry.supps_jukebox_speakers.JukeboxSpeakers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A jukebox's speaker's menu is just its range ({@link JukeboxSpeakers}): no message, no mode. Both are only hidden, so
 * they're kept as they were for if the speaker is moved off the jukebox.
 */
@Mixin(SpeakerBlockScreen.class)
public abstract class SpeakerBlockScreenMixin extends Screen {
    @Unique
    private static final Component LIFEGUARD$JUKEBOX = Component.translatable("gui.lifeguard.speaker_block.jukebox");

    @Shadow
    private EditBox editBox;
    @Shadow
    private Button modeBtn;
    @Shadow
    @Final
    private SpeakerBlockTile tileSpeaker;

    @Unique
    private boolean lifeguard$jukebox;

    protected SpeakerBlockScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void lifeguard$jukeboxSpeaker(CallbackInfo ci) {
        this.lifeguard$jukebox = this.tileSpeaker.getLevel() != null
            && JukeboxSpeakers.linked(this.tileSpeaker.getLevel(), this.tileSpeaker.getBlockPos());
        if (!this.lifeguard$jukebox)
            return;
        this.editBox.visible = false;
        this.editBox.setFocused(false);
        this.modeBtn.visible = false;
        this.setFocused(null);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void lifeguard$jukeboxCaption(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (this.lifeguard$jukebox)
            graphics.drawCenteredString(this.font, LIFEGUARD$JUKEBOX, this.width / 2, this.height / 4 + 40, 0xA0A0A0);
    }
}
