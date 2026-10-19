# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

백엔드 전용 문서입니다. 인프라(Docker, 커넥터 등록)와 프론트엔드 연동 등 공통 내용은 루트의 `../CLAUDE.md`를 봅니다.

## 명령어

```bash
./gradlew bootRun
./gradlew build
./gradlew test
./gradlew test --tests "com.dreamhyuk.dream_order.domain.shop.service.ShopServiceTest"
./gradlew test --tests "*ShopServiceTest.saveMockData"
./gradlew compileJava      # QueryDSL Q클래스 재생성 (엔티티 변경 후)
```

- `bootRun`에는 MySQL, Redis(localhost:6379), Kafka, Elasticsearch가 모두 떠 있어야 합니다. Redis는 루트 `compose.yml`에 없으므로 따로 띄웁니다.
- `.gitignore`에 `*.yml`이 있어 `src/main/resources/application.yml`과 `src/test/resources/application.yml`은 git에 올라가지 않습니다. 설정 키를 추가·변경하면 커밋에 포함되지 않으니 따로 알려야 합니다.
- `scripts/register-connector.sh`는 소스 커넥터만 등록하는 예전 버전입니다. 루트의 `../scripts/register-connector.sh`를 사용합니다.

## 프레임워크

Spring Boot 3.5.10 / Java 17 (toolchain) / Gradle 8.11.1 wrapper. 패키지는 `com.dreamhyuk.dream_order`입니다.

- Web, Validation, Security + JJWT 0.12.3
- Spring Data JPA + MySQL, Flyway, QueryDSL 5 (jakarta)
- Spring Data Redis, Spring Session Redis
- Spring Data Elasticsearch, Spring Kafka
- Lombok
- 테스트: JUnit 5, H2

## 구조

`domain/<aggregate>/{controller,service,dto,repository,listener}` 형태이고, 엔티티와 리포지토리는 aggregate 루트에 둡니다. 공통 코드는 `global/` (config, jwt, exception, userdetails, init)에 둡니다.

**요청 흐름.** Controller가 요청 DTO를 `toCommand()`로 `*Command` 객체로 변환해 Service에 넘기고, Service가 엔티티의 생성·비즈니스 메서드를 호출한 뒤 응답 DTO로 변환해 반환합니다.

**인증.**
- Stateless JWT입니다. `JwtAuthenticationFilter`가 `Authorization: Bearer` 토큰을 검증하고 `CustomUserDetails`를 principal로 넣습니다. 컨트롤러에서는 `@AuthenticationPrincipal CustomUserDetails`의 `getMemberId()`로 사용자 ID를 얻습니다.
- 인증 없이 접근 가능한 경로는 **두 군데**에 등록되어 있습니다: `SecurityConfig`의 `permitAll()` 목록과 `JwtAuthenticationFilter` 안의 경로 목록. 공개 경로를 추가할 때는 둘 다 수정해야 합니다. 필터에만 빠지면 토큰 없는 요청이 401로 막힙니다. `SecurityConfig`에서는 role 기반 matcher보다 *앞에* 넣습니다.
- 권한은 `SecurityConfig`의 URL 규칙으로 강제합니다 (`/api/customers/**`는 ROLE_CUSTOMER, `/api/owners/**`는 ROLE_OWNER). 일부 컨트롤러에 `@PreAuthorize`가 있지만 `@EnableMethodSecurity`가 없어 동작하지 않습니다.
- access token은 30분, refresh token은 7일입니다. refresh token은 `refreshToken` 쿠키로 전달하고 Redis의 `RT:{role}:{memberId}` 키에 저장하며, 재발급할 때마다 교체합니다 (`AuthService.issueTokens`).
- `jwt.secret`은 Base64로 인코딩된 값이어야 합니다 (`JwtProvider.init`).

**영속성.**
- `ddl-auto: validate`입니다. 스키마는 Flyway(`src/main/resources/db/migration/V*.sql`)가 관리하므로, 엔티티를 변경하면 새 마이그레이션 파일(`V2__...sql`)이 필요합니다.
- QueryDSL 커스텀 쿼리는 `XxxRepositoryCustom` 인터페이스와 `XxxRepositoryCustomImpl`로 작성하고 `JPAQueryFactory`(`QuerydslConfig`)를 주입받습니다. 예: `domain/menu/repository/MenuGroupRepositoryCustomImpl`.
- 장바구니는 MySQL에 없습니다. `CartService`가 `RedisCart`를 JSON으로 직렬화해 Redis의 `cart:{customerId}` 키에 저장합니다. 장바구니 하나는 가게 하나에만 묶입니다. 주문 생성(`OrderService.saveOrderFromCart`)은 이 장바구니를 읽어 가격·가게를 MySQL 기준으로 재검증한 뒤 비웁니다.

**도메인 구분.** "음식 배달 + 마트" 구조로 가는 중이며, 나중에 이 경계대로 서비스를 분리합니다. 같은 구분 안에서는 JPA 연관관계를 쓰고, 구분을 넘어갈 때는 ID만 저장합니다.
- 공통: 회원, 주문, 결제, 배달
- 음식 배달: 가게, 메뉴, 리뷰
- 마트: 상품(`domain/product`), 재고(`domain/inventory`)

