package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sotw.marketdata.AskLevel;


import org.agrona.concurrent.UnsafeBuffer;
import messages.marketdata.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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

    @Override
    public AlgoLogic createAlgoLogic() {

        return new MyAlgoLogic(13000, 108.5);
        // return new MyAlgoLogic();
    }


    @Test
    public void testExampleBackTest() throws Exception {
        //create a sample market data tick....
        send(createTick());

        //ADD asserts when you have implemented your algo logic

        assertEquals(3, container.getState().getChildOrders().size());

        //when: market data moves towards us
         send(createTick2());

        //then: get the state
        var state = container.getState();

        //Check things like filled quantity, cancelled order count etc....
        long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        assertEquals(501, filledQuantity);
    }

}





    // protected UnsafeBuffer createTick(){
    //     final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    //     final BookUpdateEncoder encoder = new BookUpdateEncoder();


    //     final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
    //     final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

    //     //write the encoded output to the direct buffer
    //     encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

    //     //set the fields to desired values
    //     encoder.venue(Venue.XLON);
    //     encoder.instrumentId(123L);
    //     encoder.source(Source.STREAM);

    //     encoder.bidBookCount(3)
    //             .next().price(98L).size(100L)
    //             .next().price(95L).size(200L)
    //             .next().price(91L).size(300L);

    //     encoder.askBookCount(4)
    //             .next().price(100L).size(101L)
    //             .next().price(110L).size(200L)
    //             .next().price(115L).size(5000L)
    //             .next().price(119L).size(5600L);

    //     encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

    //     return directBuffer;
    // }

    // protected UnsafeBuffer createTick2(){

    //     final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    //     final BookUpdateEncoder encoder = new BookUpdateEncoder();

    //     final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
    //     final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

    //     //write the encoded output to the direct buffer
    //     encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

    //     //set the fields to desired values
    //     encoder.venue(Venue.XLON);
    //     encoder.instrumentId(123L);
    //     encoder.source(Source.STREAM);

    //     encoder.bidBookCount(3)
    //             .next().price(95L).size(100L)
    //             .next().price(93L).size(200L)
    //             .next().price(91L).size(300L);

    //     encoder.askBookCount(4)
    //             .next().price(98L).size(501L)
    //             .next().price(101L).size(200L)
    //             .next().price(110L).size(5000L)
    //             .next().price(119L).size(5600L);

    //     encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

    //     return directBuffer;
    // }