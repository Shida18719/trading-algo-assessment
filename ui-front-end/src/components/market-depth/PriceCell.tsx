export interface PriceCellProps {
    price: number;
}

export const PriceCell = (props: PriceCellProps) => {
    const {price} = props;
    return (
        <td>{price}</td>
    )
}