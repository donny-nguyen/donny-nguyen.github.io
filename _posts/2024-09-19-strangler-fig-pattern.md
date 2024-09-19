# Strangler Fig Pattern

The **Strangler Fig Pattern** is a software development strategy used for gradually migrating legacy systems to new architectures. Here's a concise overview:

### Origin

The pattern is named after the strangler fig, a vine that grows around trees, eventually replacing them.

### Purpose

It's used to incrementally replace an old system with a new one, piece by piece.

### Process

   - Start by building new functionality around the existing system
   - Gradually migrate features from the old to the new system
   - Eventually, the new system completely replaces the old one

![Strangler Fig Pattern]({{ site.baseurl }}/images/strangler_fig_pattern.png)

### Benefits

   - Reduces risk compared to complete rewrites
   - Allows for incremental updates and testing
   - Maintains system functionality throughout the transition

### Challenges

   - Requires careful planning and coordination
   - May involve temporary complexity during transition

This pattern is particularly useful for large, complex systems where a complete rewrite would be too risky or time-consuming.

<em>References:</em>
[Embracing the Strangler Fig pattern for legacy modernization](https://www.thoughtworks.com/insights/articles/embracing-strangler-fig-pattern-legacy-modernization-part-one)
