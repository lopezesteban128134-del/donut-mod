package net.ely.auction;

public class AuctionState {
    public static String ign = "_hna";
    public static String leader = "Aucun";
    public static long currentBid = 0;
    public static long minBid = 1;
    public static int timeLeftSeconds = 60;
    public static boolean isActive = false;

    private static long lastTickTime = 0;

    public static void tick() {
        if (!isActive) return;

        long now = System.currentTimeMillis();
        if (now - lastTickTime >= 1000) {
            if (timeLeftSeconds > 0) {
                timeLeftSeconds--;
            } else {
                isActive = false;
            }
            lastTickTime = now;
        }
    }

    // Convertit les grands nombres en format abrégé propre au Donut SMP (ex: 40000000 -> 40M)
    public static String formatAmount(long amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000.0).replace(".0", "");
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0).replace(".0", "");
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000.0).replace(".0", "");
        return String.valueOf(amount);
    }
}
