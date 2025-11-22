from flask import Blueprint, request, jsonify
from app.services.image_service import (
    add_image_to_firm,
    delete_image_from_firm
)


IMAGE_FIRM_BLUEPRINT = Blueprint('image_firm', __name__)
# Add an image to a firm
# # link: localhost:5000/api/image_firms/add
'''
{
    "firm_id": 1,
    "image_url": "http://example.com/image.jpg"
}

'''

@IMAGE_FIRM_BLUEPRINT.route('/add', methods=['POST'])
def add_image_to_firm_route():
    """Add an image to a firm."""
    data = request.get_json()
    if not data:
        return jsonify({'message': 'No data provided'}), 400
    if 'firm_id' not in data or 'image_url' not in data:
        return jsonify({'message': 'Firm ID and image URL are required'}), 400
    try:
        firm_id = data.get('firm_id')
        image_url = data.get('image_url')
        if not firm_id or not image_url:
            return jsonify({'message': 'Firm ID and image URL are required'}), 400
        
        result = add_image_to_firm(firm_id, image_url)
        return jsonify({'message': 'Image added successfully'}), 201
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    


# Delete an image from a firm
# # link: localhost:5000/api/image_firms/delete/<int:image_id>
@IMAGE_FIRM_BLUEPRINT.route('/delete/<int:image_id>', methods=['DELETE'])
def delete_image_from_firm_route(image_id):
    """Delete an image from a firm."""
    try:
        result = delete_image_from_firm(image_id)
        return jsonify({'message': 'Image deleted successfully'}), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500