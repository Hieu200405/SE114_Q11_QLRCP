from flask import Blueprint, request, jsonify
from app.services.image_service import (
    add_image_to_film,
    delete_image_from_film
)


IMAGE_FILM_BLUEPRINT = Blueprint('image_film', __name__)
# Add an image to a film
# # link: localhost:5000/api/image_films/add
'''
{
    "film_id": 1,
    "image_url": "http://example.com/image.jpg"
}

'''

@IMAGE_FILM_BLUEPRINT.route('/add', methods=['POST'])
def add_image_to_film_route():
    """Add an image to a film."""
    data = request.get_json()
    if not data:
        return jsonify({'message': 'No data provided'}), 400
    if 'film_id' not in data or 'image_url' not in data:
        return jsonify({'message': 'Film ID and image URL are required'}), 400
    try:
        film_id = data.get('film_id')
        image_url = data.get('image_url')
        if not film_id or not image_url:
            return jsonify({'message': 'Film ID and image URL are required'}), 400
        
        result = add_image_to_film(film_id, image_url)
        return jsonify({'message': 'Image added successfully'}), 201
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    


# Delete an image from a film
# # link: localhost:5000/api/image_films/delete/<int:image_id>
@IMAGE_FILM_BLUEPRINT.route('/delete/<int:image_id>', methods=['DELETE'])
def delete_image_from_film_route(image_id):
    """Delete an image from a film."""
    try:
        result = delete_image_from_film(image_id)
        return jsonify({'message': 'Image deleted successfully'}), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500