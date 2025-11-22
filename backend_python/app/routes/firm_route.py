from app.services.firm_service import (
    get_firm_by_id,
    get_all_firms,
    create_firm,
    update_firm,
    delete_firm,
    list_firmIds_broadcast_today
)
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.models.Firm import Firm




FIRM_BLUEPRINT = Blueprint('firm', __name__)
# Get all firms

# # link: localhost:5000/api/firms/get_all
# @jwt_required()
@FIRM_BLUEPRINT.route('/get_all', methods=['GET'])
def get_all_firms_route():
    firms = get_all_firms()
    return jsonify([firm.serialize() for firm in firms]), 200



# Get firm by ID
# # link: localhost:5000/api/firms/get/<firm_id>

@FIRM_BLUEPRINT.route('/get/<int:firm_id>', methods=['GET'])
# @jwt_required()
def get_firm_by_id_route(firm_id):
    firm = get_firm_by_id(firm_id)
    if not firm:
        return jsonify({'message': 'Firm not found'}), 404
    return jsonify(firm.serialize_detail()), 200




# get all broadcasts for a firm
# # link: localhost:5000/api/firms/get_broadcasts/<firm_id>

@FIRM_BLUEPRINT.route('/get_broadcasts/<int:firm_id>', methods=['GET'])
# @jwt_required()
def get_firm_broadcasts_route(firm_id):
    firm = get_firm_by_id(firm_id)
    if not firm:
        return jsonify({'message': 'Firm not found'}), 404
    broadcasts = firm.broadcasts()
    return jsonify([broadcast.serialize() for broadcast in broadcasts]), 200


# Create a new firm
# # link: localhost:5000/api/firms/create
'''

{
    "name": "New Firm",
    "description": "This is a new firm.",
    "thumbnail": "http://example.com/thumbnail.jpg",
    "start_date": "2023-01-01",
    "end_date": "2023-12-31",
    "rating": 4.5,
    "rating_count": 100,
    "runtime": 100,
    "images": [
        "http://example.com/image1.jpg",
        "http://example.com/image2.jpg"
    ]
}

'''
@FIRM_BLUEPRINT.route('/create', methods=['POST'])
# @jwt_required()
def create_new_firm():
    data = request.get_json()
    try:
        firm = create_firm(
            name=data['name'],
            description=data['description'],
            thumbnail=data['thumbnail'],
            start_date=data.get('start_date'),
            end_date=data.get('end_date'),
            rating=data.get('rating', 0),
            rating_count=data.get('rating_count', 0),
            images=data.get('images', [])
        )
        return jsonify(firm.serialize()), 201
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    

# Update an existing firm
# # link: localhost:5000/api/firms/update/<firm_id>
'''

{
    "name": "Updated Firm",
    "description": "This is an updated firm.",
    "thumbnail": "http://example.com/updated_thumbnail.jpg",
    "rating": 4.8,
    "rating_count": 150,
    "runtime": 110
}

'''
@FIRM_BLUEPRINT.route('/update/<int:firm_id>', methods=['PUT'])
def update_firm_route(firm_id):
    data = request.get_json()
    try:
        firm = update_firm(
            firm_id=firm_id,
            name=data.get('name'),
            description=data.get('description'),
            thumbnail=data.get('thumbnail'),
            rating=data.get('rating'),
            rating_count=data.get('rating_count'),
            runtime=data.get('runtime', 60)  # Default runtime is 60 seconds
        )
        return jsonify(firm.serialize()), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    

# Delete a firm
# # link: localhost:5000/api/firms/delete/<firm_id>
'''

{
    "firm_id": 1
}

'''
@FIRM_BLUEPRINT.route('/delete/<int:firm_id>', methods=['DELETE'])
def delete_firm_route(firm_id):
    try:
        delete_firm(firm_id)
        return jsonify({'message': 'Firm deleted successfully'}), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': f'An error occurred while deleting the firm: {e}'}), 500
    

# List firm IDs that have broadcasts today
# # link: localhost:5000/api/firms/list_firmIds_broadcast_today
@FIRM_BLUEPRINT.route('/list_firmIds_broadcast_today', methods=['GET'])
def list_firmIds_broadcast_today_route():
    try:
        firm_ids = list_firmIds_broadcast_today()
        print(firm_ids)
        return jsonify({'firm_ids': list(firm_ids)}), 200
    except Exception as e:
        return jsonify({'message': f'An error occurred: {e}'}), 500