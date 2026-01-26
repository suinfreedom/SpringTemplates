package producer;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.transaction.UserTransaction;

@SpringBootApplication
@ImportResource("classpath:broker-config.xml")
public class ArtemisTestApplication implements CommandLineRunner {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ConnectionFactory brokerConnectionFactory;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private UserTransaction narayanaUserTransaction; // from broker-config.xml

    public static void main(String[] args) {
        SpringApplication.run(ArtemisTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Artemis Broker XA Test...");

        // Test 1: Connection Factory
        testConnectionFactory();

        // Test 2: Programmatic XA transaction
        testXAMessageFlow();

        // Test 3: Camel routes
        testCamelMessageFlow();

        System.out.println("All tests completed successfully!");
    }

    private void testConnectionFactory() {
        try {
            System.out.println("Testing connection factory...");
            Connection connection = brokerConnectionFactory.createConnection();
            connection.start();
            System.out.println("Connection established successfully");

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("Session created successfully");

            session.close();
            connection.close();
            System.out.println("Connection closed cleanly");
        } catch (JMSException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testXAMessageFlow() {
        System.out.println("Testing XA transaction with JMS...");

        Connection connection = null;
        Session session = null;

        try {
            // Start a user transaction
            narayanaUserTransaction.begin();

            connection = brokerConnectionFactory.createConnection();
            connection.start();

            session = connection.createSession(true, Session.SESSION_TRANSACTED); // XA session
            Queue queue = session.createQueue("TEST.XA.QUEUE");

            MessageProducer producer = session.createProducer(queue);
            String msg = "XA Test message at " + System.currentTimeMillis();
            producer.send(session.createTextMessage(msg));
            System.out.println("Sent message in XA transaction: " + msg);

            // Commit transaction
            narayanaUserTransaction.commit();
            System.out.println("XA transaction committed successfully");

        } catch (Exception e) {
            System.err.println("XA transaction failed: " + e.getMessage());
            e.printStackTrace();
            try {
                if (narayanaUserTransaction != null) {
                    narayanaUserTransaction.rollback();
                    System.out.println("XA transaction rolled back");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private void testCamelMessageFlow() throws Exception {
        System.out.println("Testing message flow with Camel routes...");

        // Send a test message via Camel
        String testMessage = "Test message via Camel at " + System.currentTimeMillis();
        producerTemplate.sendBody("activemq:queue:TEST.QUEUE", testMessage);
        System.out.println("Message sent: " + testMessage);

        // Let the Camel consumer route pick it up
        Thread.sleep(2000);
    }

    @Bean
    public RouteBuilder testRoutes() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                // Consume from TEST.QUEUE
                from("activemq:queue:TEST.QUEUE")
                        .routeId("test-consumer")
                        .log("Received message: ${body}")
                        .to("log:test-output");

                // Periodic producer
                from("timer:test?period=10000")
                        .routeId("test-producer")
                        .setBody(constant("Periodic test message"))
                        .to("activemq:queue:TEST.QUEUE");
            }
        };
    }
}
