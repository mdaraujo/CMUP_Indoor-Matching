import json
import paho.mqtt.client as mqtt


BROKER_ADDRESS = "localhost"
GATEWAY_TOPIC = "gateway_service"

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
        # TODO add last timestamp to delete not responding users

    def setPos(self, x, y):
        self.x = x
        self.y = y

    def setMatch(self, match_id):
        self.match_id = match_id

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


def printUsers():
    for uid, user in users.items():
        print(user)


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
        users[user_id] = User(user_id, json_msg['name'])

    elif msg_type == 1:                         # update user position
        user = users.get(user_id, None)
        if user:
            user.setPos(json_msg['x'], json_msg['y'])
            # check match
            if user.match_id is not None:
                # send new info to match
                match_topic = "users/" + user.match_id
                json_msg = users[user_id].to_json()
                print("Publishing message to topic",
                      match_topic, json_msg, sep=" : ")
                print()
                client.publish(match_topic, json_msg)

            else:
                # match user with similar available user
                for uid, other_user in users.items():
                    if uid != user_id and other_user.match_id is None:
                        user.match_id = uid
                        other_user.match_id = user_id

    printUsers()


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
