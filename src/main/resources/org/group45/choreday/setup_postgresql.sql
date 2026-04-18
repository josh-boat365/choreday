-- ChoreDay Database Setup for PostgreSQL

-- Drop tables if they exist (clean setup)
DROP TABLE IF EXISTS chores;
DROP TABLE IF EXISTS weather_records;
DROP TABLE IF EXISTS users;

-- Create Users table
CREATE TABLE users (
    student_id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Create Weather Records table
CREATE TABLE weather_records (
    id SERIAL PRIMARY KEY,
    temperature VARCHAR(10),
    wind_speed VARCHAR(10),
    humidity VARCHAR(10),
    uv_index VARCHAR(10),
    feels_like VARCHAR(10),
    weather_description VARCHAR(100),
    city VARCHAR(100),
    country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Chores table
CREATE TABLE chores (
    id SERIAL PRIMARY KEY,
    activity_name VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    weather_id INTEGER REFERENCES weather_records(id),
    student_id VARCHAR(50) REFERENCES users(student_id),
    scheduled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optional: Initial default user for testing
INSERT INTO users (student_id, full_name, password) VALUES ('S12345', 'Demo User', 'password123');
