# The Java Collection Framework Hierarchy

![Collections]({{ site.baseurl }}/images/java-collection-iterable.png)

**Iterable**

* The root interface of the collection hierarchy.
* Provides the `iterator()` method to iterate over the elements of a collection.

**Collection**

* Represents a general-purpose collection.
* Provides methods for adding, removing, searching, and iterating over elements.

**Set**

* Represents a collection that does not allow duplicate elements.
* Has three implementations: `HashSet`, `LinkedHashSet`, and `TreeSet`.

**List**

* Represents a collection that allows duplicate elements and maintains the order of elements.
* Has three implementations: `ArrayList`, `LinkedList`, and `Vector`.

**Queue**

* Represents a collection that follows the FIFO (First-In-First-Out) order.
* Has two implementations: `ArrayDeque` and `LinkedList`.

**Deque**

* Represents a double-ended queue (deque) that allows elements to be added or removed from both ends.
* Has one implementation: `ArrayDeque`.

![Map]({{ site.baseurl }}/images/java-collection-map.png)

**Map**

* Represents a collection of key-value pairs.
* Does not extend `Collection`.
* Has three implementations: `HashMap`, `LinkedHashMap`, and `TreeMap`.
