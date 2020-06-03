## chap15

### OrderServer - WebFlux

### Reference - https://github.com/spring-projects-experimental/spring-fu

### Create Table

```sql
CREATE TABLE `ORDERS` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` bigint(20) NOT NULL,
  `PRODUCT_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
```