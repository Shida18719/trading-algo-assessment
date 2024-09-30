import {MarketDepthRow} from "./useMarketDepthData";
import "./MarketDepthPanel.css";

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    console.log({ props });
     return <table></table>;

  };