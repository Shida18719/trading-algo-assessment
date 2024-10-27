package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.orderbook.order.Order;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static codingblackfemales.action.NoAction.NoAction;


public class MyAlgoLogic implements AlgoLogic {
    // Tracks and logs messages
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    private final long quantityToTrade;
    private final double targetVWAP;
    private long executedQuantity = 0;

    public MyAlgoLogic(long quantityToTrade, long targetVWAP) {
        this.quantityToTrade = quantityToTrade;
        this.targetVWAP = targetVWAP;
    }

    // Checks the current state of the market and makes decisions about whether to
    // place new orders.
    @Override
    public Action evaluate(SimpleAlgoState state) {

        // Converts the current market state into a readable string and 
        // log the current state
        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);    
        
        /********
         *
         * Add your logic here....
         * 
         * Logic Summary:
         
         * 1. Logs the current state of the order book.
         
         * 2. Calculate how much trade has been Executed (executedQuantity) and
         *    keep track of the total quantity traded (executedQuantity).
         *    and determine the remaining quantity to trade (remQuantity).
         * 
         * 3. Check how many active child orders exist, before placing new orders.
         *    Limit the number of active child orders created to (orderCount - max 5 orders).
         * 
         * 4. Cancel any unfilled orders if the limitOrderPrice (VWAP) is higher than the Current price.
         * 
         * 5. If fewer than 3 orders exist and there’s more quantity to trade, 
         *    the algorithm considers creating a new order.
         * 
         * 6. Places buy limit order at the best bid price based on VWAP and market conditions, 
         *    ensuring not to exceed the remaining quantity and there are fewer than 3 active buy orders, and the remaining quantity to buy.
         * 
         * 7. Places a sell order ensuring there are fewer than 3 active sell orders, remaining quantity to sell,
         *    and the best bid is less than or equal to the best ask ("going flat" mechanism).
         * 
         * 8. Uses VWAP calculated based on the bid prices as a benchmark to help decide,
         *    if the current VWAP price (limitOrderPrice) is below the target VWAP (targetVWAP),
         *    and if so, create a buy order at the best bid price available.
         * 
         * 9. Return NoAction in a situation where there are no active orders or reaches trading limit
         *
         */

        // Transform each element into long type and calculates the total quantity 
        // of shares that have been traded so far, summing up child orders
        executedQuantity = state.getChildOrders().stream().mapToLong(o -> o.getQuantity()).sum();

        long limitOrderPrice = CalculateVWAP(state); // Stores VWAP Method
        
        // Calculate the remaining quantity to trade
        // long quantityToTrade = 1300;
        long remQuantity = quantityToTrade - executedQuantity;

        final BidLevel bestBid = state.getBidAt(0);
        final AskLevel bestAsk = state.getAskAt(0);

        // Retrieves a list of child orders, if Order Creation Limit of 5 is Reached, 
        // prevents the algorithm from creating more orders
        var orderCount = state.getChildOrders().size();
        if (orderCount > 5) {
            return NoAction;
        }

        //checks how many child orders are currently active
        // Exit condition: stop if Total Quantity reached, since we don't want to over trade
        if (executedQuantity >= quantityToTrade) {
            logger.info("[MYALGO] Order limit achieved: No more orders to place.");
            return NoAction;
        }

        // All active child orders
        final var activeOrders = state.getActiveChildOrders();

         // If there are active orders, check conditions before canceling
        if (activeOrders.size() > 0) {

            //Cancel the Any active order, if it exists before canceling order
            // based on if the price is above the VWAP 
            final var option = activeOrders.stream().findFirst();

            if (option.isPresent()) {
                var childOrder = option.get();

                long orderPrice = childOrder.getPrice(); // Current price
                var filledOrderQty = childOrder.getFilledQuantity();
                long totalFilledQuantity = childOrder.getQuantity();

                logger.info("[MYALGO] Total Filled Quantity: " + totalFilledQuantity);

                // Checks case where the order is already filled
                if (childOrder.getState() == OrderState.FILLED) {
                    logger.info("[MYLALGO] Order is already filled, cannot cancel: " + childOrder);
                    return NoAction;
                }

                // Check when orders are partially filled
                else if (filledOrderQty > 0 && filledOrderQty < totalFilledQuantity) {
                    logger.info("[MYLALGO] Partially filled order detected, do not cancel: " + childOrder);
                    return NoAction;
                }

                // Skip sell orders (Market Order)
                if (childOrder.getSide() == Side.SELL) {
                    logger.info("[MYALGO] Skipping SELL order: " + childOrder);
                    return NoAction;
                }

                // Cancel the order if the limitOrderPrice (VWAP) is higher than the Current price
                if (childOrder.getSide() == Side.BUY && filledOrderQty == 0) {

                    if (limitOrderPrice >= orderPrice) {

                        logger.info("[MYALGO] Cancelling UNFILLED ORDER: " + childOrder);
                        return new CancelChildOrder(childOrder);
                    }
                }  
            } 
            else {
                return NoAction;
            }
        }



