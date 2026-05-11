package net.raohome;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class PaymentRecord {
	BigDecimal principal;
	BigDecimal interest;
	BigDecimal escrow;
	BigDecimal fees;

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public void setPrincipal(String principal) {
		this.principal = convertCurrency(principal);
	}

	public BigDecimal getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = convertCurrency(interest);
	}

	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}

	public BigDecimal getEscrow() {
		return escrow;
	}


	public void setEscrow(String escrow) {
		this.escrow = convertCurrency(escrow);
	}
	
	public void setEscrow(BigDecimal escrow) {
		this.escrow = escrow;
	}

	public BigDecimal getFees() {
		return fees;
	}
	public void setFees(String fees) {
		this.fees = convertCurrency(fees);
	}
	public void setFees(BigDecimal fees) {
		this.fees = fees;
	}

	@Override
	public String toString() {
		return String.format("Principal :%s, Interest :%s, Escrow :%s, Fees:%s", principal, interest, escrow, fees);
	}
	public static BigDecimal convertCurrency(String val) {
		NumberFormat cf = NumberFormat.getCurrencyInstance();
		if (cf instanceof DecimalFormat decimalFormat) {
			decimalFormat.setParseBigDecimal(true);
		}
		try {
			return (BigDecimal) cf.parse(val);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
