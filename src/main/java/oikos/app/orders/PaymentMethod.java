package oikos.app.orders;

public enum PaymentMethod {
  BankTransfer("Transfert Bancaire"),
  CreditCard("Carte de crédit");
  private String name;

  PaymentMethod(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
