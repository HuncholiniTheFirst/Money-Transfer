# Money-Transfer
A REST Api for transferring money between accounts

# To run:

Run main.java, the port the service is listening on will be printed to standard output.

You can then use Postman or a preferred tool to query the API

# Routes:

  POST Routes
  
  /transfer
  
  /account/new
  
  PUT Routes
  
  /deposit
  
  /withdraw
  
  GET Routes
  
  /accounts
  
  /accounts/:id
  
  /accounts/:accID/transactions
  
  /accounts/:accID/transactions/:transID
  
  /transactions
  
  The service is instantiated with some test data, this can be viewed in initialiseTestData() in MoneyTransferService.java
  or you can use the /accounts endpoint to list all accounts


# JSON body examples:

New account

{
 "currency" : "GBP",
 "user" : "New User",
 "balance" : "999999"
}

Transfer

{
 "amount" : "500000",
 "fromAccountID" : "2cjpLZ",
 "toAccountID" : "0tUI9Q"
}

Deposit

{
 "amount" : "100000",
 "toAccount" : "100abc"
}


Withdraw

{
 "amount" : "100000",
 "fromAccount" : "100abc"
}
