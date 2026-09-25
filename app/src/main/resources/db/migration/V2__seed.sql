-- Testdaten: Nettopreise. Bruttopreis = netto × (1 − Aktion) × 1,19, kaufmännisch gerundet.
insert into product (id, name, brand, category, net_price, promotion_percent, bulky, b_stock) values
  (1, 'Stratos Classic E-Gitarre', 'Fermate Custom', 'E-Gitarren', 1680.67, 0, false, false),
  (2, 'Flügel F-188 Konzertflügel', 'Klavierbau Nord', 'Klaviere & Flügel', 10084.03, 0, true, false),
  (3, 'Studio Drum Set 5-teilig', 'Rimshot', 'Schlagzeug', 755.46, 10, true, false),
  (4, 'Instrumentenkabel 6 m', 'Fermate Basics', 'Kabel', 8.39, 0, false, false),
  (5, 'Plektren-Set 12 Stück', 'Fermate Basics', 'Zubehör', 2.09, 0, false, false),
  (6, 'Stage Piano SP-73 (B-Ware)', 'Keyworks', 'Stage Pianos', 377.31, 15, false, true);

insert into variant (id, sku, color, handedness, stock, product_id) values
  (11, 'GIT-STRAT-SB-R', 'Sunburst', 'Rechtshänder', 5, 1),
  (12, 'GIT-STRAT-SB-L', 'Sunburst', 'Linkshänder', 0, 1),
  (13, 'GIT-STRAT-BK-R', 'Schwarz', 'Rechtshänder', 2, 1),
  (21, 'PNO-F188-BK', 'Schwarz poliert', null, 1, 2),
  (31, 'DRM-STUDIO-RD', 'Rot', null, 3, 3),
  (41, 'CAB-6M', null, null, 250, 4),
  (51, 'PIK-12', null, null, 800, 5),
  (61, 'KEY-SP73-B', null, null, 1, 6);
