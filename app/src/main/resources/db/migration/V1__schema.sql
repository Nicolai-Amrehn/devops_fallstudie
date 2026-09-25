create table product (
  id bigint primary key,
  name varchar(200) not null,
  brand varchar(100) not null,
  category varchar(100) not null,
  net_price numeric(10,2) not null,
  promotion_percent int not null default 0,
  bulky boolean not null default false,
  b_stock boolean not null default false
);

create table variant (
  id bigint primary key,
  sku varchar(40) not null unique,
  color varchar(60),
  handedness varchar(20),
  stock int not null check (stock >= 0),
  product_id bigint not null references product(id)
);

create table customer_order (
  id bigserial primary key,
  customer_id varchar(100) not null,
  customer_name varchar(200),
  status varchar(20) not null,
  shipping_method varchar(20) not null,
  payment_method varchar(20) not null,
  payment_reference varchar(100) unique,
  total numeric(12,2) not null,
  created_at timestamptz not null,
  paid_at timestamptz
);

create table order_line (
  id bigserial primary key,
  sku varchar(40) not null,
  description varchar(300) not null,
  quantity int not null check (quantity > 0),
  unit_price numeric(10,2) not null,
  bulky boolean not null default false
);

-- Join-Tabelle der unidirektionalen @OneToMany von customer_order zu order_line
create table customer_order_lines (
  customer_order_id bigint not null references customer_order(id),
  lines_id bigint not null unique references order_line(id)
);

-- Eine Buchung je Zahlungsreferenz
create table payment_receipt (
  id bigserial primary key,
  payment_reference varchar(100) not null unique,
  order_id bigint not null references customer_order(id),
  amount numeric(12,2) not null,
  received_at timestamptz not null
);

create index idx_order_customer on customer_order(customer_id, created_at desc);
