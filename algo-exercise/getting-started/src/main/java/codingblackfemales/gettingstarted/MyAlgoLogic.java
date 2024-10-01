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
        double askVWAPThreshold = askVWAP - (0.01 * askVWAP);

        //spread variables
        final BidLevel highestBid = state.getBidAt(0);
        final AskLevel lowestAsk = state.getAskAt(0);
        final long spread = lowestAsk.price - highestBid.price;
        final long spreadThreshold = 3;

        //timestamps variables
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        final var activeOrders = state.getActiveChildOrders();
        final var allOrders = state.getChildOrders();
        logger.info("[MYALGO] Current time stamps " + timestamps);


        final var option = activeOrders.stream().findFirst();
        final var potential = allOrders.stream().findFirst();

        //exit condition
        if (spread > spreadThreshold) {
            logger.info("[MYALGO] Exiting market, spread is too wide to trade! Current spread: " + spread);
            return NoAction.NoAction;
        }

        // If we have fewer than 2 child orders, we want to add new ones
        if (totalOrderCount < 2 ) {
            logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3" );


            //check if any askprice is less than askVWAP threshold
                //loop not required, ask price should be at level 0 - further set dynamic quantity
                long askQuantity = state.getAskAt(0).quantity;
                long askPrice = state.getAskAt(0).price;

                final AskLevel currentAskLevel = state.getAskAt(0);

                long bidQuantity = state.getBidAt(0).quantity;
                long bidPrice = state.getBidAt(0).price;
                final BidLevel currentBidLevel = state.getBidAt(0);


            //create a buy order if ask price is good
                if (askVWAP >= askPrice && askVWAPThreshold > askPrice ) {

                    logger.info("[MYALGO] Price is rare. Volume-Weighted Av Price is " + askVWAP + "and current price is: " + askPrice);
                long rarePriceQuantity = askQuantity / 3;
//                    timestamps.add(ts);

                    logger.info("[MYALGO]Creating child order at " + ts + "ORDER " + rarePriceQuantity + askPrice);
                    return new CreateChildOrder(Side.BUY, rarePriceQuantity, askPrice) ;

                }else if (askVWAP >= askPrice){
                logger.info("[MYALGO] AAAVolume-Weighted Av Price is " + askVWAP + "threshold: " + askVWAPThreshold + "price: " + askPrice);

                long goodPriceQuantity = askQuantity / 5;

                logger.info("[MYALGO]Creating child order at " + ts + " ORDER: " + goodPriceQuantity + "@" + askPrice);
                return new CreateChildOrder(Side.BUY, goodPriceQuantity, askPrice) ;


            } else {
                    logger.info("[MYALGO] Cannot trade, price too high ");
                }


        }
        //check if any bidprice is more than askVWAP threshold - TO BE FINALISED

//                    long bidPrice = state.getBidAt(0).price;
//                    long bidQuantity = state.getBidAt(0).quantity;
//
//                    //DO I NEED TO SET A CONDITION FOR SPREAD HERE?
//                    if (bidPrice >= bidVWAP && state.getBidLevels() > 0) {
//                        logger.info("[MYALGO] VWAP Sell Condition Met. Creating child sell order.");
//                        timestamps.add(ts);
//                        return new CreateChildOrder(Side.SELL, bidQuantity, bidPrice);
//                    } else {
//                        logger.info("[MYALGO] VWAP Sell Condition not Met. ");
//
//                }

//        if order has been present ON PASSIVE SIDE for longer than 6 milliseconds, cancel the order
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
        }
//        else {
//            logger.info("[MYALGO] Have: " + totalOrderCount + " child orders, no further orders needed.");
//
//            return NoAction.NoAction;
//        }
        logger.info("[MYALGO] ORDER COMPLETE");

        return NoAction.NoAction;
    }
}