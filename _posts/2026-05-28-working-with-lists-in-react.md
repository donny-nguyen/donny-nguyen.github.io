---
layout: post
title: "Working with Lists in React: Approaches for Add/Update/Remove Operations"
date: 2026-05-28
categories: [React.js, State Management]
---

When building React applications, managing lists of items that require add, update, and remove operations while keeping the UI synchronized with a backend server is a common challenge. This article explores various approaches and best practices for handling such scenarios.

## The Challenge

Consider a form where users can:
- Add new items to a list
- Update existing items
- Remove items from the list
- Submit all changes to a backend server

The key challenges include:
1. **Temporary vs. Server IDs**: New items don't have server-generated IDs until they're persisted
2. **Optimistic Updates**: Providing instant feedback while waiting for server confirmation
3. **Consistency**: Ensuring updates and deletes work correctly across client and server
4. **Error Handling**: Gracefully managing failures and rollbacks

## Approach 1: Temporary ID Strategy

### Overview

Use temporary client-generated IDs for new items and replace them with server-generated IDs upon successful creation.

### Implementation

```jsx
import { useState } from 'react';
import { v4 as uuidv4 } from 'uuid';

function ItemListManager() {
  const [items, setItems] = useState([]);
  const [pendingOperations, setPendingOperations] = useState(new Set());

  const addItem = (itemData) => {
    const tempId = `temp-${uuidv4()}`;
    const newItem = {
      ...itemData,
      id: tempId,
      isNew: true,
      isOptimistic: true
    };

    setItems(prev => [...prev, newItem]);
    setPendingOperations(prev => new Set(prev).add(tempId));

    // Optimistically add to UI, then sync with server
    createItemOnServer(itemData)
      .then(serverItem => {
        // Replace temporary item with server response
        setItems(prev =>
          prev.map(item =>
            item.id === tempId
              ? { ...serverItem, isOptimistic: false }
              : item
          )
        );
        setPendingOperations(prev => {
          const next = new Set(prev);
          next.delete(tempId);
          return next;
        });
      })
      .catch(error => {
        // Remove optimistic item on error
        setItems(prev => prev.filter(item => item.id !== tempId));
        setPendingOperations(prev => {
          const next = new Set(prev);
          next.delete(tempId);
          return next;
        });
        console.error('Failed to create item:', error);
      });
  };

  const updateItem = (id, updates) => {
    // Optimistically update UI
    setItems(prev =>
      prev.map(item =>
        item.id === id
          ? { ...item, ...updates, isOptimistic: true }
          : item
      )
    );
    setPendingOperations(prev => new Set(prev).add(id));

    updateItemOnServer(id, updates)
      .then(serverItem => {
        setItems(prev =>
          prev.map(item =>
            item.id === id
              ? { ...serverItem, isOptimistic: false }
              : item
          )
        );
        setPendingOperations(prev => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
      })
      .catch(error => {
        // Revert optimistic update on error
        setItems(prev =>
          prev.map(item =>
            item.id === id
              ? { ...item, isOptimistic: false }
              : item
          )
        );
        setPendingOperations(prev => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
        console.error('Failed to update item:', error);
      });
  };

  const removeItem = (id) => {
    // Store the item for potential rollback
    const itemToRemove = items.find(item => item.id === id);
    
    // Optimistically remove from UI
    setItems(prev => prev.filter(item => item.id !== id));
    setPendingOperations(prev => new Set(prev).add(id));

    deleteItemOnServer(id)
      .then(() => {
        setPendingOperations(prev => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
      })
      .catch(error => {
        // Restore item on error
        setItems(prev => [...prev, itemToRemove]);
        setPendingOperations(prev => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
        console.error('Failed to delete item:', error);
      });
  };

  return (
    <div>
      <ItemList
        items={items}
        onUpdate={updateItem}
        onRemove={removeItem}
        pendingOperations={pendingOperations}
      />
      <AddItemForm onAdd={addItem} />
    </div>
  );
}
```

### Advantages

- ✅ Immediate UI feedback
- ✅ Clear separation between temporary and persisted items
- ✅ Easy to track pending operations

