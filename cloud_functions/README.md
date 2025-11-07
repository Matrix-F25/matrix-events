### How to Deploy Scheduled Python Cloud Functions
Ensure the source code is in the following directory structure:
```
cloud_functions/
├── main.py               // code for Google Cloud functions
└── requirements.txt      // tells Google Cloud which Python libraries your function needs
```

Deployment is a two-step process using the gcloud command-line tool. Run these commands inside `cloud_functions/` directory.

This command uploads your code and creates the function, telling it to listen to your chosen Pub/Sub topic:
```
gcloud functions deploy YOUR_FUNCTION_NAME \
  --runtime=python311 \
  --region=YOUR_REGION \
  --source=. \
  --entry-point=YOUR_FUNCTION_NAME \
  --trigger-resource=YOUR_TOPIC_NAME \
  --trigger-event=google.pubsub.topic.publish
```

This command creates the timer that sends a message to your function's topic every minute, thus triggering your function to run:
```
gcloud scheduler jobs create pubsub YOUR_JOB_NAME \
  --schedule="every 1 minutes" \
  --topic=YOUR_TOPIC_NAME \
  --message-body="MESSAGE" \
  --location=YOUR_REGION
```

This command creates the topic the timer will send a message to:
```
gcloud pubsub topics create YOUR_TOPIC_NAME
```

#### Example deployment:
```
gcloud pubsub topics create run-every-minute-topic

gcloud functions deploy update_registration_opened \
  --runtime=python311 \
  --region=us-west1 \
  --source=. \
  --entry-point=update_registration_opened \
  --trigger-resource=run-every-minute-topic \
  --trigger-event=google.pubsub.topic.publish

gcloud functions deploy run_event_lottery \
  --runtime=python311 \
  --region=us-west1 \
  --source=. \
  --entry-point=run_event_lottery \
  --trigger-resource=run-every-minute-topic \
  --trigger-event=google.pubsub.topic.publish

gcloud functions deploy expire_pending_invitations \
  --runtime=python311 \
  --region=us-west1 \
  --source=. \
  --entry-point=expire_pending_invitations \
  --trigger-resource=run-every-minute-topic \
  --trigger-event=google.pubsub.topic.publish

gcloud scheduler jobs create pubsub run-every-minute-scheduler \
  --schedule="every 1 minutes" \
  --topic=run-every-minute-topic \
  --message-body="Running every minute..." \
  --location=us-west1
```