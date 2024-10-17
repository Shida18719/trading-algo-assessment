package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;

import java.util.stream.Collectors;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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


    private static final long quantityToTrade = 13000L;
    private static final double targetVWAP = 100L;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic(quantityToTrade, targetVWAP);
    }



    @Test
    public void testExampleBackTest() throws Exception {
        //create a sample market data tick....
        send(createTick());

        //ADD asserts when you have implemented your algo logic

        assertEquals(6, container.getState().getChildOrders().size());

        //when: market data moves towards us
         send(createTick2());
         send(createTick3());
         send(createTick4());

        //then: get the state
        var state = container.getState();

        //Check things like filled quantity, cancelled order count etc....
        long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        assertEquals(802, filledQuantity); //we should have 802 filled quantity
    }


    @Test
    public void testFilledOrPartialFilledOrders() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Filter out filled or partially filled orders
        List<ChildOrder> filledOrPartialFilledOrders = state.getChildOrders().stream()
        .filter(order -> order.getState() == OrderState.FILLED)
        .collect(Collectors.toList());

        // Assert none of the filled or partial order have been cancelled
        boolean anyCancelled = filledOrPartialFilledOrders.stream()
        .anyMatch(order -> order.getState() == OrderState.CANCELLED);

        // Assert that none of the filled orders are cancelled
        assertFalse("Filled orders should not be cancelled", anyCancelled);
    }

}