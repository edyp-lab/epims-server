-- Add new acquisition columns and category for all entities
ALTER TABLE public.acquisition ADD COLUMN method_name varchar(255);
ALTER TABLE public.acquisition ADD COLUMN injection_volume FLOAT;
ALTER TABLE public.acquisition ADD column  vial_information varchar(255);
ALTER TABLE public.acquisition ADD column  "category" varchar(255);
ALTER TABLE public.study ADD column  "category" varchar(255);
ALTER TABLE public.sample ADD column  "category" varchar(255);

----------
-- Create Robot entry for CellenOne  ---

--- Search non spetrometer instument : should be only robot ... 
select * from instrument i where id not in (
    select  i.id from instrument i , spectrometer s
    where s.id  = i.id )
	
-- search for incorrect 	
select id from instrument i where id not in (
    select  i.id from instrument i , spectrometer s
    where s.id  = i.id )
 and i."name" = 'TimsTOFDemo'; -- TimsTOFDemo was not spetrometer !

-- !!! TimsTOFDemo not spectrometre ! add it !
insert into spectrometer (id)
select id from instrument i where id not in (
    select  i.id from instrument i , spectrometer s
    where s.id  = i.id ) and i."name" = 'TimsTOFDemo';

-- Test there is only one "none spectrometer"
select * from instrument i where id not in (
    select  i.id from instrument i , spectrometer s
    where s.id  = i.id )
 -- and  i.manufacturer = 'Tecan'; -- and is Tecan

-- Add Tecan instrument as robot if needed 
select * from robot; -- if none and not Tecan add it
insert into robot (id) select id from instrument i where id not in (
    select  i.id from instrument i , spectrometer s
    where s.id  = i.id ) and  i.manufacturer = 'Tecan';

select * from robot; -- should be one ;)

-- Update run_robot to reference new created Tecan robot
select * from run_robot rr where rr.robot is not null; -- none 
select * from run_robot rr where rr.robot is null; --all 

UPDATE run_robot rr
SET robot = (
    SELECT i.id
    FROM instrument i
    WHERE NOT EXISTS (
        SELECT 1
        FROM spectrometer s
        WHERE s.id = i.id
    )
      AND i.manufacturer = 'Tecan'
)
WHERE rr.robot IS NULL;

select * from run_robot rr where rr.robot is not null; -- all 
select * from run_robot rr where rr.robot is null; -- none

-- Add CellenOne  instrument and associated Robot
insert into instrument ("name", manufacturer, model, status) values ('CellenOne_1', 'Scienion', 'CellenOne', 'available');
insert into robot (id) select id from instrument i where i.name = 'CellenOne_1'  and  i.manufacturer = 'Scienion';
