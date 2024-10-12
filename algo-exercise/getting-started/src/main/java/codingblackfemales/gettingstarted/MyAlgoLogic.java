package codingblackfemales.gettingstarted;

import codingblackfemales.action.*;
import codingblackfemales.algo.AlgoLogic;

import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.orderbook.OrderBook;
import codingblackfemales.gettingstarted.VWAPCalculator;

import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.order.Side;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    private long totalProfit = 0;
    public long ownedShares = 0; // Setting initial sharesOwned to 500    private long totalProfit = 0; // to track totalProfit/loss based on trades
    private long totalSpendings = 0; // to track totalProfit/loss based on trades
    private long totalEarnings = 0; // to track totalProfit/loss based on trades
    private VWAPCalculator vwapCalculator = new VWAPCalculator();


    @Override
    public Action evaluate(SimpleAlgoState state) {

        logger.info("[MYALGO] Current order book:\n" + Util.orderBookToString(state));
        final String book = Util.orderBookToString(state);

        //-------------------------------- DEFINING VARIABLES --------------------------
        // Count of existing child orders
        final var activeOrders = state.getActiveChildOrders();
        int totalOrderCount = state.getChildOrders().size();

        // Best ask and Best bid
        AskLevel bestAsk = state.getAskAt(0);
        BidLevel bestBid = state.getBidAt(0);

        long askPrice = bestAsk.price;
        long askQuantity = bestAsk.quantity;
        long bestBidPrice = bestBid.price;

        // Calculate the average market price (VWAP) for ask and
        double askVWAP = vwapCalculator.calculateVWAP(state, true);
        double bidVWAP = vwapCalculator.calculateVWAP(state, false);
        double askVWAPThreshold = askVWAP * 0.99;
        // CONSTANT
        Stream<ChildOrder> buyOrders = activeOrders.stream()
                .filter(order -> order.getSide()
                        .equals(Side.BUY));
        List<ChildOrder> buyOrdersList = buyOrders.collect(Collectors.toList());

        final int maxBuyOrders = 2;
        final int maxTotalOrders = 4;
        final long buyOrderCount = buyOrdersList.stream().count();



        //define dynamic spread between best prices and a threshold
        long currentSpread = bestAsk.price - bestBid.price;
        long maxSpreadAllowed = 3;

        //---------------------------------CANCEL LOGIC-----------------------------------------------

        //-------------------------------- EXIT LOGIC ------------------------------------

        // Before entering the market Check if the spread is too wide to trade, if so exit market
        if (totalOrderCount == 0 && currentSpread > maxSpreadAllowed) {
            logger.info("[MYALGO] Spread too wide, not trading. Spread: " + currentSpread);
            logger.info("[MYALGO] PROFIT: " + totalProfit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings);
            return NoAction.NoAction;
        }

        if (totalOrderCount > 4){
            return NoAction.NoAction;
        }





        //-------------------------------- BUY LOGIC -------------------------------------

        if (buyOrderCount < maxBuyOrders) {

            logger .info("[MYALGO] Total Orders: " + totalOrderCount);
            logger.info("[MYALGO] ACTIVE Orders: " + activeOrders);


            // Check if the ask price is lower than the VWAP threshold
            if (askVWAPThreshold > askPrice ) {
                logger.info("[MYALGO] Price is rare. VWAP: " + askVWAP + ", current price: " + askPrice);

                ownedShares += askQuantity;
                totalSpendings += askPrice * askQuantity;

                logger.info("[MYALGO] Creating BUY order: " + askQuantity + "@" + askPrice);
                totalProfit = totalEarnings - totalSpendings;

                logger.info("[MYALGO] PROFIT: " + totalProfit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings);

                return new CreateChildOrder(Side.BUY, askQuantity, askPrice);

              // Check if the ask price is below the VWAP price
            } else if  (askVWAP > askPrice && askPrice > askVWAPThreshold) {
                long goodPriceBuyQuantity = askQuantity / 2;  // Buy a portion of available quantity
                ownedShares += goodPriceBuyQuantity;
                totalSpendings += askPrice * goodPriceBuyQuantity;

                logger.info("[MYALGO] Creating a BUY order: " + goodPriceBuyQuantity + "@" + askPrice);
                return new CreateChildOrder(Side.BUY, goodPriceBuyQuantity, askPrice);
            }

        }
        //--------------------------------------------------------------------------------

        //-------------------------------- SELL LOGIC ------------------------------------

        //We only want to sell when we've reached two buy orders and the
        if (buyOrderCount == 2) {

            //should i keep selling if the price is good?
            if (bestBidPrice >= bidVWAP) {
                logger.info("[MYALGO] Total Orders: " + totalOrderCount);
                logger.info("[MYALGO] Attempting to sell since we have " + totalOrderCount + " buy orders.");

                final var firstOrder = activeOrders.stream().findFirst(); //find the first order in the list
                var childOrder = firstOrder.get();
                long firstBuyOrderQuantity = childOrder.getQuantity();

                ownedShares -= firstBuyOrderQuantity;
                totalEarnings += bestBidPrice * firstBuyOrderQuantity;
                totalProfit = totalEarnings - totalSpendings;

                logger.info("[MYALGO] Creating a SELL order: " + firstBuyOrderQuantity + "@" + bestBidPrice);

                return new CreateChildOrder(Side.SELL, firstBuyOrderQuantity, bestBidPrice);
            }
        }

        totalProfit = totalEarnings - totalSpendings;

        logger.info("[MYALGO] PROFIT: " + totalProfit + " --- TOTAL EARNED: " + totalEarnings + " --- TOTAL SPENT: " + totalSpendings);
        return NoAction.NoAction;
    }}