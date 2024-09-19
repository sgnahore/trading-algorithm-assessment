import React, { useRef } from 'react';

interface OfferProps{
    offer: string;
    offerQuantity: number;
    index: number;
}

export const Offer: React.FC<OfferProps> = ({ offer, offerQuantity}) => {
    const previousOfferRef = useRef<string | null>(null);
    const currentOffer = offer;
    const previousOffer = previousOfferRef.current;

    let offerDirection = '';
    if (currentOffer > previousOffer) {
        offerDirection =  '↑';

    } else if( currentOffer < previousOffer) {
        offerDirection = '↓';  
    }

    return(
        <>

        <td>{offer}{offerDirection}</td>
        <td>{offerQuantity}</td>
        </>
    )
}