package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;

import java.nio.ByteBuffer;
import messages.marketdata.*;
import org.agrona.concurrent.UnsafeBuffer;
import static org.junit.Assert.assertEquals;

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

        
        //simple assert to check we had 3 orders created
        assertEquals(3, container.getState().getChildOrders().size());

    }
}
