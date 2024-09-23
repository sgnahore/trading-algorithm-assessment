import "./Table.css";
import { Bid } from "../bid/Bid";
import { Offer } from "../offer/Offer";
interface TableColumn {
  bidQuantity: number;
  bid: number;
  offer: number;
  offerQuantity: number;
}

interface TableProps {
  data: TableColumn[];
}

export const Table: React.FC<TableProps> = ({ data }) => {

  return (
    <table>
      <thead>
        <tr>
          <th ></th>
          <th colSpan={2}>Bid</th>
          <th colSpan={2}>Ask</th>
        </tr>
        <tr>
          <th ></th>
          <th >Quantity</th>
          <th className="bidPriceHeading" style={{ textAlign: 'right'}}>Price</th>
          <th >Price</th>
          <th >Quantity</th>
          
        </tr>
      </thead>
      <tbody>
        {data.map((columns, index) => (
          <tr key={index}>
            <td className="levels">{index}</td> 
            <Bid bid={columns.bid} bidQuantity={columns.bidQuantity} index={index} />
             <Offer offer={columns.offer} offerQuantity={columns.offerQuantity} index={index} />
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default Table;
