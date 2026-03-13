-- ============================================================
--  Barangay Document Request System - Database Schema
--  Tested with: MySQL 5.1.23 (XAMPP)
-- ============================================================

CREATE DATABASE IF NOT EXISTS barangay_db
    CHARACTER SET utf8
    COLLATE utf8_general_ci;

USE barangay_db;

-- ============================================================
--  TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          INT           NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)   NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    full_name   VARCHAR(100)  NOT NULL,
    address     VARCHAR(255)  NOT NULL DEFAULT '',
    role        ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
--  TABLE: document_requests
--
--  MySQL 5.1 note:
--    Only ONE TIMESTAMP column per table can use DEFAULT
--    CURRENT_TIMESTAMP. processed_date is declared NULL so
--    MySQL 5.1 does not throw error #1293.
-- ============================================================
CREATE TABLE IF NOT EXISTS document_requests (
    id              INT           NOT NULL AUTO_INCREMENT,
    user_id         INT           NOT NULL,
    document_type   VARCHAR(100)  NOT NULL,
    purpose         VARCHAR(255)  NOT NULL,
    status          ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_remarks   VARCHAR(255)  NULL DEFAULT NULL,
    request_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_date  TIMESTAMP     NULL DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_doc_req_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
--  DEFAULT ADMIN ACCOUNT
--  Username: admin | Password: admin123
-- ============================================================
INSERT INTO users (username, password, full_name, address, role)
VALUES ('admin', 'admin123', 'System Administrator', 'Barangay Hall', 'ADMIN');