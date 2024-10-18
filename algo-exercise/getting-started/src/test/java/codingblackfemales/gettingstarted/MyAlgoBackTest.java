package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import codingblackfemales.sotw.ChildOrder;



/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the spread (i.e. you BUY an order with a price which is == or > askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    @Test
    public void testExampleBackTest() throws Exception {
        //create a sample market data tick....
        send(createBuyAndSellTick());
        assertEquals(4, container.getState().getActiveChildOrders().size());
        var state = container.getState();
        long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        assertEquals(602, filledQuantity);

        //passed
//        send(createSpreadTooWide());
//        assertEquals(0, container.getState().getChildOrders().size());
// send(createBuyOkayPrice());
//        assertEquals(4, container.getState().getActiveChildOrders().size());


//
//        send(createNoBuyTick());
//        //simple assert to check we had 3 orders created
//        assertEquals(0, container.getState().getChildOrders().size());
//        send(createBuyButNoSellTick());
//        assertEquals(2, container.getState().getActiveChildOrders().size());



    }

}
