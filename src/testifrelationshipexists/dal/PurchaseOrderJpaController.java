/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testifrelationshipexists.dal;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import testifrelationshipexists.dal.exceptions.NonexistentEntityException;
import testifrelationshipexists.dal.exceptions.PreexistingEntityException;
import testifrelationshipexists.entity.Product;
import testifrelationshipexists.entity.Customer;
import testifrelationshipexists.entity.PurchaseOrder;
import testifrelationshipexists.entity.PurchaseOrder_;

/**
 *
 * @author kuw
 */
public class PurchaseOrderJpaController implements Serializable {

    public PurchaseOrderJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PurchaseOrder purchaseOrder) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Product productId = purchaseOrder.getProductId();
            if (productId != null) {
                productId = em.getReference(productId.getClass(), productId.getProductId());
                purchaseOrder.setProductId(productId);
            }
            Customer customerId = purchaseOrder.getCustomerId();
            if (customerId != null) {
                customerId = em.getReference(customerId.getClass(), customerId.getCustomerId());
                purchaseOrder.setCustomerId(customerId);
            }
            em.persist(purchaseOrder);
            if (productId != null) {
                productId.getPurchaseOrderList().add(purchaseOrder);
                productId = em.merge(productId);
            }
            if (customerId != null) {
                customerId.getPurchaseOrderList().add(purchaseOrder);
                customerId = em.merge(customerId);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPurchaseOrder(purchaseOrder.getOrderNum()) != null) {
                throw new PreexistingEntityException("PurchaseOrder " + purchaseOrder + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PurchaseOrder purchaseOrder) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PurchaseOrder persistentPurchaseOrder = em.find(PurchaseOrder.class, purchaseOrder.getOrderNum());
            Product productIdOld = persistentPurchaseOrder.getProductId();
            Product productIdNew = purchaseOrder.getProductId();
            Customer customerIdOld = persistentPurchaseOrder.getCustomerId();
            Customer customerIdNew = purchaseOrder.getCustomerId();
            if (productIdNew != null) {
                productIdNew = em.getReference(productIdNew.getClass(), productIdNew.getProductId());
                purchaseOrder.setProductId(productIdNew);
            }
            if (customerIdNew != null) {
                customerIdNew = em.getReference(customerIdNew.getClass(), customerIdNew.getCustomerId());
                purchaseOrder.setCustomerId(customerIdNew);
            }
            purchaseOrder = em.merge(purchaseOrder);
            if (productIdOld != null && !productIdOld.equals(productIdNew)) {
                productIdOld.getPurchaseOrderList().remove(purchaseOrder);
                productIdOld = em.merge(productIdOld);
            }
            if (productIdNew != null && !productIdNew.equals(productIdOld)) {
                productIdNew.getPurchaseOrderList().add(purchaseOrder);
                productIdNew = em.merge(productIdNew);
            }
            if (customerIdOld != null && !customerIdOld.equals(customerIdNew)) {
                customerIdOld.getPurchaseOrderList().remove(purchaseOrder);
                customerIdOld = em.merge(customerIdOld);
            }
            if (customerIdNew != null && !customerIdNew.equals(customerIdOld)) {
                customerIdNew.getPurchaseOrderList().add(purchaseOrder);
                customerIdNew = em.merge(customerIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = purchaseOrder.getOrderNum();
                if (findPurchaseOrder(id) == null) {
                    throw new NonexistentEntityException("The purchaseOrder with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PurchaseOrder purchaseOrder;
            try {
                purchaseOrder = em.getReference(PurchaseOrder.class, id);
                purchaseOrder.getOrderNum();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The purchaseOrder with id " + id + " no longer exists.", enfe);
            }
            Product productId = purchaseOrder.getProductId();
            if (productId != null) {
                productId.getPurchaseOrderList().remove(purchaseOrder);
                productId = em.merge(productId);
            }
            Customer customerId = purchaseOrder.getCustomerId();
            if (customerId != null) {
                customerId.getPurchaseOrderList().remove(purchaseOrder);
                customerId = em.merge(customerId);
            }
            em.remove(purchaseOrder);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PurchaseOrder> findPurchaseOrderEntities() {
        return findPurchaseOrderEntities(true, -1, -1);
    }

    public List<PurchaseOrder> findPurchaseOrderEntities(int maxResults, int firstResult) {
        return findPurchaseOrderEntities(false, maxResults, firstResult);
    }

    private List<PurchaseOrder> findPurchaseOrderEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PurchaseOrder.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public PurchaseOrder findPurchaseOrder(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PurchaseOrder.class, id);
        } finally {
            em.close();
        }
    }

    public int getPurchaseOrderCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PurchaseOrder> rt = cq.from(PurchaseOrder.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<PurchaseOrder> findPurchaseOrderEntities(Customer customer) {
        EntityManager em = getEntityManager();
        List resultList = null;
        if (customer != null) {

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PurchaseOrder> cq = cb.createQuery(PurchaseOrder.class);
            Root<PurchaseOrder> fromPurchaseOrder = cq.from(PurchaseOrder.class);
            cq.select(fromPurchaseOrder);
            cq.where(cb.equal(fromPurchaseOrder.get(PurchaseOrder_.customerId),customer));
            TypedQuery<PurchaseOrder> poQry = em.createQuery(cq);

            try {
                resultList = poQry.getResultList();
            } finally {
                em.close();
            }

        // Alternative IF entity is still attached to the EntityManager: just
        // use the getter method. But here the entity has already been detached,
        // so we need to merge it back in. The resulting query looks to be similar
        // to what the Criteria Query above produces. Try it...
/*
        System.out.println("Merge customer into mergedCustomer...");
        Customer mergedCustomer = em.merge(customer);
        System.out.println("Merge complete... calling getter method on mergedCustomer...");
        resultList = mergedCustomer.getPurchaseOrderList();
        System.out.println("Getter method finished.");
*/
        }
        return resultList;
    }

}