### Disadvantages

- ❌ Requires ID mapping logic
- ❌ Complexity in handling references between items

## Approach 2: Operation Queue with Batch Submission

### Overview

Queue all operations locally and submit them in a single batch when the user triggers a save action.

### Implementation

```jsx
import { useState, useReducer } from 'react';

const operationsReducer = (state, action) => {
  switch (action.type) {
    case 'ADD':
      return {
        ...state,
        additions: [...state.additions, action.payload]
      };
    case 'UPDATE':
      return {
        ...state,
        updates: {
          ...state.updates,
          [action.payload.id]: action.payload.data
        }
      };
    case 'REMOVE':
      return {
        ...state,
        deletions: [...state.deletions, action.payload]
      };
    case 'CLEAR':
      return { additions: [], updates: {}, deletions: [] };
    default:
      return state;
  }
};

function BatchItemManager() {
  const [items, setItems] = useState([]);
  const [operations, dispatch] = useReducer(operationsReducer, {
    additions: [],
    updates: {},
    deletions: []
  });
  const [isSaving, setIsSaving] = useState(false);

  const addItem = (itemData) => {
    const tempId = `temp-${Date.now()}`;
    const newItem = { ...itemData, id: tempId };
    
    setItems(prev => [...prev, newItem]);
    dispatch({ type: 'ADD', payload: newItem });
  };

  const updateItem = (id, updates) => {
    setItems(prev =>
      prev.map(item => (item.id === id ? { ...item, ...updates } : item))
    );
    dispatch({ type: 'UPDATE', payload: { id, data: updates } });
  };

  const removeItem = (id) => {
    setItems(prev => prev.filter(item => item.id !== id));
    dispatch({ type: 'REMOVE', payload: id });
  };

  const submitChanges = async () => {
    setIsSaving(true);

    try {
      // Process deletions first
      await Promise.all(
        operations.deletions
          .filter(id => !id.startsWith('temp-'))
          .map(id => deleteItemOnServer(id))
      );

      // Process updates
      const updatePromises = Object.entries(operations.updates)
        .filter(([id]) => !id.startsWith('temp-'))
        .map(([id, data]) => updateItemOnServer(id, data));
      await Promise.all(updatePromises);

      // Process additions and get server IDs
      const newItemsPromises = operations.additions.map(item =>
        createItemOnServer(item)
      );
      const createdItems = await Promise.all(newItemsPromises);

      // Update items with server IDs
      setItems(prev => {
        let updated = [...prev];
        operations.additions.forEach((addedItem, index) => {
          updated = updated.map(item =>
            item.id === addedItem.id ? createdItems[index] : item
          );
        });
        return updated;
      });

      // Clear operations queue
      dispatch({ type: 'CLEAR' });
      
      alert('Changes saved successfully!');
    } catch (error) {
      console.error('Failed to save changes:', error);
      alert('Failed to save changes. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const hasUnsavedChanges =
    operations.additions.length > 0 ||
    Object.keys(operations.updates).length > 0 ||
    operations.deletions.length > 0;

  return (
    <div>
      <ItemList
        items={items}
        onUpdate={updateItem}
        onRemove={removeItem}
      />
      <AddItemForm onAdd={addItem} />
      <button
        onClick={submitChanges}
        disabled={!hasUnsavedChanges || isSaving}
      >
        {isSaving ? 'Saving...' : 'Save All Changes'}
      </button>
      {hasUnsavedChanges && (
        <div className="unsaved-indicator">
          You have unsaved changes
        </div>
      )}
    </div>
  );
}
```

### Advantages

- ✅ Single point of failure (one save operation)
- ✅ User has explicit control over when to sync
- ✅ Can implement "save draft" functionality
- ✅ Easier to implement undo/redo

### Disadvantages

- ❌ Risk of losing all changes if save fails
- ❌ No automatic synchronization
- ❌ Users must remember to save

## Approach 3: Hybrid with Debounced Auto-Save

### Overview

Combine optimistic updates with automatic saving after a debounce period, providing the best of both worlds.

