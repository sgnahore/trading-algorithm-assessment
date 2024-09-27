package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.action.CreateChildOrder;

import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.order.Side;


import java.util.ArrayList;
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

        //EXTRA VARIABLES
        //getting individual ask level

        //an array to push all price x quantity objects
        //List<AskLevel> askLevels = new ArrayList<>();

        double vwap = 111.324;
        double vwapThreshold = 0.01 * vwap;


            // If we have fewer than 3 child orders, we want to add new ones
            if (totalOrderCount < 3) {

                //hard quoted quantity, to be made dynamic
                long quantity = 200;

                for (int i = 0; i < state.getAskLevels(); i++) {
                    final AskLevel farTouch = state.getAskAt(i);
                    //askLevels.add(farTouch);
                    logger.info("[MYALGO] Current level: " + farTouch);

                    long askPrice = state.getAskAt(i).getPrice();
                    //if price is less that vwap by 1%, buy
                    if (vwap > askPrice){
                        logger.info("[MYALGO] Volume-Weighted Av Price is " + vwap);
                        logger.info("[MYALGO] Current ask price " + askPrice + " is more than 1% below VWAP, creating child order at price " + askPrice + " for quantity " + quantity );
                        return new CreateChildOrder(Side.BUY, quantity, askPrice);
                    }
                }
            } else {
                logger.info("[MYALGO] Have: " + totalOrderCount + " child orders, no further orders needed.");

                return NoAction.NoAction;
            }

        return NoAction.NoAction;
    }
}