# Money-Transfer
A REST Api for transferring money between accounts

To run:
Run main.java, the port the service is listening on will be printed to standard output.

Routes:
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
