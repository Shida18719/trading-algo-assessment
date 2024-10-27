package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;

import static org.junit.Assert.assertEquals;
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

    private static final long quantityToTrade = 3000L;
    private static final long targetVWAP = 100L;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic(quantityToTrade, targetVWAP);
    }
    

    /**
     * Tests Cases Summary:
     * 1. Order Creation Limit (testCreateThreeChildOrders):
     *    - Asserts check three child orders created, if there are fewer than 3
     * 2. Buy Order Creation (testBuyOrderCreated):
     *    - Asserts the number of active buy orders doesn't exceed 3
     * 3. Sell Order Creation (testSellOrdersCreated):
     *    - Asserts the number of active sell orders doesn't exceed 3
     * 4. Order Quantity Validation (testExecutedOrderQuantity):
     *    - Asserts orders doesn't exceed the target quantity of 13000
     * 5. VWAP Calculation (testCalculateVWAP):
     *    - Asserts check that calculatedVWAP creates order close (relatively cheaper) to target of 100.
     * 
     *  Test Configuration:
    * - Target Quantity: 13000
    * - Target VWAP: 100
     */

    @Test
    public void testBuyOrderCreated() throws Exception {

        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Check number of active buy orders
        long activeBuyOrders = state.getActiveChildOrders().stream()
                               .filter(order -> order.getSide() == Side.BUY)
                               .count();
                             
        assertTrue("Buy orders should at least 3", activeBuyOrders <= 3);   
    }

    
    @Test
    public void testSellOrdersCreated() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();
        // Check number of active sell orders
        long activeSellOrders = state.getChildOrders().stream()
        .filter(order -> order.getSide() == Side.SELL)
        .count();

        assertTrue("Sell orders should not exceed 3", activeSellOrders <= 3);
    }


    @Test
    public void testExecutedOrderQuantity() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();
        // Check if the executed order does not exceed quantity
        long executedQuantity = state.getChildOrders().stream().mapToLong(order -> order.getQuantity()).sum();

        assertTrue("Executed quantity should not exceed target", executedQuantity > 0 && executedQuantity <= 3000);
    }


    @Test
    public void testReturnNoActions() throws Exception {

        SimpleAlgoState state = container.getState();

        // Simple Assert NoActions is returned
        MyAlgoLogic algoLogic = (MyAlgoLogic) createAlgoLogic();
        Action action = algoLogic.evaluate(state);

        // Simple asserts there are no active child orders
        assertTrue("There should be no active orders", state.getActiveChildOrders().isEmpty());

        // Assert that NoAction is returned
        assertTrue("NoAction should be returned when there are no active orders", action instanceof NoAction);

        assertEquals(NoAction.class, action.getClass());
    }


    @Test
    public void testCreateThreeChildOrders() throws Exception {
        
        send(createTick());
        send(createTick2());

        // simple assert to check we had 3 orders created
        assertEquals(3, container.getState().getChildOrders().size());
    }

}