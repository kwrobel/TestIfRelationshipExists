/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testifrelationshipexists;

import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import testifrelationshipexists.dal.CustomerJpaController;
import testifrelationshipexists.dal.util.ExistenceMethodType;
import testifrelationshipexists.dal.util.PurchaseOrderInclusionMethodType;
import testifrelationshipexists.dal.PurchaseOrderJpaController;
import testifrelationshipexists.entity.Customer;
import testifrelationshipexists.entity.PurchaseOrder;

/**
 *
 * @author kuw
 */
public class TestIfRelationshipExists {

    private final CustomerJpaController customerFacade;
    private final PurchaseOrderJpaController poFacade;
    private Customer customer;
    private Boolean hasCustomerPurchaseOrders;
    private static final int CUSTOMER_ID_WITH_POS = 3;
    private static final int CUSTOMER_ID_WITHOUT_POS = 25;

    public TestIfRelationshipExists(EntityManagerFactory emf) {
        customerFacade = new CustomerJpaController(emf);
        poFacade = new PurchaseOrderJpaController(emf);
    }

    public TestIfRelationshipExists() {
        this(Persistence.createEntityManagerFactory("TestIfRelationshipExistsPU"));
    }

    private void setCustomer(int id) {
        customer = customerFacade.findCustomer(id);
    }

    private Customer getCustomer() {
        return customer;
    }

    public Boolean getHasCustomerPurchaseOrders() {
        if (hasCustomerPurchaseOrders == null) {
            hasCustomerPurchaseOrders = customerFacade.hasPurchaseOrders(customer);
        }
        return hasCustomerPurchaseOrders;
    }

    private void showPoStatus() {
        System.out.println();
        if (getHasCustomerPurchaseOrders()) {
            System.out.println("Customer " + customer.getName() + " (ID: " + customer.getCustomerId() + ") HAS purchase orders");
            showPurchaseOrders();
        } else {
            System.out.println("Customer " + customer.getName() + " (ID: " + customer.getCustomerId() + ") has NO purchase orders");
        }
    }

    private void showPurchaseOrders() {

        // Grab POs for an existing customer
            List<PurchaseOrder> poList = poFacade.findPurchaseOrderEntities(customer);
            for (PurchaseOrder po : poList) {
                System.out.println("Order: " + po.getOrderNum());
            }

    }

    private void showCustomerListUsingExists() {

        List<Customer> noPoCusts = customerFacade.findCustomerEntities(PurchaseOrderInclusionMethodType.DOES_NOT_HAVE, ExistenceMethodType.EXISTS);
        showCustomerLists(noPoCusts, "CUSTOMERS WITHOUT PURCHASE ORDERS (EXISTS-VERSION)");

        List<Customer> poCusts = customerFacade.findCustomerEntities(PurchaseOrderInclusionMethodType.DOES_HAVE, ExistenceMethodType.EXISTS);
        showCustomerLists(poCusts, "CUSTOMERS WITH PURCHASE ORDERS (EXISTS-VERSION)");

    }

    private void showCustomerListUsingIsEmpty() {

        List<Customer> noPoCusts = customerFacade.findCustomerEntities(PurchaseOrderInclusionMethodType.DOES_NOT_HAVE, ExistenceMethodType.EMPTY);
        showCustomerLists(noPoCusts, "CUSTOMERS WITHOUT PURCHASE ORDERS (EMPTY-VERSION)");

        List<Customer> poCusts = customerFacade.findCustomerEntities(PurchaseOrderInclusionMethodType.DOES_HAVE, ExistenceMethodType.EMPTY);
        showCustomerLists(poCusts, "CUSTOMERS WITH PURCHASE ORDERS (EMPTY-VERSION)");

    }

    private void showCustomerLists(List<Customer> customers, String msg) {

        System.out.println();
        System.out.println(msg);
        System.out.println(new String(new char[msg.length()]).replace("\0", "="));
        for (Customer customer : customers) {
            System.out.print(customer.getName());
            System.out.print(", ID: ");
            System.out.print(customer.getCustomerId());
            System.out.println();
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Grab a factory to pass along to each Unit Of Work
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("TestIfRelationshipExistsPU");
        TestIfRelationshipExists uow;

        uow = new TestIfRelationshipExists(emf);
        uow.setCustomer(CUSTOMER_ID_WITH_POS);
        uow.showPoStatus();

        uow = new TestIfRelationshipExists(emf);
        uow.setCustomer(CUSTOMER_ID_WITHOUT_POS);
        uow.showPoStatus();

        // Just testing other queries
        uow.showCustomerListUsingExists();
        uow.showCustomerListUsingIsEmpty();
    }

}
