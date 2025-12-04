package Server;

import java.io.Serializable;
import java.sql.Date;

public class Order implements Serializable {

	private static final long serialVersionUID = 1L;

	private int orderNumber;
	private Date orderDate;
	private int numberOfGuests;
	private int confirmationCode;
	private int subscriberId;
	private Date dateOfPlacingOrder;

	public Order(int orderNumber,
            Date orderDate,
            int numberOfGuests,
            int confirmationCode,
            int subscriberId,
            Date dateOfPlacingOrder) {

   this.orderNumber = orderNumber;
   this.orderDate = orderDate;
   this.numberOfGuests = numberOfGuests;
   this.confirmationCode = confirmationCode;
   this.subscriberId = subscriberId;
   this.dateOfPlacingOrder = dateOfPlacingOrder;
}


	public int getOrderNumber() {
		return orderNumber;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	public int getConfirmationCode() {
		return confirmationCode;
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public Date getDateOfPlacingOrder() {
		return dateOfPlacingOrder;
	}

	@Override
	public String toString() {
		return "Order{" +
            "orderNumber=" + orderNumber +
            ", orderDate=" + orderDate +
            ", numberOfGuests=" + numberOfGuests +
            ", confirmationCode='" + confirmationCode + '\'' +
            ", subscriberId=" + subscriberId +
            ", dateOfPlacingOrder=" + dateOfPlacingOrder +
            '}';
	}

}