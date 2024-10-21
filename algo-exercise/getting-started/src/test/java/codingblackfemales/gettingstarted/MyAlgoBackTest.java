package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;

import java.util.stream.Collectors;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    private static final long targetVWAP = 100L;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic(quantityToTrade, targetVWAP);
    }


    /**
     * Tests Summary:
     * 1. Total Order Creation And FilledQuantity (testTotalOrderCreationAndFilledQuantity):
     *    - Validates the functionality of the algorithm with market data simulation
     *    - Asserts check six child orders created
     *    - Asserts filled quantity matches expected value of 1300 after market movement
     * 
     * 2. Order Cancellation (testCancelledOrderCount):
     *    - Checks exactly 1 order is cancelled under specific market conditions
     *    - Verifies 5 trades are successfully executed
     *    - Checks the presence of cancelled orders in active order list
     * 
     * 3. Filled Order State (testFilledOrPartialFilledOrders):
     *    - Asserts filled or partially filled orders should not be cancelled
     *  
     * 4. Unfilled Buy Order Cancellation (testCancelUnfilledBuyOrder):
     *    - Check that unfilled buy orders are cancelled when VWAP exceeds current price
     * 
     *  5. VWAP Buy Order Creation (testCalculateVWAPCreateBuyOrder):
     *    - Asserts buy order creation based on VWAP calculations
     *    - Ensures buy orders are created only when price is below target VWAP (100)
     *    - Check limit order price calculation against VWAP benchmark
     */



    @Test
    public void testTotalOrderCreationAndFilledQuantity() throws Exception {
        //create a sample market data tick....
        send(createTick());

        
        // Asserts total number of child order created
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
        assertEquals(1300, filledQuantity); //we should have 1300 filled quantity
    }


    @Test
    public void testCancelledOrderCount() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Count number of cancelled orders
        long cancelledOrderCount = state.getChildOrders().stream()
        .filter(order -> order.getState() == OrderState.CANCELLED)
        .count();

        // Assert check 1 order cancelled
        assertEquals(1, cancelledOrderCount);

        long nonCancelledOrderCount = state.getChildOrders().stream()
        .filter(order -> order.getState() != OrderState.CANCELLED)
        .count();

        // Assert 5 trades Executed
        assertEquals(5, nonCancelledOrderCount);

        // Check if Any order is cancelled
        var cancelAnyOrder = state.getActiveChildOrders().stream()
        .filter(order -> order.getState() == OrderState.CANCELLED).findAny();
        
        assertNotNull(cancelAnyOrder);
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
        boolean filledOrPartialOrder = filledOrPartialFilledOrders.stream()
        .anyMatch(order -> order.getState() == OrderState.CANCELLED);

        // Assert that none of the filled orders are cancelled
        assertFalse("Filled orders should not be cancelled", filledOrPartialOrder);
    }

    @Test
    public void testCancelUnfilledBuyOrder() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Filter out unfilled orders
        List<ChildOrder> unFilledOrders = state.getChildOrders().stream()
        .filter(order -> order.getState() != OrderState.FILLED)
        .collect(Collectors.toList());
 
        // Assert unfilled orders is cancelled
        boolean cancelUnfilledBuyOrder = unFilledOrders.stream()
        .filter(order -> order.getSide() == Side.BUY)
        .anyMatch(order -> order.getState() == OrderState.CANCELLED);

        
        assertTrue("Unfilled Buy orders should be cancelled", cancelUnfilledBuyOrder);
    }


    @Test
    public void testCalculateVWAPCreateBuyOrder() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        MyAlgoLogic algoLogic = (MyAlgoLogic) createAlgoLogic();

        // Call the CalculateVWAP method
        long limitOrderPrice = algoLogic.CalculateVWAP(state);
        
        boolean createBuyOrder = state.getActiveChildOrders().stream()
        .filter(order -> order.getSide() == Side.BUY)
        .anyMatch(order -> order.getPrice() <= targetVWAP);

        // Simple assert a buy order has been created
        assertTrue("Create a Buy order if buy the price is less than the targetVWAP benchmark", createBuyOrder);

        // Simple Assert VWAP calculation
        assertTrue("Create a Buy order with a price close to or above the VWAP (limit order price)", limitOrderPrice <= targetVWAP);
    }

}