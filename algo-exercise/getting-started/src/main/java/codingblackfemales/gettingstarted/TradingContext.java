package codingblackfemales.gettingstarted;
import codingblackfemales.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TradingContext {
    private long totalProfit = 0;
    private long ownedShares = 0;
    private long totalSpendings = 0;
    private long totalEarnings = 0;

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);


    public void addOwnedShares(long quantity) {
        ownedShares += quantity;
    }

    public void subtractOwnedShares(long quantity) {
        ownedShares -= quantity;
    }

    public void addSpendings(long amount) {
        totalSpendings += amount;
    }

    public void addEarnings(long amount) {
        totalEarnings += amount;
    }

    public long getTotalProfit() {
        totalProfit = totalEarnings - totalSpendings;
        return totalProfit;
    }

    public long getOwnedShares() {
        return ownedShares;
    }

    public long getTotalSpendings() {
        return totalSpendings;
    }

    public long getTotalEarnings() {
        return totalEarnings;
    }
    public void logTradeStatus() {
        logger.info("[MYALGO] PROFIT: " + getTotalProfit() +
                " --- TOTAL EARNED: " + getTotalEarnings() +
                " --- TOTAL SPENT: " + getTotalSpendings());
    }
}
