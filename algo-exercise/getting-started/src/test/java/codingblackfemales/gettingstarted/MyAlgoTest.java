package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;

import java.nio.ByteBuffer;
import java.util.List;

import messages.marketdata.*;
import messages.order.Side;

import org.agrona.concurrent.UnsafeBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * This test is designed to check your algo behavior in isolation of the order book.
 *
 * You can tick in market data messages by creating new versions of createTick() (ex. createTick2, createTickMore etc..)
 *
 * You should then add behaviour to your algo to respond to that market data by creating or cancelling child orders.
 *
 * When you are comfortable you algo does what you expect, then you can move on to creating the MyAlgoBackTest.
 *
 */
public class MyAlgoTest extends AbstractAlgoTest {
    // private  MyAlgoLogic algoLogic;

    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes

        return new MyAlgoLogic(13000, 108.5);
    }
    

    @Test
    public void testDispatchThroughSequencer() throws Exception {

        //create a sample market data tick....
        send(createTick());

        SimpleAlgoState state = container.getState();

        // AlgoLogic algoLogic = createAlgoLogic();

        // Check number of active buy orders
        long activeBuyOrders = state.getActiveChildOrders().stream()
                               .filter(order -> order.getSide() == Side.BUY)
                               .count();
                             
        assertTrue("Number of active buy orders should not exceed 3", activeBuyOrders <= 3);


        // Check number of active sell orders FAILING TEST - NO SELL ORDER YET
        long activeSellOrders = state.getActiveChildOrders().stream()
                .filter(order -> order.getSide() == Side.SELL)
                .count();
        assertTrue("Number of active sell orders should not exceed 3", activeSellOrders <= 3);


        // Check if the executed order does not exceed quantity
        long executedQuantity = state.getChildOrders().stream().mapToLong(order -> order.getQuantity()).sum();
        assertTrue("Executed quantity should not exceed target", executedQuantity <= 13000);

        // Check if the algo is cancelling orders when necessary
        
        // //simple assert to check we had 3 orders created
        // assertEquals(3, container.getState().getChildOrders().size());
    }
}