**주문.**
- `Order`는 가게와 회원을 객체로 참조하지 않습니다. `orderType`(`FOOD`/`MART`), `sellerId`(음식이면 가게 ID), `sellerName`, `customerId`를 값으로 가집니다.
- `OrderItem`도 메뉴를 객체로 참조하지 않고 `itemId`, `itemName`, `orderPrice`를 주문 시점의 값으로 저장합니다. 메뉴 이름이나 가격이 나중에 바뀌어도 주문 내역은 바뀌지 않습니다.
- 주문 응답 DTO의 필드 이름(`shopName`, `menuName`)은 프론트엔드와 맞춰 둔 것이라 그대로이고, 값만 `sellerName`, `itemName`에서 가져옵니다.

**마트.**
- 상품(`Product`)과 상품 카테고리(`ProductCategory`)는 음식 가게의 `Category`와 별개입니다. 관리 API가 없어 dev 시드 데이터(`DataInitializer`)로만 들어갑니다.
- 마트 장바구니는 `MartCartService`가 Redis의 `mart-cart:{customerId}` 키에 저장합니다. 음식 장바구니와 달리 가게에 묶이지 않습니다.
- 마트 주문 생성은 `MartOrderService.saveOrderFromCart`(`POST /api/customers/mart/orders`)가 맡습니다. 판매처 ID는 비우고 이름은 "B마트"로 넣습니다. 결제, 취소, 조회는 음식 주문과 같은 `OrderService`와 API를 씁니다.
- 마트 주문은 결제하면 재고를 확정하고 바로 접수 상태(`ACCEPTED`)가 됩니다. 그 뒤의 출고·배달 단계는 아직 없습니다.
- 음식 메뉴는 수량 재고가 없고 `Menu.soldOut`(품절 여부)만 가집니다. 사장님이 `PATCH /api/owners/shops/{shopId}/menus/{menuId}/sold-out`으로 바꿉니다.

**재고 / 결제.**
- 추후 별도 서비스로 분리할 예정이라 `domain/inventory`와 `domain/payment`는 다른 도메인의 엔티티를 참조하지 않습니다. `orderId`, `productId`, 수량, 금액만 주고받고, 테이블에도 `orders`로 가는 FK가 없습니다.
- 재고는 마트 상품에만 있고(`productId` 기준) 선점 구조입니다. 마트 주문 생성(`MartOrderService.saveOrderFromCart`) 시 `InventoryService.reserve`, 결제(`OrderService.payOrder`) 시 `confirm`, 취소(`cancelOrder`) 시 `release`를 호출합니다. 음식 주문은 재고를 건드리지 않습니다. 세 메서드는 같은 주문에 여러 번 호출해도 수량이 한 번만 바뀝니다.
- `Inventory`의 수량은 엔티티 변경 감지로 바꾸지 않고 `InventoryRepository`의 조건부 UPDATE 쿼리로만 바꿉니다. 동시 주문에서 수량이 덮어써지는 것을 막기 위해서입니다.
- 결제는 외부 PG 없이 상태만 기록하는 가짜 결제입니다 (`PaymentService`). 사장님은 결제된 주문만 접수할 수 있습니다 (`acceptOrder`).

**검색 / CDC 동기화.**
- `@KafkaListener`가 Debezium 토픽 `dbserver.mydb.<table>`을 수신합니다. envelope 원문(`payload.op`, `before`/`after`)을 파싱하고, tombstone(null)은 건너뛰고, `shop_id`를 추출해 `*SearchSyncService`를 호출합니다.
  - `ShopEventListener`: `shops`, `menu_groups`, `menus` 토픽. 변경이 생기면 MySQL에서 가게와 메뉴 그룹을 다시 읽어 비정규화된 `ShopDocument`를 통째로 덮어씁니다 (`ShopSearchSyncService.sync`). 가게가 삭제되면 문서도 삭제합니다.
  - `ReviewEventListener`: `reviews` 토픽에서 `ReviewSearchSyncService`를 호출합니다.
- `ShopDocument`(인덱스 `shops`)와 `ReviewDocument`(인덱스 `reviews`)는 `elasticsearch/settings.json`(nori 분석기)을 사용합니다.
- 고객용 가게 검색(`ShopService.search`)은 MySQL이 아니라 ES를 `NativeQuery`로 조회합니다.

**Dev 프로필.** `spring.profiles.active: dev`가 기본값이고, 이때 `global/init/DataInitializer`가 동작합니다. 시작할 때마다 **ES의 `shops`, `reviews` 인덱스를 삭제 후 재생성**하고, 고객·점주·가게·메뉴·카테고리 시드 데이터를 넣습니다.

## 코딩 규칙

기존 코드에서 일관되게 쓰이는 패턴입니다.

