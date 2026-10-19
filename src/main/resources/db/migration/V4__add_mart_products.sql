    -- 마트 상품을 추가하고, 메뉴에 임시로 붙어 있던 재고를 상품 기준으로 옮긴다.
    -- 음식 메뉴는 수량 재고 대신 품절 여부만 가진다.

    create table product_categories (
        product_category_id bigint not null auto_increment,
        name varchar(255) not null,
        priority integer not null default 0,
        primary key (product_category_id)
    ) engine=InnoDB;

    create table products (
        product_id bigint not null auto_increment,
        product_category_id bigint,
        name varchar(255) not null,
        price integer not null,
        sale_status enum ('ON_SALE','STOPPED') not null,
        primary key (product_id)
    ) engine=InnoDB;

    -- 상품과 상품 카테고리는 같은 마트 도메인이라 FK를 건다.
    alter table products
       add constraint FK_products_product_category
       foreign key (product_category_id)
       references product_categories (product_category_id);

    -- 메뉴 기준으로 들어 있던 재고 데이터는 상품과 무관하므로 비운다.
    delete from inventory_reservations;
    delete from inventories;

    alter table inventories
       rename column menu_id to product_id;

    alter table inventory_reservations
       rename column menu_id to product_id;

    alter table menus
       add column sold_out bit not null default false;
