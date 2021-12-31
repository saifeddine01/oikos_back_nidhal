SELECT date_part('year', ts::date) as year,
       date_part('month', ts::date) AS monthly,
       COUNT(id)
FROM service_view sv
where serviceid = '00000000'
  and sv.ts between '2021-01-01 23:55:00'::timestamp
  AND now()::timestamp
GROUP BY year, monthly
ORDER BY year, monthly;

SELECT date_part('year', ts::date) as year,
       date_part('month', ts::date) as month,
       date_part('day', ts::date) AS day,
       COUNT(id)
FROM service_view sv
where  sv.ts between '2021-01-01 23:55:00'::timestamp
  AND '2022-01-01 23:55:00'::timestamp
GROUP by year, month, day
ORDER BY year,month, day;

SELECT date_part('year', ts::date) as year,
	   date_part('month', ts::date) as month,
       date_part('week', ts::date) AS week,
       COUNT(id)
FROM service_view sv
where  sv.ts between '2021-01-01 23:55:00'::timestamp
  AND '2022-01-01 23:55:00'::timestamp
GROUP by year, month, week
ORDER BY year,month, week;
