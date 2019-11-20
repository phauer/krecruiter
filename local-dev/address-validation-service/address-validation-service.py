#!/usr/bin/env python3
import json

import sys
from faker import Faker
from flask import Flask, Response, request

app = Flask(__name__)
faker = Faker('en')


@app.route('/validateAddress', methods=['GET'])
def validate_address():
    address = request.args.get('address')
    if address is None:
        response_json = {
            'message': "Missing query parameter `address`",
        }
        return Response(json.dumps(response_json), mimetype='application/json'), 200
    else:
        response_json = {
            'address': address,
            'valid': faker.boolean(chance_of_getting_true=75)
        }
        return Response(json.dumps(response_json), mimetype='application/json'), 200


if __name__ == '__main__':
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 5000
    app.run(debug=False, host='0.0.0.0', port=port)
    # app.run(debug=True, port=5000, host='0.0.0.0')
