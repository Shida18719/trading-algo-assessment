package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static codingblackfemales.action.NoAction.NoAction;

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

        // retrieves a list of child orders, if more than 20, 
        // prevents the algorithm from creating more orders
        var orderCount = state.getChildOrders().size();
        if (orderCount > 20) {
            return NoAction;
        }

        //checks how many child orders are currently active
        final var activeOrders = state.getActiveChildOrders();

        // Cancel the first active order, if it exist, before placing new order
        if (activeOrders.size() > 0) {
            var childOrder = activeOrders.get(0);  // Take the first active order
            logger.info("[MYALGO] Cancelling order: " + childOrder);
            return new CancelChildOrder(childOrder);
        }

        // Exit condition: stop once the total traded quantity matches the target
        if (executedQuantity >= quantityToTrade) {
            logger.info("[MYALGO] VWAP target achieved. No more orders to place.");
            return NoAction;
        }

        // Calculate the remaining quantity to trade
        long remQuantity = quantityToTrade - executedQuantity;

        // If fewer than 3 child orders exist and there is still more quantity to trade continue
        if (state.getChildOrders().size() < 3 && remQuantity > 0){
            
            long totalVolume = 0;
            long volumePrice = 0;

            // VWAP loops through all child orders and sums their prices and quantities
            for (int i = 0; i < state.getChildOrders().size(); i++) {
                final BidLevel level = state.getBidAt(i);
                if (level == null) {
                    continue;  // Check for null values
                }

            // Calculate VWAP from market data (simple average based on bid price and volume)
            totalVolume += level.quantity;
            volumePrice += level.price * level.quantity;
            }

            double calculatedVWAP = totalVolume > 0 ? (double) volumePrice / totalVolume : 0;

            logger.debug("[MYALGO] Calculated VWAP: " + calculatedVWAP);

            // Create a new buy limit order if the calculated VWAP is less than the target VWAP
            if (calculatedVWAP <= targetVWAP) {

                BidLevel bestBid = state.getBidAt(0); // Best bid price at the top of the book
                long price = bestBid.price; // highest price a buyer is willing to pay

                // Ensures the remaining amount to trade is the minimum of the best bid quantity
                long quantity = Math.min(bestBid.quantity, remQuantity);  // The amount left to trade
                
                    if(quantity > 0) {
                        logger.info("[MYALGO] Creating new order, have:" + state.getChildOrders().size() + " children, want 3, joining passive side of book with: " + quantity + " @ " + price);
                        executedQuantity += quantity;
                        return new CreateChildOrder(Side.BUY, quantity, price); // Place a buy limit order
                    }
            }
        } else {
        logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, done.");
        // Do nothing, when 3 child orders exist
    }
        return NoAction;  
    }       
}
