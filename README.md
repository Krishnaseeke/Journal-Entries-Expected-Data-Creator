# Journal-Entries-Expected-Data-Creator
Steps To Use
1. Clone the Repo
2. In Main Class, pass the number of Dr and Cr Account should be picked for JE
3. Add Transaction Type as "Transaction" and Pass Adjustment Type as null / Add Transacation Type as "Adjustments" and Pass Adjustment as "cr"/"dr"
4. Run the Code
5. Expected Data will be generated in Excel in Resource Folder

Excel Sheet Details:
1. Unique Accounts - This sheet contains all the accounts which we can use for JE

Code Generated Sheets:
1. JEUI - This sheet contains JE Transaction Line Items with Amounts
2. Impact Sheet - After Creating JE Transaction, the Accounts which are used in that specific JE will be highlighted with blue color in this sheet
3. Total Balances - After JE, User can compare the Final Balances of Trail Balance, Balance Sheet and Profit and Loss. Along with this Using Impact Sheet COA main screen will be validated

**Note: This Repo contains Currently Backup4_1.vyp Backup Accounts. Before Performing JE Transactions user should restore the attached backup to create JE and Validate the Values between expected and Actual Data**

Here, Code will generate the Expected Data for a Combination and same JE should be performed on the screen by the user using JEUI sheet and then compare the values.


**How to Use this code According to your backup**
1. Create your backup using vapar [Refer attached file]
2. Add the Accounts in Unique Accounts sheet[Excel attached in the Repo] with Balances
3. Then use it for for personalized JE Creations to Compare Expected and Actual Data.
