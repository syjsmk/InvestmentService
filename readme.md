
* 문제 분석
  * REST api 구현
    * 다수의 서버에서 다수의 인스턴스로 동작하더라도 기능에 문제가 없도록 해야 함
  * 다량의 트래픽에도 무리가 없도록 작성되어야 함


* 문제해결 전략
  * 서비스가 상태를 가지지 않게 해야 함
  * 필요한 리소스는 외부에서 주입받아서 사용해야 함
  * 비동기-논블로킹 프레임워크의 사용으로 다량의 트래픽에 대한 높은 처리 성능 구현 
  

* spring-webflux
  * 비동기-논블로킹을 지원하여 기존의 spring-mvc보다 높은 성능을 낼 수 있음
  * 동기/블로킹의 문제인 blocking이나 polling이 없음
  

* R2DBC
  * spring-webflux와 함께 reactive type을 지원하는 ORM으로 높은 동시성을 요구하는 서비스에 좋은 성능을 낼 수 있음
  * webflux를 사용하더라도 기존의 RDB ORM을 쓸 경우 DB 요청 시 논블로킹의 장점을 누릴 수 없음


* postgresql
  *


* HTTP 요청 정의
  * 


* HTTP 응답 정의
  * 

  
* 테이블 정의
  * user_investment_goods
    * 사용자의 상품 투자 내역 저장
  * investment_goods
    * 투자 상품 관련 정보 저장
  * 상품 ID는 사용자의 투자상품, 전체 투자상품에 공통되게 있으며 고유 값으로 생각되므로 두 테이블을 조인하는 값으로 사용함
  * 현재 투자 상품 조회 API에서 조회하는 데이터 중 현재 모집금액, 투자자 수의 정보는 필드를 따로 둘 경우 투자하기 API 실행 시 매번 업데이트가 필요하므로 조회시 계산하여 사용함 
```
CREATE TABLE user_investment_goods(
    id serial PRIMARY KEY,
    user_id integer,
    goods_id integer,
    user_investing_amount bigint,
    invest_date timestamp
);
```
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

* 부하 테스트 결과
  * JMeter를 사용하여 1000개의 스레드로 실행
  * 전체 투자 상품 조회 API
    * 평균 15~20 밀리초
  * 투자하기 API
    * 평균 15~20 밀리초
  * 나의 투자상품 조회 API
    * 평균 15~20 밀리초

