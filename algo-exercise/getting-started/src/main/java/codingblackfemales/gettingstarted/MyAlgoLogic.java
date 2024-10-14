package codingblackfemales.gettingstarted;

import codingblackfemales.action.*;
import codingblackfemales.algo.AlgoLogic;

import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.orderbook.OrderBook;

import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;

import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.order.Side;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;
public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    private VWAPCalculator vwapCalculator = new VWAPCalculator();
    private TradingContext tradingContext = new TradingContext();
    private OrderBook orderBook;
    private Double initialAskVWAP = null;
    private Double initialBidVWAP = null;


    @Override
    public Action evaluate(SimpleAlgoState state) {
        logger.info("[MYALGO] Current order book:\n" + Util.orderBookToString(state));

        // Variables for best ask and bid levels
        AskLevel bestAsk = state.getAskAt(0);
        BidLevel bestBid = state.getBidAt(0);

        long bestAskPrice = bestAsk.price;
        long askQuantity = bestAsk.quantity;
        long bestBidPrice = bestBid.price;

        // Calculate VWAP (Volume Weighted Average Price)
        double askVWAP = vwapCalculator.calculateVWAP(state, true);
        double bidVWAP = vwapCalculator.calculateVWAP(state, false);

        // Get active orders and count orders
        var activeOrders = state.getActiveChildOrders();
        int totalOrderCount = state.getChildOrders().size();
        long activeOrdersCount = state.getActiveChildOrders().stream().count();

        if (initialAskVWAP == null) {
            initialAskVWAP = askVWAP;
            logger.info("[MYALGO] Initial ask VWAP set to: " + initialAskVWAP);
        }

        if (initialBidVWAP == null) {
            initialBidVWAP = bidVWAP;
            logger.info("[MYALGO] Initial bid VWAP set to: " + initialBidVWAP);
        }

        double askVWAPLowerThreshold = initialAskVWAP * 0.95;


        // Count active buy orders
        List<ChildOrder> buyOrdersList = activeOrders.stream()
                .filter(order -> order.getSide().equals(Side.BUY))
                .collect(Collectors.toList());
        final long buyOrderCount = buyOrdersList.size();

        // Count active buy orders
        List<ChildOrder> sellOrdersList = activeOrders.stream()
                .filter(order -> order.getSide().equals(Side.SELL))
                .collect(Collectors.toList());
        final long sellOrderCount = sellOrdersList.size();

        // Get details of the spread
        long currentSpread = bestAsk.price - bestBid.price;
        long maxSpreadAllowed = 3;

        //EXIT LOGIC - if we haven't created any orders and the spread is too wide, then exit
        if (totalOrderCount == 0 && currentSpread > maxSpreadAllowed) {
            logger.info("[MYALGO] Spread too wide, not trading. Spread: " + currentSpread);
             return NoAction.NoAction;
        }

        //BUY AND SELL
        if (totalOrderCount <= 4){
            logger.info("[MYALGO] Total Orders: " + totalOrderCount);
            logger.info("[MYALGO] PROFIT: " + tradingContext.getTotalProfit() +
                    " --- TOTAL EARNED: " + tradingContext.getTotalEarnings() +
                    " --- TOTAL SPENT: " + tradingContext.getTotalSpendings() +
                    " --- TOTAL SHARES OWNED: " + tradingContext.getOwnedShares());

        // BUY LOGIC - if we have less than two buy orders and there
        if (buyOrderCount < 2) {
                logger.info("[MYALGO] VWAP: " + initialAskVWAP + ", current price: " + bestAskPrice);


            //if the best ask price is lower than the average price, buy the whole quantity
            if (askVWAPLowerThreshold > bestAskPrice) {

                // Update trading context with buy action
                tradingContext.addOwnedShares(askQuantity);
                tradingContext.addSpendings(bestAskPrice * askQuantity);

                logger.info("[MYALGO] Price is very good. Creating BUY order with whole quantity. Details: " + askQuantity + "@" + bestAskPrice);
                return new CreateChildOrder(Side.BUY, askQuantity, bestAskPrice);

            //if the best ask price is between the ask vwap and it's threshold
            } else if (initialAskVWAP >= bestAskPrice && bestAskPrice >= askVWAPLowerThreshold) {
                long goodPriceBuyQuantity = askQuantity / 2;

                tradingContext.addOwnedShares(goodPriceBuyQuantity);
                tradingContext.addSpendings(bestAskPrice * goodPriceBuyQuantity);

                logger.info("[MYALGO] Creating a BUY order: " + goodPriceBuyQuantity + "@" + bestAskPrice);
                return new CreateChildOrder(Side.BUY, goodPriceBuyQuantity, bestAskPrice);

            } else {
                logger.info("[MYALGO] Cannot create buy order, exiting market");
                return NoAction.NoAction;
            }
        }

        // SELL LOGIC
            // if we have two buy orders and we haven't made two sells, sell
        if (buyOrderCount == 2 && sellOrderCount <= 2) {

            ChildOrder childOrder = activeOrders.stream().findFirst().get();
            if (bestBidPrice >= initialBidVWAP) {

                logger.info("[MYALGO] Attempting to sell since we have " + buyOrderCount + " buy orders.");

                    long firstBuyOrderQuantity = childOrder.getQuantity();

                    tradingContext.subtractOwnedShares(firstBuyOrderQuantity);
                    tradingContext.addEarnings(bestBidPrice * firstBuyOrderQuantity);

                    logger.info("[MYALGO] Creating a SELL order: " + firstBuyOrderQuantity + "@" + bestBidPrice);
                    return new CreateChildOrder(Side.SELL, firstBuyOrderQuantity, bestBidPrice);
            }}
        }
        if (activeOrdersCount > 4) {

            ChildOrder lastOrder = activeOrders.get((int) activeOrdersCount - 1);

            tradingContext.subtractOwnedShares(lastOrder.getQuantity());
            tradingContext.subtractEarnings(lastOrder.getPrice() * lastOrder.getQuantity());

            logger.info("[MYALGO] Cancelling last order: " + lastOrder);
            return new CancelChildOrder(lastOrder);

        }

        logger.info("[MYALGO] FINAL: PROFIT: " + tradingContext.getTotalProfit() +
                " --- TOTAL EARNED: " + tradingContext.getTotalEarnings() +
                " --- TOTAL SPENT: " + tradingContext.getTotalSpendings() +
                " --- TOTAL SHARES OWNED: " + tradingContext.getOwnedShares());

        logger.info("trading complete with " + activeOrdersCount + " active orders");
        logger.info("all orders " + activeOrders.stream().collect(Collectors.toList()));
        return NoAction.NoAction;
    }}
