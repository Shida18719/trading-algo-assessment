import { useEffect, useRef, useState } from "react"; // React hooks
export interface PriceCellProps {
    price: number; //Current price
    side: "bid" | "offer"; // Side prop to differentiate between bid and offer
}

export const PriceCell = (props: PriceCellProps) => {
    const {price, side} = props;

    const previousPrice = useRef<number | null>(null); // Store values that doesn't need rerendering 
    const [symbolDirection, setSymbolDirection] = useState<string>("");

    useEffect(() => {
        if (previousPrice.current !== null) { //useRef() only returns one item. It returns an Object called current.
            const changeInPrice = price - previousPrice.current; 
            if (changeInPrice > 0) {
                setSymbolDirection("🡅");   // Price Up  
            } else if (changeInPrice < 0) {
                setSymbolDirection("🡇");  // Price Down 
            } else {
                setSymbolDirection("");  // No change in price
            }
        }
        // Update the previous price after each price change
        previousPrice.current = price;
    }, [previousPrice, price]);  // Effect runs whenever the price changes
    
    //Render conditional logic on the side
    return (
        <>
            <td className={`priceCell ${side === "bid" ? "bidArrow" : "offerArrow"}`}>
                {price.toFixed(2)}
                {/* Price display at 2 decimal places.*/}
                <span>{symbolDirection}</span>
                
            </td>
        </>
    );
};