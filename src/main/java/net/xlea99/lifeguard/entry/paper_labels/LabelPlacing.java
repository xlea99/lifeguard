package net.xlea99.lifeguard.entry.paper_labels;

import net.mehvahdjukaar.labels.LabelsMod;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Paper places Labels' labels ({@link PaperLabels}, {@code mixin.paper_labels}).
 *
 * <p>Paper's use on a block is handed to Labels' own label item, so the label goes up exactly as Labels places it, and
 * uses up the paper. Paper is a plain item, so it comes after the block in the click order: a chest opens unless the
 * player sneaks, as with string or a torch. Broken and picked labels give paper.
 */
public final class LabelPlacing {
    private LabelPlacing() {
    }

    /** Paper used on a block: places a label there if one fits, otherwise does nothing, as paper did. */
    public static InteractionResult place(UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.PASS;
        InteractionResult result = LabelsMod.LABEL_ITEM.get().useOn(context);
        // A label that doesn't fit stays paper's quiet pass, so the other hand still gets its turn.
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }
}
