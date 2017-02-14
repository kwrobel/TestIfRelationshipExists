/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testifrelationshipexists;

import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import testifrelationshipexists.dal.CustomerJpaController;
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
            hasCustomerPurchaseOrders = hasPurchaseOrders();
        }
        return hasCustomerPurchaseOrders;
    }

    private boolean hasPurchaseOrders() {
        if (customer != null) {
            // Ultimate question: does a customer have Purchase Orders?
            Query hasPoQry = customerFacade.getEntityManager().createNamedQuery("Customer.hasPurchaseOrdersViaEmpty");
            hasPoQry.setParameter("customerId", customer);
            try {
                Object singleResult = hasPoQry.getSingleResult();
            } catch (NoResultException ex) {
                return false;
            }
        }
        return true;
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
        if (customer != null) {
            Query poQry = poFacade.getEntityManager().createNamedQuery("PurchaseOrder.findByCustomerId");
            poQry.setParameter("customerId", customer);
            List<PurchaseOrder> resultList = poQry.getResultList();
            for (PurchaseOrder po : resultList) {
                System.out.println("Order: " + po.getOrderNum());
            }
        }

    }

    private void showCustomerListUsingExists() {

        Query noPoCustsQry = customerFacade.getEntityManager().createNamedQuery("Customer.findWithoutPo");
        List<Customer> noPoCusts = noPoCustsQry.getResultList();
        showCustomerLists(noPoCusts, "CUSTOMERS WITHOUT PURCHASE ORDERS (EXISTS-VERSION)");

        Query poCustsQry = customerFacade.getEntityManager().createNamedQuery("Customer.findWithPo");
        List<Customer> poCusts = poCustsQry.getResultList();
        showCustomerLists(poCusts, "CUSTOMERS WITH PURCHASE ORDERS (EXISTS-VERSION)");

    }

    private void showCustomerListUsingIsEmpty() {

        Query noPoCustsQry = customerFacade.getEntityManager().createNamedQuery("Customer.findPoEmpty");
        List<Customer> noPoCusts = noPoCustsQry.getResultList();
        showCustomerLists(noPoCusts, "CUSTOMERS WITHOUT PURCHASE ORDERS (EMPTY-VERSION)");

        Query poCustsQry = customerFacade.getEntityManager().createNamedQuery("Customer.findPoNotEmpty");
        List<Customer> poCusts = poCustsQry.getResultList();
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
