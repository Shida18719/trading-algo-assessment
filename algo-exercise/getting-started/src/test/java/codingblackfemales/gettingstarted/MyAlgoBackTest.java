package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sotw.marketdata.AskLevel;


import org.agrona.concurrent.UnsafeBuffer;
import messages.marketdata.*;
import messages.order.Side;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the srpead (i.e. you BUY an order with a price which is == or > askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */

 
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    // @Override
    // public AlgoLogic createAlgoLogic() {

    //     return new MyAlgoLogic(13000, 106.5);
    //     // return new MyAlgoLogic();
    // }


    private static final long TARGET_QUANTITY = 13000;
    private static final double TARGET_VWAP = 100;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic(TARGET_QUANTITY, TARGET_VWAP);
    }



    @Test
    public void testExampleBackTest() throws Exception {
        //create a sample market data tick....
        send(createTick());

        //ADD asserts when you have implemented your algo logic

        assertEquals(3, container.getState().getChildOrders().size());

        //when: market data moves towards us
         send(createTick2());
         send(createTick3());
         send(createTick4());

        //then: get the state
        var state = container.getState();

        //Check things like filled quantity, cancelled order count etc....
        //long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        //assertEquals(225, filledQuantity); //we should have 225 filled quantity
    }

}