
# MyAlgoLogic - Trading Algorithm

## Overview

`MyAlgoLogic` is a simple trading algorithm implementation designed to evaluate market conditions and place orders based on a few key strategies. The algorithm follows a Volume Weighted Average Price (VWAP) strategy and dynamically responds to market fluctuations. It is implemented as part of a trading framework that includes order books, child orders, and market data for ask and bid levels. The algorithm also maintains a `TradingContext` to track performance metrics like profit, earnings, spendings, and owned shares.

## Features

- **VWAP Calculation**: The algorithm uses a VWAP strategy to evaluate whether current prices offer good buying or selling opportunities. The VWAP is recalculated for each evaluation cycle, and initial thresholds are set to assess if market conditions are favorable for trading.

- **Order Placement Logic**:
    - **Buy Orders**: If fewer than two buy orders are present and the ask price is below or near the VWAP, the algorithm attempts to buy shares.
    - **Sell Orders**: If buy orders are present, and the bid price exceeds the initial bid VWAP, the algorithm places a sell order to lock in profits.

- **Spread Management**: The algorithm ensures that no orders are placed if the spread between the best ask and best bid exceeds a defined threshold (5 units).

- **Active Order Management**: If the number of active orders exceeds 4, or if there are unfilled orders, it attempts to cancel them to avoid potential losses.

- **Logging and Performance Tracking**: The algorithm logs its actions extensively, tracking:
    - Total profit, earnings, and spendings
    - Number of owned shares
    - Active and historical orders

## Class Structure

- **VWAPCalculator**: Calculates VWAP for both the ask and bid sides of the order book.

- **TradingContext**: Keeps track of the current state of the portfolio, including profit, earnings, spendings, and the number of owned shares.

- **OrderBook**: Represents the list of orders currently in the market, separated into bids (buy orders) and asks (sell orders).

- **ChildOrder**: Represents an individual order that is either active (in the market) or fulfilled (executed).

## Key Methods

- **evaluate(SimpleAlgoState state)**: Main logic for the algorithm. This method:
    1. Fetches the current order book.
    2. Calculates VWAP and sets initial VWAP values if they haven't been set.
    3. Evaluates whether to place a buy or sell order based on VWAP comparisons and spread thresholds.
    4. Cancels any unfilled or irrelevant orders if certain conditions are met.
    5. Logs the final state of the trading strategy after each evaluation cycle.

## Trading Logic

### Buy Logic
- The algorithm places buy orders if:
    - Fewer than 2 buy orders are currently active.
    - The best ask price is favorable compared to the ask VWAP threshold.

### Sell Logic
- The algorithm places sell orders if:
    - There are exactly 2 buy orders.
    - The best bid price is higher than or equal to the initial bid VWAP.

### Exit Condition
- If no orders have been created and the spread is wider than allowed (`maxSpreadAllowed = 5`), the algorithm exits without placing any orders.

## Limitations

- **Fixed Order Limits**: The algorithm allows a maximum of 6 orders, and the decision logic is tailored to manage no more than 2 simultaneous buy orders.
- **Static Spread Threshold**: The maximum allowed spread (5 units) is hardcoded and may not be flexible in volatile markets.
  
