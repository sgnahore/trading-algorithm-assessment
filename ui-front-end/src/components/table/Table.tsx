import "./Table.css";
import { Bid } from "../bid/Bid";
import { Offer } from "../offer/Offer";
interface TableColumn {
  bidQuantity: number;
  bid: string;
  offer: string;
  offerQuantity: number;
}

interface TableProps {
  data: TableColumn[];
}

export const Table: React.FC<TableProps> = ({ data }) => {
  if (data.length === 0) {
    return <p>No data available</p>;
  }

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
          <th >Price</th>
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
