package me.adibasik;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class FarmLinkScreen extends Screen {

    private TextFieldWidget slot2Field;
    private TextFieldWidget delayField;
    private ButtonWidget applyButton;
    private ButtonWidget tapemouseToggleButton;

    private static final Map<String, int[]> SLOT_RANGES = new HashMap<>();
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int SUBTEXT_COLOR = 0xA0A0A0;
    private static final int HIGHLIGHT_COLOR = 0x55FF55;
    private static final int ERROR_COLOR = 0xFF5555;
    private static final int BACKGROUND_COLOR = 0x6B000000;



    protected FarmLinkScreen() {
        super(Text.of("Auto Reconnector Configuration").copy().formatted(Formatting.BOLD));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        // Slot field
        slot2Field = new TextFieldWidget(textRenderer, centerX - 100, y, 200, 20,
                Text.of("Slot").copy().formatted(Formatting.GRAY));
        slot2Field.setMaxLength(2);
        slot2Field.setText(String.valueOf(FarmLink.COMPASS_SLOT_INDEX2)); // Initial value
        addDrawableChild(slot2Field);

        // Delay field
        delayField = new TextFieldWidget(textRenderer, centerX - 100, y + 40, 200, 20,
                Text.of("Delay (ms)").copy().formatted(Formatting.GRAY));
        delayField.setMaxLength(5);
        delayField.setText(String.valueOf(FarmLink.delayMs));
        addDrawableChild(delayField);

        // Apply button
        applyButton = ButtonWidget.builder(Text.of("Apply").copy().formatted(Formatting.GREEN), button -> applySettings())
                .dimensions(centerX - 100, y + 80, 200, 20)
                .build();
        addDrawableChild(applyButton);

        // Tapemouse toggle button
        tapemouseToggleButton = ButtonWidget.builder(getTapemouseLabel(), button -> {
            FarmLink.tapemouseEnabled = !FarmLink.tapemouseEnabled;
            button.setMessage(getTapemouseLabel());
        }).dimensions(centerX - 100, y + 110, 200, 20).build();
        addDrawableChild(tapemouseToggleButton);

        // Close button
        addDrawableChild(ButtonWidget.builder(Text.of("Close").copy().formatted(Formatting.RED), button -> close())
                .dimensions(centerX - 100, y + 140, 200, 20)
                .build());
    }

    private void applySettings() {
        try {
            int grifSlot = Integer.parseInt(slot2Field.getText());
            FarmLink.COMPASS_SLOT_INDEX2 = convertGrifToSlot(grifSlot);
            FarmLink.delayMs = Integer.parseInt(delayField.getText());
            MinecraftClient.getInstance().setScreen(null);
        } catch (NumberFormatException e) {
            applyButton.setMessage(Text.of("Invalid input!").copy().formatted(Formatting.RED));
        }
    }

    private int convertGrifToSlot(int grifSlot) {
        for (int[] values : SLOT_RANGES.values()) {
            int startSlot = values[2];
            int endSlot = values[3];
            if (grifSlot >= startSlot && grifSlot <= endSlot) {
                return values[0] + (grifSlot - startSlot);
            }
        }
        return grifSlot; // fallback
    }

    private Text getTapemouseLabel() {
        return FarmLink.tapemouseEnabled ?
                Text.of("Tapemouse: ON").copy().formatted(Formatting.GREEN) :
                Text.of("Tapemouse: OFF").copy().formatted(Formatting.RED);
    }

    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int y = this.height / 4;

        context.fill(centerX - 110, y - 40, centerX + 110, y + 180, BACKGROUND_COLOR);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.of("Auto Reconnector").copy().formatted(Formatting.BOLD, Formatting.GOLD),
                centerX, y - 30, TEXT_COLOR);

        context.drawTextWithShadow(textRenderer,
                Text.of("Slot (0-48)").copy().formatted(Formatting.YELLOW),
                centerX - 100, slot2Field.getY() - 12, SUBTEXT_COLOR);

        context.drawTextWithShadow(textRenderer,
                Text.of("Action Delay (ms)").copy().formatted(Formatting.YELLOW),
                centerX - 100, delayField.getY() - 12, SUBTEXT_COLOR);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
