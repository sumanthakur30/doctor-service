CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, shop_id, code)
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    account_id BIGINT NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    display_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100),
    specialization VARCHAR(255),
    qualification VARCHAR(500),
    consultation_fee DOUBLE PRECISION NOT NULL DEFAULT 0,
    follow_up_fee DOUBLE PRECISION NOT NULL DEFAULT 0,
    commission_percent DOUBLE PRECISION NOT NULL DEFAULT 0,
    signature_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, shop_id, account_id)
);

CREATE TABLE IF NOT EXISTS doctor_schedules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    branch_id BIGINT NOT NULL,
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 15,
    max_patients_per_slot INT NOT NULL DEFAULT 1,
    opd_room VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS doctor_leaves (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    leave_date DATE NOT NULL,
    leave_type VARCHAR(30) NOT NULL DEFAULT 'FULL_DAY',
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_doctors_tenant_shop ON doctors (tenant_id, shop_id, status);
CREATE INDEX IF NOT EXISTS ix_doctor_schedules_doctor ON doctor_schedules (doctor_id, day_of_week);
