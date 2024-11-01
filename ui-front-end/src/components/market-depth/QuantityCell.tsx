
export interface QuantityCellProps {
    quantity: number; // value for the bar
    side: "bid" | "offer"; 
    maxQuantity: number; 
}


export const QuantityCell = (props: QuantityCellProps) => {
    // Normalize the bar width based on the maximum quantity
    const {quantity, side, maxQuantity} = props;

    const barWidth = maxQuantity > 0 ? (quantity / maxQuantity) * 100 : 0;

     const sideClass = side === "bid" ? "bidBar" : "offerBar";


    return (
        <td className="quantityCell">
            {/* Bar background */}
            <td
                className={`quantityBar ${sideClass}`}
                style={{
                    width: `${barWidth}%`, // Set width based on quantity
                }}>
                <span>{quantity}</span>
            </td>
        </td>
    );
};
