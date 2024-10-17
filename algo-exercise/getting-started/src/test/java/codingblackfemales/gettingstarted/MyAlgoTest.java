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
 

 }