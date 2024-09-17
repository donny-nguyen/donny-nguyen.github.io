# AWS Snowball

![AWS Snowball Flow]({{ site.baseurl }}/images/aws-snowball.png)

**AWS Snowball** is a physical data transfer service that allows us to securely and efficiently transfer large amounts of data into and out of the AWS cloud. It's particularly useful for organizations with massive datasets that would take too long or be too expensive to transfer over the internet.

**Key features and benefits of AWS Snowball**

* **Petabyte-scale data transfer:** Can handle large volumes of data, making it ideal for migrating entire data centers or archives.
* **Secure data transfer:** Uses tamper-evident packaging and encryption to protect data in transit.
* **Offline data transfer:** Can be used in locations with limited or no internet connectivity.
* **Cost-effective:** Can be a more cost-effective option than transferring data over the internet for large datasets.
* **Easy to use:** AWS provides a simple process for requesting, receiving, and returning Snowball devices.

**How it works**
1. **Request a Snowball device:** You request a Snowball device through the AWS Management Console.
2. **Receive the device:** AWS ships the device to your location.
3. **Transfer data:** You transfer your data to the Snowball device.
4. **Return the device:** You return the device to AWS.
5. **Data transfer:** AWS transfers your data to the desired destination in the cloud.

![AWS Snowball Flow]({{ site.baseurl }}/images/AWS-snowball-flow.webp)

**Device Types**

There are two main types of devices:
  * **Snowball Edge Storage Optimized:** Primarily for large-scale data migrations, backup, and disaster recovery with up to 80 TB of usable storage.
  * **Snowball Edge Compute Optimized:** Designed for both data transfer and edge computing, with more compute capacity for processing data locally before transfer.

**Use cases**
* Migrating large datasets to the cloud
* Backing up and archiving data
* Analyzing large datasets offline
* Running compute-intensive workloads in remote locations

In essence, AWS Snowball is a reliable and secure solution for transporting large amounts of data between your on-premises environment and the AWS cloud.

<em>References:</em> [AWS Snowball: Use Cases, Benefits, Limitations & Alternatives](https://www.resilio.com/blog/aws-snowball-and-alternatives)