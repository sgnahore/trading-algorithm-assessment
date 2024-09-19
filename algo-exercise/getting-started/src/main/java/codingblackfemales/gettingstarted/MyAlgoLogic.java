package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.order.Side;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The current state of the order book is:\n" + orderBookAsString);
        var totalOrderCount = state.getChildOrders().size();

        //exit condition...
        final var currentOrders = state.getActiveChildOrders();
        BidLevel firstBid = state.getBidAt(0);
        BidLevel finalBid = state.getBidAt(state.getBidLevels() - 1);
        AskLevel firstAsk = state.getAskAt(0);
        AskLevel finalAsk = state.getAskAt(state.getAskLevels() - 1);
        final long spread = finalAsk.getPrice() - finalBid.getPrice();
        final long spreadThreshold = 2;

        if (totalOrderCount > 5 && spread < spreadThreshold) {
            logger.info("[MYALGO] Exiting market, spread is too wide to trade! Current spread: " + spread);

            return NoAction.NoAction;

        }

//        long vwap = totalPriceVolume / totalQuantity;

        List<Long> bidPrices = new ArrayList<>();
        List<Long> bidQuantities = new ArrayList<>();

        int availableBidLevels = Math.min(5, state.getBidLevels());  // Limit to 5 or the number of available bid levels


        double totalValue = 0.0; // Initialize total value
        long totalQuantity = 0;

        // Add the first 5 bid levels (or fewer, if fewer exist)
        for (int i = 0; i < availableBidLevels; i++) {
            BidLevel eachPrice = state.getBidAt(i);
            BidLevel eachQuantity = state.getBidAt(i);
            final long prices = eachPrice.getPrice();
            bidPrices.add(prices);

            final long quantities = eachQuantity.getQuantity();
            bidQuantities.add(quantities);

            final long value = prices * quantities;
totalValue += value;
totalQuantity += bidQuantities.get(i);


        }
//        for (int i = 0; i < bidPrices.size(); i++) {
//            totalValue += bidPrices.get(i) * bidQuantities.get(i); // Sum up (price × quantity)
//            totalQuantity += bidQuantities.get(i);
//        }

        double vwap = 0.0;
        if (totalQuantity != 0) {
            vwap = totalValue / totalQuantity;
        }
        logger.info("[MYALGO] VWAP: " + vwap);

        //cancel order if price is too high
        if (!currentOrders.isEmpty()) {

            final var firstOrder = currentOrders.stream().findFirst();
            logger.info("[MYALGO] Current order details: ID:" + firstOrder.get().getOrderId() + "Price: " + firstOrder.get().getPrice());

                var childOrder = firstOrder.get();
                //stoploss condition
                final long threshold = 200;

            if (childOrder.getPrice() > threshold && childOrder.getQuantity() < 100) {
                logger.info("[MYALGO] Cancelling order: " + "price: " + childOrder.getPrice() + " quantity: " + childOrder.getQuantity() + " as it's above the price threshold: " + threshold);
                return new CancelChildOrder(childOrder);

              } else {
                logger.info("[MYALGO] Current order details: " + childOrder);

                final long quantityThreshold = 90;

                // Check if the quantity exceeds the threshold
                if (childOrder.getQuantity() < quantityThreshold) {
                    logger.info("[MYALGO] Cancelling order: " + "price: " + childOrder.getPrice() + " quantity: " + childOrder.getQuantity() + " as it's above the quantity threshold: " + quantityThreshold);
                    return new CancelChildOrder(childOrder);
                }
            }
        } else {
            //add take profit condition here

            logger.info("[MYALGO] Current bid level;" + firstBid);
            logger.info("[MYALGO] Last bid is ;" + finalBid);


            logger.info("[MYALGO] There are currently " + state.getBidLevels() + " bids present");

            final long cost = firstBid.price;
            final long amount = firstBid.quantity;
            logger.info("[MYALGO] Adding order for" + amount + "@" + cost);
            return new CreateChildOrder(Side.BUY, amount, cost);
                }
        return NoAction.NoAction;
            }

        }
