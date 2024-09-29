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


        //vwap logic ASK SIDE

        List<Long> askPrices = new ArrayList<>();
        List<Long> askQuantities = new ArrayList<>();
        int askLevels = state.getAskLevels();


        long totalPriceByQuantities = 0;
        long totalQuantities = 0;

        for (int i = 0; i < askLevels ; i++) {
            long askQuantity = state.getAskAt(i).quantity;
            long askPrice = state.getAskAt(i).price;

            totalQuantities += askQuantity;
            totalPriceByQuantities += askQuantity * askPrice;
        }


        double vwap = totalPriceByQuantities / totalQuantities;
        double vwapThreshold = 0.01 * vwap;

        //spread variables
        final BidLevel highestBid = state.getBidAt(0);
        final AskLevel lowestAsk = state.getAskAt(0);
        final long spread = lowestAsk.price - highestBid.price;
        final long spreadThreshold = 5;

        //timestamps variables
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        final var activeOrders = state.getActiveChildOrders();
        logger.info("[MYALGO] Current time stamps " + timestamps);

        final var option = activeOrders.stream().findFirst();

        //exit condition
        if (spread > spreadThreshold) {
            logger.info("[MYALGO] Exiting market, spread is too wide to trade! Current spread: " + spread);
            return NoAction.NoAction;
        }

            // If we have fewer than 4 child orders, we want to add new ones
            if (totalOrderCount < 10 && option.isEmpty()) {
                logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3" );


                    //check if any askprice is less than vwap threshold
                for (int i = 0; i < askLevels; i++) {
                    long askQuantity = state.getAskAt(i).quantity;
                    long askPrice = state.getAskAt(i).price;

                    final AskLevel currentAskLevel = state.getAskAt(i);

                    long bidQuantity = state.getBidAt(i).quantity;
                    long bidPrice = state.getBidAt(i).price;
                    final BidLevel currentBidLevel = state.getBidAt(i);

                //create a buy order if ask price is good
                    if (vwap - vwapThreshold >= askPrice) {

                        logger.info("[MYALGO] Volume-Weighted Av Price is " + vwap);

                        logger.info("[MYALGO] Best " + currentAskLevel + ". This is more than 1% below ask VWAP, a steal! Creating child order at " + ts);
                        timestamps.add(ts);
                        return new CreateChildOrder(Side.BUY, askQuantity, askPrice) ;

                    } else {
                        logger.info("[MYALGO] Cannot trade, price too high ");
                    }


                    }

                //if bid price is good, create sell order
                }
//            for (int i = 0; i < state.getBidLevels(); i++) {
//                    long bidPrice = state.getBidAt(i).price;
//                    long bidQuantity = state.getBidAt(i).quantity;
//
//                    if (bidPrice >= vwap + vwapThreshold) {
//                        logger.info("[MYALGO] VWAP Sell Condition Met. Creating child sell order.");
//                        timestamps.add(ts);
//                        return new CreateChildOrder(Side.SELL, bidQuantity, bidPrice);
//                    }
//                }

                //if order has been present ON PASSIVE SIDE for longer than 6 milliseconds, cancel the order
                 if (!timestamps.isEmpty()) {
                    for (int j = 0; j < timestamps.size(); j++ ){

                        long timeDifference = ts.getTime() - timestamps.get(j).getTime();
                        if (timeDifference > 600) {
                            logger.info("[MYALGO] Cancelling order at level " + j + " order is older than 1 minute: " + timestamps.get(j));
                            return new CancelChildOrder(activeOrders.get(j)) ;

                        } else {
                            logger.info("[MYALGO] No order to cancel ");

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