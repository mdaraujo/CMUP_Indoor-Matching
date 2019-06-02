import time
import json
import random
import string
from datetime import datetime
import paho.mqtt.client as mqtt  # import the client1


BROKER_ADDRESS = "localhost"
GATEWAY_TOPIC = "gateway_service"


def randomString(stringLength):
    """Generate a random string with the combination of lowercase and uppercase letters """
    letters = string.ascii_letters
    return ''.join(random.choice(letters) for i in range(stringLength))


user_id = randomString(12)
user_topic = "users/" + user_id

match_x = 5
match_y = 5


def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    print("Subscribing messages to topic", user_topic)
    client.subscribe(user_topic)


def on_publish(client, userdata):
    print('on_publish')


def sendMsg(client, msg):

    json_msg = json.dumps(msg)

    print("\nPublishing message to topic",
          GATEWAY_TOPIC, json_msg, sep=" : ")

    client.publish(GATEWAY_TOPIC, json_msg)


def on_message(client, userdata, msg):
    global match_x
    global match_y

    json_msg = json.loads(msg.payload)
    print("Received msg:", json_msg)

    match_x = json_msg['x']
    match_y = json_msg['y']


def main():
    print("creating new instance")
    client = mqtt.Client(user_id)  # create new instance

    client.on_connect = on_connect
    client.on_publish = on_publish
    client.on_message = on_message

    print("connecting to broker")
    client.connect(BROKER_ADDRESS)  # connect to broker
    client.loop_start()

    msg = {
        "msgType": 0,
        "userId": user_id,
        "name": "Name of " + user_id,
        "interests": [3, 7, 13, 15, 5943153747, 9991232322, 10045457582]
    }

    sendMsg(client, msg)

    dist_to_match = 4.5
    last_time = datetime.now()

    while True:

        msg = {
            "msgType": 1,
            "userId": user_id,
            "x": match_x + dist_to_match,
            "y": match_y + dist_to_match
        }

        sendMsg(client, msg)

        time.sleep(0.4)

        time_diff = datetime.now() - last_time

        if time_diff.total_seconds() > 3:
            last_time = datetime.now()
            dist_to_match -= 1
            print("\n\ndist_to_match:", dist_to_match)


if __name__ == "__main__":
    main()
