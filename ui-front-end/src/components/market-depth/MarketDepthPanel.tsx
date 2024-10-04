import {MarketDepthRow} from "./useMarketDepthData";
import "./MarketDepthPanel.css";
import { PriceCell } from "./PriceCell";

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    const {data}= props;
    console.log({ props });
     return (
      <div>
        <h3>Market Depth</h3>
        <div className="MarketDepthPanel">
          <table className="marketDepthPanel-table">
          <thead>
            <tr>
            <th colSpan={1}></th>
              <th colSpan={2}>Bid</th>
              <th colSpan={3}>Offer</th>
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
                <td>{row.level}</td>
                <td>{row.bidQuantity}</td>
                <PriceCell price={row.bid} side={"bid"}/>
                
                {/* {offer side} */}
                <PriceCell price={row.offer} side={"offer"}/>
                <td>{row.offerQuantity}</td>
              </tr>
            ))}
          </tbody>
          </table>
        </div>
      </div>
    );

  };