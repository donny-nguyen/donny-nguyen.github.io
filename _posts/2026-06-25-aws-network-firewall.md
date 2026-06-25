# AWS Network Firewall

**AWS Network Firewall** is a managed firewall service that protects traffic entering, leaving, and moving across Amazon VPCs. It gives you network-level filtering, intrusion prevention, domain filtering, and centralized policy management without requiring you to deploy and maintain firewall appliances yourself.

Network Firewall is useful when security teams need deeper traffic inspection than security groups and network ACLs can provide. Security groups and network ACLs are important for basic allow and deny rules, but Network Firewall can inspect packet content, evaluate stateful traffic flows, block known malicious patterns, and apply rules across many VPCs in a consistent way.

**Key capabilities of AWS Network Firewall:**

* **Stateful traffic inspection:** Tracks network connections and makes decisions based on the full traffic flow, not only individual packets.
* **Stateless packet filtering:** Evaluates packets quickly based on protocol, source, destination, port, and direction.
* **Intrusion prevention:** Uses Suricata-compatible rules to detect and block suspicious or malicious traffic patterns.
* **Domain filtering:** Allows or denies outbound traffic based on fully qualified domain names.
* **Centralized rule management:** Lets teams define reusable firewall policies and apply them across multiple VPCs and accounts.
* **Managed availability:** AWS handles infrastructure scaling, patching, and high availability across Availability Zones.

## How AWS Network Firewall Works

AWS Network Firewall is deployed inside a VPC through **firewall endpoints**. A firewall endpoint is created in each Availability Zone where traffic needs inspection. Route tables then direct traffic through those endpoints before the traffic reaches its destination.

The typical flow is:

1. A workload sends traffic to a destination such as the internet, another subnet, another VPC, or an on-premises network.
2. The subnet route table sends that traffic to the Network Firewall endpoint.
3. Network Firewall evaluates the traffic against stateless and stateful rule groups.
4. If the traffic is allowed, it continues to the next hop, such as an internet gateway, NAT gateway, transit gateway, or VPC peering connection.
5. If the traffic matches a deny rule or threat signature, Network Firewall drops or rejects it based on the rule configuration.

Because routing controls which traffic passes through the firewall, good route table design is critical. If traffic is not routed through a firewall endpoint, Network Firewall cannot inspect it.

## Core Components

**Firewall**

A firewall is the Network Firewall resource deployed in a VPC. It creates firewall endpoints in the selected subnets.

**Firewall policy**

A firewall policy defines how traffic should be inspected. It references stateless rule groups, stateful rule groups, default actions, and policy-level behavior.

**Rule group**

A rule group contains the actual filtering rules. Rule groups can be stateless or stateful:

* **Stateless rule groups** inspect each packet independently. They are fast and useful for simple protocol, IP, and port filtering.
* **Stateful rule groups** understand connection context. They are useful for domain filtering, application-aware rules, and intrusion prevention.

**Firewall endpoint**

A firewall endpoint is the VPC endpoint that receives traffic for inspection in a specific Availability Zone.

## Common Use Cases

**Control outbound internet access**

Network Firewall can restrict which domains, IP ranges, and protocols workloads are allowed to reach. For example, private subnets can route outbound traffic through Network Firewall before sending it to a NAT gateway.

**Protect inbound traffic**

Traffic from the internet can be routed through Network Firewall before it reaches public-facing applications. This helps block malicious IPs, suspicious protocols, and known attack signatures.

**Inspect traffic between VPCs**

When multiple VPCs are connected through AWS Transit Gateway, Network Firewall can be deployed in an inspection VPC. Traffic between application VPCs is routed through the inspection VPC for centralized security control.

**Meet compliance requirements**

Some environments require centralized logging, traffic inspection, and rule enforcement. Network Firewall helps satisfy these requirements by providing managed inspection, rule policies, and integration with monitoring services.

## AWS Network Firewall vs Security Groups and NACLs

| Feature | Security Group | Network ACL | AWS Network Firewall |
|---------|----------------|-------------|----------------------|
| Scope | Elastic network interface | Subnet | Routed VPC traffic |
| Rule type | Stateful allow rules | Stateless allow and deny rules | Stateless and stateful allow, deny, alert, and intrusion rules |
| Traffic inspection | Basic IP, protocol, and port | Basic IP, protocol, and port | Deep packet and flow inspection |
| Domain filtering | No | No | Yes |
| Intrusion prevention | No | No | Yes |
| Centralized policies | Limited | Limited | Yes |

Security groups should still be used as the first layer of workload-level access control. Network ACLs can provide subnet-level guardrails. Network Firewall adds a deeper inspection layer for traffic that must be centrally filtered, logged, or monitored.

## Deployment Patterns

**Distributed deployment**

Each VPC has its own Network Firewall. This pattern is simple and keeps inspection close to the workloads. It is useful when VPCs are managed independently or have different security requirements.

**Centralized inspection VPC**

A dedicated inspection VPC contains Network Firewall endpoints. Other VPCs route traffic through this inspection VPC using AWS Transit Gateway. This pattern is useful for large organizations that want one shared security architecture across many VPCs.

**Egress inspection**

Private workloads send outbound traffic to Network Firewall before it reaches a NAT gateway or internet gateway. This pattern is commonly used to control and log internet-bound traffic.

**East-west inspection**

Traffic between internal networks, VPCs, or subnets is routed through Network Firewall. This helps reduce lateral movement risk if one workload becomes compromised.

## Logging and Monitoring

AWS Network Firewall can publish logs to Amazon CloudWatch Logs, Amazon S3, or Amazon Kinesis Data Firehose.

**Supported log types:**

* **Alert logs:** Record traffic that matches alert rules.
* **Flow logs:** Record network flow metadata for inspected traffic.
* **TLS logs:** Record information related to TLS inspection when configured.

These logs are useful for threat detection, auditing, troubleshooting, and understanding application traffic patterns. Network Firewall also integrates with Amazon CloudWatch metrics so teams can monitor dropped packets, allowed traffic, and firewall health.

## Best Practices

* **Deploy endpoints in every Availability Zone used by workloads:** This avoids cross-zone routing and improves availability.
* **Use clear route table boundaries:** Confirm that the intended traffic is routed through the firewall endpoint.
* **Start with alert rules before blocking:** Observe real traffic first to avoid accidentally breaking applications.
* **Layer security controls:** Use Network Firewall together with security groups, network ACLs, IAM, AWS WAF, and VPC Flow Logs.
* **Centralize reusable rules:** Use shared rule groups and firewall policies for consistency across environments.
* **Log important traffic:** Send logs to CloudWatch, S3, or Kinesis Data Firehose for investigation and compliance.
* **Review rules regularly:** Remove obsolete rules and tune noisy signatures to keep policies effective.

## Limitations and Considerations

* **Routing is required:** Network Firewall only inspects traffic that route tables send to firewall endpoints.
* **Cost depends on usage:** You pay for firewall endpoint hours and data processing, so high-throughput environments should estimate cost carefully.
* **Application-layer protection is separate:** For HTTP-specific web application protection, AWS WAF is usually the better fit.
* **Encrypted traffic may need extra planning:** Deep inspection of encrypted traffic requires TLS inspection configuration and certificate management.
* **Policy mistakes can block production traffic:** Test changes in a lower environment or use alert mode before enforcing strict deny rules.

AWS Network Firewall is a strong choice when a VPC needs managed, centralized, and deeper network traffic inspection. It does not replace every AWS security control, but it fills an important gap between basic network rules and application-layer protection.