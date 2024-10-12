package codingblackfemales.gettingstarted;

import codingblackfemales.sotw.SimpleAlgoState;

public class VWAPCalculator {

    // Method to calculate VWAP (Volume-Weighted Average Price) for either ask side or bid side
    public static double calculateVWAP(SimpleAlgoState state, boolean isAsk) {
        long totalPriceByQuantity = 0;
        long totalQuantity = 0;
        int levels = isAsk ? state.getAskLevels() : state.getBidLevels();

        for (int i = 0; i < levels; i++) {
            long price = isAsk ? state.getAskAt(i).price : state.getBidAt(i).price;
            long quantity = isAsk ? state.getAskAt(i).quantity : state.getBidAt(i).quantity;
            totalPriceByQuantity += price * quantity;
            totalQuantity += quantity;
        }

        // Avoid division by zero if no quantities exist
        if (totalQuantity == 0) return 0;

        return Math.floor(totalPriceByQuantity / totalQuantity);
    }
}
