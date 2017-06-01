[Heroku 101](https://github.com/ibigfoot/heroku-101) | [Heroku 201](https://github.com/ibigfoot/heroku-201) | [Heroku 301](https://github.com/ibigfoot/heroku-301) | [Heroku 401](https://github.com/ibigfoot/heroku-401)

# Heroku 301 - Generator

In this exercise we are going to look at Kafka Producer and Consumer application as well as a web/worker pattern as a means for distributing load in Heroku via a traditional queue as well.

## Prerequisites 
You should definitely have a look at the Heroku-101 prerequisites, as we use the same tools there again. 
We also need to install our first plugin to the Heroku CLI (that's right, it's extensible!) 

See [Preparing Your Environment](https://devcenter.heroku.com/articles/kafka-on-heroku#preparing-your-development-environment) for details on how to ensure the Kafka CLI commands are installed. 

## Kafka
The introduction section on the Kafka home does an excellent job of explaining what Kafka is, so I encourage you to head over and have a look. Also, have a look at the Heroku docs that talk about our service as well. 

For the purpose of this exercise today we will use the multi-tennant kafka service, currently this service is in beta (unless of course I just haven't updated this README...) 

## Create a Kafka Producer

This time we are going to run this a little differently, as we have already built our app from scratch. This time, let us clone the codebase we need and start from there.

```
> git clone https://github.com/ibigfoot/heroku-301-generator
> cd heroku-301-generator
```

This will give us a copy of code locally we can work with and explore. 
Now, let's get this ready for eclipse and explore the code (or you could just use a text editor or something.. )

```
> mvn eclipse:eclipse
```

![Build Generator](images/1-buildGenerator.png)

Then we can build the codebase to ensure we are good to go.
```
> mvn clean package
``` 

### Deploy 

So now we want to connect to some Kafka! But first we will need to create an app and probably a Topic on our Kafka service. 
Firstly, creating an app and push your code to it.

```
> heroku create my-kafka-generator
> git push heroku master
```

From the build log, you should see that this is another java application. 
Open up Eclipse (or your tool of choice) and look at the Procfile, it should have a single line in it. 

```
worker: java -jar target/kafka-gen-jar-with-dependencies.jar
```

Now you need to create a Kafka and add it to your application. This command will use the beta version of the Multi-tennant Kafka service

```
> heroku addons:create heroku-kafka:beta-mt-0
```

Create a Kafka Topic that you are going to use for this tutorial, I've called mine generator but feel free to name yours something else
```
> heroku kafka:topics:create generator --retention-time 86400000
```
The retention time is 24hrs (minimum) in milliseconds.

If you look at your configuration variables you will see there have been a few new ones added to your app as a result of adding in Kafka.

```
> heroku config
```

The application source code will look for the following that have been created as part of attaching the Heroku Kafka addon
- KAFKA_CLIENT_CERT
- KAFKA_CLIENT_CERT_KEY
- KAFKA_TRUSTED_CERT
- KAFKA_URL
- KAFKA_PREFIX

But we will also need two more
- KAFKA_TOPC
- INTERVAL

go ahead and add them now

```
> heroku config:set KAFKA_TOPIC=generator
> heroku config:set INTERVAL=500
```

You should now be ready to start you application and have it sending messages to your kafka topic.

```
> heroku ps:scale worker=1:Standard-1x
```

![Config](images/5-addConfig.png)

Once it has started up, tail the logs

```
> heroku logs --tail
```

Or, have a look at what is being published on the topic. 

```
> heroku kafka:topics:tail <<your topic name>>
```

Now, if for some reason yours isn't working as described and you really aren't too bothered to debug it.. 

try clicking this button 

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)