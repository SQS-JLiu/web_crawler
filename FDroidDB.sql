create database FDroidDB;

use FDroidDB;

create table app(
	id				int	primary key,
	name	 		varchar(100) not null,
	packageName 	varchar(100) not null,
	introduction	varchar(1000),
	appUrl			varchar(128) not null,
	issuesUrl		varchar(128)
);

create table issues(
	id				int not null,
	title			varchar(1000) not null,
	body			longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
	comments_url	varchar(128) not null,
	comments		smallint default 0,
	type			char(1)
);

create table comments(
	comments_url 	varchar(128) not null,
	body			longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci
);