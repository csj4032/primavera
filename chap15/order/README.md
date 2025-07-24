## chap14

### OrderServer - WebFlux

### Reference : https://github.com/spring-projects-experimental/spring-fu

### R2DBC : https://github.com/mariadb-corporation/mariadb-connector-r2dbc

### R2DBC Pool : https://github.com/r2dbc/r2dbc-pool

### Create Table

```sql
CREATE TABLE `ORDERS` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` bigint(20) NOT NULL,
  `PRODUCT_ID` bigint(20) NOT NULL,
  `AMOUNT` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

TRUNCATE `primavera`.`ORDERS`;

`````