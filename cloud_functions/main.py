import firebase_admin
from firebase_admin import firestore

import random
from datetime import datetime, timezone

firebase_admin.initialize_app()

# --- Cloud Function 0: Update Registration Opened ---
# This function runs when a message is published to a certain topic.
# If finds events whose registration just opened and updates it
# to reflect its new state.

def update_registration_opened(data, context) -> None:
    db = firestore.client()
    now = datetime.now(timezone.utc)
    
    # Query for events whose registration just opened and state hasn't been updated
    query = db.collection("events") \
        .where("registrationStartDateTime", "<=", now) \
        .where("registrationOpened", "==", False)
    events_to_process = query.stream()

    print("Checking for events whose registration state needs to be updated...")
    
    for doc in events_to_process:
        print(f"Updating event registration state for event: {doc.id}")
        doc.reference.update({"registrationOpened": True})
        

# --- Cloud Function 1: Run Event Lottery ---
# This function runs when a message is published to a certain topic.
# It finds events where registration has just closed and runs the lottery.
# The lottery moves entrants in the waitlist to the pending list.

def run_event_lottery(data, context) -> None:
    db = firestore.client()
    now = datetime.now(timezone.utc)

    # Query for events where registration has ended and the lottery hasn't been processed
    query = db.collection("events") \
        .where("registrationEndDateTime", "<=", now) \
        .where("lotteryProcessed", "==", False)
    events_to_process = query.stream()

    print("Checking for events that need lottery processing...")

    for doc in events_to_process:

        print(f"Processing lottery for event: {doc.id}")

        event_data = doc.to_dict()
        wait_list = event_data.get("waitList", [])
        
        # If waitlist is empty, just mark as processed and skip
        if not wait_list:
            print(f"Event {doc.id} has an empty waitlist. Marking as processed.")
            doc.reference.update({"lotteryProcessed": True})
            continue

        # Determine how many winners to select
        accepted_list = event_data.get("acceptedList", [])
        event_capacity = event_data.get("eventCapacity", 0)
        slots_to_fill = max(0, event_capacity - len(accepted_list))
        num_winners = min(slots_to_fill, len(wait_list))
        
        print(f"Selecting {num_winners} winners for event {doc.id}.")
        
        # Lottery select winners
        random.shuffle(wait_list)
        winners = wait_list[:num_winners]
        remaining_waitlist = wait_list[num_winners:]
        
        # Send out automated notifications
        batch = db.batch()
        sender_profile = event_data.get("organizer")
        
        # Notify winners
        for winner in winners:
            query = db.collection("profiles").where("deviceId", "==", winner).limit(1)
            profile_docs = list(query.stream())
            if not profile_docs:
                print(f"Error: Profile for winner deviceId {winner} not found. Skipping notification for this user.")
                continue
            recv_profile = profile_docs[0].to_dict()
            message = "Congrats! You have been selected for the " \
                    + f"{event_data.get('name')} event. Please " \
                    + "accept or decline the invitation at your " \
                    + "earliest convenience.\n\n" \
                    + "This is an automated message."
            read_flag = False
            notification_data = {
                "sender": sender_profile,
                "receiver": recv_profile,
                "message": message,
                "readFlag": read_flag,
                "timestamp": now
            }
            new_doc = db.collection("notifications").document()
            notification_data["id"] = new_doc.id
            batch.set(new_doc, notification_data)
        
        # Notify losers
        for loser in remaining_waitlist:
            query = db.collection("profiles").where("deviceId", "==", loser).limit(1)
            profile_docs = list(query.stream())
            if not profile_docs:
                print(f"Error: Profile for loser deviceId {loser} not found. Skipping notification for this user.")
                continue
            recv_profile = profile_docs[0].to_dict()
            message = "Sorry! You have NOT been selected for the " \
                    + f"{event_data.get('name')} event. Please " \
                    + "keep in mind selected entrants may choose " \
                    + "to decline their spot, resulting in a second " \
                    + "chance for you.\n\n" \
                    + "This is an automated message."
            read_flag = False
            notification_data = {
                "sender": sender_profile,
                "receiver": recv_profile,
                "message": message,
                "readFlag": read_flag,
                "timestamp": now
            }
            new_doc = db.collection("notifications").document()
            notification_data["id"] = new_doc.id
            batch.set(new_doc, notification_data)
            
        batch.commit()
            
        # Update the event
        doc.reference.update({
            "waitList": remaining_waitlist,
            "pendingList": firestore.ArrayUnion(winners),
            "lotteryProcessed": True,
        })
        print(f"Successfully processed lottery for event {doc.id}.")


# --- Cloud Function 2: Expire Pending Invitations ---
# This function runs when a message is published to a certain topic.
# If finds events that have just started, and moves any users who haven't 
# responded in the pending list to the declined list.

def expire_pending_invitations(data, context) -> None:
    db = firestore.client()
    now = datetime.now(timezone.utc)
    
    # Query for events that have started and whose pending lists haven't been expired
    query = db.collection("events") \
        .where("eventStartDateTime", "<=", now) \
        .where("pendingExpired", "==", False)
    events_to_process = query.stream()

    print("Checking for events with pending invitations to expire...")
    
    for doc in events_to_process:
        event_data = doc.to_dict()

        print(f"Expiring pending invitations for event: {doc.id}")

        pending_list = event_data.get("pendingList", [])

        # If pending list is empty, just mark as processed and skip
        if not pending_list:
            print(f"No pending users to expire for event {doc.id}. Marking as processed.")
            doc.reference.update({"pendingExpired": True})
            continue

        print(f"Moving {len(pending_list)} users from pending to declined for event {doc.id}.")
        doc.reference.update({
            "pendingList": [],  # Clear the pending list
            "declinedList": firestore.ArrayUnion(pending_list),
            "pendingExpired": True,
        })
        print(f"Successfully processed expire pending list for event {doc.id}.")