### Implementation

```jsx
import { useState, useCallback, useRef, useEffect } from 'react';
import { debounce } from 'lodash';

function AutoSaveItemManager() {
  const [items, setItems] = useState([]);
  const [pendingChanges, setPendingChanges] = useState(new Map());
  const autoSaveTimeoutRef = useRef(null);

  // Debounced auto-save function
  const autoSave = useCallback(
    debounce(async () => {
      const changes = Array.from(pendingChanges.entries());
      
      for (const [id, change] of changes) {
        try {
          if (change.type === 'create') {
            const serverItem = await createItemOnServer(change.data);
            setItems(prev =>
              prev.map(item =>
                item.id === id ? serverItem : item
              )
            );
          } else if (change.type === 'update') {
            await updateItemOnServer(id, change.data);
          } else if (change.type === 'delete') {
            await deleteItemOnServer(id);
          }

          setPendingChanges(prev => {
            const next = new Map(prev);
            next.delete(id);
            return next;
          });
        } catch (error) {
          console.error(`Failed to sync ${change.type} for item ${id}:`, error);
        }
      }
    }, 2000),
    [pendingChanges]
  );

  const addItem = (itemData) => {
    const tempId = `temp-${Date.now()}-${Math.random()}`;
    const newItem = { ...itemData, id: tempId };
    
    setItems(prev => [...prev, newItem]);
    setPendingChanges(prev =>
      new Map(prev).set(tempId, { type: 'create', data: itemData })
    );
    
    autoSave();
  };

  const updateItem = (id, updates) => {
    setItems(prev =>
      prev.map(item => (item.id === id ? { ...item, ...updates } : item))
    );
    setPendingChanges(prev =>
      new Map(prev).set(id, { type: 'update', data: updates })
    );
    
    autoSave();
  };

  const removeItem = (id) => {
    setItems(prev => prev.filter(item => item.id !== id));
    
    if (!id.startsWith('temp-')) {
      setPendingChanges(prev =>
        new Map(prev).set(id, { type: 'delete' })
      );
      autoSave();
    }
  };

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      autoSave.cancel();
    };
  }, [autoSave]);

  return (
    <div>
      <ItemList
        items={items}
        onUpdate={updateItem}
        onRemove={removeItem}
      />
      <AddItemForm onAdd={addItem} />
      {pendingChanges.size > 0 && (
        <div className="saving-indicator">
          Saving changes...
        </div>
      )}
    </div>
  );
}
```

### Advantages

- ✅ Automatic synchronization
- ✅ Optimistic UI updates
- ✅ Reduces server load with debouncing
- ✅ Good balance between UX and reliability

### Disadvantages

- ❌ More complex implementation
- ❌ Potential race conditions if not handled carefully
- ❌ May save incomplete data if user navigates away quickly

## Best Practices

### 1. Always Use Unique Keys

```jsx
// Good: Stable keys
{items.map(item => (
  <ItemComponent key={item.id} item={item} />
))}

// Bad: Using index as key
{items.map((item, index) => (
  <ItemComponent key={index} item={item} />
))}
```

### 2. Track Operation State

```jsx
const [itemStates, setItemStates] = useState({});

const getItemState = (id) => {
  return itemStates[id] || 'idle'; // 'idle' | 'pending' | 'error'
};

const updateItemState = (id, state) => {
  setItemStates(prev => ({ ...prev, [id]: state }));
};
```

### 3. Implement Optimistic Updates Carefully

```jsx
const optimisticUpdate = async (id, updates, apiCall) => {
  // Store original state for rollback
  const originalItem = items.find(item => item.id === id);
  
  // Apply optimistic update
  setItems(prev =>
    prev.map(item => (item.id === id ? { ...item, ...updates } : item))
  );

  try {
    const result = await apiCall();
    // Update with server response
    setItems(prev =>
      prev.map(item => (item.id === id ? result : item))
    );
    return result;
  } catch (error) {
    // Rollback on error
    setItems(prev =>
      prev.map(item => (item.id === id ? originalItem : item))
    );
    throw error;
  }
};
```