**컨트롤러**
- 사용자 유형별로 클래스를 나눕니다: `Customer*ApiController`(`/api/customers/...`), `Owner*ApiController`(`/api/owners/...`).
- `@RestController` + `@RequiredArgsConstructor`, 반환 타입은 `ResponseEntity<T>`입니다.
- 사용자 ID는 요청 본문으로 받지 않고 `userDetails.getMemberId()`에서 꺼내 서비스 파라미터로 따로 넘깁니다.

**서비스**
- 클래스에 `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)`를 붙이고, 쓰기 메서드에만 `@Transactional`을 붙입니다.
- 입력은 `service` 패키지의 `*Command` 클래스로 받습니다. 용도별 static 중첩 클래스(`ShopCommand.Create`, `OrderCommand.CreateFromCart`)에 `@Getter` + `@Builder`, 필드는 `private final`입니다.

**DTO**
- 요청 DTO는 `@Getter` + `@NoArgsConstructor`이고 `toCommand()` 메서드를 가집니다. 검증은 Bean Validation 애너테이션(`@Valid`, `@NotNull`, `@NotBlank`)으로 합니다.
- 응답 DTO는 `@Getter` + `@Builder`이고, 엔티티를 받는 정적 팩토리 `of(...)` 또는 `from(...)`으로 만듭니다. 엔티티를 그대로 응답으로 내보내지 않습니다.
- 한 도메인의 DTO를 static 중첩 클래스로 묶는 형태도 씁니다 (`OrderRequest.Create`, `AuthResponseDto.Token`).

**엔티티**
- `@Entity`, `@Table(name = "<복수형>")`, `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`. `@Setter`와 `@Data`는 쓰지 않습니다.
- PK는 `@GeneratedValue(strategy = IDENTITY)`이고 컬럼명은 `<단수형>_id`입니다 (`shop_id`, `menu_id`).
- 생성은 정적 팩토리 `createXxx(...)` 또는 private 생성자 + `@Builder`로 합니다.
- 상태 변경과 검증은 엔티티의 비즈니스 메서드에 둡니다 (`Shop.validateSupport`, `Order.accept`, `Shop.updateRating`).
- 연관관계는 `fetch = LAZY`입니다. `Menu`와 `MenuGroup`은 `Shop`을 객체로 참조하지 않고 `shopId`(Long)만 가집니다. N+1은 `@EntityGraph`나 QueryDSL `fetchJoin()`으로 막습니다.

**예외**
- 비즈니스 오류는 `throw new BusinessException(ErrorCode.X)`로 던집니다. `GlobalExceptionHandler`가 `ErrorCode`의 HTTP 상태와 `ErrorResponse(code, message)`로 변환합니다.
- `BusinessException` 이외의 예외(`EntityNotFoundException`, `IllegalArgumentException`, 인자 없는 `orElseThrow()`)는 모두 500 `SERVER_ERROR`로 응답됩니다. 기존 코드에 이런 예외가 많이 남아 있으니, 클라이언트가 구분해야 하는 오류는 `ErrorCode`를 추가해 `BusinessException`으로 던집니다.
- `ErrorCode`의 코드는 도메인별 접두사를 씁니다: `G`(공통), `A`(인증), `S`(가게), `M`(회원), `C`(장바구니), `O`(주문), `I`(재고), `P`(결제), `T`(마트 상품). 프론트엔드가 코드에 의존하므로 기존 값을 바꾸지 않습니다. `C002`는 `CART_EMPTY`와 `CART_SHOP_MISMATCH`에 중복 사용되고 있습니다.

**기타**
- 주석, 로그 메시지, 에러 메시지는 한국어로 씁니다. 로깅은 `@Slf4j`를 사용합니다.
- 의존성 주입은 `@RequiredArgsConstructor` + `private final` 필드로 합니다.

## 테스트

- `src/test/resources/application.yml`은 인메모리 H2(`ddl-auto: create-drop`)를 사용합니다. 마이그레이션 SQL이 MySQL 전용이라 `spring.flyway.enabled: false`로 Flyway를 끄고 엔티티에서 스키마를 만듭니다.
- 테스트는 `@SpringBootTest`이며 `ElasticsearchOperations`를 `@MockBean`으로 대체합니다. 새 통합 테스트도 같은 방식으로 ES를 대체해야 컨텍스트가 뜹니다.
- 현재 테스트는 `DreamOrderApplicationTests`(컨텍스트 로드), `ShopServiceTest`, `InventoryServiceTest`, `InventoryServiceConcurrencyTest`, `PaymentServiceTest`, `OrderServiceTest`, `MartOrderServiceTest`, `ProductServiceTest`입니다.
- 테스트는 H2 하나를 공유하고 대부분 롤백하지 않습니다. 다른 테스트의 데이터와 겹치지 않도록 고유한 ID나 이름을 쓰고, 전체 건수에 의존하는 검증은 피합니다.
- 장바구니는 Redis에 있어서 `OrderServiceTest`는 `CartService`를, `MartOrderServiceTest`는 `MartCartService`를 `@MockBean`으로 대체합니다.
