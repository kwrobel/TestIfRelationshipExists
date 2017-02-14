# Test If Relationship Exists
PoC Project Testing For Existence of Child Entities from a Parent

This project is for personal use only. I am simply testing how to determine whether a parent entity has any child records. In this case, I am testing the relationship between the Customer entity and the PurchaseOrder entity. The question I asked myself is: can I test whether a Customer has Purchase Orders using the `EXISTS` clause (as I know from SQL standard) or is there a different way? In doing so, I came up with two alternatives for testing: `EXISTS(subquery)` logic and the `IS EMPTY` on the parent's collection field of the child entity. Both produce different queries, as expected. One uses the actual `EXISTS` SQL keyword, the other produces a sub-query using the `COUNT(*)` function. Unfortunately, the SAMPLE database is too small to test any performance impact. However, according to most database performance articles I've read online, `EXISTS` is always preferred over `COUNT(*)` as the latter will have to traverse all records of the relationship.

The project contains OpenJPA 2.4.2 library, a Derby Embedded JDBC driver, and a snapshot of the SAMPLE database for Derby.

To run the project, simply clone the GIT repoitory and run the program. Follow the output and inspect the code, particularly the Named Queries that were added by myself.
