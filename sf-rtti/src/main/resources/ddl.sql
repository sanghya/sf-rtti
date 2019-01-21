CREATE TABLE IF NOT EXISTS V2X_RTTI_FEATURE (
	roadId varchar(12) not null,
	event_time timestamp not null,
	mean double,
	sd double,
	flows integer,
	density integer,
	design_speed integer,
	lane integer,
	length integer,
	min_probe double,
	speed double,
	sflag unsigned_tinyint,
	uflag unsigned_tinyint,
	CONSTRAINT pk PRIMARY KEY (roadId, event_time)
)

CREATE TABLE IF NOT EXISTS V2X_RTTI_PERIOD (
  roadId varchar(12) not null,
  day_of_week unsigned_tinyint not null,
  event_date char(8) not null,
  start_time timestamp not null,
  count smallint,
  speed double,
  CONSTRAINT pk PRIMARY KEY (roadId, day_of_week, event_date, start_time)
)

CREATE TABLE IF NOT EXISTS V2X_RTTI_PATTERN (
  roadId varchar(12) not null,
  day_of_week unsigned_tinyint not null,
  event_date char(8),
  start_time timestamp,
  end_time timestamp,
  count integer,
  mean double,
  CONSTRAINT pk PRIMARY KEY (roadId, day_of_week)
)


============ V2X_RTTI_PERIOD 데이터 삽입문 ============
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 12:12:09.0',76.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 12:07:09.0',83.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 12:02:09.0',84.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:57:09.0',77.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:52:09.0',74.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:47:09.0',64.0);

UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:42:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:37:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:32:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:27:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:22:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:17:09.0',64.0);

UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:12:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:07:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 11:02:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:57:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:52:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:47:09.0',64.0);

UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:42:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:37:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:32:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:27:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:22:09.0',64.0);
UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES('142442498',3,'20181219','2018-12-19 10:17:09.0',64.0);