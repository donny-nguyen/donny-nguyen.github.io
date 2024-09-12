# Java Virtual Machine (JVM)

The Java Virtual Machine (JVM) architecture is designed to provide a runtime environment for Java applications. Here’s a breakdown of its main components:

![Java Virtual Machine]({{ site.baseurl }}/images/java-virtual-machine.png)

## Class Loader

This subsystem loads class files into the JVM. It performs three main functions:

* **Loading:** Reads the .class file.

* **Linking:** Combines the different classes and interfaces.

* **Initialization:** Initializes static variables and executes static blocks.

## Runtime Data Areas

These are the memory areas used during the execution of a program:

* **Method Area:** Stores class structures like metadata, constant runtime pool, and the code for methods.

* **Heap:** The runtime data area from which memory for all class instances and arrays is allocated.

* **Stack:** Stores frames, which hold local variables and partial results, and plays a part in method invocation and return.

* **PC Register:** Each thread has its own PC (Program Counter) register, which stores the address of the currently executing JVM instruction.

* **Native Method Stack:** Contains all the native methods used in the application.

## Execution Engine

This component is responsible for executing the bytecode:

* **Interpreter:** Reads and executes the bytecode instructions one by one.

* **Just-In-Time (JIT) Compiler:** Improves performance by compiling bytecode into native machine code at runtime.

* **Garbage Collector:** Automatically manages memory by reclaiming memory used by objects that are no longer reachable.

## Native Method Interface

This framework allows the JVM to call and be called by native applications (programs specific to the hardware and operating system).

## Native Method Libraries

These are the libraries (written in other languages like C or C++) required for the execution of native methods.

In summary, the JVM architecture ensures that Java applications can run on any device or operating system that has a compatible JVM, embodying the “Write Once, Run Anywhere” principle.

<em>References:</em>
* [What is JVM (Java Virtual Machine): Architecture Explained!](https://www.guru99.com/java-virtual-machine-jvm.html)
* [Java Memory Management - In-Depth Tutorial](https://www.golinuxcloud.com/java-memory-management/)
* [Java Virtual Machine - Core java tutorial for beginners](https://www.startertutorials.com/corejava/java-virtual-machine.html)