### 4. Handle Race Conditions

```jsx
import { useRef } from 'react';

function useSequentialUpdates() {
  const updateQueueRef = useRef(Promise.resolve());

  const enqueueUpdate = (updateFn) => {
    updateQueueRef.current = updateQueueRef.current
      .then(updateFn)
      .catch(error => {
        console.error('Update failed:', error);
      });
    
    return updateQueueRef.current;
  };

  return enqueueUpdate;
}

// Usage
function ItemManager() {
  const enqueueUpdate = useSequentialUpdates();

  const updateItem = (id, updates) => {
    enqueueUpdate(async () => {
      // Your update logic here
      await updateItemOnServer(id, updates);
    });
  };
}
```

### 5. Provide Visual Feedback

```jsx
function ItemRow({ item, isPending, hasError, onUpdate, onRemove }) {
  return (
    <div className={`item-row ${isPending ? 'pending' : ''} ${hasError ? 'error' : ''}`}>
      <span className="item-name">{item.name}</span>
      
      {isPending && <Spinner size="small" />}
      {hasError && <ErrorIcon />}
      
      {item.isOptimistic && (
        <span className="badge">Not Saved</span>
      )}
      
      <button onClick={() => onUpdate(item.id)} disabled={isPending}>
        Edit
      </button>
      <button onClick={() => onRemove(item.id)} disabled={isPending}>
        Remove
      </button>
    </div>
  );
}
```

## Error Handling Strategies

### Strategy 1: Retry with Exponential Backoff

```jsx
async function retryWithBackoff(fn, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      
      const delay = Math.pow(2, i) * 1000; // 1s, 2s, 4s
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
}

// Usage
const updateItem = async (id, updates) => {
  try {
    await retryWithBackoff(() => updateItemOnServer(id, updates));
    // Handle success
  } catch (error) {
    // Handle final failure
    showErrorNotification('Failed to update item after multiple attempts');
  }
};
```

### Strategy 2: Conflict Resolution

```jsx
function resolveConflict(clientVersion, serverVersion) {
  // Last-write-wins strategy
  if (clientVersion.updatedAt > serverVersion.updatedAt) {
    return clientVersion;
  }
  return serverVersion;
}

// Or prompt user to choose
function showConflictDialog(clientVersion, serverVersion) {
  return new Promise((resolve) => {
    // Show modal with both versions
    // Let user choose which to keep
  });
}
```

## Testing Considerations

```jsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

describe('ItemListManager', () => {
  it('should optimistically add item and sync with server', async () => {
    const { getByText, getByLabelText } = render(<ItemListManager />);
    
    const input = getByLabelText('Item Name');
    const addButton = getByText('Add Item');
    
    await userEvent.type(input, 'New Item');
    await userEvent.click(addButton);
    
    // Should appear immediately (optimistic)
    expect(screen.getByText('New Item')).toBeInTheDocument();
    
    // Wait for server sync
    await waitFor(() => {
      expect(screen.queryByText('Not Saved')).not.toBeInTheDocument();
    });
  });

  it('should rollback on server error', async () => {
    // Mock server error
    global.fetch = jest.fn(() =>
      Promise.reject(new Error('Server error'))
    );
    
    const { getByText } = render(<ItemListManager />);
    
    await userEvent.click(getByText('Add Item'));
    
    // Item should be removed after error
    await waitFor(() => {
      expect(screen.queryByText('New Item')).not.toBeInTheDocument();
    });
  });
});
```

## Conclusion

Working with lists in React requires careful consideration of user experience, data consistency, and error handling. The approach you choose depends on your specific requirements:

- **Use Temporary ID Strategy** when you need immediate feedback and individual operation tracking
- **Use Operation Queue** when users need explicit control over save operations
- **Use Hybrid Auto-Save** for automatic synchronization with good UX

Regardless of the approach, always:
- Track operation states
- Implement proper error handling
- Provide clear visual feedback
- Test edge cases thoroughly
- Consider offline scenarios

By following these patterns and best practices, you can build robust list management features that provide a smooth user experience while maintaining data consistency with your backend.
