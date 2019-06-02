import json
import math
from datetime import datetime
import paho.mqtt.client as mqtt


BROKER_ADDRESS = "localhost"
GATEWAY_TOPIC = "gateway_service"
MAX_INACTIVITY_TIME = 8
MIN_SIMILARITY = 0.6

MSG_TYPE_USER_INFO = 0
MSG_TYPE_USER_POS = 1
MSG_TYPE_MATCH_LEAVE = 2
MSG_TYPE_PROXIMITY_FAR = 3
MSG_TYPE_PROXIMITY_CLOSE = 4

PROXIMITY_FAR = 5
PROXIMITY_CLOSE = 1

client = mqtt.Client()

users = {}


class User:
    def __init__(self, uid, name, interests):
        self.uid = uid
        self.name = name
        self.x = None
        self.y = None
        self.match_id = None
        self.interests = interests
        self.timestamp = datetime.now()

    def setPos(self, x, y):
        self.x = x
        self.y = y

    def to_json(self):
        """ Convert to a json object. """
        msg = {
            "msgType": MSG_TYPE_USER_POS,
            "id": self.uid,
            "name": self.name,
            "x": self.x,
            "y": self.y,
            "matchId": self.match_id
        }
        return json.dumps(msg)

    def __str__(self):
        return "User ID: {}, Name: {}, X: {}, Y: {}, Match ID: {}".format(self.uid, self.name, self.x, self.y, self.match_id)


def print_users():
    print("Users List:")
    for uid, user in users.items():
        print(user)


def checkUserInactivity(user_id):
    td = datetime.now() - users[user_id].timestamp

    if td.total_seconds() > MAX_INACTIVITY_TIME:
        del users[user_id]
        return True
    return False


def matchUser(user_id):
    user = users[user_id]
    for uid, other_user in users.items():
        if uid != user_id and other_user.match_id is None:
            if checkUserInactivity(other_user.uid):
                continue

            intersection = user.interests & other_user.interests

            similarity = len(intersection) / \
                min(len(user.interests), len(other_user.interests))

            print("similarity:", similarity)

            if similarity > MIN_SIMILARITY:
                user.match_id = uid
                other_user.match_id = user_id
                sendInfoToMatch(user)
                sendInfoToMatch(other_user)
                break


def sendInfoToMatch(user):
    if user.x is None:
        return
    match_topic = "users/" + user.match_id
    json_msg = user.to_json()
    print("Publishing message to topic",
          match_topic, json_msg, sep=" : ")
    print()
    client.publish(match_topic, json_msg)


def sendMsgTypeToUser(user_id, msg_type):
    msg = {
        "msgType": msg_type
    }
    user_topic = "users/" + user_id
    json_msg = json.dumps(msg)
    print("Publishing LEAVE message to topic",
          user_topic, json_msg, sep=" : ")
    print()
    client.publish(user_topic, json_msg)


def getDistance(user1, user2):
    return math.sqrt(math.pow(user1.x - user2.x, 2) + math.pow(user1.y - user2.y, 2))


def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    print("Subscribing messages to topic", GATEWAY_TOPIC)
    client.subscribe(GATEWAY_TOPIC)


def on_message(client, userdata, msg):
    # print(msg.topic, str(msg.payload), sep=" : ")

    json_msg = json.loads(msg.payload)

    print("\nReceived msg:", json_msg)

    msg_type = json_msg['msgType']
    user_id = json_msg['userId']

    if msg_type == 0:                           # update user info
        user = users.get(user_id, None)
        if user:
            user.x = None
            user.y = None
            user.interests = set(json_msg['interests'])
            user.name = json_msg['name']
            user.timestamp = datetime.now()
        else:
            users[user_id] = User(
                user_id, json_msg['name'], set(json_msg['interests']))

    elif msg_type == MSG_TYPE_USER_POS:                         # update user position
        user = users.get(user_id, None)
        if user:
            user.setPos(json_msg['x'], json_msg['y'])
            user.timestamp = datetime.now()
            # check match
            if user.match_id is not None:
                # check match timestamp
                if checkUserInactivity(user.match_id):
                    sendMsgTypeToUser(user_id, MSG_TYPE_MATCH_LEAVE)
                    user.match_id = None
                    matchUser(user_id)
                else:
                    # send new info to match
                    sendInfoToMatch(user)
                    dist_to_match = getDistance(user, users[user.match_id])
                    if dist_to_match < PROXIMITY_CLOSE:
                        sendMsgTypeToUser(user_id, MSG_TYPE_PROXIMITY_CLOSE)
                        sendMsgTypeToUser(
                            user.match_id, MSG_TYPE_PROXIMITY_CLOSE)
                        del users[user_id]
                        del users[user.match_id]

                    elif dist_to_match < PROXIMITY_FAR:
                        sendMsgTypeToUser(user_id, MSG_TYPE_PROXIMITY_FAR)
                        sendMsgTypeToUser(
                            user.match_id, MSG_TYPE_PROXIMITY_FAR)
            else:
                # match user with similar available user
                matchUser(user_id)

    print_users()


def main():
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(BROKER_ADDRESS, 1883, 60)

    # Blocking call that processes network traffic, dispatches callbacks and
    # handles reconnecting.
    # Other loop*() functions are available that give a threaded interface and a
    # manual interface.
    client.loop_forever()


if __name__ == "__main__":
    main()
