package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.orderbook.order.Order;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static codingblackfemales.action.NoAction.NoAction;

import java.util.List;

public class MyAlgoLogic implements AlgoLogic {
    // Tracks and logs messages
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    private final long quantityToTrade;
    private final double targetVWAP;
    private long executedQuantity = 0;
    private double calculatedVWAP = 0;

    public MyAlgoLogic(long quantityToTrade, double targetVWAP) {
        this.quantityToTrade = quantityToTrade;
        this.targetVWAP = targetVWAP;
    }

    // Checks the current state of the market and makes decisions about whether to place new orders.
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
         */

        // Transform each element into long type and sum them up

        executedQuantity = state.getChildOrders().stream().mapToLong(o -> o.getQuantity()).sum();

<<<<<<< HEAD

=======
>>>>>>> test-branch
        // Exit condition: stop once the total traded quantity matches the target, since we don't want to over trade
        if (executedQuantity >= quantityToTrade) {
            logger.info("[MYALGO] ======VWAP target achieved======. No more orders to place.");
            return NoAction;
        }

        // retrieves a list of child orders, if more than 20, 
        // prevents the algorithm from creating more orders
        var orderCount = state.getChildOrders().size();
        if (orderCount > 20) {
            return NoAction;
        }

        // Modify method was interfering with order fulfillment
        //checks how many child orders are currently active
        final var activeOrders = state.getActiveChildOrders();

        // If there are active orders, check conditions before canceling
        if (activeOrders.size() > 0) {

        // Cancel the first active order, if it exists before placing new order:
        //  based on if the price has moved significantly 
            final var option = activeOrders.stream().findFirst();

            if (option.isPresent()) {
                var childOrder = option.get();

                double orderPrice = childOrder.getPrice();

                // Cancel the order if the price has moved significantly
                // Set a threshold for when the price is far enough to warrant cancellation
                double priceThreshold = 0.05; // 5% threshold

                // Calculates the absolute difference between the order price
                if(Math.abs(orderPrice - calculatedVWAP) / calculatedVWAP > priceThreshold) {

                logger.info("[MYLALGO] Cancelling order:" + childOrder);
                return new CancelChildOrder(childOrder);
                }
            }
            else {
                return NoAction;
            }
        }

        // Calculate the remaining quantity to trade
        long remQuantity = quantityToTrade - executedQuantity;

        long totalVolume = 0;
        long volumePrice = 0;

        // Check if we should place a buy order
        // If fewer than 3 child orders exist and there is still more quantity to trade continue
        if (state.getChildOrders().size() < 3 && remQuantity > 0){
            
//            long totalVolume = 0;
//            long volumePrice = 0;

            // Loop through all child buy orders and sums their prices and quantities
                for (int i = 0; i < state.getChildOrders().size(); i++) {
                    final BidLevel level = state.getBidAt(i);
                    if (level == null) {
                        continue;  // Check for null values
                    }
                // Calculate VWAP from market data (simple average based on bid price and volume)
                totalVolume += level.quantity;
                volumePrice += level.price * level.quantity;
                }

            calculatedVWAP = totalVolume > 0 ? (double) volumePrice / totalVolume : 0;

            logger.debug("[MYALGO] Calculated VWAP: " + calculatedVWAP);

            // Create a new buy limit order if the calculated VWAP is less than the target VWAP
            if (calculatedVWAP <= targetVWAP) {

                // Get bid price at the top of the book
                final BidLevel bestBid = state.getBidAt(0);
                long price = bestBid.price; // highest price a buyer is willing to pay

                // Ensures the remaining quantity to trade is the minimum of the best bid quantity
                long minQtyToTrade = Math.min(bestBid.quantity, remQuantity);  // The amount left to trade
                
                if(minQtyToTrade > 0) {
                    logger.info("[MYALGO] Creating a limit BUY order, have:" + state.getChildOrders().size() + " children, want 3, on passive side of book with: " + minQtyToTrade + " @ " + price);
                    executedQuantity += minQtyToTrade;
                    return new CreateChildOrder(Side.BUY, minQtyToTrade, price); // Place a buy limit order
                }
                else {
                    logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, done.");
                    // Do nothing, when 3 child orders exist
                }
            }
        }
        // Check if we should place a sell order
        // Get ask price at the top of the book
        // final AskLevel bestAsk = state.getAskAt(0);
        // long askPrice = bestAsk.price; // lowest price a seller is willing to accept

        // long askQtyToTrade = Math.min(bestAsk.quantity, remQuantity);  // The amount left to trade


        if (state.getChildOrders().size() < 3 && remQuantity > 0) {

//            long totalVolume = 0;
//            long volumePrice = 0;

            // Loop through all child orders and sums their prices and quantities
            for (int i = 0; i < state.getChildOrders().size(); i++) {
                final AskLevel level = state.getAskAt(i);
                if (level == null) {
                    continue;  // Check for null values
                }
                // Calculate VWAP from market data (simple average based on bid price and volume)
                totalVolume += level.quantity;
                volumePrice += level.price * level.quantity;
            }

            calculatedVWAP = totalVolume > 0 ? (double) volumePrice / totalVolume : 0;

            logger.debug("[MYALGO] Calculated VWAP: " + calculatedVWAP);

            // Create a new sell limit order if the calculated VWAP is greater than the target VWAP
            if (calculatedVWAP >= targetVWAP) {

                // Get ask price at the top of the book
                final AskLevel bestAsk = state.getAskAt(0);
                long price = bestAsk.price; // lowest price a seller is willing to accept

                // Ensures the remaining quantity to trade is the minimum of the best ask quantity
                long minQtyToTrade = Math.min(bestAsk.quantity, remQuantity);  // The amount left to trade

                if (minQtyToTrade > 0) {
                    logger.info("[MYALGO] Creating a limit SELL order, have:" + state.getChildOrders().size() + " children, want 3, joining passive side of book with: " + minQtyToTrade + " @ " + price);
                    executedQuantity += minQtyToTrade;
                    return new CreateChildOrder(Side.SELL, minQtyToTrade, price); // Place a sell limit order
                } else {
                    logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, done.");
                    // Do nothing, when 3 child orders exist
                }
            }
        }
        return NoAction;
    }
}            
