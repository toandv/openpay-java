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
package mx.openpay.core.client;

import static mx.openpay.client.utils.SearchParams.search;
import static mx.openpay.core.client.TestConstans.API_KEY;
import static mx.openpay.core.client.TestConstans.CUSTOMER_CHARGE_ID;
import static mx.openpay.core.client.TestConstans.CUSTOMER_ID;
import static mx.openpay.core.client.TestConstans.ENDPOINT;
import static mx.openpay.core.client.TestConstans.MERCHANT_CHARGE_ID;
import static mx.openpay.core.client.TestConstans.MERCHANT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.TimeZone;

import junit.framework.Assert;
import mx.openpay.client.Address;
import mx.openpay.client.Card;
import mx.openpay.client.Charge;
import mx.openpay.client.core.OpenpayAPI;
import mx.openpay.client.core.operations.ChargeOperations;
import mx.openpay.client.core.requests.card.CreateCardParams;
import mx.openpay.client.core.requests.transactions.CreateCardChargeParams;
import mx.openpay.client.core.requests.transactions.RefundParams;
import mx.openpay.client.exceptions.OpenpayServiceException;
import mx.openpay.client.exceptions.ServiceUnavailableException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author elopez
 */
public class ChargesOperationsTest {

    OpenpayAPI api;

    ChargeOperations charges;

    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Mexico/General"));
        this.api = new OpenpayAPI(ENDPOINT, API_KEY, MERCHANT_ID);
        this.charges = this.api.charges();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreate_Customer_WithId_Old() throws ServiceUnavailableException, OpenpayServiceException {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";

        List<Card> cards = this.api.cards().list(CUSTOMER_ID, search().offset(0).limit(10));
        Assert.assertNotNull(cards);

        String orderId = String.valueOf(System.currentTimeMillis());
        Charge transaction = this.charges.create(CUSTOMER_ID, cards.get(0).getId(), amount, desc, orderId);
        Assert.assertNotNull(transaction);
        Assert.assertEquals(amount, transaction.getAmount());
        Assert.assertEquals(desc, transaction.getDescription());
    }

    @Test
    public void testCreate_Customer_WithId() throws ServiceUnavailableException, OpenpayServiceException {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";

        List<Card> cards = this.api.cards().list(CUSTOMER_ID, search().offset(0).limit(10));
        Assert.assertNotNull(cards);

        String orderId = String.valueOf(System.currentTimeMillis());
        CreateCardChargeParams charge = new CreateCardChargeParams()
                .customerId(CUSTOMER_ID)
                .cardId(cards.get(0).getId())
                .amount(amount)
                .description(desc)
                .orderId(orderId);
        Charge transaction = this.charges.create(charge);
        Assert.assertNotNull(transaction);
        Assert.assertEquals(amount, transaction.getAmount());
        Assert.assertEquals(desc, transaction.getDescription());
    }

