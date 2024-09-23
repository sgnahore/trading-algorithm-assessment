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

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The current state of the order book is:\n" + orderBookAsString);
        var totalOrderCount = state.getChildOrders().size();

        //exit condition...
        //to define the spread
        BidLevel finalBid = state.getBidAt(state.getBidLevels() - 1);
        AskLevel finalAsk = state.getAskAt(state.getAskLevels() - 1);
        final long spread = finalAsk.getPrice() - finalBid.getPrice();

        //hard coded spread for reference, to be made dynamic
        final long spreadThreshold = 10;

        //if there are active orders and the spread is greater than the threshold
        if (totalOrderCount > 0 && spread > spreadThreshold) {
            logger.info("[MYALGO] Exiting market, spread is too wide to trade! Current spread: " + spread);
            return NoAction.NoAction;
        }


        //extra variables for later
        final var currentOrders = state.getActiveChildOrders();
        BidLevel firstBid = state.getBidAt(0);
        AskLevel firstAsk = state.getAskAt(0);


        //vwap logic
        int availableBidLevels = Math.min(5, state.getBidLevels());  // Limit to 5 or the number of available bid levels
        double totalVolume = 0.0;
        long totalQuantity = 0;

        for (int i = 0; i < availableBidLevels; i++) {
            BidLevel bidLevel = state.getBidAt(i);

            final long prices = bidLevel.getPrice();
            final long quantities = bidLevel.getQuantity();

            final long value = prices * quantities;
            totalVolume += value;
            totalQuantity += quantities;
        }

        double vwap = 0.0;
        if (totalQuantity != 0) {
            vwap = totalVolume / totalQuantity;
        }
        final long vwapThreshold = 150;

        logger.info("[MYALGO] The current volume weighted average price is: " + vwap);

        if (!currentOrders.isEmpty() && state.getChildOrders().size() < 3) {

            //cancel order if price is too high

            // Iterate through all active orders
            for (var childOrder : currentOrders) {
//                if() {
                //stoploss condition
//
//            if (childOrder.getPrice() > threshold && childOrder.getQuantity() < 100) {
//                logger.info("[MYALGO] Cancelling order as quantity too low and threshold too high. \n order details: " + "price: " + childOrder.getPrice() + " quantity: " + childOrder.getQuantity() + " as it's above the price threshold: " + threshold);
//                return new CancelChildOrder(childOrder);
//              } else {
                logger.info("[MYALGO] Evaluating order: ID:" + childOrder.getOrderId() + " Price: " +
                        childOrder.getPrice() + " Quantity: " + childOrder.getQuantity());

                final long quantityThreshold = 80;

                // Check if the quantity is below the threshold
                if (childOrder.getQuantity() < quantityThreshold) {
                    logger.info("[MYALGO] Cancelling order as quantity is less than threshold: " + "price: " + childOrder.getPrice() + " quantity: " + childOrder.getQuantity() + " as it's above the quantity threshold: " + quantityThreshold);
                    return new CancelChildOrder(childOrder);
                }

                //add logic
                else if (vwap <= vwapThreshold) {

                    //add take profit condition here

                    logger.info("[MYALGO] There are currently " + state.getBidLevels() + " bids present");

                    final long cost = firstBid.price;
                    final long amount = firstBid.quantity;
                    logger.info("[MYALGO] Adding order for" + amount + "@" + cost);
                    return new CreateChildOrder(Side.BUY, amount, cost);
                }
            }
        }
        logger.info("[MYALGO] ORDERS COMPLETE");

        return NoAction.NoAction;
    }
}
