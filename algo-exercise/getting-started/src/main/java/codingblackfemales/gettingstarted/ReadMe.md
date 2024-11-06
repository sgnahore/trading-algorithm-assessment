# Welcome to MyAlgoLogic. 

This is a simple trading algorithm implementation designed to evaluate market conditions and place orders based on a few key strategies. The algorithm follows a Volume Weighted Average Price (VWAP) strategy and dynamically responds to market fluctuations. 

## Table of contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Backtesting](#backtesting)
- [Key Features](#key-features) 
- [Order Scenarios](#order-scenarios)


## Requirements

- JDK 17 or higher
- Apache Maven 3.6 or newer

## Installation
1. Clone the repository:
```bash
git clone git@github.com:sgnahore/trading-algorithm-assessment.git
```
2. Install dependencies:
```bash
npm install
```
3. Navigate to the project directory:  
```bash
cd trading-algorithm-assessment
```
4. Build the project with Maven:
```bash
mvn clean install
```
or Test all 
```bash
algo-exercise projects: ./mvnw test --projects algo-exercise
```

## Backtesting

There are several tests available in the MyAlgoBackTest class, created to ensure all potential edge cases are considered:
	
	•	createSpreadTooWide triggers a market exit when the spread is too wide.
	•	createBuyAndSell simulates buying below the askVWAP threshold and selling at the highest price.
	•	createBuyOkayPrice simulates buying above the askVWAP threshold but less than the askVWAP and selling at the highest 

  • createBuyButNoSellTick simulates buying but not sellings
  • createNoBuyTick triggers a market exit

**Testing:**
You can run the tests with the following Maven command:
`mvn test`

## Key Features

- **VWAP Calculation**: The algorithm uses a VWAP strategy to evaluate whether current prices offer good buying or selling opportunities. The VWAP is recalculated for each evaluation cycle, and initial thresholds are set to assess if market conditions are favourable for trading.

- **Order Placement Logic**:
    - **Buy Orders**: If fewer than two buy orders are present and the ask price is below or near the VWAP, the algorithm attempts to buy shares.
    - **Sell Orders**: If buy orders are present, and the bid price exceeds the initial bid VWAP, the algorithm places a sell order to lock in profits.

- **Spread Management**: The algorithm ensures that no orders are placed if the spread between the best ask and best bid exceeds a defined threshold (5 units).

- **Active Order Management**: If the number of active orders exceeds 4, or if there are unfilled orders, it attempts to cancel them to avoid potential losses.

- **Exit Condition**: If no orders have been created and the spread is wider than allowed (`maxSpreadAllowed = 5`), the algorithm exits without placing any orders.

- **Logging and Performance Tracking**:
The algorithm logs its actions extensively, tracking:
    - Total profit, earnings, and spendings
    - Number of owned shares
    - Active and historical orders


## Order Scenarios

**Buying on the ask side**
<br> _Scenario 1: Good Price_
<br> Given the created child orders are less than 2
<br> And the ask price is less than the ask VWAP threshold
<br> When entering the market and creating a buy order on the ask side
<br> Then buy more at this price

_Scenario 2: Okay Price_
<br> Given the created child orders are less than 2
<br> And the ask price is less than the ask VWAP
<br> When entering the market and creating a buy order on the ask side
<br> Then buy a good amount at this price

<br> _Scenario 3: No buy_
<br>Given that none of this criteria is met
<br>And whether we have orders created or not
<br>Then we should exit the market
![alt text](https://export-download.canva.com/4dRxs/DAGQ2B4dRxs/197/0/0010-7235478809881405674.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJHKNGJLC2J7OGJ6Q%2F20241105%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20241105T092612Z&X-Amz-Expires=47631&X-Amz-Signature=12af9e1953cfa89b57363a49667b5f2e15601415b9bbec1f0ed1dcd7f75275e7&X-Amz-SignedHeaders=host&response-content-disposition=attachment%3B%20filename%2A%3DUTF-8%27%27PROJECT%2520DEMO.png&response-expires=Tue%2C%2005%20Nov%202024%2022%3A40%3A03%20GMT)

### Selling on the bid side
<br> _Scenario 1: Good Price_
<br> Given the created child orders are more than or equal to 2
<br> And the ask price is less than the ask VWAP
<br> When entering the market and creating a buy order on the ask side
<br> Then buy a good amount at this price
![alt text](https://export-download.canva.com/4dRxs/DAGQ2B4dRxs/199/0/0014-6638751864098540120.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJHKNGJLC2J7OGJ6Q%2F20241105%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20241105T125018Z&X-Amz-Expires=37801&X-Amz-Signature=6edf1c8dbb0d358842bfbfaf844c1829d29dc917e59cd86001b19eec6532865e&X-Amz-SignedHeaders=host&response-content-disposition=attachment%3B%20filename%2A%3DUTF-8%27%27PROJECT%2520DEMO.png&response-expires=Tue%2C%2005%20Nov%202024%2023%3A20%3A19%20GMT)

### Cancelling child orders
<br> _Scenario: Too many orders/Unfilled orders_
<br> Given that we have 4 active child orders or any unfilled orders
<br> Cancel the extra active order or unfilled order
<br> Cancelling orders if they become irrelevant or exceed limits.

