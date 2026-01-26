# Description
Securing one-way TLS (also known as Server-side SSL) ensures that the client validates the identity of the broker and that all data sent between them is encrypted. In this setup, only the broker needs a certificate; the client does not.

# Steps
## Server side
1. Create broker certificates
~~~
# Generate a new keystore for the broker
keytool -genkey -alias broker -keyalg RSA -keystore broker.ks -storepass password

#e.g.
Enter the distinguished name. Provide a single dot (.) to leave a sub-component empty or press ENTER to use the default value in braces.
What is your first and last name?
  [amq.example.com]:  amq.example.com
What is the name of your organizational unit?
  [Unknown]:  RH
What is the name of your organization?
  [Unknown]:  RH
What is the name of your City or Locality?
  [Unknown]:  Rome
What is the name of your State or Province?
  [Unknown]:  Italy
What is the two-letter country code for this unit?
  [Unknown]:  IT
Is CN=amq.example.com, OU=RH, O=RH, L=Rome, ST=Italy, C=IT correct?
  [no]:  yes


~~~
Note: When prompted for "First and Last Name," enter the FQDN (Fully Qualified Domain Name) of your broker server (e.g., amq.example.com). This is crucial for hostname validation

2. Configure the broker
~~~
        <acceptor name="artemis">
            tcp://0.0.0.0:61616?tcpSendBufferSize=1048576;
            tcpReceiveBufferSize=1048576;
            amqpMinLargeMessageSize=102400;
            protocols=CORE,AMQP,STOMP,HORNETQ,MQTT,OPENWIRE;
            useEpoll=true;
            amqpCredits=1000;
            amqpLowCredits=300;
            amqpDuplicateDetection=true;
            supportAdvisory=false;
            suppressInternalManagementObjects=false;
            sslEnabled=true;
            keyStorePath=/opt/apache-artemis-2.33.0.redhat-00010/bin/one_way_tls_broker/etc/broker.ks;
            keyStorePassword=password
        </acceptor>
~~~


## Client side
1. Export the broker certificate
~~~
keytool -export -alias broker -keystore /opt/apache-artemis-2.33.0.redhat-00010/bin/one_way_tls_broker/etc/broker.ks -file amq-server.cer -storepass password
~~~

2. Create client truststore and import certificate
~~~
keytool -import -alias broker -file amq-server.cer -keystore client-truststore.ts -storepass password
~~~

# Test: Produce a message

~~~
./artemis producer --destination simplequeue --message="text message" --message-count=1 --url "tcp://localhost:61616?sslEnabled=true&verifyHost=false&trustStorePath=/home/mdicarlo/Cases/04358892/Test_connection/client-truststore.ts&trustStorePassword=password"
~~~





