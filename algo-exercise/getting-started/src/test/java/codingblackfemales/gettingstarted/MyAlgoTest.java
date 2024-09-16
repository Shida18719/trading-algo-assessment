package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
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
    private  MyAlgoLogic algoLogic;

    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        algoLogic = new MyAlgoLogic(6000, 108.5);
        return algoLogic;
        
    }

     // Simulate a market data tick that will be passed to the algorithm
     @Override
    protected UnsafeBuffer createTick() {
        // Create and return a properly initialized UnsafeBuffer representing current market data
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        // Write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        // Set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        encoder.askBookCount(3)
                .next().price(100L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L);

        encoder.bidBookCount(3)
                .next().price(98L).size(100L)
                .next().price(95L).size(200L)
                .next().price(91L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }
    

    @Test
    public void testDispatchThroughSequencer() throws Exception {
        

        //create a sample market data tick....
        send(createTick());

        //simple assert to check we had 3 orders created
        assertEquals(3, container.getState().getChildOrders().size());
    }
}
