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

//      VWAP IMPLEMENTATION
        double askVWAP = getAskVWAP(state);
        double bidVWAP = getBidVWAP(state);

        double askVWAPThreshold = askVWAP - (0.01 * askVWAP);
        double bidVWAPThreshold = bidVWAP + (0.01 * bidVWAP);

        //spread = lowest ask - highest bid
        final long spread = state.getAskAt(0).price - state.getBidAt(0).price;


        final long spreadThreshold = 3;

        //bid variables
        long bidQuantity = state.getBidAt(0).quantity;
        long bidPrice = state.getBidAt(0).price;
        final BidLevel currentBidLevel = state.getBidAt(0);


        //timestamps variables
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        final var activeOrders = state.getActiveChildOrders();
        final var allOrders = state.getChildOrders();


        //exit condition
        if (spread > spreadThreshold) {
            logger.info("[MYALGO] Exiting market, spread is too wide to trade! Current spread: " + spread);
            logger.info("[MYALGO] Have: " + totalOrderCount + " orders");

            return NoAction.NoAction;
        }

//        if order has been present ON PASSIVE SIDE for longer than 6 milliseconds, cancel the order
//        cancel logic
        if (totalOrderCount > 0 && !timestamps.isEmpty()) {
            for (int j = 0; j < timestamps.size(); j++ ){

                long timeDifference = ts.getTime() - timestamps.get(j).getTime();
                if (timeDifference > 60) {
                    logger.info("[MYALGO] Current timestamps " + timestamps);
                    logger.info("[MYALGO] Cancelling order at level " + j + " order is older than 1 minute: " + timestamps.get(j));
                    timestamps.remove(j);
                    return new CancelChildOrder(activeOrders.get(j)) ;

                } else {
                    logger.info("[MYALGO] No order to cancel ");

                }
            }
        }

        // If we have fewer than 2 child orders, we want to add new ones
        if (totalOrderCount < 3 ) {
            logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3" );

            final var option = activeOrders.stream().findFirst();


            //check if any askprice is less than askVWAP threshold
                long askQuantity = state.getAskAt(0).quantity;
                long askPrice = state.getAskAt(0).price;

                final AskLevel currentAskLevel = state.getAskAt(0);

//              create a buy order with larger quantity if ask price is rare (less than vwap threshold)
            if (askVWAPThreshold > askPrice) {
                logger.info("[MYALGO] Price is rare. VWAP: " + askVWAP + ", current price: " + askPrice);
                long rarePriceQuantity = askQuantity / 3;
                logger.info("[MYALGO] Creating BUY order: " + rarePriceQuantity + "@" + askPrice);

                if (bidPrice >= bidVWAPThreshold) {
                    logger.info("[MYALGO] VWAP Sell Condition Met. VWAP: " + bidVWAP + ", bid price: " + bidPrice);
                    timestamps.add(ts);

                    return new CreateChildOrder(Side.SELL, rarePriceQuantity, bidPrice);
                } else if (bidPrice >= bidVWAP) {
                    logger.info("[MYALGO] VWAP Sell Condition Met. Creating child sell order.");
                    logger.info("[MYALGO] Volume-Weighted Av Price is " + bidVWAP + "price: " + bidPrice);

                    timestamps.add(ts);
                    return new CreateChildOrder(Side.SELL, rarePriceQuantity, bidPrice);
                } else {
                    logger.info("[MYALGO] VWAP Sell Condition not Met. ");

                }
                return new CreateChildOrder(Side.BUY, rarePriceQuantity, askPrice);


                //create a buy order with normal quantity if ask price is good (between vwap and vwap threshold)
                }else if (askVWAP >= askPrice){
                    logger.info("[MYALGO] Volume-Weighted Av Price is " + askVWAP + "threshold: " + askVWAPThreshold + "price: " + askPrice);

                    long goodPriceQuantity = askQuantity / 5;

                    logger.info("[MYALGO]ORDER: " + goodPriceQuantity + "@" + askPrice + "created at: " + ts );
                    return new CreateChildOrder(Side.BUY, goodPriceQuantity, askPrice) ;
                }
            else {
                var childOrder = option.get();

                logger.info("[MYALGO] VWAP Sell Condition not Met. Cancelling buy order");
                return new CancelChildOrder(childOrder);
                }

         }

        logger.info("[MYALGO] Have: " + totalOrderCount + " child orders, no further orders needed. All orders: " + allOrders);
        logger.info("[MYALGO] ORDER COMPLETE");

        return NoAction.NoAction;
    }

    private static double getAskVWAP(SimpleAlgoState state) {
        //askVWAP logic
        List<Long> askPrices = new ArrayList<>();
        List<Long> askQuantities = new ArrayList<>();
        int askLevels = state.getAskLevels();


        long totalAskPriceByQuantities = 0;
        long totalAskQuantities = 0;

        for (int i = 0; i < askLevels ; i++) {
            long askQuantity = state.getAskAt(i).quantity;
            long askPrice = state.getAskAt(i).price;

            totalAskQuantities += askQuantity;
            totalAskPriceByQuantities += askQuantity * askPrice;
        }
        double askVWAP = totalAskPriceByQuantities / totalAskQuantities;
        return askVWAP;
    }
private static double getBidVWAP(SimpleAlgoState state) {
    //bidVWAP logic
    List<Long> bidPrices = new ArrayList<>();
    List<Long> bidQuantities = new ArrayList<>();
    int bidLevels = state.getBidLevels();


    long totalBidPriceByQuantities = 0;
    long totalBidQuantities = 0;

    for (int i = 0; i < bidLevels ; i++) {
        long bidQuantity = state.getBidAt(i).quantity;
        long bidPrice = state.getBidAt(i).price;

        totalBidQuantities += bidQuantity;
        totalBidPriceByQuantities += bidQuantity * bidPrice;
    }


    double bidVWAP = totalBidPriceByQuantities / totalBidQuantities;
    return bidVWAP;
    }
}