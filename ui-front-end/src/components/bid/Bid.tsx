import React, { useRef } from 'react';
import './Bid.css'

interface BidProps {
  bid: number;
  bidQuantity: number;
  index: number;
}
export const Bid: React.FC<BidProps> = ({ bid, bidQuantity }) => {
    const previousBidRef = useRef<number | null>(null);  
    const currentBid = bid;
    const previousBid = previousBidRef.current;  
  
    let bidDirection = '';

      if (currentBid > previousBid) {
        bidDirection = '⬆';  

      } else if (currentBid < previousBid) {
        bidDirection = '⬇';  
    }
  
    previousBidRef.current = currentBid;

    const dynamicPercentage: number =  (currentBid / 70) * 100;
    // console.log(currentBid);

  
    return (<>
    
    <td className='bidQuantity' style={{  background: `linear-gradient(to left, blue ${dynamicPercentage}%, #ffffff ${dynamicPercentage}%`}}>
      {bidQuantity}
    </td>
    <td className="bid">
  <span style={{ color: 'grey' }}>{bidDirection}</span>
  <span>{bid}</span>
</td>

      </>
    );
  };

