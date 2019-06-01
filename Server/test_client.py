import time
import json
import random
import string
import paho.mqtt.client as mqtt  # import the client1


BROKER_ADDRESS = "localhost"
GATEWAY_TOPIC = "gateway_service"


def randomString(stringLength):
    """Generate a random string with the combination of lowercase and uppercase letters """
    letters = string.ascii_letters
    return ''.join(random.choice(letters) for i in range(stringLength))


user_id = randomString(12)
user_topic = "users/" + user_id

match_x = 0
match_y = 0
dist_to_match = 2


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
        "name": "Miguel Araújo"
    }

    sendMsg(client, msg)

    while True:

        msg = {
            "msgType": 1,
            "userId": user_id,
            "x": match_x + dist_to_match,
            "y": match_y + dist_to_match
        }

        sendMsg(client, msg)

        time.sleep(1)


if __name__ == "__main__":
    main()
