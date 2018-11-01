import sys
import base64

from os import urandom

APP_PROPERTIES_FILE = 'src/main/resources/application.properties'

def generate_key():
    print('generating app key')
    key = base64.b64encode(urandom(64)).decode('utf-8')
    print('app key:', key)

    with open(APP_PROPERTIES_FILE, 'a') as propertiesFile:
        propertiesFile.write('app.security.secret=' + key)


actions = {
    'generateKey': generate_key
}

if __name__ == "__main__":
    print("ServiceMonitor util script")

    if len(sys.argv) <= 1:
        print('Please specify action')
        print('Available actions:')
        for action in actions:
            print(action)
        exit(0)

    actionName = sys.argv[1]
    if actionName not in actions:
        print('Action not found')

    actions[actionName]()
