from app.services.film_service import (
    get_film_by_id,
    get_all_films,
    create_film,
    update_film,
    delete_film,
    list_filmIds_broadcast_today
)
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.models.Film import Film




FILM_BLUEPRINT = Blueprint('film', __name__)
# Get all films

# # link: localhost:5000/api/films/get_all
# @jwt_required()
@FILM_BLUEPRINT.route('/get_all', methods=['GET'])
def get_all_films_route():
    films = get_all_films()
    return jsonify([film.serialize() for film in films]), 200



# Get film by ID
# # link: localhost:5000/api/films/get/<film_id>

@FILM_BLUEPRINT.route('/get/<int:film_id>', methods=['GET'])
# @jwt_required()
def get_film_by_id_route(film_id):
    film = get_film_by_id(film_id)
    if not film:
        return jsonify({'message': 'Film not found'}), 404
    return jsonify(film.serialize_detail()), 200




# get all broadcasts for a film
# # link: localhost:5000/api/films/get_broadcasts/<film_id>

@FILM_BLUEPRINT.route('/get_broadcasts/<int:film_id>', methods=['GET'])
# @jwt_required()
def get_film_broadcasts_route(film_id):
    film = get_film_by_id(film_id)
    if not film:
        return jsonify({'message': 'Film not found'}), 404
    broadcasts = film.broadcasts()
    return jsonify([broadcast.serialize() for broadcast in broadcasts]), 200


# Create a new film
# # link: localhost:5000/api/films/create
'''

{
    "name": "New Film",
    "description": "This is a new film.",
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
@FILM_BLUEPRINT.route('/create', methods=['POST'])
# @jwt_required()
def create_new_film():
    data = request.get_json()
    try:
        film = create_film(
            name=data['name'],
            description=data['description'],
            thumbnail=data['thumbnail'],
            start_date=data.get('start_date'),
            end_date=data.get('end_date'),
            rating=data.get('rating', 0),
            rating_count=data.get('rating_count', 0),
            images=data.get('images', [])
        )
        return jsonify(film.serialize()), 201
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    

# Update an existing film
# # link: localhost:5000/api/films/update/<film_id>
'''

{
    "name": "Updated Film",
    "description": "This is an updated film.",
    "thumbnail": "http://example.com/updated_thumbnail.jpg",
    "rating": 4.8,
    "rating_count": 150,
    "runtime": 110
}

'''
@FILM_BLUEPRINT.route('/update/<int:film_id>', methods=['PUT'])
def update_film_route(film_id):
    data = request.get_json()
    try:
        film = update_film(
            film_id=film_id,
            name=data.get('name'),
            description=data.get('description'),
            thumbnail=data.get('thumbnail'),
            rating=data.get('rating'),
            rating_count=data.get('rating_count'),
            runtime=data.get('runtime', 60)  # Default runtime is 60 seconds
        )
        return jsonify(film.serialize()), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    

# Delete a film
# # link: localhost:5000/api/films/delete/<film_id>
'''

{
    "film_id": 1
}

'''
@FILM_BLUEPRINT.route('/delete/<int:film_id>', methods=['DELETE'])
def delete_film_route(film_id):
    try:
        delete_film(film_id)
        return jsonify({'message': 'Film deleted successfully'}), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': f'An error occurred while deleting the film: {e}'}), 500
    

# List film IDs that have broadcasts today
# # link: localhost:5000/api/films/list_filmIds_broadcast_today
@FILM_BLUEPRINT.route('/list_filmIds_broadcast_today', methods=['GET'])
def list_filmIds_broadcast_today_route():
    try:
        film_ids = list_filmIds_broadcast_today()
        print(film_ids)
        return jsonify({'film_ids': list(film_ids)}), 200
    except Exception as e:
        return jsonify({'message': f'An error occurred: {e}'}), 500