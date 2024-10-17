export interface PriceCellProps {
    price: number;
    side: "bid" | "offer"; // Add a side prop to differentiate between bid and offer
}

export const PriceCell = (props: PriceCellProps) => {
    const {price, side} = props;
    //Render conditional logic on the side
    return (
        <>
            <td className={side === "bid" ? "bidArrow" : "offerArrow"}>
                {price}
                <span>{side === "bid" ? "↑" : "↓"}</span>
                
            </td>
        </>
    );
}