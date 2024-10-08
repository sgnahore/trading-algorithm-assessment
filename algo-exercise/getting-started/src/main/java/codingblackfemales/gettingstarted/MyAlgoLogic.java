package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.action.CreateChildOrder;

import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.order.Side;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private long amountOfSharesOwned = 0; // Setting initial sharesOwned to 500
    private long profit = 0; // to track profit/loss based on trades
    private long totalSpendings = 0; // to track profit/loss based on trades
    private long totalEarnings = 0; // to track profit/loss based on trades

    @Override
    public Action evaluate(SimpleAlgoState state) {

        logger.info("[MYALGO] Current order book:\n" + Util.orderBookToString(state));

        //-------------------------------- DEFINING VARIABLES --------------------------
        // Count of existing child orders
        final var activeOrders = state.getActiveChildOrders();
        int totalOrderCount = state.getChildOrders().size();

        // Get the best (lowest) ask price and quantity
        AskLevel bestAsk = state.getAskAt(0);
        long askPrice = bestAsk.price;
        long askQuantity = bestAsk.quantity;

        // Get the best (highest) bid price
        BidLevel bestBid = state.getBidAt(0);
        long bestBidPrice = bestBid.price;

        // Calculate the average market price (VWAP) for asks
        double askVWAP = calculateVWAP(state, true);
        double bidVWAP = calculateVWAP(state, false);
        double askVWAPThreshold = askVWAP - (0.01 * askVWAP);    //define dynamic threshold for ask prices

        // CONSTANT
        final int MAX_BUY_ORDERS = 2;

        //define dynamic spread between best prices and a threshold
        long currentSpread = bestAsk.price - bestBid.price;
        long spreadThreshold = 400;

        //--------------------------------------------------------------------------------


        //-------------------------------- EXIT LOGIC ------------------------------------

        // Check if the spread is too wide to trade, if so exit market
        if (currentSpread > spreadThreshold) {
            logger.info("[MYALGO] Spread too wide, not trading. Spread: " + currentSpread);
            logger.info("[MYALGO] PROFIT: " + profit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings + " --- TOTAL ORDERS: " + totalOrderCount);

            return NoAction.NoAction;
        }
        //--------------------------------------------------------------------------------

        //-------------------------------- SELL LOGIC ------------------------------------
        //If we already have 2 buy orders and the price is equal to or more than bidVWAP, attempt to sell
        if (totalOrderCount == MAX_BUY_ORDERS && !activeOrders.isEmpty() ) {


            if (totalOrderCount <= MAX_BUY_ORDERS && bestBidPrice >= bidVWAP) {
                logger.info("[MYALGO] Total Orders: " + totalOrderCount);
                logger.info("[MYALGO] ACTIVE Orders: " + activeOrders);

                final var firstOrder = activeOrders.stream().findFirst(); //find the first order in the list

                var childOrder = firstOrder.get();
                long oldestOrderPrice = childOrder.getPrice();

                logger.info("[MYALGO] Attempting to sell since we have " + totalOrderCount + " buy orders.");

                long firstBuyOrderQuantity = childOrder.getQuantity();

                amountOfSharesOwned -= firstBuyOrderQuantity;
                totalEarnings += bestBidPrice * firstBuyOrderQuantity;
                profit = totalEarnings - totalSpendings;

                logger.info("[MYALGO] Creating a SELL order: " + firstBuyOrderQuantity + "@" + bestBidPrice);
                logger.info("[MYALGO] PROFIT: " + profit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings + " --- TOTAL ORDERS: " + totalOrderCount);

                return new CreateChildOrder(Side.SELL, firstBuyOrderQuantity, bestBidPrice);
            }

//            final var firstActiveOrder = activeOrders.stream().findFirst();
//            if (firstActiveOrder.isPresent()) {
//                ChildOrder orderToCancel = firstActiveOrder.get();
//                logger.info("[MYALGO] Cancelling order: " + orderToCancel);
//                profit = totalEarnings - totalSpendings;
//                logger.info("[MYALGO] PROFIT: " + profit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings);
//
//                return new CancelChildOrder(orderToCancel);
//            }
        }

        //--------------------------------------------------------------------------------

        //-------------------------------- BUY LOGIC -------------------------------------

        // Check if we can create new buy orders
        if (totalOrderCount < MAX_BUY_ORDERS) {
            logger .info("[MYALGO] Total Orders: " + totalOrderCount);
            logger.info("[MYALGO] ACTIVE Orders: " + activeOrders);


            // Check if the ask price is lower than the VWAP threshold
            if (askVWAPThreshold > askPrice ) {
                long rarePriceQuantity = askQuantity / 3;
                logger.info("[MYALGO] Price is rare. VWAP: " + askVWAP + ", current price: " + askPrice);

                amountOfSharesOwned += rarePriceQuantity;
                totalSpendings += askPrice * rarePriceQuantity;

                logger.info("[MYALGO] Creating BUY order: " + rarePriceQuantity + "@" + askPrice);
                profit = totalEarnings - totalSpendings;

                logger.info("[MYALGO] PROFIT: " + profit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings + " --- TOTAL ORDERS: " + totalOrderCount);

                return new CreateChildOrder(Side.BUY, rarePriceQuantity, askPrice);

            // Check if the ask price is below the VWAP price
            } else if  (askVWAP > askPrice) {
                long buyQuantity = askQuantity / 3;  // Buy a portion of available quantity
                amountOfSharesOwned += buyQuantity;
                totalSpendings += askPrice * buyQuantity;

                logger.info("[MYALGO] Creating a BUY order: " + buyQuantity + "@" + askPrice);
                return new CreateChildOrder(Side.BUY, buyQuantity, askPrice);
            }

    }
        profit = totalEarnings - totalSpendings;
        logger.info("[MYALGO] PROFIT: " + profit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings + " --- TOTAL ORDERS: " + totalOrderCount);
        return NoAction.NoAction;
    }
    //-------------------------------- HELPER METHOD -------------------------------------

    // Helper method to calculate VWAP (Volume-Weighted Average Price) for either ask side or bid side
    private double calculateVWAP(SimpleAlgoState state, boolean isAsk) {
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