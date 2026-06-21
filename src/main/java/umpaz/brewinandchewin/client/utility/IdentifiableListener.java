package umpaz.brewinandchewin.client.utility;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface IdentifiableListener extends PreparableReloadListener {
    ResourceLocation getId();
}
