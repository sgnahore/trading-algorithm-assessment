package codingblackfemales.sotw.marketdata;

public class AskLevel extends AbstractLevel {
    @Override
    public String toString() {
        return "ASK DETAILS[" + getQuantity() + "@" + getPrice() + "]";
    }
}
