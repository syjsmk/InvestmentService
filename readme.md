
* table
```
CREATE TABLE investment_goods(
    goods_id serial PRIMARY KEY,
    title varchar(30) NOT NULL,
    total_investing_amount bigint,
    status bool,
    started_at timestamp,
    finished_at timestamp
);
```
```
CREATE TABLE user_investment_goods(
    id serial PRIMARY KEY,
    user_id integer,
    goods_id integer,
    user_investing_amount bigint,
    invest_date timestamp
);
```