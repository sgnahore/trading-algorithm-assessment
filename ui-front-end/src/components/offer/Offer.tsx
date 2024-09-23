import React, { useRef } from 'react';
import './Offer.css'

interface OfferProps{
    offer: number;
    offerQuantity: number;
    index: number;
}

export const Offer: React.FC<OfferProps> = ({ offer, offerQuantity}) => {
    const previousOfferRef = useRef<string | null>(null);
    const currentOffer = offer;
    const previousOffer = previousOfferRef.current;

    let offerDirection = '';

  
    if (currentOffer > previousOffer) {
        offerDirection = '⬆';  

      } else if (currentOffer < previousOcurrentOffer) {
        offerDirection = '⬇';  
    }

    const dynamicPercentage: number =  (currentOffer / 70) * 100;

    return(
        <>

        <td>{offer}<span className='offer' >{offerDirection}</span></td>
        <td className='offerQuantity' style={{  background: `linear-gradient(90deg, #b20a26 ${dynamicPercentage}%, #ffffff ${dynamicPercentage}%`, height: '70%'}}>{offerQuantity}</td>
        </>
    )
}