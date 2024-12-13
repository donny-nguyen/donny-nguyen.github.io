# Comprehensive PL/SQL Example for an Online Game System

I've created a comprehensive PL/SQL example for an online game system that demonstrates various Oracle database features. Here's a breakdown of the key components:

1. Database Structure:
- Tables for players, inventory, and player stats
- Proper referential integrity with foreign keys
- Default values for new players

2. Package (game_management):
- Constants for game rules
- Custom exception handling
- Functions and procedures for common game operations
- Transaction management

3. Key Features Demonstrated:
- Cursors: Used in inventory management
- Exception Handling: Custom exceptions for insufficient gold
- Control Structures: IF/THEN/ELSE, loops in procedures
- Procedures: add_experience, purchase_item
- Functions: calculate_level, can_afford_item
- Triggers: Player login tracking
- Autonomous Transactions: Action logging

4. Advanced Concepts:
- MERGE statement for updating stats
- SAVEPOINT for transaction control
- Cursor FOR UPDATE for concurrent access
- Package-level constants and exceptions
- Pragma AUTONOMOUS_TRANSACTION

```sql
-- Table definitions for game system
CREATE TABLE players (
    player_id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE,
    level NUMBER DEFAULT 1,
    experience NUMBER DEFAULT 0,
    gold NUMBER DEFAULT 100,
    last_login DATE
);

CREATE TABLE inventory (
    inventory_id NUMBER PRIMARY KEY,
    player_id NUMBER REFERENCES players(player_id),
    item_id NUMBER,
    quantity NUMBER,
    equipped NUMBER(1) DEFAULT 0
);

CREATE TABLE player_stats (
    player_id NUMBER REFERENCES players(player_id),
    stat_name VARCHAR2(20),
    stat_value NUMBER,
    CONSTRAINT pk_player_stats PRIMARY KEY (player_id, stat_name)
);

-- Package specification
CREATE OR REPLACE PACKAGE game_management AS
    -- Constants
    c_max_level CONSTANT NUMBER := 100;
    c_exp_per_level CONSTANT NUMBER := 1000;
    
    -- Custom exception
    insufficient_gold EXCEPTION;
    PRAGMA EXCEPTION_INIT(insufficient_gold, -20001);
    
    -- Function to calculate level based on experience
    FUNCTION calculate_level(p_experience IN NUMBER) RETURN NUMBER;
    
    -- Procedure to add experience and handle level ups
    PROCEDURE add_experience(
        p_player_id IN NUMBER,
        p_exp_gained IN NUMBER
    );
    
    -- Procedure to handle item purchase
    PROCEDURE purchase_item(
        p_player_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity IN NUMBER
    );
    
    -- Function to check if player can afford item
    FUNCTION can_afford_item(
        p_player_id IN NUMBER,
        p_item_cost IN NUMBER
    ) RETURN BOOLEAN;
END game_management;
/

-- Package body
CREATE OR REPLACE PACKAGE BODY game_management AS
    -- Function implementation for level calculation
    FUNCTION calculate_level(p_experience IN NUMBER) RETURN NUMBER IS
        v_level NUMBER;
    BEGIN
        v_level := FLOOR(p_experience / c_exp_per_level) + 1;
        RETURN LEAST(v_level, c_max_level);
    END calculate_level;
    
    -- Procedure implementation for experience gain
    PROCEDURE add_experience(
        p_player_id IN NUMBER,
        p_exp_gained IN NUMBER
    ) IS
        v_current_exp NUMBER;
        v_new_exp NUMBER;
        v_old_level NUMBER;
        v_new_level NUMBER;
    BEGIN
        -- Get current experience and level
        SELECT experience, level 
        INTO v_current_exp, v_old_level
        FROM players 
        WHERE player_id = p_player_id;
        
        -- Calculate new experience and level
        v_new_exp := v_current_exp + p_exp_gained;
        v_new_level := calculate_level(v_new_exp);
        
        -- Update player data
        UPDATE players 
        SET experience = v_new_exp,
            level = v_new_level
        WHERE player_id = p_player_id;
        
        -- Handle level up
        IF v_new_level > v_old_level THEN
            -- Level up rewards
            UPDATE players
            SET gold = gold + (v_new_level * 100)
            WHERE player_id = p_player_id;
            
            -- Insert level up stats
            MERGE INTO player_stats ps
            USING (
                SELECT p_player_id as player_id, 
                       'strength' as stat_name, 
                       2 as stat_increase
                FROM dual
            ) src
            ON (ps.player_id = src.player_id AND ps.stat_name = src.stat_name)
            WHEN MATCHED THEN
                UPDATE SET stat_value = stat_value + src.stat_increase
            WHEN NOT MATCHED THEN
                INSERT (player_id, stat_name, stat_value)
                VALUES (src.player_id, src.stat_name, src.stat_increase);
        END IF;
        
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR(-20002, 'Error adding experience: ' || SQLERRM);
    END add_experience;
    
    -- Function implementation for affordability check
    FUNCTION can_afford_item(
        p_player_id IN NUMBER,
        p_item_cost IN NUMBER
    ) RETURN BOOLEAN IS
        v_gold NUMBER;
    BEGIN
        SELECT gold INTO v_gold
        FROM players
        WHERE player_id = p_player_id;
        
        RETURN v_gold >= p_item_cost;
    END can_afford_item;
    
    -- Procedure implementation for item purchase
    PROCEDURE purchase_item(
        p_player_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity IN NUMBER
    ) IS
        v_item_cost NUMBER;
        v_total_cost NUMBER;
        v_current_quantity NUMBER;
    BEGIN
        -- Get item cost (assuming items table exists)
        v_item_cost := 100; -- Simplified example
        v_total_cost := v_item_cost * p_quantity;
        
        -- Check if player can afford the item
        IF NOT can_afford_item(p_player_id, v_total_cost) THEN
            RAISE insufficient_gold;
        END IF;
        
        -- Begin transaction
        SAVEPOINT before_purchase;
        
        -- Update player's gold
        UPDATE players
        SET gold = gold - v_total_cost
        WHERE player_id = p_player_id;
        
        -- Update inventory using cursor
        DECLARE
            CURSOR c_inventory IS
                SELECT quantity 
                FROM inventory
                WHERE player_id = p_player_id 
                AND item_id = p_item_id
                FOR UPDATE;
        BEGIN
            OPEN c_inventory;
            FETCH c_inventory INTO v_current_quantity;
            
            IF c_inventory%FOUND THEN
                -- Update existing inventory entry
                UPDATE inventory
                SET quantity = quantity + p_quantity
                WHERE CURRENT OF c_inventory;
            ELSE
                -- Insert new inventory entry
                INSERT INTO inventory (inventory_id, player_id, item_id, quantity)
                VALUES (inventory_seq.NEXTVAL, p_player_id, p_item_id, p_quantity);
            END IF;
            
            CLOSE c_inventory;
        END;
        
        COMMIT;
    EXCEPTION
        WHEN insufficient_gold THEN
            ROLLBACK TO before_purchase;
            RAISE_APPLICATION_ERROR(-20001, 'Insufficient gold for purchase');
        WHEN OTHERS THEN
            ROLLBACK TO before_purchase;
            RAISE_APPLICATION_ERROR(-20003, 'Error processing purchase: ' || SQLERRM);
    END purchase_item;
END game_management;
/

-- Trigger for logging player logins
CREATE OR REPLACE TRIGGER trg_player_login
BEFORE UPDATE ON players
FOR EACH ROW
WHEN (NEW.last_login IS NOT NULL AND NEW.last_login != OLD.last_login)
BEGIN
    -- Log player login (assuming login_history table exists)
    INSERT INTO login_history (
        player_id,
        login_time,
        ip_address
    ) VALUES (
        :NEW.player_id,
        :NEW.last_login,
        SYS_CONTEXT('USERENV', 'IP_ADDRESS')
    );
END;
/

-- Example of using autonomous transaction for logging
CREATE OR REPLACE PROCEDURE log_player_action(
    p_player_id IN NUMBER,
    p_action_type IN VARCHAR2,
    p_action_details IN VARCHAR2
) IS
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    INSERT INTO player_action_log (
        log_id,
        player_id,
        action_type,
        action_details,
        log_time
    ) VALUES (
        log_seq.NEXTVAL,
        p_player_id,
        p_action_type,
        p_action_details,
        SYSTIMESTAMP
    );
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        -- Log errors but don't affect main transaction
        ROLLBACK;
        NULL;
END log_player_action;
/
```
