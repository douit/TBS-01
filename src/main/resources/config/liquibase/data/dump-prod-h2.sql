INSERT INTO contact (id,email,phone,address,street,city,country_id) VALUES
(1,'','',NULL,NULL,NULL,NULL)
,(2,NULL,NULL,NULL,NULL,NULL,NULL)
,(3,NULL,NULL,NULL,NULL,NULL,NULL)
,(35,'','',NULL,NULL,NULL,NULL)
,(36,NULL,NULL,NULL,NULL,NULL,NULL)
;


INSERT INTO customer (id,"identity",identity_type,"name",contact_id) VALUES
(1,'2000000023','IQAMA','test4',1)
,(2,'035f6856-6d7e-457a-aa3f-bbd7b0eacca1',NULL,'moslem nakhli',2)
,(3,'dc9aef91-af35-4e17-9912-05352ac83767',NULL,'Fahad Aldajani',3)
,(35,'200000009',NULL,'test From post man',35)
,(36,'9c3dfc0b-6715-434a-b4aa-ae1e03addda3',NULL,'بدر آل دغيش',36)
;

INSERT INTO discount (id,i_percentage,value,"type",created_by,created_date,last_modified_by,last_modified_date) VALUES
(1,false,550.00,'ITEM','TAHAQAQ','2019-10-29 16:53:50.000','TAHAQAQ','2019-10-29 16:53:50.000')
,(2,false,550.00,'ITEM','TAHAQAQ','2019-10-29 17:06:57.000','TAHAQAQ','2019-10-29 17:06:57.000')
,(3,false,550.00,'ITEM','TAHAQAQ','2019-10-29 17:27:05.000','TAHAQAQ','2019-10-29 17:27:05.000')
,(35,false,356.00,'ITEM','TAHAQAQ','2019-10-29 22:04:10.000','TAHAQAQ','2019-10-29 22:04:10.000')
;

