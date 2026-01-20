CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES expense_groups(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    paid_by_member_id BIGINT NOT NULL REFERENCES group_members(id),
    split_type VARCHAR(20) NOT NULL CHECK (split_type IN ('EQUAL', 'AMOUNT', 'PERCENTAGE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);