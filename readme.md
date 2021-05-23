
* 문제 분석
  * REST api 구현
    * 다수의 서버에서 다수의 인스턴스로 동작하더라도 기능에 문제가 없도록 해야 함
  * 다량의 트래픽에도 무리가 없도록 작성되어야 함


* 문제해결 전략
  * 서비스가 상태를 가지지 않게 해야 함
  * 필요한 리소스는 외부에서 주입받아서 사용해야 함
  * 비동기-논블로킹 프레임워크의 사용으로 다량의 트래픽에 대한 높은 처리 성능 구현
  * 커넥션 풀을 이용하여 시간당 사용자 요청 수에 맞게 최적화
  

* spring-webflux
  * 비동기-논블로킹을 지원하여 기존의 spring-mvc보다 높은 성능을 낼 수 있음
  * 동기/블로킹의 문제인 blocking이나 polling이 없음
  

* R2DBC
  * spring-webflux와 함께 reactive type을 지원하는 ORM으로 높은 동시성을 요구하는 서비스에 좋은 성능을 낼 수 있음
  * webflux를 사용하더라도 기존의 RDB ORM을 쓸 경우 DB 요청 시 논블로킹의 장점을 누릴 수 없음


* postgresql
  * NoSQL과 비교했을 때 데이터베이스의 일관성이 더 높음
  * 거래 정보 등의 종요한 정보를 다루는 DB는 안정성이 높아야 할 것으로 판단하여 선택
  * r2dbc가 초반에 지원한 DB가 H2, MySQL, Postgresql이었기 때문에 참고 자료가 많고 개발이 수월함


* HTTP 응답 정의
  * 전체 투자 상품 조회 API
    * 조회 성공 (200, ok)
    * 파라미터 없음 등의 잘못된 요청 (400, bad request)
    * 요청하는 상품 ID가 존재하지 않음 (404, not found)
  * 투자하기 API
    * 투자 성공 (200, ok)
    * 투자하기로 인해 상품이 soldout됨, responsebody의 status 값이 false로 설정, 남은 투자 금액만큼만 투자됨 (200, ok)
    * 모집 종료된 상품에 요청, responsebody의 status 값이 false, 투자 금액이 0원으로 표시됨 (200, ok)
    * 파라미터 없음 등의 잘못된 요청 (400, bad request)
    * 투자하는 상품 ID가 존재하지 않음 (404, not found)
  * 나의 투자상품 조회 API
    * 조회 성공 (200, ok)
    * 파라미터 없음 등의 잘못된 요청 (400, bad request)
    * 존재하지 않는 유저 ID로 조회 (404, not found)

  
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
  * JMeter를 사용하여 500개의 스레드로 실행
  * 최대 커넥션 풀 사이즈 50
  * 전체 투자 상품 조회 API
    * 평균 3902 밀리초
  * 투자하기 API
    * 평균 3636 밀리초
  * 나의 투자상품 조회 API
    * 평균 4190 밀리초

