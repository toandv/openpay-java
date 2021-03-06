/*
 * Copyright 2013 Opencard Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mx.openpay.client.core.requests.transactions;

import java.math.BigDecimal;
import java.util.Date;

import mx.openpay.client.Customer;
import mx.openpay.client.core.requests.RequestBuilder;
import mx.openpay.client.enums.ChargeMethods;

/**
 * Parameters to create a new charge that will be paid through a Bank transfer. The charge will be created with a
 * payment method with information for the customer to make the transfer.
 * @author elopez
 */
public class CreateBankChargeParams extends RequestBuilder {

    public CreateBankChargeParams() {
        this.with("method", ChargeMethods.BANK_ACCOUNT.name().toLowerCase());
    }

    /**
     * The amount to charge the customer. Required. The customer should pay this exact amount.
     */
    public CreateBankChargeParams amount(final BigDecimal amount) {
        return this.with("amount", amount);
    }

    /**
     * A description for the charge. Optional.
     */
    public CreateBankChargeParams description(final String description) {
        return this.with("description", description);
    }

    /**
     * A custom unique ID to identify the charge. Optional.
     */
    public CreateBankChargeParams orderId(final String orderId) {
        return this.with("order_id", orderId);
    }

	/**
	 * Due date for this charge.
	 */
	public CreateBankChargeParams dueDate(final Date dueDate) {
		return this.with("due_date", dueDate);
	}
	
	public CreateBankChargeParams customer(final Customer customer) {
	    return this.with("customer", customer);
	}
}
