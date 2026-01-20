CREATE TABLE expense_splits (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES group_members(id),
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    percentage DECIMAL(5,2) CHECK (percentage >= 0 AND percentage <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(expense_id, member_id)
);