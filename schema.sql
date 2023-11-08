-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE users
(
    id                         INT PRIMARY KEY,
    token                      TEXT not null,
    name                       TEXT unique not null ,
    pass_hash                  TEXT not null,
    consecutive_incorrect_pass INT,
    access_group               INT
);

CREATE TABLE effortcat
(
    id INTEGER PRIMARY KEY,
    name TEXT
);

CREATE TABLE subordinates
(
    id INTEGER PRIMARY KEY,
    name TEXT,
    ec INTEGER,
    FOREIGN KEY(ec) REFERENCES effortcat(id)
);

CREATE TABLE lifecycle
(
    id INTEGER PRIMARY KEY,
    name TEXT,
    ec INTEGER,
    d INTEGER,
    FOREIGN KEY (ec) REFERENCES effortcat(id),
    FOREIGN KEY (d) REFERENCES subordinates(id)
);

CREATE TABLE projects
(
    id INTEGER PRIMARY KEY,
    name TEXT,
    lcSteps TEXT
);

CREATE TABLE effortlogs
(
    id INTEGER PRIMARY KEY,
    start TEXT,
    stop TEXT,
    project INTEGER,
    lcStep INTEGER,
    ec INTEGER,
    subi INTEGER,
    subt TEXT,
    user_id INT,
    FOREIGN KEY (project) REFERENCES projects(id),
    FOREIGN KEY (lcStep) REFERENCES lifecycle(id),
    FOREIGN KEY (ec) REFERENCES effortcat(id),
    FOREIGN KEY (subi) REFERENCES subordinates(id)
);

CREATE TABLE defects
(
    id INTEGER PRIMARY KEY,
    project INTEGER,
    name TEXT,
    open INTEGER,
    info TEXT,
    lcInject INTEGER,
    lcRemove INTEGER,
    category INTEGER,
    fixDefect INTEGER,
    FOREIGN KEY (lcInject) REFERENCES lifecycle(id),
    FOREIGN KEY (lcRemove) REFERENCES lifecycle(id),
    FOREIGN KEY (category) REFERENCES subordinates(id),
    FOREIGN KEY (project) REFERENCES projects(id)
);
