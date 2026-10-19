    -- 재고/결제는 추후 별도 서비스(DB)로 분리할 예정이라 menus, orders 로 가는 FK를 걸지 않는다.

    create table inventories (
        inventory_id bigint not null auto_increment,
        menu_id bigint not null,
        quantity integer not null,
        reserved_quantity integer not null default 0,
        primary key (inventory_id)
    ) engine=InnoDB;

    create table inventory_reservations (
        reservation_id bigint not null auto_increment,
        order_id bigint not null,
        menu_id bigint not null,
        quantity integer not null,
        status enum ('CONFIRMED','RELEASED','RESERVED') not null,
        created_at datetime(6) not null,
        primary key (reservation_id)
    ) engine=InnoDB;

    create table payments (
        payment_id bigint not null auto_increment,
        order_id bigint not null,
        amount integer not null,
        status enum ('CANCELLED','PAID') not null,
        paid_at datetime(6),
        primary key (payment_id)
    ) engine=InnoDB;

    alter table inventories
       add constraint UK_inventories_menu_id unique (menu_id);

    alter table inventory_reservations
       add constraint UK_inventory_reservations_order_menu unique (order_id, menu_id);

    alter table payments
       add constraint UK_payments_order_id unique (order_id);

    -- 기존 메뉴는 기본 재고 100개로 시작한다.
    insert into inventories (menu_id, quantity, reserved_quantity)
    select menu_id, 100, 0 from menus;
