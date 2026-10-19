    -- 주문이 가게/메뉴/회원을 객체가 아니라 ID와 주문 시점에 복사한 값으로만 참조하도록 바꾼다.
    -- 주문은 음식(FOOD)과 마트(MART) 양쪽에서 쓰는 공통 도메인이 되므로 shops, menus, customers 로 가는 FK를 제거한다.

    alter table orders
       drop foreign key FK21gttsw5evi5bbsvleui69d7r;

    alter table orders
       drop foreign key FKpxtb8awmi0dk6smoh2vp1litg;

    alter table order_items
       drop foreign key FKl768w9ey6elx9j3a7u2m2i47c;

    -- 판매처: 음식 주문이면 가게 ID, 마트 주문이면 센터 ID
    alter table orders
       rename column shop_id to seller_id;

    alter table orders
       add column seller_name varchar(255),
       add column order_type enum ('FOOD','MART');

    -- 주문 대상: 음식 주문이면 메뉴 ID, 마트 주문이면 상품 ID
    alter table order_items
       rename column menu_id to item_id;

    alter table order_items
       add column item_name varchar(255);

    -- 기존 주문은 모두 음식 주문이다.
    update orders o
       left join shops s on s.shop_id = o.seller_id
       set o.seller_name = s.shop_name,
           o.order_type = 'FOOD';

    update order_items oi
       left join menus m on m.menu_id = oi.item_id
       set oi.item_name = m.menu_name;

    alter table orders
       modify column order_type enum ('FOOD','MART') not null;
