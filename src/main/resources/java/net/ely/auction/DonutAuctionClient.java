package net.ely.auction;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutAuctionClient implements ClientModInitializer {
    public static KeyBinding openConfigKey;
    
    // Pattern exact pour le chat du Donut SMP : "Pseudo paid you $ Montant"
    private static final Pattern PAY_PATTERN = Pattern.compile("^([a-zA-Z0-9_]{3,16})\\s+paid\\s+you\\s+\\$\\s*([0-9.]+[a-zA-Z]?)");

    @Override
    public void onInitializeClient() {
        // Enregistrement de la touche de configuration (Touche 'O' par défaut)
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donut_auction.open",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_O,
                "category.donut_auction"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                client.setScreen(new AuctionConfigScreen());
            }
        });

        // Détection et lecture automatique des Millions (M) et Milliers (K) du Donut SMP
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, time) -> {
            if (!AuctionState.isActive) return;

            String text = message.getString().trim();
            String cleanText = text.replaceAll("(?i)§[0-9a-fk-or]", ""); // Nettoie les couleurs du serveur

            Matcher matcher = PAY_PATTERN.matcher(cleanText);
            if (matcher.find()) {
                try {
                    String player = matcher.group(1);
                    String rawAmount = matcher.group(2).toUpperCase();

                    double parsedAmount = 0;
                    if (rawAmount.endsWith("M")) {
                        parsedAmount = Double.parseDouble(rawAmount.replace("M", "")) * 1_000_000;
                    } else if (rawAmount.endsWith("K")) {
                        parsedAmount = Double.parseDouble(rawAmount.replace("K", "")) * 1_000;
                    } else if (rawAmount.endsWith("B")) {
                        parsedAmount = Double.parseDouble(rawAmount.replace("B", "")) * 1_000_000_000;
                    } else {
                        parsedAmount = Double.parseDouble(rawAmount);
                    }

                    long amount = (long) parsedAmount;

                    // L'offre doit être supérieure au prix minimum et battre le leader actuel
                    if (amount >= AuctionState.minBid && amount > AuctionState.currentBid) {
                        AuctionState.leader = player;
                        AuctionState.currentBid = amount;
                        
                        if (MinecraftClient.getInstance().player != null) {
                            MinecraftClient.getInstance().player.sendMessage(
                                    Text.literal("§d[ElyAuction] §aNouveau leader : " + player + " (" + rawAmount + ")"),
                                    false
                            );
                        }
                    }
                } catch (Exception e) {
                    // Évite que le jeu crash en cas de message mal formé
                }
            }
        });

        // Dessin de l'encadré violet (HUD) identique à ton screen
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!AuctionState.isActive) return;

            AuctionState.tick(); // Diminue le chrono d'une seconde

            int x = 10;
            int y = 10;

            int minutes = AuctionState.timeLeftSeconds / 60;
            int seconds = AuctionState.timeLeftSeconds % 60;
            String timeStr = String.format("%02d:%02d", minutes, seconds);

            // Rendu de la boîte de fond et des contours violets (Norme Mojang 1.21.11)
            context.fill(x, y, x + 160, y + 55, 0x90000000); 
            
            int borderCol = 0xFF4A0E4E; // Couleur violette sombre
            context.fill(x, y, x + 160, y + 1, borderCol);
            context.fill(x, y + 54, x + 160, y + 55, borderCol);
            context.fill(x, y, x + 1, y + 55, borderCol);
            context.fill(x + 159, y, x + 160, y + 55, borderCol);

            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

            // Affichage des textes colorés à l'écran
            context.drawText(renderer, "ELY AUCTION: IGN " + AuctionState.ign, x + 5, y + 4, 0xFFC71585, false);
            context.drawText(renderer, "In the Lead: " + AuctionState.leader, x + 5, y + 16, 0xFF55FFFF, false);
            context.drawText(renderer, "$" + AuctionState.formatAmount(AuctionState.currentBid), x + 5, y + 28, 0xFF55FF55, false);
            context.drawText(renderer, "Time Left: " + timeStr, x + 5, y + 40, 0xFF55FF55, false);
        });
    }
}
