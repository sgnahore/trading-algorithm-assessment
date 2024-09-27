package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.action.CreateChildOrder;

import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.order.Side;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    List<Timestamp> timestamps = new ArrayList<>();


    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The current state of the order book is:\n" + orderBookAsString);
        var totalOrderCount = state.getChildOrders().size();
        logger.info("[MYALGO] In Algo Logic....");

        final String book = Util.orderBookToString(state);

        logger.info("[MYALGO] Algo Sees Book as:\n" + book);


        //static vwap logic
        double vwap = 111;
        double vwapThreshold = 0.01 * vwap;

        //spread variables
        final BidLevel highestBid = state.getBidAt(0);
        final AskLevel lowestAsk = state.getAskAt(0);
        final long spread = lowestAsk.price - highestBid.price;
        final long spreadThreshold = 3;

        //timestamps variables
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        final var activeOrders = state.getActiveChildOrders();
        final var option = activeOrders.stream().findFirst();
        logger.info("[MYALGO] Current time stamps " + timestamps);
        

            // If we have fewer than 3 child orders, we want to add new ones
            if (totalOrderCount < 3) {
                logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3");

                //exit condition
                if (spread > spreadThreshold) {
                    logger.info("[MYALGO] Exiting market, spread is too wide to trade! Current spread: " + spread);
                    return NoAction.NoAction;
                } else{
                    //check if any askprice is less than vwap threshold

                    for (int i = 0; i < state.getAskLevels(); i++) {
                    long askPrice = state.getAskAt(i).price;
                    long askQuantity = state.getAskAt(i).quantity;
                    final AskLevel currentAskLevel = state.getAskAt(i);


                    if (vwap - vwapThreshold >= askPrice) {
                        timestamps.add(ts);

                        logger.info("[MYALGO] Volume-Weighted Av Price is " + vwap);

                        logger.info("[MYALGO] Best " + currentAskLevel + ". This is more than 1% below ask VWAP, a steal! Creating child order at " + ts);
                        return new CreateChildOrder(Side.BUY, askQuantity, askPrice) ;

                        //if order older than one minute, cancel
                    } else if (!timestamps.isEmpty()) {
                        for (int j = 0; j < timestamps.size(); j++) {

                            var childOrder = option.get();

                            long timeDifference = ts.getTime() - timestamps.get(j).getTime();
                            if (timeDifference > 60_000) {
                                logger.info("[MYALGO] Cancelling order, timestamp older than 1 minute: " + timestamps.get(j));
                                return new CancelChildOrder(childOrder) ;
                            }
                        }
                    }
                    }
                }
            } else {
                logger.info("[MYALGO] Have: " + totalOrderCount + " child orders, no further orders needed.");

                return NoAction.NoAction;
            }
        logger.info("[MYALGO] ORDER COMPLETE");

        return NoAction.NoAction;
    }
}