        long buyOrder = state.getActiveChildOrders().stream()
             .filter(order -> order.getSide() == Side.BUY).count();

        // Check if we should place a buy order
        // If fewer than 3 child orders exist and there is still more quantity to trade continue
        if (buyOrder < 3 && remQuantity > 0){

            // Create a new buy limit order if the limit Order VWAP Price is less than the target VWAP
            if (limitOrderPrice <= targetVWAP) {

                if (bestBid != null) {
                    logger.info("[MYALGO] CHECK NULL BID:" + bestBid);

                        // Get bid price at the top of the book
                        // long price = bestBid.price; // highest price a buyer is willing to pay

                        long price = limitOrderPrice;
                        
                        // Ensures the remaining quantity to trade is the minimum of the best bid quantity
                        long quantity = Math.min(bestBid.quantity, remQuantity);  // The amount left to trade
                        
                        logger.info("[MYALGO] Taget Price: " + targetVWAP);
                        logger.info("[MYALGO] Remaining Quantity: " + remQuantity);

                    if(quantity > 0) {
                        logger.info("[MYALGO] Creating a limit BUY order, have:" + state.getChildOrders().size() + " children, want 3, on passive side of book with: " + quantity + " @ " + price);
                        
                        // Updates the amount of quantity traded when a buy order is placed.
                        executedQuantity += quantity;

                        logger.info("[MYALGO] Calculated PASSIVE EXECUTED QUANTITY: " + executedQuantity);

                        return new CreateChildOrder(Side.BUY, quantity, price); // Place a buy limit order
                    }
                    else {
                        logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, done.");
                            // Do nothing, when 3 child orders exist
                    }
                }
            } return NoAction;
        }


        // Executing SELL orders at the Bid price
        long sellOrder = state.getActiveChildOrders().stream()
             .filter(order -> order.getSide() == Side.SELL).count();

        if (sellOrder < 3 && remQuantity > 0) {
            logger.info("[MYALGO] Checking for Sell Orders. Remaining quantity: " + remQuantity);
            
            /**
            * Sell at the current Market best bid price, 
            * if the bestAsk is greater than the bestBid
            * Perharps we are going flat
            */
            if (bestBid != null) {
                

                long price = bestBid.price;

                if (bestBid.price <= bestAsk.price){
                    long quantity = Math.min(bestBid.quantity, remQuantity);

                    logger.info("[MYALGO] Best Bid: " + bestBid.price);
                    
                
                    if (quantity > 0) {

                        logger.info("[MYALGO] Creating a LIMIT SELL order, have:" + state.getChildOrders().size() + " on Ask side of book with: " + quantity + " @ " + price);
                        logger.info("[MYALGO] Creating a limit SELL order at price: " + price + " for quantity: " + quantity);

                        // Updates the amount of quantity traded when a sell order is placed.
                        executedQuantity += quantity; 
                        return new CreateChildOrder(Side.SELL, quantity, price); // Place sell order
                    }
                }
                else {
                    logger.info("[MYALGO] Have" + state.getChildOrders().size() + " children, want 3, done.");
                }
            }
        }
        return NoAction;
    }  


    // Method to calculate VWAP
    protected long CalculateVWAP(SimpleAlgoState state) { 
        long totalVolume = 0;
        long volumePrice = 0;

        // Loop through all child buy orders and sums their prices and quantities
        for (int i = 0; i < state.getChildOrders().size(); i++) {
            final BidLevel bid = state.getBidAt(i);
            if (bid == null) {
                continue; // Check for null values
            }
            // Calculate VWAP from market data (Average based on bid price and
            // volume)
            totalVolume += bid.quantity;
            volumePrice += bid.price * bid.quantity;
        } 
        
        
        return totalVolume > 0 ? (long) volumePrice / totalVolume : 0;
    }
}