    @Test
    public void testCreate_Customer_WithCard() throws Exception {
        Address address = this.createAddress();

        CreateCardParams card = new CreateCardParams()
                .cardNumber("5243385358972033")
                .holderName("Juanito Pérez Nuñez")
                .cvv2("111")
                .expirationMonth(9)
                .expirationYear(14)
                .address(address);

        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());
        Charge deposit = this.charges.create(new CreateCardChargeParams()
                .customerId(CUSTOMER_ID)
                .card(card)
                .amount(amount)
                .description(desc)
                .orderId(orderId));
        assertNotNull(deposit);
        assertNotNull(deposit.getCard());
        assertNull(deposit.getCard().getCvv2());
        assertNull(deposit.getCard().getId());
    }

    @Test
    public void testCreate_Customer_NoCardOrId() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());
        try {
            this.charges.create(new CreateCardChargeParams()
                    .customerId(CUSTOMER_ID)
                    .amount(amount)
                    .description(desc)
                    .orderId(orderId));
            fail();
        } catch (OpenpayServiceException e) {
            assertEquals(422, e.getHttpCode().intValue());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreate_Customer_NoId_Old() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());
        try {
            this.charges.create(CUSTOMER_ID, (String) null, amount, desc, orderId);
            fail();
        } catch (OpenpayServiceException e) {
            assertEquals(422, e.getHttpCode().intValue());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRefund_Customer_Old() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        List<Card> cards = this.api.cards().list(CUSTOMER_ID, search().offset(0).limit(10));
        Assert.assertNotNull(cards);
        String orderId = String.valueOf(System.currentTimeMillis());

        Charge transaction = this.charges.create(new CreateCardChargeParams()
                .amount(amount)
                .description(desc)
                .orderId(orderId)
                .customerId(CUSTOMER_ID)
                .cardId(cards.get(0).getId()));
        String originalTransactionId = transaction.getId();
        Assert.assertNotNull(transaction);
        assertNull(transaction.getRefund());

        transaction = this.charges.refund(CUSTOMER_ID, transaction.getId(), "cancelacion (ignored description)", null);
        Assert.assertNotNull(transaction.getRefund());

        transaction = this.charges.get(CUSTOMER_ID, originalTransactionId);
        assertNotNull(transaction.getRefund());
    }

    @Test
    public void testRefund_Customer() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        List<Card> cards = this.api.cards().list(CUSTOMER_ID, search().offset(0).limit(10));
        Assert.assertNotNull(cards);
        String orderId = String.valueOf(System.currentTimeMillis());

        Charge transaction = this.charges.create(new CreateCardChargeParams()
                .amount(amount)
                .description(desc)
                .orderId(orderId)
                .customerId(CUSTOMER_ID)
                .cardId(cards.get(0).getId()));
        String originalTransactionId = transaction.getId();
        Assert.assertNotNull(transaction);
        assertNull(transaction.getRefund());

        transaction = this.charges.refund(new RefundParams()
                .customerId(CUSTOMER_ID)
                .chargeId(transaction.getId()));
        Assert.assertNotNull(transaction.getRefund());

        transaction = this.charges.get(CUSTOMER_ID, originalTransactionId);
        assertNotNull(transaction.getRefund());
    }

    @Test
    public void testGet_Customer() throws Exception {
        Charge deposit = this.charges.get(CUSTOMER_ID, "tkktbgh01gx5mpnv7yc3");
        assertNotNull(deposit);
    }

    @Test
    public void testList_Customer() throws Exception {
        List<Charge> deposits = this.charges.list(CUSTOMER_ID, search().limit(3));
        assertEquals(3, deposits.size());

        deposits = this.charges.list(CUSTOMER_ID, search().limit(5));
        assertEquals(5, deposits.size());
    }

    @Test
    public void testList_Customer_Empty() throws Exception {
        List<Charge> deposits = this.charges.list(CUSTOMER_ID, search().limit(2).offset(10000));
        assertTrue(deposits.isEmpty());
    }

    @Test
    public void testList_CustomerDoesNotExist() throws Exception {
        try {
            this.charges.list("blahblahblah", search().limit(2));
            fail();
        } catch (OpenpayServiceException e) {
            assertEquals(404, e.getHttpCode().intValue());
        }
    }

    @Test
    public void testCreate_Merchant_WithCard() throws Exception {
        Address address = this.createAddress();

        CreateCardParams card = new CreateCardParams();
        card.cardNumber("5243385358972033");
        card.holderName("Juanito Pérez Nuñez");
        card.cvv2("111");
        card.expirationMonth(9);
        card.expirationYear(14);
        card.address(address);

        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());
        Charge deposit = this.charges.create(new CreateCardChargeParams()
                .card(card)
                .amount(amount)
                .description(desc)
                .orderId(orderId));
        assertNotNull(deposit);
        assertNotNull(deposit.getCard());
        assertNull(deposit.getCard().getCvv2());
        assertNull(deposit.getCard().getId());
    }

    private Address createAddress() {
        Address address = new Address();
        address.setCity("Querétaro");
        address.setLine1("Camino #11 - 01");
        address.setPostalCode("76090");
        address.setState("Queretaro");
        address.setCountryCode("MX");
        return address;
    }

    @Test
    public void testCreate_Merchant_NoCard() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());
        try {
            this.charges.create(new CreateCardChargeParams()
                    .amount(amount)
                    .description(desc)
                    .orderId(orderId));
            fail();
        } catch (OpenpayServiceException e) {
            assertEquals(422, e.getHttpCode().intValue());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRefund_Merchant_Old() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());

        Charge transaction = this.charges.create(this.getCard(), amount, desc, orderId);
        String originalTransactionId = transaction.getId();
        Assert.assertNotNull(transaction);
        assertNull(transaction.getRefund());

        transaction = this.charges.refund(transaction.getId(), "cancelacion (ignored description)", null);
        Assert.assertNotNull(transaction.getRefund());

        transaction = this.charges.get(originalTransactionId);
        assertNotNull(transaction.getRefund());
    }

    @Test
    public void testRefund_Merchant() throws Exception {
        BigDecimal amount = new BigDecimal("10000.00");
        String desc = "Pago de taxi";
        String orderId = String.valueOf(System.currentTimeMillis());

        Charge transaction = this.charges.create(new CreateCardChargeParams()
                .amount(amount)
                .description(desc)
                .orderId(orderId)
                .card(this.getCreateCard()));
        String originalTransactionId = transaction.getId();
        Assert.assertNotNull(transaction);
        assertNull(transaction.getRefund());

        transaction = this.charges.refund(new RefundParams().chargeId(transaction.getId()));
        Assert.assertNotNull(transaction.getRefund());

        transaction = this.charges.get(originalTransactionId);
        assertNotNull(transaction.getRefund());
    }

    @Test
    public void testGet_Merchant() throws Exception {
        Charge sale = this.charges.get(MERCHANT_CHARGE_ID);
        assertNotNull(sale);
    }

    @Test
    public void testGet_MerchantForCustomer() throws Exception {
        try {
            this.charges.get(CUSTOMER_CHARGE_ID);
        } catch (OpenpayServiceException e) {
            assertEquals(404, e.getHttpCode().intValue());
        }
    }

    @Test
    public void testList_Merchant() throws Exception {
        List<Charge> sale = this.charges.list(search().limit(3));
        assertEquals(3, sale.size());

        sale = this.charges.list(search().limit(5));
        assertEquals(5, sale.size());
    }

    private Card getCard() {
        Address address = this.createAddress();

        Card card = new Card();
        card.setCardNumber("5243385358972033");
        card.setHolderName("Holder");
        card.setExpirationMonth("12");
        card.setExpirationYear("15");
        card.setCvv2("123");
        card.setAddress(address);
        return card;
    }

    /**
     * @return
     */
    private CreateCardParams getCreateCard() {
        Address address = this.createAddress();
        CreateCardParams card = new CreateCardParams();
        card.cardNumber("5243385358972033");
        card.holderName("Holder");
        card.expirationMonth(12);
        card.expirationYear(15);
        card.cvv2("123");
        card.address(address);
        return card;
    }

}