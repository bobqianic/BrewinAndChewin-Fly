package umpaz.brewinandchewin.fabric.registry;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;

public class BnCAttachments {
    public static final AttachmentType<RagingAttachment> RAGING = AttachmentRegistry.createPersistent(RagingAttachment.ID, RagingAttachment.CODEC);
    public static final AttachmentType<TipsyHeartsAttachment> TIPSY_HEARTS =  AttachmentRegistry.createPersistent(TipsyHeartsAttachment.ID, TipsyHeartsAttachment.CODEC);

    public static void registerAll() {}
}
