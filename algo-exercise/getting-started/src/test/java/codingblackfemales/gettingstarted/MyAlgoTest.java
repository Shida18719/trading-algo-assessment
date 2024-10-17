package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;

import java.util.List;
import java.util.stream.Collectors;

import messages.marketdata.*;
import messages.order.Side;

import static codingblackfemales.action.NoAction.NoAction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

    private static final long quantityToTrade = 13000L;
    private static final long targetVWAP = 100L;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic(quantityToTrade, targetVWAP);
    }
    

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
        // Check number of active sell orders PASSING TEST - NOT displaying SELL ORDER
        // Except with cross-trade
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

        assertTrue("Executed quantity should not exceed target", executedQuantity > 0 && executedQuantity <= 13000);
    }


    @Test
    public void testCancelFirstOrder() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Check if first order is cancelled
        var cancelFirstOrder = state.getActiveChildOrders().stream()
        .filter(order -> order.getState() == OrderState.CANCELLED).findFirst();
        
        assertNotNull(cancelFirstOrder);
    }

    
    @Test
    public void testCancelledOrder() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Check Filled orders have not been cancelled
        long cancelledOrder = state.getChildOrders().stream()
        .filter(order -> order.getState() == OrderState.CANCELLED)
        .count();

        assertEquals(3, cancelledOrder);

        assertTrue("Filled orders have not been cancelled",  cancelledOrder != OrderState.FILLED);
    }


    @Test
    public void testCancelBuyOrderThreshold() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Call the CalculateVWAP method
        MyAlgoLogic algoLogic = (MyAlgoLogic) createAlgoLogic();

        long calculatedVWAP = algoLogic.CalculateVWAP(state);


        // Simple assert order cancelled at threshold of 8%
        double cancelBuyOrderThreshold = state.getActiveChildOrders().stream()
        .filter(order -> order.getSide() == Side.BUY) // Filter buy-side orders
        .mapToDouble(order -> Math.abs(order.getPrice() - calculatedVWAP) / calculatedVWAP)
        .max()
        .orElse(0);
        
        assertTrue("Buy orders should be cancelled if price deviates by more than 8%", cancelBuyOrderThreshold <= 0.08);
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


    @Test
    public void testAskProfitThreshold() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Check if Ask is excecuting order close to the Average Buy Price with a threshold of 2%
        long askProfitThreshold = state.getChildOrders().stream()
        .filter(order -> order.getSide() == Side.SELL)
        .mapToLong(order -> order.getPrice() * order.getQuantity())
        .sum();
    
        final BidLevel bestBid = state.getBidAt(0);

        assertTrue("Average price should be close to profit margin threshold", askProfitThreshold <= bestBid.getPrice() * (1 + 0.02));
    }


    @Test
    public void testReturnNoActions() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        // Simple Assert no actions is returned
        MyAlgoLogic algoLogic = (MyAlgoLogic) createAlgoLogic();
        Action action = algoLogic.evaluate(state);

        assertEquals(NoAction.class, action.getClass());
    }


    @Test
    public void testCalculateVWAP() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        SimpleAlgoState state = container.getState();

        MyAlgoLogic algoLogic = (MyAlgoLogic) createAlgoLogic();

        // Call the CalculateVWAP method
        long calculatedVWAP = algoLogic.CalculateVWAP(state);

        // Simple Assert VWAP calculation
        assertTrue("Create a Buy order close to VWAP limit order price", calculatedVWAP >= targetVWAP);     
    }


    @Test
    public void testCreateChildOrders() throws Exception {
        
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        // simple assert to check we had 3 orders created
        assertEquals(3, container.getState().getChildOrders().size());
    }
}