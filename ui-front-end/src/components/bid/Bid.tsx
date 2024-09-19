import React, { useRef } from 'react';
import './Bid.css'
interface BidProps {
  bid: string;
  bidQuantity: number;
  index: number;
}
export const Bid: React.FC<BidProps> = ({ bid, bidQuantity }) => {
    const previousBidRef = useRef<string | null>(null);  // Store the previous bid
    const currentBid = bid;
    const previousBid = previousBidRef.current;  // Get the previous bid from the ref
  
    let bidDirection = '';
    if (previousBid !== null) {
      if (currentBid > previousBid) {
        bidDirection = '↑';  
      } else if (currentBid < previousBid) {
        bidDirection = '↓';  
      } 
    }
  
    // Update the ref with the current bid (this will be used in the next render)
    previousBidRef.current = currentBid;
  
    return (<>
    <td className='bidQuantity'>
      {bidQuantity}
    </td>
      <td className="bid">
        {bidDirection} {bid}  
      </td>
      </>
    );
  };

