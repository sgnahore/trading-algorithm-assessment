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
        send(createTick());
////        //simple assert to check we had 1 order created
        assertEquals(4, container.getState().getChildOrders().size());

        //when: market data moves towards us
//        send(createTick2());
//
//////        //then: get the state
//        var state = container.getState();
//        long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
////
////        //and: check that our algo state was updated to reflect our fills when the market data
//        assertEquals(434, filledQuantity);

//        send(createTightSpreadTick());
        //simple assert to check we had 3 orders created
//        assertEquals(3, container.getState().getChildOrders().size());
//        send(testTick());


    }

}
