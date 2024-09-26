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
import java.util.List;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The current state of the order book is:\n" + orderBookAsString);
        var totalOrderCount = state.getChildOrders().size();
        logger.info("[MYALGO] In Algo Logic....");

        final String book = Util.orderBookToString(state);

        logger.info("[MYALGO] Algo Sees Book as:\n" + book);

        final AskLevel farTouch = state.getAskAt(0);

        //take as much as we can from the far touch....
        long quantityThreshold = 101;
//        long quantity = farTouch.quantity;
//        long price = farTouch.price;

        List<AskLevel> askLevels = state.getAskLevels();
//        List<int> levels = state.getAskLevels();
        // Iterate over each ask level
        for (AskLevel askLevel : askLevels) {
//            int quantity = askLevel.quantity;
//            long price = askLevel.price;

            logger.info("[MYALGO] all levels: " + askLevel);

            // If we have fewer than 3 child orders, we want to add new ones
            if (totalOrderCount < 3) {
//                if (quantity > quantityThreshold) {
//                    logger.info("[MYALGO] Quantity " + quantity + " is above threshold, creating child order at " + price + " using given quantity");
//                    return new CreateChildOrder(Side.BUY, quantity, price);
//                } else {
//                    // If the quantity is less than or equal to the threshold, create an order with the threshold quantity
//                    logger.info("[MYALGO] Quantity is below threshold, creating child order using threshold: " + quantityThreshold + " @ " + price);
//                    return new CreateChildOrder(Side.BUY, quantityThreshold, price);
//                }
            } else {
                logger.info("[MYALGO] Have: " + totalOrderCount + " child orders, no further orders needed.");
                return NoAction.NoAction;
            }
        }

        // If no action is taken within the loop, return NoAction
        return NoAction.NoAction;
    }
}