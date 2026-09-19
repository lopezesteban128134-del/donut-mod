package net.ely.auction;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TextFieldWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.text.Text;

public class AuctionConfigScreen extends Screen {
    private TextFieldWidget ignField;
    private TextFieldWidget timeField;
    private TextFieldWidget minBidField;

    public AuctionConfigScreen() {
        super(Text.literal("Configuration de l'Enchère"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Configuration du champ IGN
        ignField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 60, 200, 20, Text.literal(""));
        ignField.setValue(AuctionState.ign);
        this.addSelectableChild(ignField);

        // Configuration du champ Temps (en secondes)
        timeField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 20, 200, 20, Text.literal(""));
        timeField.setValue(String.valueOf(AuctionState.timeLeftSeconds));
        this.addSelectableChild(timeField);

        // Configuration du champ Enchère Minimum
        minBidField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY + 20, 200, 20, Text.literal(""));
        minBidField.setValue(String.valueOf(AuctionState.minBid));
        this.addSelectableChild(minBidField);

        // Bouton de validation pour démarrer ou interrompre l'enchère
        String btnText = AuctionState.isActive ? "§cARRÊTER L'ENCHÈRE" : "§aLANCER L'ENCHÈRE";
        this.addRenderableWidget(Button.builder(Text.literal(btnText), button -> {
            if (AuctionState.isActive) {
                AuctionState.isActive = false;
            } else {
                try {
                    AuctionState.ign = ignField.getValue();
                    AuctionState.timeLeftSeconds = Integer.parseInt(timeField.getValue());
                    AuctionState.minBid = Long.parseLong(minBidField.getValue());
                    AuctionState.currentBid = 0;
                    AuctionState.leader = "Aucun";
                    AuctionState.isActive = true;
                } catch (NumberFormatException e) {
                    // Ignoré si l'entrée est invalide
                }
            }
            // MODIFICATION : Ferme proprement le menu instantanément sous Minecraft 1.21.11
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
        }).bounds(centerX - 100, centerY + 55, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        context.drawCenteredString(this.textRenderer, "Configuration de l'Enchère Donut SMP", centerX, centerY - 85, 0xFFFFFF);
        context.drawString(this.textRenderer, "Pseudo de l'enchère (IGN) :", centerX - 100, centerY - 72, 0xA0A0A0, false);
        context.drawString(this.textRenderer, "Temps de l'enchère (en secondes) :", centerX - 100, centerY - 32, 0xA0A0A0, false);
        context.drawString(this.textRenderer, "Enchère minimum (Chiffre brut, ex: 1000000) :", centerX - 100, centerY + 8, 0xA0A0A0, false);
        
        ignField.render(context, mouseX, mouseY, delta);
        timeField.render(context, mouseX, mouseY, delta);
        minBidField.render(context, mouseX, mouseY, delta);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
