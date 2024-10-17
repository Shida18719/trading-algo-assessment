package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.orderbook.order.Order;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static codingblackfemales.action.NoAction.NoAction;

import java.util.List;
import java.util.stream.Collectors;

public class MyAlgoLogic implements AlgoLogic {
    // Tracks and logs messages
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    private final long quantityToTrade;
    private final double targetVWAP;
    private long executedQuantity = 0;

    public MyAlgoLogic(long quantityToTrade, double targetVWAP) {
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
         * Logs the current market state.
         * Calculate how much trade has been (executedQuantity) 
         * and determine the remaining quantity to trade (remQuantity).
         * Limit the number of active child orders to (orderCount - max 5 orders).
         * Cancel active orders if the price has moved significantly (to avoid poor trades).
         * Place buy or sell orders based on VWAP and market conditions, 
         * ensuring not to exceed the remaining quantity.
         * Use VWAP to help decide if the current market price is favorable for placing orders.
         *
         */

        // Transform each element into long type and calculates the total quantity 
        // of shares that have been traded so far, summing up child orders
        executedQuantity = state.getChildOrders().stream().mapToLong(o -> o.getQuantity()).sum();

        long limitOrderPrice = CalculateVWAP(state); // Stores VWAP Method
        
        // Calculate the remaining quantity to trade
        long remQuantity = quantityToTrade - executedQuantity;

        final BidLevel bestBid = state.getBidAt(0);
        final AskLevel bestAsk = state.getAskAt(0);

        // retrieves a list of child orders, if Order Creation Limit of 5 is Reached, 
        // prevents the algorithm from creating more orders
        var orderCount = state.getChildOrders().size();
        if (orderCount > 5) {
            return NoAction;
        }

        //checks how many child orders are currently active
        final var activeOrders = state.getActiveChildOrders();

        // Exit condition: stop if Total Quantity reached, since we don't want to over trade
        if (executedQuantity >= quantityToTrade) {
            logger.info("[MYALGO] Order limit achieved: No more orders to place.");
            return NoAction;
        }


        // If there are active orders, check conditions before canceling
        if (activeOrders.size() > 0) {

            //Cancel the first active order, if it exists before placing new order:
            // based on if the price has moved significantly 
            final var option = activeOrders.stream().findFirst();

            if (option.isPresent()) {
                    var childOrder = option.get();

                    double orderPrice = childOrder.getPrice(); // Current price
                    var filledOrderQty = childOrder.getFilledQuantity();
                    double totalFilledQuantity = childOrder.getQuantity();

                    logger.info("[MYALGO] Total Filled Quantity: " + totalFilledQuantity);

                    if (childOrder.getState() == OrderState.FILLED) {
                        // Checks case where the order is already filled and ensures not to cancel
                        logger.info("[MYLALGO] Order is already filled, cannot cancel: " + childOrder);
                        return NoAction;
                    } 
                    else if (filledOrderQty > 0 && filledOrderQty < totalFilledQuantity) {
                        // Checks when orders are partially filled
                        logger.info("[MYLALGO] Partially filled order detected, do not cancel: " + childOrder);
                        return NoAction;
                    }

                    // Cancel the order if the price has moved significantly above the price on the top level
                    // Set a threshold for when the price is far enough to warrant cancellation
                    // double priceThreshold = 0.08 & 0.03; // 8%, 5% threshold

                    // Check if the order is still (not filled or partially filled)

                    if (childOrder.getSide() == Side.BUY) {
                        double priceThreshold = 0.08;

                        // Calculate the price slide for a buy order
                        double priceSlide = Math.abs(orderPrice - limitOrderPrice) / limitOrderPrice;
                        if (priceSlide > priceThreshold && childOrder.getState() != OrderState.FILLED) {

                            logger.info("[MYALGO] Cancelling BUY ORDER with PRICESLIDE: " + priceSlide);
                            return new CancelChildOrder(childOrder);
                        }
                    } 
                    else if (childOrder.getSide() == Side.SELL) {
                        double priceThreshold = 0.03;

                        // Calculate the price slide for a sell order
                        double priceSlide = Math.abs(orderPrice - bestBid.price) / bestBid.price;

                        if (priceSlide > priceThreshold) {
                            logger.info("[MYALGO] Cancelling SELL ORDER with PRICESLIDE: " + priceSlide);
                            return new CancelChildOrder(childOrder);
                    }
                }
                
            } else {
                return NoAction;
            }
        }
        


        long buyOrder = state.getActiveChildOrders().stream()
             .filter(order -> order.getSide() == Side.BUY).count();

        // Check if we should place a buy order
        // If fewer than 3 child orders exist and there is still more quantity to trade continue
        if (buyOrder < 3 && remQuantity > 0){
            logger.info("[MYALGO] Calculated Limit Order VWAP FOR BUY: " + limitOrderPrice);

            // Create a new buy limit order if the limit Order VWAP Price is less than the target VWAP
            if (limitOrderPrice <= targetVWAP) {

                if (bestBid != null) {
                    logger.info("[MYALGO] CHECK NULL BID:" + bestBid);

                        // Get bid price at the top of the book
                        long price = bestBid.price; // highest price a buyer is willing to pay

                        // Ensures the remaining quantity to trade is the minimum of the best bid quantity
                        long minQtyToTrade = Math.min(bestBid.quantity, remQuantity);  // The amount left to trade
                        
                        logger.info("[MYALGO] Remaining Quantity: " + remQuantity);

                    if(minQtyToTrade > 0) {
                        logger.info("[MYALGO] Creating a limit BUY order, have:" + state.getChildOrders().size() + " children, want 3, on passive side of book with: " + minQtyToTrade + " @ " + price);
                        executedQuantity += minQtyToTrade;

                        logger.info("[MYALGO] Calculated PASSIVE EXECUTED QUANTITY: " + executedQuantity);

                        return new CreateChildOrder(Side.BUY, minQtyToTrade, price); // Place a buy limit order
                    }
                    else {
                        logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, done.");
                            // Do nothing, when 3 child orders exist
                    }
                }
            } return NoAction;
        }


        // Executing BUY orders at the ask price
        long sellOrder = state.getActiveChildOrders().stream()
             .filter(order -> order.getSide() == Side.SELL).count();

        if (sellOrder < 3 && remQuantity > 0) {
            logger.info("[MYALGO] Checking for Sell Orders. Remaining quantity: " + remQuantity);
        
            // Set a profit margin threshold, 2% above average buy price
            double profitThreshold = 0.02;

            double averageBuyPrice = state.getChildOrders().stream()
            .filter(o -> o.getSide() == Side.BUY)
            .mapToDouble(o -> o.getPrice() * o.getQuantity())
            .sum() / executedQuantity;

            // Sell when the best ask price offer is greater than the profit threshold
            // Sell if the best ask is greater than the average buy price plus margin (2% profit margin)
            if (bestAsk != null && bestAsk.price >= averageBuyPrice * (1 + profitThreshold)) {
                long price = bestAsk.price;
                long minQtyToTrade = Math.min(bestAsk.quantity, remQuantity);

                logger.info("[MYALGO] Best Ask: " + bestAsk.price);
                logger.info("[MYALGO] SELL AverageBuyPrice: " + averageBuyPrice);

                logger.info("[MYALGO] SELL ProfitThreshold: " + averageBuyPrice * (1 + profitThreshold));
            
                if (minQtyToTrade > 0) {

                    logger.info("[MYALGO] Creating a LIMIT SELL order, have:" + state.getChildOrders().size() + " on Ask side of book with: " + minQtyToTrade + " @ " + price);
                    logger.info("[MYALGO] Creating a limit SELL order at price: " + price + " for quantity: " + minQtyToTrade);

                    // Updates the amount of quantity traded when a buy order is placed.
                    executedQuantity += minQtyToTrade; 
                    return new CreateChildOrder(Side.BUY, minQtyToTrade, price); // Place sell order
                }
            }
            else {
                logger.info("[MYALGO] No sell order placed.");
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
