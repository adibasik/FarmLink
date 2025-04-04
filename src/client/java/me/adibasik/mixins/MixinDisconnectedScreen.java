package me.adibasik.mixins;

import me.adibasik.FarmLink;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// mixin inheritance due to render method is no longer in DisconnectedScreen class
// refers to: https://www.fabricmc.net/wiki/tutorial:mixinheritance
@Mixin(DisconnectedScreen.class)
public class MixinDisconnectedScreen extends MixinScreen {
    private static final int FONT_HEIGHT = 9;

    //ref: https://fabricmc.net/wiki/tutorial:mixin_injects
    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (FarmLink.lastestServerEntry == null) return;
        if (FarmLink.disconnectTick == 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;
        if (mc.currentScreen == null) return;
        int width = mc.currentScreen.width;
        int height = mc.currentScreen.height;
    }
}
