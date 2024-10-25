
import {MarketDepthRow} from "./useMarketDepthData";
import "./MarketDepthPanel.css";
import { PriceCell } from "./PriceCell";
import { QuantityCell } from "./QuantityCell";

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    const {data}= props;
    console.log({ props });
    // Calculate max quantity for the bars
    const maxQuantity = Math.max(...props.data.map(row => Math.max(row.bidQuantity, row.offerQuantity)));

    return (

        <table className="marketDepthPanel">
          <thead>
            {/* {Header} */}
            <tr>
            <th colSpan={1}></th>
              <th colSpan={2}>Bid</th>
              <th colSpan={2}>Offer</th>
            </tr>
            <tr>
              <th>Level</th>
              <th>Quantity</th>
              <th>Price</th>
              <th>Price</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {data.map((row, index) => (
              <tr key={index}>
                
                {/* {bid side} */}
                <td className= "level">{row.level}</td>

                <QuantityCell quantity={row.bidQuantity} side={"bid"} maxQuantity={maxQuantity} />
                <PriceCell price={row.bid} side={"bid"}/>  
                
                {/* {offer side} */}
                <PriceCell price={row.offer} side={"offer"}/>
                <QuantityCell quantity={row.offerQuantity} side="offer" maxQuantity={maxQuantity} />
              </tr>
            ))}
          </tbody>
        </table>
    );
  };