INSERT INTO invoice (id,customer_id,status,"number",note,due_date,subtotal,amount,discount_id,client_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(1,1,'APPROVED',NULL,NULL,NULL,550.00,27.50,NULL,3,'TAHAQAQ','2019-10-29 16:53:50.000','TAHAQAQ','2019-10-29 16:53:50.000')
,(2,1,'APPROVED',NULL,NULL,NULL,550.00,27.50,NULL,3,'TAHAQAQ','2019-10-29 17:06:57.000','TAHAQAQ','2019-10-29 17:06:57.000')
,(3,1,'APPROVED',NULL,NULL,NULL,550.00,27.50,NULL,3,'TAHAQAQ','2019-10-29 17:27:05.000','TAHAQAQ','2019-10-29 17:27:05.000')
,(4,1,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 17:42:38.000','TAHAQAQ','2019-10-29 17:42:38.000')
,(5,1,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 17:50:13.000','TAHAQAQ','2019-10-29 17:50:13.000')
,(35,1,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 18:35:19.000','TAHAQAQ','2019-10-29 18:35:19.000')
,(36,2,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 18:39:03.000','TAHAQAQ','2019-10-29 18:39:03.000')
,(37,2,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 19:23:00.000','TAHAQAQ','2019-10-29 19:23:00.000')
,(38,2,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 19:31:47.000','TAHAQAQ','2019-10-29 19:31:47.000')
,(39,3,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 20:19:36.000','TAHAQAQ','2019-10-29 20:19:36.000')
;
INSERT INTO invoice (id,customer_id,status,"number",note,due_date,subtotal,amount,discount_id,client_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(40,3,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-29 20:19:38.000','TAHAQAQ','2019-10-29 20:19:38.000')
,(69,1,'APPROVED',NULL,NULL,NULL,506.00,506.00,NULL,3,'TAHAQAQ','2019-10-29 20:56:24.000','TAHAQAQ','2019-10-29 20:56:24.000')
,(101,1,'APPROVED',NULL,NULL,NULL,506.00,506.00,NULL,3,'TAHAQAQ','2019-10-29 21:11:26.000','TAHAQAQ','2019-10-29 21:11:26.000')
,(102,1,'APPROVED',NULL,NULL,NULL,506.00,506.00,NULL,3,'TAHAQAQ','2019-10-29 22:02:26.000','TAHAQAQ','2019-10-29 22:02:26.000')
,(103,35,'APPROVED',NULL,NULL,NULL,506.00,150.00,NULL,3,'TAHAQAQ','2019-10-29 22:04:10.000','TAHAQAQ','2019-10-29 22:04:10.000')
,(104,36,'APPROVED',NULL,NULL,NULL,506.00,506.00,NULL,3,'TAHAQAQ','2019-10-29 23:50:25.000','TAHAQAQ','2019-10-29 23:50:25.000')
,(105,36,'APPROVED',NULL,NULL,NULL,429.00,429.00,NULL,3,'TAHAQAQ','2019-10-29 23:51:47.000','TAHAQAQ','2019-10-29 23:51:47.000')
,(106,3,'APPROVED',NULL,NULL,NULL,506.00,506.00,NULL,3,'TAHAQAQ','2019-10-30 04:06:38.000','TAHAQAQ','2019-10-30 04:06:38.000')
,(107,3,'APPROVED',NULL,NULL,NULL,506.00,506.00,NULL,3,'TAHAQAQ','2019-10-30 04:06:43.000','TAHAQAQ','2019-10-30 04:06:43.000')
,(108,3,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-30 05:31:17.000','TAHAQAQ','2019-10-30 05:31:17.000')
;
INSERT INTO invoice (id,customer_id,status,"number",note,due_date,subtotal,amount,discount_id,client_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(109,3,'APPROVED',NULL,NULL,NULL,550.00,577.50,NULL,3,'TAHAQAQ','2019-10-30 05:31:23.000','TAHAQAQ','2019-10-30 05:31:23.000')
,(110,3,'APPROVED',NULL,NULL,NULL,522.00,522.50,NULL,3,'TAHAQAQ','2019-10-30 09:00:04.000','TAHAQAQ','2019-10-30 09:00:04.000')
,(111,3,'APPROVED',NULL,NULL,NULL,522.00,522.50,NULL,3,'TAHAQAQ','2019-10-30 09:00:07.000','TAHAQAQ','2019-10-30 09:00:07.000')
,(112,3,'APPROVED',NULL,NULL,NULL,522.00,522.50,NULL,3,'TAHAQAQ','2019-10-30 09:00:11.000','TAHAQAQ','2019-10-30 09:00:11.000')
,(113,3,'APPROVED',NULL,NULL,NULL,522.00,522.50,NULL,3,'TAHAQAQ','2019-10-30 09:00:15.000','TAHAQAQ','2019-10-30 09:00:15.000')
,(134,2,'APPROVED',NULL,NULL,NULL,506.00,531.30,NULL,3,'TAHAQAQ','2019-10-30 10:28:52.000','TAHAQAQ','2019-10-30 10:28:52.000')
,(135,2,'APPROVED',NULL,NULL,NULL,506.00,531.30,NULL,3,'TAHAQAQ','2019-11-06 15:14:52.000','TAHAQAQ','2019-11-06 15:14:52.000')
;


INSERT INTO invoice_item (id,"name",description,amount,quantity,tax_name,tax_rate,invoice_id,discount_id,item_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(1,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,1,1,1,'TAHAQAQ','2019-10-29 16:53:50.000','TAHAQAQ','2019-10-29 16:53:50.000')
,(2,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,2,2,1,'TAHAQAQ','2019-10-29 17:06:57.000','TAHAQAQ','2019-10-29 17:06:57.000')
,(3,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,3,3,1,'TAHAQAQ','2019-10-29 17:27:05.000','TAHAQAQ','2019-10-29 17:27:05.000')
,(4,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,4,NULL,1,'TAHAQAQ','2019-10-29 17:42:38.000','TAHAQAQ','2019-10-29 17:42:38.000')
,(5,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,5,NULL,1,'TAHAQAQ','2019-10-29 17:50:13.000','TAHAQAQ','2019-10-29 17:50:13.000')
,(35,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,35,NULL,1,'TAHAQAQ','2019-10-29 18:35:19.000','TAHAQAQ','2019-10-29 18:35:19.000')
,(36,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,36,NULL,1,'TAHAQAQ','2019-10-29 18:39:03.000','TAHAQAQ','2019-10-29 18:39:03.000')
,(37,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,37,NULL,1,'TAHAQAQ','2019-10-29 19:23:00.000','TAHAQAQ','2019-10-29 19:23:00.000')
,(38,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,38,NULL,1,'TAHAQAQ','2019-10-29 19:31:47.000','TAHAQAQ','2019-10-29 19:31:47.000')
,(39,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,39,NULL,1,'TAHAQAQ','2019-10-29 20:19:36.000','TAHAQAQ','2019-10-29 20:19:36.000')
;
INSERT INTO invoice_item (id,"name",description,amount,quantity,tax_name,tax_rate,invoice_id,discount_id,item_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(40,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,40,NULL,1,'TAHAQAQ','2019-10-29 20:19:38.000','TAHAQAQ','2019-10-29 20:19:38.000')
,(69,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,69,NULL,4,'TAHAQAQ','2019-10-29 20:56:24.000','TAHAQAQ','2019-10-29 20:56:24.000')
,(101,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,101,NULL,4,'TAHAQAQ','2019-10-29 21:11:26.000','TAHAQAQ','2019-10-29 21:11:26.000')
,(102,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,102,NULL,4,'TAHAQAQ','2019-10-29 22:02:26.000','TAHAQAQ','2019-10-29 22:02:26.000')
,(103,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,103,35,4,'TAHAQAQ','2019-10-29 22:04:10.000','TAHAQAQ','2019-10-29 22:04:10.000')
,(104,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,104,NULL,4,'TAHAQAQ','2019-10-29 23:50:25.000','TAHAQAQ','2019-10-29 23:50:25.000')
,(105,'1f5e38f9-cd76-4676-8509-d3f85c646899','شهادة مهنية',429.00,1,'Total_Taxes',0.00,105,NULL,5,'TAHAQAQ','2019-10-29 23:51:47.000','TAHAQAQ','2019-10-29 23:51:47.000')
,(106,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,106,NULL,4,'TAHAQAQ','2019-10-30 04:06:38.000','TAHAQAQ','2019-10-30 04:06:38.000')
,(107,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',0.00,107,NULL,4,'TAHAQAQ','2019-10-30 04:06:43.000','TAHAQAQ','2019-10-30 04:06:43.000')
,(108,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,108,NULL,1,'TAHAQAQ','2019-10-30 05:31:17.000','TAHAQAQ','2019-10-30 05:31:17.000')
;
INSERT INTO invoice_item (id,"name",description,amount,quantity,tax_name,tax_rate,invoice_id,discount_id,item_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(109,'5221aa37-6ede-4bdb-ab45-86435ebaa911','الشهادات العلمية',550.00,1,'Total_Taxes',5.00,109,NULL,1,'TAHAQAQ','2019-10-30 05:31:23.000','TAHAQAQ','2019-10-30 05:31:23.000')
,(110,'ab37656a-fff6-4ce5-bcbc-e7e594694d6a','الوثائق الثبوتية',522.00,1,'Total_Taxes',0.00,110,NULL,3,'TAHAQAQ','2019-10-30 09:00:04.000','TAHAQAQ','2019-10-30 09:00:04.000')
,(111,'ab37656a-fff6-4ce5-bcbc-e7e594694d6a','الوثائق الثبوتية',522.00,1,'Total_Taxes',0.00,111,NULL,3,'TAHAQAQ','2019-10-30 09:00:07.000','TAHAQAQ','2019-10-30 09:00:07.000')
,(112,'ab37656a-fff6-4ce5-bcbc-e7e594694d6a','الوثائق الثبوتية',522.00,1,'Total_Taxes',0.00,112,NULL,3,'TAHAQAQ','2019-10-30 09:00:11.000','TAHAQAQ','2019-10-30 09:00:11.000')
,(113,'ab37656a-fff6-4ce5-bcbc-e7e594694d6a','الوثائق الثبوتية',522.00,1,'Total_Taxes',0.00,113,NULL,3,'TAHAQAQ','2019-10-30 09:00:15.000','TAHAQAQ','2019-10-30 09:00:15.000')
,(134,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',5.00,134,NULL,4,'TAHAQAQ','2019-10-30 10:28:52.000','TAHAQAQ','2019-10-30 10:28:52.000')
,(135,'696f1bed-83d6-4282-bc4b-b707cbaa1689','شهادة أكاديمية',506.00,1,'Total_Taxes',5.00,135,NULL,4,'TAHAQAQ','2019-11-06 15:14:52.000','TAHAQAQ','2019-11-06 15:14:52.000')
;

ALTER TABLE invoice_item DISABLE TRIGGER ALL;
ALTER TABLE invoice DISABLE TRIGGER ALL;

update invoice set id = id + 7000000065;
update invoice_item set invoice_id = invoice_id + 7000000065;

ALTER TABLE invoice_item ENABLE TRIGGER ALL;
ALTER TABLE invoice ENABLE TRIGGER ALL;

-- 40 --> 7000000105
-- 134 --> 7000000199

INSERT INTO payment (id,amount,status,expiration_date,invoice_id,payment_method_id,created_by,created_date,last_modified_by,last_modified_date) VALUES
(1,577.00,'SUCCESSFUL',NULL,7000000105,1,'system','2019-11-03 12:29:42.000','system','2019-11-03 12:29:42.000')
;

select setval('contact_id_seq', 36);
select setval('customer_id_seq', 36);
select setval('discount_id_seq', 36);
select setval('invoice_id_seq', 7000000997);
select setval('invoice_item_id_seq', 135);
select setval('payment_id_seq', 1);

COMMIT;
