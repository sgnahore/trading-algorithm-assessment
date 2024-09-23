import React from 'react';

interface DirectionArrowProps {
  currentValue: number;
  previousValue: number | null;
}

export const DirectionArrow: React.FC<DirectionArrowProps> = ({ currentValue, previousValue }) => {
  let direction = '';

  if (currentValue > (previousValue ?? 0)) {
    direction = '⬆';
  } else if (currentValue < (previousValue ?? 0)) {
    direction = '⬇';
  }

  return <span>{direction}</span>;
};
