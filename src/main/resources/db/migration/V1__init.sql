
    create table categories (
        category_id bigint not null auto_increment,
        category_type varchar(255) not null,
        name varchar(255),
        priority int not null default 0,
        primary key (category_id)
    ) engine=InnoDB;

    create table customers (
        customer_id bigint not null auto_increment,
        email varchar(255),
        password varchar(255),
        username varchar(255),
        role enum ('CUSTOMER','OWNER','RIDER'),
        primary key (customer_id)
    ) engine=InnoDB;

    create table delivery (
        delivery_id bigint not null auto_increment,
        city varchar(255),
        street varchar(255),
        zipcode varchar(255),
        status enum ('DELIVERED','DELIVERING','NONE','PENDING','READY_FOR_PICKUP'),
        primary key (delivery_id)
    ) engine=InnoDB;

    create table menu_groups (
        priority integer,
        menu_group_id bigint not null auto_increment,
        shop_id bigint,
        name varchar(255),
        primary key (menu_group_id)
    ) engine=InnoDB;

    create table menus (
        price integer not null,
        menu_group_id bigint,
        menu_id bigint not null auto_increment,
        shop_id bigint,
        menu_name varchar(255),
        primary key (menu_id)
    ) engine=InnoDB;

    create table my_addresses (
        customer_id bigint,
        my_address_id bigint not null auto_increment,
        city varchar(255),
        nickname varchar(255),
        street varchar(255),
        zipcode varchar(255),
        primary key (my_address_id)
    ) engine=InnoDB;

    create table order_items (
        count integer not null,
        order_price integer not null,
        menu_id bigint,
        order_id bigint,
        order_item_id bigint not null auto_increment,
        primary key (order_item_id)
    ) engine=InnoDB;

    create table orders (
        customer_id bigint,
        delivery_id bigint,
        order_date datetime(6),
        order_id bigint not null auto_increment,
        shop_id bigint,
        delivery_type enum ('DREAM_DELIVERY','SHOP_DELIVERY','TAKEOUT'),
        status enum ('ACCEPTED','CANCEL','COMP','DELIVERING','PENDING','PREPARING'),
        primary key (order_id)
    ) engine=InnoDB;

    create table owners (
        owner_id bigint not null auto_increment,
        business_number varchar(255),
        email varchar(255),
        password varchar(255),
        role enum ('CUSTOMER','OWNER','RIDER'),
        primary key (owner_id)
    ) engine=InnoDB;

    create table reviews (
        score float(53),
        customer_id bigint,
        order_id bigint,
        review_id bigint not null auto_increment,
        shop_id bigint,
        content varchar(255),
        primary key (review_id)
    ) engine=InnoDB;

    create table shop_categories (
        category_id bigint,
        shop_category_id bigint not null auto_increment,
        shop_id bigint,
        primary key (shop_category_id)
    ) engine=InnoDB;

    create table shop_delivery_types (
        shop_delivery_type_id bigint not null auto_increment,
        shop_id bigint,
        delivery_type enum ('DREAM_DELIVERY','SHOP_DELIVERY','TAKEOUT'),
        primary key (shop_delivery_type_id)
    ) engine=InnoDB;

    create table shops (
        average_rating float(53),
        review_count integer,
        owner_id bigint,
        shop_id bigint not null auto_increment,
        city varchar(255),
        shop_name varchar(255),
        street varchar(255),
        zipcode varchar(255),
        primary key (shop_id)
    ) engine=InnoDB;

    alter table categories 
       add constraint UK5le3ghmfrckg818rfx1xja57o unique (category_type);

    alter table orders 
       add constraint UK9ct0l8xfeaiqruabcqjh1neui unique (delivery_id);

    alter table reviews 
       add constraint UKsbkc1fll14ly5y6yxxk2jwlef unique (order_id);

    alter table shop_categories 
       add constraint UKflsxs4t3svqp7yfagd4v7iky7 unique (shop_id, category_id);

    alter table menus 
       add constraint FKnhi992mylws75a48hciilrve3 
       foreign key (menu_group_id) 
       references menu_groups (menu_group_id);

    alter table my_addresses 
       add constraint FKmccoxi1kjptqghk7swat3r448 
       foreign key (customer_id) 
       references customers (customer_id);

    alter table order_items 
       add constraint FKl768w9ey6elx9j3a7u2m2i47c 
       foreign key (menu_id) 
       references menus (menu_id);

    alter table order_items 
       add constraint FKbioxgbv59vetrxe0ejfubep1w 
       foreign key (order_id) 
       references orders (order_id);

    alter table orders 
       add constraint FKpxtb8awmi0dk6smoh2vp1litg 
       foreign key (customer_id) 
       references customers (customer_id);

    alter table orders 
       add constraint FKtkrur7wg4d8ax0pwgo0vmy20c 
       foreign key (delivery_id) 
       references delivery (delivery_id);

    alter table orders 
       add constraint FK21gttsw5evi5bbsvleui69d7r 
       foreign key (shop_id) 
       references shops (shop_id);

    alter table reviews 
       add constraint FK4sm0k8kw740iyuex3vwwv1etu 
       foreign key (customer_id) 
       references customers (customer_id);

    alter table reviews 
       add constraint FKqwgq1lxgahsxdspnwqfac6sv6 
       foreign key (order_id) 
       references orders (order_id);

    alter table reviews 
       add constraint FK3a0c998ccabg95h3c160yoq11 
       foreign key (shop_id) 
       references shops (shop_id);

    alter table shop_categories 
       add constraint FKckox988iaq9xgd4j55pk0nnys 
       foreign key (category_id) 
       references categories (category_id);

    alter table shop_categories 
       add constraint FKidgwn2wkyt6ebkm9a1u1n7usl 
       foreign key (shop_id) 
       references shops (shop_id);

    alter table shop_delivery_types 
       add constraint FK8h0hprgi1gnh0tkqofedadqim 
       foreign key (shop_id) 
       references shops (shop_id);

    alter table shops 
       add constraint FKq1drp50weenlcusy6tkcwefhl 
       foreign key (owner_id) 
       references owners (owner_id);
