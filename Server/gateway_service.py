import json
from datetime import datetime
import paho.mqtt.client as mqtt


BROKER_ADDRESS = "localhost"
GATEWAY_TOPIC = "gateway_service"
MAX_INACTIVITY_TIME = 5

client = mqtt.Client()

users = {}


class User:
    def __init__(self, uid, name):
        self.uid = uid
        self.name = name
        self.x = None
        self.y = None
        self.match_id = None
        self.interests = None
        self.timestamp = datetime.now()

    def setPos(self, x, y):
        self.x = x
        self.y = y

    def to_json(self):
        """ Convert to a json object. """
        user_dict = {}
        user_dict['id'] = self.uid
        user_dict['name'] = self.name
        user_dict['x'] = self.x
        user_dict['y'] = self.y
        user_dict['matchId'] = self.match_id
        return json.dumps(user_dict)

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
    for uid, other_user in users.items():
        if uid != user_id and other_user.match_id is None:
            if checkUserInactivity(other_user.uid):
                continue

            users[user_id].match_id = uid
            other_user.match_id = user_id
            sendInfoToMatch(users[user_id])


def sendInfoToMatch(user):
    match_topic = "users/" + user.match_id
    json_msg = user.to_json()
    print("Publishing message to topic",
          match_topic, json_msg, sep=" : ")
    print()
    client.publish(match_topic, json_msg)


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
            # user.interests = json_msg['interests']
            user.name = json_msg['name']
            user.timestamp = datetime.now()
        else:
            users[user_id] = User(user_id, json_msg['name'])

    elif msg_type == 1:                         # update user position
        user = users.get(user_id, None)
        if user:
            user.setPos(json_msg['x'], json_msg['y'])
            user.timestamp = datetime.now()
            # check match
            if user.match_id is not None:
                # check match timestamp
                if checkUserInactivity(user.match_id):
                    user.match_id = None
                    matchUser(user_id)
                else:
                    # send new info to match
                    sendInfoToMatch(user)

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
