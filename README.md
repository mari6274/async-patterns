This project has been prepared as an "example" part of my presentation titled "Asynchronous communication
patterns with RabbitMQ".

Run profiles:
-Phttp - send messages directly to http api
-Prabbit-transacted - publish messages to rabbit with transaction mechanism, update to db in batch
-Prabbit-transacted,rabbit-transacted-one-by-one - publish messages to rabbit with transaction mechanism, update to 
db one by one
-Prabbit-publish-confirm - publish messages to rabbit with publish confirm mechanism, update to db in batch
-Prabbit-publish-confirm,rabbit-publish-confirm-one-by-one - publish messages to rabbit with publish confirm 
mechanism, update to db one by one
