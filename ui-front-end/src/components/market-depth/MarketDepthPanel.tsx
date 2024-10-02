import {MarketDepthRow} from "./useMarketDepthData";
import "./MarketDepthPanel.css";

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
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

                <td>{row.symbolLevel}</td>
                <td>{row.level}</td>
                <td>{row.bidQuantity}</td>
                
                <td className="bidArrow">
                <span>↑</span>{row.bid}</td>
                
                {/* {offer side} */}
                <td className="askArrow">
                <span>↓</span>{row.offer}</td>
                <td>{row.offerQuantity}</td>
              </tr>
            ))}
          </tbody>
          </table>
        </div>
      </div>
    );

  };