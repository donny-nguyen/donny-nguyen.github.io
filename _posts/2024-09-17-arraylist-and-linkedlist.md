# ArrayList and LinkedList

Both ```ArrayList``` and ```LinkedList``` are part of Java’s ```java.util``` package and implement the ```List``` interface, but they have some key differences:

| | ArrayList | LinkedList |
| --- | --- | --- |
| Underlying Data Structure | Uses a dynamic array to store elements | Uses a doubly linked list to store elements |
| Access Time | Provides constant-time access (O(1)) to elements using the get method, making it faster for random access | Provides linear-time access (O(n)) to elements, making it slower for random access |
| Insertion/Deletion | Slower for insertions and deletions, especially at the start of the list, as it requires shifting elements (O(n)) | Faster for insertions and deletions as it only requires updating pointers (O(1)) |
| Memory Usage | Generally uses less memory per element compared to ```LinkedList``` because it doesn’t store pointers to the next and previous elements | Uses more memory per element due to the storage of pointers to the next and previous elements |
| When to Use | Need fast random access to elements and your application involves more read operations | Need fast insertions and deletions, especially in the middle of the list |
