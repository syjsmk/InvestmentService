
* table
```
CREATE TABLE user_investment_goods(
    id serial PRIMARY KEY,
    title varchar(30) NOT NULL,
    total_investing_amount bigint,
    user_investing_amount bigint,
    invest_date timestamp
);
```
```
CREATE TABLE user_investment_goods(
    user_id serial,
    product_id serial,
    user_investing_amount bigint,
    invest_date timestamp
);
```